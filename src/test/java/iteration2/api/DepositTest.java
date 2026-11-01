package iteration2.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.requests.steps.DataBaseSteps;
import api.generators.DepositRequestGenerator;
import api.models.DepositRequest;
import api.models.DepositResponse;
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
public class DepositTest extends BaseTest {

    @Test
    public void userCanDepositMoneyWithCorrectAmount() {
        AccountDao before = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber()
        );
        assertNotNull(before, "Счёт не найден в БД");

        DepositRequest depositRequest =
                DepositRequestGenerator.generate(senderAccount.getId());

        DepositResponse depositResponse =
                UserSteps.deposit(userSpec, depositRequest);

        ModelAssertions
                .assertThatModels(depositRequest, depositResponse)
                .match();

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber()
        );
        assertNotNull(accountDao, "Счёт не найден в БД");

        DaoAndModelAssertions
                .assertThat(depositResponse, accountDao)
                .match();

        assertEquals(
                before.getBalance() + depositRequest.getBalance(),
                accountDao.getBalance(),
                DELTA
        );
    }

    public static Stream<Arguments> depositValidData() {
        return Stream.of(
                Arguments.of(0.01),
                Arguments.of(4999.99),
                Arguments.of(5000)
        );
    }

    @MethodSource("depositValidData")
    @ParameterizedTest
    public void userCanDepositMoneyWithBoundaryValidAmount(double balance) {
        AccountDao before = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber()
        );
        assertNotNull(before, "Счёт не найден в БД");

        DepositRequest depositRequest =
                DepositRequestGenerator.generate(
                        senderAccount.getId(),
                        balance
                );

        DepositResponse depositResponse =
                UserSteps.deposit(userSpec, depositRequest);

        ModelAssertions
                .assertThatModels(depositRequest, depositResponse)
                .match();

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber()
        );
        assertNotNull(accountDao, "Счёт не найден в БД");

        DaoAndModelAssertions
                .assertThat(depositResponse, accountDao)
                .match();

        assertEquals(
                before.getBalance() + depositRequest.getBalance(),
                accountDao.getBalance(),
                DELTA
        );
    }

    public static Stream<Arguments> depositInvalidData() {
        return Stream.of(
                Arguments.of(0, "Invalid account or amount"),
                Arguments.of(-100, "Invalid account or amount"),
                Arguments.of(5000.01, "Deposit amount exceeds the 5000 limit")
        );
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotDepositMoneyWithInvalidAmount(
            double balance,
            String errorMessage
    ) {
        AccountDao before = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber()
        );
        assertNotNull(before, "Счёт не найден в БД");

        DepositRequest depositRequest =
                DepositRequestGenerator.generate(
                        senderAccount.getId(),
                        balance
                );

        new CrudRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequestWithPlainText(errorMessage),
                Endpoint.DEPOSIT
        ).post(depositRequest);

        AccountDao after = DataBaseSteps.getAccountByAccountNumber(
                senderAccount.getAccountNumber()
        );
        assertNotNull(after, "Счёт не найден в БД");

        assertEquals(
                before.getBalance(),
                after.getBalance(),
                DELTA
        );
    }
}