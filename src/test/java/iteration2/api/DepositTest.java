package iteration2.api;

import api.generators.DepositRequestGenerator;
import api.models.DepositRequest;
import api.models.DepositResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositTest extends BaseTest {

    @Test
    public void userCanDepositMoneyWithCorrectAmount() {

        DepositRequest depositRequest =
                DepositRequestGenerator.generate(senderAccount.getId());

        DepositResponse depositResponse =
                UserSteps.deposit(userSpec, depositRequest);

        ModelAssertions
                .assertThatModels(depositRequest, depositResponse)
                .match();
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

        DepositRequest depositRequest =
                DepositRequestGenerator.generate(
                        senderAccount.getId(),
                        balance);

        DepositResponse depositResponse =
                UserSteps.deposit(userSpec, depositRequest);

        ModelAssertions
                .assertThatModels(depositRequest, depositResponse)
                .match();
    }

    public static Stream<Arguments> depositInvalidData() {
        return Stream.of(
                Arguments.of(
                        0,
                        "Deposit amount must be at least 0.01"
                ),
                Arguments.of(
                        -100,
                        "Deposit amount must be at least 0.01"
                ),
                Arguments.of(
                        5000.01,
                        "Deposit amount cannot exceed 5000"
                )
        );
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotDepositMoneyWithInvalidAmount(
            double balance,
            String errorMessage) {

        DepositRequest depositRequest =
                DepositRequestGenerator.generate(
                        senderAccount.getId(),
                        balance);

        new CrudRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequestWithPlainText(errorMessage),
                Endpoint.DEPOSIT)
                .post(depositRequest);
    }
}