package iteration2;

import generators.DepositRequestGenerator;
import generators.TransferRequestGenerator;
import models.DepositRequest;
import models.TransferRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.UserSteps;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class TransferTest extends BaseTest {

    private void depositMoneyToSenderAccount(double balance) {
        DepositRequest depositRequest =
                DepositRequestGenerator.generate(
                        senderAccount.getId(),
                        balance);

        UserSteps.deposit(userSpec, depositRequest);
    }

    @Test
    public void userCanTransferMoneyBetweenOwnAccounts() {

        depositMoneyToSenderAccount(100);

        TransferRequest transferRequest =
                TransferRequestGenerator.generate(
                        senderAccount.getId(),
                        receiverAccount.getId(),
                        50);

        UserSteps.transfer(userSpec, transferRequest);
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

        TransferRequest transferRequest =
                TransferRequestGenerator.generate(
                        senderAccount.getId(),
                        receiverAccount.getId(),
                        amount);

        UserSteps.transfer(userSpec, transferRequest);
    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(
                Arguments.of(
                        0,
                        "Transfer amount must be at least 0.01"
                ),
                Arguments.of(
                        -100,
                        "Transfer amount must be at least 0.01"
                ),
                Arguments.of(
                        10000.01,
                        "Transfer amount cannot exceed 10000"
                )
        );
    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    public void userCanNotTransferMoneyWithInvalidAmount(
            double amount,
            String errorValue) {

        TransferRequest transferRequest =
                TransferRequestGenerator.generate(
                        senderAccount.getId(),
                        receiverAccount.getId(),
                        amount);

        new CrudRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequestWithPlainText(errorValue),
                Endpoint.TRANSFER)
                .post(transferRequest);
    }
}