package iteration2.ui;

import api.configs.Config;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import common.extensions.BrowserMatchExtension;
import iteration2.api.BaseTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import ui.pages.BasePage;

import java.util.Map;

@ExtendWith(BrowserMatchExtension.class)
public class BaseUITest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = Config.getProperty("uiRemote");
        Configuration.baseUrl = Config.getProperty("uiBaseUrl");
        Configuration.browser = Config.getProperty("browser");
        Configuration.browserSize = Config.getProperty("browserSize");

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", false, "enableLog", true)
        );
        Configuration.browserCapabilities.setCapability("unhandledPromptBehavior", "ignore");

        Configuration.timeout = 10000;
        Configuration.pageLoadTimeout = 60000;
        Configuration.pageLoadStrategy = "eager";
        Configuration.headless = true;
    }

    @BeforeEach
    public void authorizeUser() {
        BasePage.authAsUser(user);
    }

    @AfterEach
    public void closeBrowser() {
        Selenide.closeWebDriver();
    }

}
