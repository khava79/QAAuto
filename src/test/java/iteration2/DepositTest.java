package iteration2;

import iteration2.BaseTest;
import models.DepositRequest;
import models.DepositResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.DepositRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositTest extends BaseTest {

    @Test
    public void userCanDepositMoneyWithCorrectAmount() {
        DepositRequest depositRequest = DepositRequest.builder()
                .id(1)
                .balance(100)
                .build();

        DepositResponse depositResponse = new DepositRequester(
                RequestSpecs.authAsUser("kate2026", "Password33$"),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(DepositResponse.class);

        softly.assertThat(depositResponse.getId()).isEqualTo(depositRequest.getId());
        softly.assertThat(depositResponse.getBalance()).isGreaterThanOrEqualTo(depositRequest.getBalance());
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
        DepositRequest depositRequest = DepositRequest.builder()
                .id(1)
                .balance(balance)
                .build();

        DepositResponse depositResponse = new DepositRequester(
                RequestSpecs.authAsUser("kate2026", "Password33$"),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(DepositResponse.class);

        softly.assertThat(depositResponse.getId()).isEqualTo(depositRequest.getId());
        softly.assertThat(depositResponse.getBalance()).isGreaterThanOrEqualTo(balance);
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
        DepositRequest depositRequest = DepositRequest.builder()
                .id(1)
                .balance(balance)
                .build();

        new DepositRequester(
                RequestSpecs.authAsUser("kate2026", "Password33$"),
                ResponseSpecs.requestReturnsBadRequestWithPlainText(errorMessage))
                .post(depositRequest);
    }
}