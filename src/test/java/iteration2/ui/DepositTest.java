package iteration2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static io.restassured.RestAssured.given;


public class DepositTest {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://host.docker.internal:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability(
                "selenoid:options",
                Map.of(
                        "enableVNC", true,
                        "enableLog", true
                )
        );
    }


    // TC-DEP-01 и TC-DEP-02
    // Позитивные сценарии: 100 и 5000


    public static Stream<Arguments> validDepositAmounts() {
        return Stream.of(
                Arguments.of(100, 100.0),
                Arguments.of(5000, 5000.0)
        );
    }

    @ParameterizedTest
    @MethodSource("validDepositAmounts")
    public void userCanDepositMoneyTest(
            double depositAmount,
            double expectedBalance) {

        // ШАГ 1: создаём пользователя через API
        CreateUserRequest user = AdminSteps.createUser();


        // ШАГ 2: логинимся через API и получаем токен
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN)
                .post(
                        LoginUserRequest.builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .build()
                )
                .extract()
                .header("Authorization");


        // ШАГ 3: открываем UI и авторизуем пользователя
        Selenide.open("/");

        executeJavaScript(
                "localStorage.setItem('authToken', arguments[0]);",
                userAuthHeader
        );


        // Создаём счёт через API

        CreateAccountResponse account = UserSteps.createAccount(
                RequestSpecs.authAsUser(
                        user.getUsername(),
                        user.getPassword()
                )
        );


        Selenide.open("/dashboard");



        // ШАГ 4: пользователь делает депозит через UI

        $(Selectors.byText("💰 Deposit Money")).click();

        $(".account-selector")
                .selectOptionContainingText(
                        account.getAccountNumber()
                );

        $(".deposit-input")
                .setValue(String.valueOf(depositAmount));

        $(Selectors.byText("💵 Deposit")).click();



        // ШАГ 5: проверяем результат на UI

        Alert alert = Selenide.switchTo().alert();

        String alertText = alert.getText();

        assertThat(alertText)
                .isEqualTo(
                        "✅ Successfully deposited $"
                                + depositAmount
                                + " to account "
                                + account.getAccountNumber()
                                + "!"
                );

        alert.accept();



        // ШАГ 6: проверяем баланс через API

        CreateAccountResponse[] accounts = given()
                .spec(
                        RequestSpecs.authAsUser(
                                user.getUsername(),
                                user.getPassword()
                        )
                )
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .as(CreateAccountResponse[].class);


        assertThat(accounts)
                .hasSize(1);

        assertThat(accounts[0].getAccountNumber())
                .isEqualTo(account.getAccountNumber());

        assertThat(accounts[0].getBalance())
                .isEqualTo(expectedBalance);
    }



    // TC-DEP-03, TC-DEP-04, TC-DEP-05
    // Негативные сценарии: 5001, 0, -100

    public static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(
                        5001,
                        "❌ Please deposit less or equal to 5000$."
                ),
                Arguments.of(
                        0,
                        "❌ Please enter a valid amount."
                ),
                Arguments.of(
                        -100,
                        "❌ Please enter a valid amount."
                )
        );
    }


    @ParameterizedTest
    @MethodSource("invalidDepositAmounts")
    public void userCanNotDepositMoneyWithInvalidAmount(
            double depositAmount,
            String expectedErrorMessage) {

        // ШАГ 1: создаём пользователя через API
        CreateUserRequest user = AdminSteps.createUser();


        // ШАГ 2: логинимся через API
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN)
                .post(
                        LoginUserRequest.builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .build()
                )
                .extract()
                .header("Authorization");


        // ШАГ 3: открываем UI и авторизуем пользователя
        Selenide.open("/");

        executeJavaScript(
                "localStorage.setItem('authToken', arguments[0]);",
                userAuthHeader
        );


        // Создаём счёт через API
        CreateAccountResponse account = UserSteps.createAccount(
                RequestSpecs.authAsUser(
                        user.getUsername(),
                        user.getPassword()
                )
        );


        Selenide.open("/dashboard");



        // ШАГ 4: вводим неправильную сумму через UI

        $(Selectors.byText("💰 Deposit Money")).click();

        $(".account-selector")
                .selectOptionContainingText(
                        account.getAccountNumber()
                );

        $(".deposit-input")
                .setValue(String.valueOf(depositAmount));

        $(Selectors.byText("💵 Deposit")).click();



        // ШАГ 5: проверяем ошибку на UI

        Alert alert = Selenide.switchTo().alert();

        String alertText = alert.getText();

        assertThat(alertText)
                .contains(expectedErrorMessage);

        alert.accept();


        // ШАГ 6: проверяем через API, что деньги не были зачислены

        CreateAccountResponse[] accounts = given()
                .spec(
                        RequestSpecs.authAsUser(
                                user.getUsername(),
                                user.getPassword()
                        )
                )
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .as(CreateAccountResponse[].class);


        assertThat(accounts)
                .hasSize(1);

        assertThat(accounts[0].getAccountNumber())
                .isEqualTo(account.getAccountNumber());

        assertThat(accounts[0].getBalance())
                .isEqualTo(0.0);
    }



    // TC-DEP-06
    // Пустое поле суммы

    @Test
    public void userCanNotDepositMoneyWithEmptyAmount() {

        // ШАГ 1: создаём пользователя через API
        CreateUserRequest user = AdminSteps.createUser();


        // ШАГ 2: логинимся через API
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN)
                .post(
                        LoginUserRequest.builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .build()
                )
                .extract()
                .header("Authorization");


        // ШАГ 3: открываем UI и авторизуем пользователя
        Selenide.open("/");

        executeJavaScript(
                "localStorage.setItem('authToken', arguments[0]);",
                userAuthHeader
        );


        // Создаём счёт через API
        CreateAccountResponse account = UserSteps.createAccount(
                RequestSpecs.authAsUser(
                        user.getUsername(),
                        user.getPassword()
                )
        );


        Selenide.open("/dashboard");



        // ШАГ 4: открываем форму депозита

        $(Selectors.byText("💰 Deposit Money")).click();

        $(".account-selector")
                .selectOptionContainingText(
                        account.getAccountNumber()
                );


        $(".deposit-input").clear();

        $(Selectors.byText("💵 Deposit")).click();



        // ШАГ 5: проверяем ошибку на UI

        Alert alert = Selenide.switchTo().alert();

        String alertText = alert.getText();

        assertThat(alertText)
                .isNotEmpty();

        alert.accept();


        // ШАГ 6: проверяем через API, что баланс остался 0


        CreateAccountResponse[] accounts = given()
                .spec(
                        RequestSpecs.authAsUser(
                                user.getUsername(),
                                user.getPassword()
                        )
                )
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .as(CreateAccountResponse[].class);


        assertThat(accounts)
                .hasSize(1);

        assertThat(accounts[0].getAccountNumber())
                .isEqualTo(account.getAccountNumber());

        assertThat(accounts[0].getBalance())
                .isEqualTo(0.0);
    }



    private String formatAmount(double amount) {

        if (amount == (long) amount) {
            return String.valueOf((long) amount);
        }

        return String.valueOf(amount);
    }
}