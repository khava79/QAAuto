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
import static org.hamcrest.MatcherAssert.assertThat;

public class DepositTest extends BaseTest {

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

    @Test
    public void userCanDepositMoneyWithCorrectAmount() {
        double balanceBefore = getAccountBalance(1);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .body("""
                        {
                          "id": 1,
                          "balance": 100
                        }
                        """)
                .post(BASE_URL + "/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        double balanceAfter = getAccountBalance(1);

        assertThat(balanceAfter, Matchers.closeTo(balanceBefore + 100, 0.001));
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
        double balanceBefore = getAccountBalance(1);

        String requestBody = String.format(
                """
                        {
                          "id": 1,
                          "balance": %s
                        }
                        """, balance);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .body(requestBody)
                .post(BASE_URL + "/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        double balanceAfter = getAccountBalance(1);

        assertThat(balanceAfter, Matchers.closeTo(balanceBefore + balance, 0.001));
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
                                                        String errorValue) {
        double balanceBefore = getAccountBalance(1);

        String requestBody = String.format(
                """
                        {
                          "id": 1,
                          "balance": %s
                        }
                        """, balance);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .body(requestBody)
                .post(BASE_URL + "/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(errorValue));

        double balanceAfter = getAccountBalance(1);

        assertThat(balanceAfter, Matchers.closeTo(balanceBefore, 0.001));
    }
}
