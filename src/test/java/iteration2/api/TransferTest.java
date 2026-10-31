package iteration2.api;

import api.dao.AccountDao;
import api.requests.steps.DataBaseSteps;
import api.generators.DepositRequestGenerator;
import api.generators.TransferRequestGenerator;
import api.models.DepositRequest;
import api.models.TransferRequest;
import api.models.TransferResponse;
import api.models.comparison.ModelAssertions;
import common.annotations.APIVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@APIVersion("with_database_with_fix")
public class TransferTest extends BaseTest {
    private void depositMoneyToSenderAccount(double balance) {
        DepositRequest depositRequest =
                DepositRequestGenerator.generate(
                        senderAccount.getId(),
                        balance
                );

        UserSteps.deposit(userSpec, depositRequest);
    }

    private void assertTransferApplied(
            AccountDao senderBefore,
            AccountDao receiverBefore,
            double amount) {
        AccountDao senderAfter = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber());

        AccountDao receiverAfter = DataBaseSteps.getAccountByAccountNumber(
                receiverAccount.getAccountNumber());

        assertNotNull(senderAfter, "Счёт отправителя не найден в БД");
        assertNotNull(receiverAfter, "Счёт получателя не найден в БД");

        assertEquals(
                senderBefore.getBalance() - amount,
                senderAfter.getBalance(),
                DELTA);

        assertEquals(
                receiverBefore.getBalance() + amount,
                receiverAfter.getBalance(),
                DELTA);
    }

    @Test
    public void userCanTransferMoneyBetweenOwnAccounts() {
        depositMoneyToSenderAccount(100);

        AccountDao senderBefore = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber());

        AccountDao receiverBefore = DataBaseSteps.getAccountByAccountNumber(
                receiverAccount.getAccountNumber());

        assertNotNull(senderBefore, "Счёт отправителя не найден в БД");
        assertNotNull(receiverBefore, "Счёт получателя не найден в БД");

        TransferRequest transferRequest =
                TransferRequestGenerator.generate(
                        senderAccount.getId(),
                        receiverAccount.getId(),
                        50);

        TransferResponse transferResponse =
                UserSteps.transfer(userSpec, transferRequest);

        ModelAssertions
                .assertThatModels(transferRequest, transferResponse)
                .match();

        assertTransferApplied(senderBefore, receiverBefore, 50);
    }

    public static Stream<Arguments> transferValidData() {
        return Stream.of(
                Arguments.of(0.01),
                Arguments.of(100));
    }

    @MethodSource("transferValidData")
    @ParameterizedTest
    public void userCanTransferMoneyWithBoundaryValidAmount(double amount) {
        depositMoneyToSenderAccount(amount);

        AccountDao senderBefore = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber());

        AccountDao receiverBefore = DataBaseSteps.getAccountByAccountNumber(
                receiverAccount.getAccountNumber());

        assertNotNull(senderBefore, "Счёт отправителя не найден в БД");
        assertNotNull(receiverBefore, "Счёт получателя не найден в БД");

        TransferRequest transferRequest =
                TransferRequestGenerator.generate(
                        senderAccount.getId(),
                        receiverAccount.getId(),
                        amount);

        TransferResponse transferResponse =
                UserSteps.transfer(userSpec, transferRequest);

        ModelAssertions
                .assertThatModels(transferRequest, transferResponse)
                .match();

        assertTransferApplied(senderBefore, receiverBefore, amount);
    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(
                Arguments.of(0, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(-100, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"));
    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    public void userCanNotTransferMoneyWithInvalidAmount(
            double amount,
            String errorValue) {
        AccountDao senderBefore = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber());

        AccountDao receiverBefore = DataBaseSteps.getAccountByAccountNumber(
                receiverAccount.getAccountNumber());

        assertNotNull(senderBefore, "Счёт отправителя не найден в БД");
        assertNotNull(receiverBefore, "Счёт получателя не найден в БД");

        TransferRequest transferRequest =
                TransferRequestGenerator.generate(
                        senderAccount.getId(),
                        receiverAccount.getId(),
                        amount);

        new CrudRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequestWithPlainText(errorValue),
                Endpoint.TRANSFER).post(transferRequest);

        AccountDao senderAfter = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber());

        AccountDao receiverAfter = DataBaseSteps.getAccountByAccountNumber(
                receiverAccount.getAccountNumber());

        assertNotNull(senderAfter, "Счёт отправителя не найден в БД");
        assertNotNull(receiverAfter, "Счёт получателя не найден в БД");

        assertEquals(senderBefore, senderAfter);
        assertEquals(receiverBefore, receiverAfter);
    }
}
