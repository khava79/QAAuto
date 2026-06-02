package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class TransferTest extends BaseTest {

    @Test
    public void userCanTransferMoneyBetweenOwnAccounts() {
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
    }

    public static Stream<Arguments> transferValidData() {
        return Stream.of(
                Arguments.of(0.01),
                Arguments.of(9999.99),
                Arguments.of(10000)
        );
    }

    @MethodSource("transferValidData")
    @ParameterizedTest
    public void userCanTransferMoneyWithBoundaryValidAmount(double amount) {
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
    }
}