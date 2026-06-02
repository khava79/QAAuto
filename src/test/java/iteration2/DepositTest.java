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

public class DepositTest extends BaseTest {

    @Test
    public void userCanDepositMoneyWithCorrectAmount() {
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
    }
}