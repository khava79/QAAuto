package iteration2.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;
import java.util.stream.Stream;

public class ProfileNameTest extends BaseUITest {
    private static final String VALID_PROFILE_NAME = "Ivan Ivanov";


    @Test
    public void userCanChangeProfileNameTest() {
        new UserDashboard().open().openProfile().changeProfileName(VALID_PROFILE_NAME).checkAlertMessageAndAccept(
                BankAlert.PROFILE_NAME_UPDATED.getMessage());

        new UserDashboard().open().checkProfileName(VALID_PROFILE_NAME);

    }


    public static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of("Ivan", "Одно слово"),
                Arguments.of("Ivan Ivanov Petrov", "Три слова"),
                Arguments.of("Ivan123 Ivanov", "Цифры в имени"),
                Arguments.of("Ivan @Ivanov", "Спецсимволы в имени")
        );
    }

    @ParameterizedTest(name = "[{index}] invalidName={0}")
    @MethodSource("invalidNames")
    public void userCannotChangeProfileNameToInvalidValueTest(
            String invalidName,
            String testCase) {

        new UserDashboard().open().openProfile().changeProfileName(invalidName).checkAlertMessageAndAccept(
                BankAlert.INVALID_PROFILE_NAME.getMessage());

    }



    @Test
    public void userCannotChangeProfileNameToEmptyValueTest() {
        new UserDashboard().open().openProfile().clearProfileName().checkAlertMessageAndAccept(
                BankAlert.INVALID_PROFILE_NAME_EMPTY.getMessage());


    }
}