package iteration2;

import models.DepositRequest;
import models.TransferRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.DepositRequester;
import requests.TransferRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class TransferTest extends BaseTest {

    private void depositMoneyToSenderAccount(double balance) {
        DepositRequest depositRequest = DepositRequest.builder()
                .id(1)
                .balance(balance)
                .build();

        new DepositRequester(
                RequestSpecs.authAsUser("kate2026", "Password33$"),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);
    }

    @Test
    public void userCanTransferMoneyBetweenOwnAccounts() {
        depositMoneyToSenderAccount(100);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(1)
                .receiverAccountId(2)
                .amount(100)
                .build();

        new TransferRequester(
                RequestSpecs.authAsUser("kate2026", "Password33$"),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest);
    }

    public static Stream<Arguments> transferValidData() {
        return Stream.of(
                Arguments.of(0.01),
                Arguments.of(100)
        );
    }

    @MethodSource("transferValidData")
    @ParameterizedTest
    public void userCanTransferMoneyWithBoundaryValidAmount(double amount) {
        depositMoneyToSenderAccount(amount);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(1)
                .receiverAccountId(2)
                .amount(amount)
                .build();

        new TransferRequester(
                RequestSpecs.authAsUser("kate2026", "Password33$"),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest);
    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(
                Arguments.of(0, "Transfer amount must be at least 0.01"),
                Arguments.of(-100, "Transfer amount must be at least 0.01"),
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000")
        );
    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    public void userCanNotTransferMoneyWithInvalidAmount(double amount,
                                                         String errorValue) {
        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(1)
                .receiverAccountId(2)
                .amount(amount)
                .build();

        new TransferRequester(
                RequestSpecs.authAsUser("kate2026", "Password33$"),
                ResponseSpecs.requestReturnsBadRequestWithPlainText(errorValue))
                .post(transferRequest);
    }
}