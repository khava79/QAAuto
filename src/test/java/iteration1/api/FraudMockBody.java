package iteration1.api;

import common.annotations.FraudCheckMock;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
class FraudMockBody {
    private String status;
    private String decision;
    private double riskScore;
    private String reason;
    private boolean requiresManualReview;
    private boolean additionalVerificationRequired;

    static FraudMockBody from(FraudCheckMock config) {
        return FraudMockBody.builder()
                .status(config.status())
                .decision(config.decision())
                .riskScore(config.riskScore())
                .reason(config.reason())
                .requiresManualReview(config.requiresManualReview())
                .additionalVerificationRequired(config.additionalVerificationRequired())
                .build();
    }
}