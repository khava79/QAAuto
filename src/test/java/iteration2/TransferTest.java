package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class TransferTest extends BaseTest {

    private double getAccountBalance(int accountId) {
        return given()
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .get(BASE_URL + "/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + accountId + " }.balance");
    }

    private void depositMoneyToSenderAccount(double amount) {
        String requestBody = String.format(
                """
                        {
                          "id": 1,
                          "balance": %s
                        }
                        """, amount);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .body(requestBody)
                .post(BASE_URL + "/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void userCanTransferMoneyBetweenOwnAccounts() {
        depositMoneyToSenderAccount(100);

        double senderBalanceBefore = getAccountBalance(1);
        double receiverBalanceBefore = getAccountBalance(2);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .body("""
                        {
                          "senderAccountId": 1,
                          "receiverAccountId": 2,
                          "amount": 100
                        }
                        """)
                .post(BASE_URL + "/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        double senderBalanceAfter = getAccountBalance(1);
        double receiverBalanceAfter = getAccountBalance(2);

        MatcherAssert.assertThat(
                senderBalanceAfter,
                Matchers.closeTo(senderBalanceBefore - 100, 0.001)
        );

        MatcherAssert.assertThat(
                receiverBalanceAfter,
                Matchers.closeTo(receiverBalanceBefore + 100, 0.001)
        );
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

        double senderBalanceBefore = getAccountBalance(1);
        double receiverBalanceBefore = getAccountBalance(2);

        String requestBody = String.format(
                """
                        {
                          "senderAccountId": 1,
                          "receiverAccountId": 2,
                          "amount": %s
                        }
                        """, amount);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .body(requestBody)
                .post(BASE_URL + "/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        double senderBalanceAfter = getAccountBalance(1);
        double receiverBalanceAfter = getAccountBalance(2);

        MatcherAssert.assertThat(
                senderBalanceAfter,
                Matchers.closeTo(senderBalanceBefore - amount, 0.001)
        );

        MatcherAssert.assertThat(
                receiverBalanceAfter,
                Matchers.closeTo(receiverBalanceBefore + amount, 0.001)
        );
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
        double senderBalanceBefore = getAccountBalance(1);
        double receiverBalanceBefore = getAccountBalance(2);

        String requestBody = String.format(
                """
                        {
                          "senderAccountId": 1,
                          "receiverAccountId": 2,
                          "amount": %s
                        }
                        """, amount);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .body(requestBody)
                .post(BASE_URL + "/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorValue));

        double senderBalanceAfter = getAccountBalance(1);
        double receiverBalanceAfter = getAccountBalance(2);

        MatcherAssert.assertThat(
                senderBalanceAfter,
                Matchers.closeTo(senderBalanceBefore, 0.001)
        );

        MatcherAssert.assertThat(
                receiverBalanceAfter,
                Matchers.closeTo(receiverBalanceBefore, 0.001)
        );
    }
}
