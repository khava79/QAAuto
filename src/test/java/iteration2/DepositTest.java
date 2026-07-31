package iteration2;

import generators.DepositRequestGenerator;
import models.DepositRequest;
import models.DepositResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.DepositRequester;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositTest extends BaseTest {


    @Test
    public void userCanDepositMoneyWithCorrectAmount() {
        DepositRequest depositRequest =
                DepositRequestGenerator.generate(senderAccount.getId());

        DepositResponse depositResponse = new DepositRequester(
                userSpec,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(DepositResponse.class);

        softly.assertThat(depositResponse.getId()).isEqualTo(depositRequest.getId());
        softly.assertThat(depositResponse.getBalance()).isGreaterThanOrEqualTo(depositRequest.getBalance());
        softly.assertAll();
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
                DepositRequestGenerator.generate(senderAccount.getId(), balance);

        DepositResponse depositResponse = new DepositRequester(
                userSpec,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(DepositResponse.class);

        softly.assertThat(depositResponse.getId()).isEqualTo(depositRequest.getId());
        softly.assertThat(depositResponse.getBalance()).isGreaterThanOrEqualTo(balance);
        softly.assertAll();
    }

    public static Stream<Arguments> depositInvalidData() {
        return Stream.of(
                Arguments.of(0, "Deposit amount must be at least 0.01"),
                Arguments.of(-100, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000")
        );
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotDepositMoneyWithInvalidAmount(double balance,
                                                        String errorMessage) {
        DepositRequest depositRequest =
                DepositRequestGenerator.generate(senderAccount.getId(), balance);

        new DepositRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequestWithPlainText(errorMessage))
                .post(depositRequest);
    }
}