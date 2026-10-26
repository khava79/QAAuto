package iteration1.ui;

import api.configs.Config;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import common.extensions.AdminSessionExtension;
import iteration2.api.BaseTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

@ExtendWith(AdminSessionExtension.class)
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
    }

    @AfterEach
    public void closeBrowser() {
        Selenide.closeWebDriver();
    }
}