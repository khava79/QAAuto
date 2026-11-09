package iteration1.api;

import api.models.TransferResponse;
import api.models.comparison.ModelAssertions;
import common.annotations.FraudCheckMock;
import common.annotations.MockFault;
import common.extensions.TimingExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static iteration1.api.FraudTestData.*;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {

    // APPROVED
    @Test
    @FraudCheckMock(decision = DECISION_APPROVED, riskScore = 0.2, reason = "Low risk transaction")
    public void approvedTransferIsProcessedImmediately() {
        TransferScenario s = TransferScenario.prepare();
        TransferResponse actual = s.transfer();
        s.assertMoneyMoved(softly);
        assertResponse(s, actual, Expected.builder()
                .status(STATUS_APPROVED)
                .message(MSG_APPROVED)
                .riskScore(0.2)
                .reason("Low risk transaction")
                .build());
    }

    // BLOCKED
    @Test
    @FraudCheckMock(decision = DECISION_BLOCKED, riskScore = 0.9, reason = "High risk transaction")
    public void blockedTransferIsRejected() {
        TransferScenario s = TransferScenario.prepare();
        TransferResponse actual = s.transfer();
        s.assertNoMoneyMoved(softly);
        assertResponse(s, actual, Expected.builder()
                .status(STATUS_BLOCKED)
                .message(MSG_BLOCKED)
                .riskScore(0.9)
                .reason("High risk transaction")
                .build());
    }

    // REVIEW_REQUIRED
    @Test
    @FraudCheckMock(decision = DECISION_REVIEW_REQUIRED, riskScore = 0.6, reason = "Needs review")
    public void reviewRequiredByDecision() {
        TransferScenario s = TransferScenario.prepare();
        TransferResponse actual = s.transfer();
        s.assertNoMoneyMoved(softly);
        assertResponse(s, actual, Expected.builder()
                .status(STATUS_MANUAL_REVIEW)
                .message(MSG_MANUAL_REVIEW)
                .riskScore(0.6)
                .reason("Needs review")
                .build());
    }

    @Test
    @FraudCheckMock(decision = DECISION_APPROVED, requiresManualReview = true,
            riskScore = 0.5, reason = "Flagged for review")
    public void reviewRequiredByManualReviewFlag() {
        TransferScenario s = TransferScenario.prepare();
        TransferResponse actual = s.transfer();
        s.assertNoMoneyMoved(softly);
        assertResponse(s, actual, Expected.builder()
                .status(STATUS_MANUAL_REVIEW)
                .message(MSG_MANUAL_REVIEW)
                .riskScore(0.5)
                .reason("Flagged for review")
                .manualReviewFlag(true)
                .build());
    }

    // VERIFICATION_REQUIRED
    @Test
    @FraudCheckMock(decision = DECISION_VERIFICATION_REQUIRED, riskScore = 0.5, reason = "Verification needed")
    public void verificationRequiredByDecision() {
        TransferScenario s = TransferScenario.prepare();
        TransferResponse actual = s.transfer();
        s.assertNoMoneyMoved(softly);
        assertResponse(s, actual, Expected.builder()
                .status(STATUS_VERIFICATION)
                .message(MSG_VERIFICATION)
                .riskScore(0.5)
                .reason("Verification needed")
                .build());
    }

    @Test
    @FraudCheckMock(decision = DECISION_APPROVED, additionalVerificationRequired = true,
            riskScore = 0.4, reason = "Extra verification")
    public void verificationRequiredByFlag() {
        TransferScenario s = TransferScenario.prepare();
        TransferResponse actual = s.transfer();
        s.assertMoneyMoved(softly);
        assertResponse(s, actual, Expected.builder()
                .status(STATUS_APPROVED)
                .message(MSG_APPROVED)
                .riskScore(0.4)
                .reason("Extra verification")
                .verificationFlag(true)
                .build());
    }

    // Сбои fraud-сервиса -> fallback в ручную проверку
    @Test
    @FraudCheckMock(httpStatus = 500)
    public void fallbackOnServerError() {
        assertFallback();
    }

    @Test
    @FraudCheckMock(httpStatus = 404)
    public void fallbackOnNotFound() {
        assertFallback();
    }

    @Test
    @FraudCheckMock(fault = MockFault.CONNECTION_RESET)
    public void fallbackOnConnectionError() {
        assertFallback();
    }

    @Test
    @Disabled("TransferService не прерывает вызов fraud-сервиса: даже при задержке 70 с ответ APPROVED, fallback не срабатывает")
    @FraudCheckMock(delayMs = 70_000)
    public void fallbackOnTimeout() {
        assertFallback();
    }

    // Общие проверки
    private void assertFallback() {
        TransferScenario s = TransferScenario.prepare();
        TransferResponse actual = s.transfer();
        s.assertNoMoneyMoved(softly);
        softly.assertThat(actual.getStatus()).isEqualTo(STATUS_MANUAL_REVIEW);
        softly.assertThat(actual.getMessage()).isEqualTo(MSG_MANUAL_REVIEW);
        softly.assertThat(actual.getFraudRiskScore()).isEqualTo(FALLBACK_RISK_SCORE);
        softly.assertThat(actual.isRequiresManualReview()).isTrue();
        softly.assertThat(actual.isRequiresVerification()).isFalse();
    }

    private void assertResponse(TransferScenario s, TransferResponse actual, Expected expected) {
        TransferResponse expectedResponse = TransferResponse.builder()
                .status(expected.status)
                .message(expected.message)
                .amount(s.amount())
                .senderAccountId(s.sender().getId())
                .receiverAccountId(s.receiver().getId())
                .fraudRiskScore(expected.riskScore)
                .fraudReason(expected.reason)
                .requiresManualReview(expected.manualReviewFlag)
                .requiresVerification(expected.verificationFlag)
                .build();
        ModelAssertions.assertThatModels(expectedResponse, actual).match();
    }

    @lombok.Builder
    private static class Expected {
        private final String status;
        private final String message;
        private final double riskScore;
        private final String reason;
        private final boolean manualReviewFlag;
        private final boolean verificationFlag;
    }
}