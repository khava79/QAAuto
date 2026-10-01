package iteration2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.DepositRequestGenerator;
import iteration2.api.BaseTest;
import models.CreateAccountResponse;
import models.DepositRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.switchTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest extends BaseTest {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote =
                "http://localhost:4444/wd/hub";

        Configuration.baseUrl =
                "http://host.docker.internal:3000";

        Configuration.browser =
                "chrome";

        Configuration.browserSize =
                "1920x1080";

        Configuration.browserCapabilities.setCapability(
                "selenoid:options",
                Map.of(
                        "enableVNC", true,
                        "enableLog", true
                )
        );
    }

    /**
     * TC-TRANS-01, TC-TRANS-02
     * Успешный перевод денежных средств.
     */
    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    public void userCanTransferMoneyTest(double transferAmount) {

        // 1. Подготавливаем тестовые данные
        depositMoneyToSenderAccount(transferAmount);

        // 2. Авторизуем пользователя
        String userAuthHeader = loginUser();

        // 3. Открываем страницу перевода
        openTransferPage(userAuthHeader);

        // 4. Заполняем форму перевода
        fillTransferForm(
                senderAccount.getAccountNumber(),
                user.getUsername(),
                receiverAccount.getAccountNumber(),
                transferAmount
        );

        // 5. Отправляем перевод
        Alert alert = sendTransfer();

        // 6. Проверяем сообщение
        String alertText = alert.getText();

        assertThat(alertText)
                .contains(String.valueOf(transferAmount));

        alert.accept();

        // 7. Проверяем балансы
        CreateAccountResponse[] accounts = getUserAccounts();

        assertThat(
                findAccount(
                        accounts,
                        senderAccount.getAccountNumber()
                ).getBalance()
        ).isEqualTo(0.0);

        assertThat(
                findAccount(
                        accounts,
                        receiverAccount.getAccountNumber()
                ).getBalance()
        ).isEqualTo(transferAmount);
    }

    public static Stream<Arguments> validTransferAmounts() {
        return Stream.of(
                Arguments.of(100.0),
                Arguments.of(10000.0)
        );
    }

    /**
     * TC-TRANS-03, TC-TRANS-06, TC-TRANS-07
     * Некорректная сумма перевода.
     */
    @ParameterizedTest
    @MethodSource("invalidTransferAmounts")
    public void userCanNotTransferMoneyWithInvalidAmount(
            double transferAmount,
            String expectedMessage) {

        // 1. Подготавливаем тестовые данные
        depositMoneyToSenderAccount(100);

        // 2. Авторизуем пользователя
        String userAuthHeader = loginUser();

        // 3. Открываем страницу перевода
        openTransferPage(userAuthHeader);

        // 4. Заполняем форму
        fillTransferForm(
                senderAccount.getAccountNumber(),
                user.getUsername(),
                receiverAccount.getAccountNumber(),
                transferAmount
        );

        // 5. Отправляем перевод
        Alert alert = sendTransfer();

        // 6. Проверяем сообщение об ошибке
        String alertText = alert.getText();

        assertThat(alertText)
                .isEqualTo(expectedMessage);

        alert.accept();
    }

    public static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(
                        0.0,
                        "❌ Error: Transfer amount must be at least 0.01"
                ),
                Arguments.of(
                        -100.0,
                        "❌ Error: Transfer amount must be at least 0.01"
                ),
                Arguments.of(
                        10000.01,
                        "❌ Error: Transfer amount cannot exceed 10000"
                )
        );
    }

    /**
     * TC-TRANS-04
     * Недостаточно средств на счёте отправителя.
     */
    @Test
    public void userCanNotTransferMoneyWhenBalanceIsInsufficient() {

        // 1. Подготавливаем тестовые данные
        depositMoneyToSenderAccount(100);

        // 2. Авторизуем пользователя
        String userAuthHeader = loginUser();

        // 3. Открываем страницу перевода
        openTransferPage(userAuthHeader);

        // 4. Заполняем форму
        fillTransferForm(
                senderAccount.getAccountNumber(),
                user.getUsername(),
                receiverAccount.getAccountNumber(),
                101
        );

        // 5. Отправляем перевод
        Alert alert = sendTransfer();

        // 6. Проверяем сообщение об ошибке
        String alertText = alert.getText();

        assertThat(alertText)
                .isEqualTo(
                        "❌ Error: Invalid transfer: insufficient funds or invalid accounts"
                );

        alert.accept();
    }

    /**
     * TC-TRANS-05
     * Перевод на несуществующий счёт.
     */
    @Test
    public void userCanNotTransferMoneyToNonexistentAccount() {

        // 1. Подготавливаем тестовые данные
        depositMoneyToSenderAccount(100);

        // 2. Авторизуем пользователя
        String userAuthHeader = loginUser();

        // 3. Открываем страницу перевода
        openTransferPage(userAuthHeader);

        // 4. Заполняем форму
        $(Selectors.byText("🔄 Make a Transfer"))
                .click();

        $(".account-selector")
                .selectOptionContainingText(
                        senderAccount.getAccountNumber()
                );

        $("input[placeholder='Enter recipient name']")
                .setValue(user.getUsername());

        $("input[placeholder='Enter recipient account number']")
                .setValue("ACC999999");

        $("input[placeholder='Enter amount']")
                .setValue("100");

        // 5. Отправляем перевод
        $("#confirmCheck")
                .click();

        $(Selectors.byText("🚀 Send Transfer"))
                .click();

        // 6. Проверяем сообщение об ошибке
        Alert alert = switchTo().alert();

        String alertText = alert.getText();

        assertThat(alertText)
                .isEqualTo(
                        "❌ No user found with this account number."
                );

        alert.accept();
    }

    /**
     * TC-TRANS-08
     * Перевод без указания суммы.
     */
    @Test
    public void userCanNotTransferMoneyWithEmptyAmount() {

        // 1. Подготавливаем тестовые данные
        depositMoneyToSenderAccount(100);

        // 2. Авторизуем пользователя
        String userAuthHeader = loginUser();

        // 3. Открываем страницу перевода
        openTransferPage(userAuthHeader);

        // 4. Заполняем форму без указания суммы
        $(Selectors.byText("🔄 Make a Transfer"))
                .click();

        $(".account-selector")
                .selectOptionContainingText(
                        senderAccount.getAccountNumber()
                );

        $("input[placeholder='Enter recipient name']")
                .setValue(user.getUsername());

        $("input[placeholder='Enter recipient account number']")
                .setValue(receiverAccount.getAccountNumber());

        // 5. Пытаемся отправить перевод
        $("#confirmCheck")
                .click();

        $(Selectors.byText("🚀 Send Transfer"))
                .click();

        // 6. Проверяем, что перевод не произошёл
        CreateAccountResponse[] accounts =
                getUserAccounts();

        CreateAccountResponse sender =
                findAccount(
                        accounts,
                        senderAccount.getAccountNumber()
                );

        CreateAccountResponse receiver =
                findAccount(
                        accounts,
                        receiverAccount.getAccountNumber()
                );

        assertThat(sender.getBalance())
                .isEqualTo(100.0);

        assertThat(receiver.getBalance())
                .isEqualTo(0.0);
    }

    private String loginUser() {
        return new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN
        )
                .post(
                        LoginUserRequest.builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .build()
                )
                .extract()
                .header("Authorization");
    }

    private void openTransferPage(String userAuthHeader) {
        Selenide.open("/");

        executeJavaScript(
                "localStorage.setItem('authToken', arguments[0]);",
                userAuthHeader
        );

        Selenide.open("/dashboard");
    }

    private void fillTransferForm(
            String senderAccountNumber,
            String recipientName,
            String recipientAccountNumber,
            double amount) {

        $(Selectors.byText("🔄 Make a Transfer"))
                .click();

        $(".account-selector")
                .selectOptionContainingText(
                        senderAccountNumber
                );

        $("input[placeholder='Enter recipient name']")
                .setValue(recipientName);

        $("input[placeholder='Enter recipient account number']")
                .setValue(recipientAccountNumber);

        $("input[placeholder='Enter amount']")
                .setValue(String.valueOf(amount));
    }

    private Alert sendTransfer() {
        $("#confirmCheck")
                .click();

        $(Selectors.byText("🚀 Send Transfer"))
                .click();

        return switchTo().alert();
    }

    private CreateAccountResponse[] getUserAccounts() {
        return given()
                .spec(
                        RequestSpecs.authAsUser(
                                user.getUsername(),
                                user.getPassword()
                        )
                )
                .get(
                        "http://localhost:4111/api/v1/customer/accounts"
                )
                .then()
                .statusCode(200)
                .extract()
                .as(CreateAccountResponse[].class);
    }

    private void depositMoneyToSenderAccount(double amount) {

        if (amount <= 5000) {

            DepositRequest depositRequest =
                    DepositRequestGenerator.generate(
                            senderAccount.getId(),
                            amount
                    );

            UserSteps.deposit(
                    userSpec,
                    depositRequest
            );

        } else {

            DepositRequest firstDeposit =
                    DepositRequestGenerator.generate(
                            senderAccount.getId(),
                            5000
                    );

            UserSteps.deposit(
                    userSpec,
                    firstDeposit
            );

            DepositRequest secondDeposit =
                    DepositRequestGenerator.generate(
                            senderAccount.getId(),
                            amount - 5000
                    );

            UserSteps.deposit(
                    userSpec,
                    secondDeposit
            );
        }
    }

    private CreateAccountResponse findAccount(
            CreateAccountResponse[] accounts,
            String accountNumber) {

        return Arrays.stream(accounts)
                .filter(account ->
                        account.getAccountNumber()
                                .equals(accountNumber)
                )
                .findFirst()
                .orElseThrow();
    }
}