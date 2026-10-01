package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfileNameTest {

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

    // Создание пользователя, авторизация и открытие страницы профиля
    private void openUserProfile() {
        // Создаём пользователя через API
        CreateUserRequest user = AdminSteps.createUser();

        // Авторизуемся через API и получаем токен
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

        // Открываем UI и сохраняем токен в localStorage
        Selenide.open("/");

        Selenide.executeJavaScript(
                "localStorage.setItem('authToken', arguments[0]);",
                userAuthHeader
        );

        // Переходим на страницу профиля
        Selenide.open("/dashboard");

        $(".profile-header")
                .shouldBe(Condition.visible)
                .click();
    }

    // Проверка успешного изменения имени
    @Test
    public void userCanChangeProfileNameTest() {
        openUserProfile();

        String newName = "Ivan Ivanov";

        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.visible)
                .setValue(newName);

        $$("button")
                .findBy(Condition.text("Save Changes"))
                .shouldBe(Condition.visible)
                .click();

        String alertText = Selenide.switchTo().alert().getText();

        assertEquals("✅ Name updated successfully!", alertText);

        Selenide.switchTo().alert().accept();

        Selenide.refresh();

        $(".user-name")
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text(newName));
    }

    // Данные для проверки некорректных имён
    static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of("Ivan", "Одно слово"),
                Arguments.of("Ivan Ivanov Petrov", "Три слова"),
                Arguments.of("Ivan123 Ivanov", "Цифры в имени"),
                Arguments.of("Ivan @Ivanov", "Спецсимволы в имени")

        );
    }

    // Проверка, что некорректное имя не сохраняется
    @ParameterizedTest(name = "{1}")
    @MethodSource("invalidNames")
    public void userCannotChangeProfileNameToInvalidValueTest(
            String invalidName,
            String testCase
    ) {
        openUserProfile();

        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.visible)
                .setValue(invalidName);

        $$("button")
                .findBy(Condition.text("Save Changes"))
                .shouldBe(Condition.visible)
                .click();

        // Проверяем сообщение об ошибке
        String alertText = Selenide.switchTo().alert().getText();

        assertEquals("Name must contain two words with letters only", alertText);

        Selenide.switchTo().alert().accept();
    }

    @Test
    public void userCannotChangeProfileNameToEmptyValueTest() {

        openUserProfile();

        // Оставляем поле имени пустым
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldBe(Condition.visible)
                .clear();

        // Нажимаем Save Changes
        $$("button")
                .findBy(Condition.text("Save Changes"))
                .shouldBe(Condition.visible)
                .click();

        // Получаем текст alert
        String alertText = Selenide.switchTo()
                .alert()
                .getText();

        // Проверяем сообщение для пустого поля
        assertEquals(
                "❌ Please enter a valid name.",
                alertText
        );

        // Закрываем alert
        Selenide.switchTo()
                .alert()
                .accept();
    }
}