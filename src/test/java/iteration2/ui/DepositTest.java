package iteration2.ui;

import api.models.CreateAccountResponse;
import common.annotations.Browsers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;


public class DepositTest extends BaseUITest {

    // TC-DEP-01 и TC-DEP-02
    // Позитивные сценарии: 100 и 5000
    public static Stream<Arguments> validDepositAmounts() {
        return Stream.of(
                Arguments.of(100.0, 100.0),
                Arguments.of(5000.0, 5000.0)
        );
    }

    @ParameterizedTest
    @MethodSource("validDepositAmounts")
    @Browsers({"chrome"})
    public void userCanDepositMoneyTest(
            double depositAmount,
            double expectedBalance) {

        new UserDashboard().open().depositMoney(senderAccount.getAccountNumber(), depositAmount)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage() + depositAmount + " to account " + senderAccount.getAccountNumber() + "!");

        CreateAccountResponse actualSenderAccount =
                userSteps.getAccountByNumber(senderAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance()).isEqualTo(expectedBalance);
    }


    // TC-DEP-03, TC-DEP-04, TC-DEP-05
    // Негативные сценарии: 5001, 0, -100
    public static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(
                        5001,
                        BankAlert.DEPOSIT_AMOUNT_MUST_BE_LESS_OR_EQUAL_5000.getMessage()
                ),
                Arguments.of(
                        0,
                        BankAlert.INVALID_DEPOSIT_AMOUNT.getMessage()
                ),
                Arguments.of(
                        -100,
                        BankAlert.INVALID_DEPOSIT_AMOUNT.getMessage()
                )
        );
    }


    @ParameterizedTest
    @MethodSource("invalidDepositAmounts")
    public void userCannotDepositMoneyWithInvalidAmount(
            double depositAmount,
            String expectedErrorMessage) {

        new UserDashboard().open().depositMoney(senderAccount.getAccountNumber(), depositAmount)
                .checkAlertMessageAndAccept(expectedErrorMessage);

        CreateAccountResponse actualSenderAccount =
                userSteps.getAccountByNumber(senderAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance()).isZero();
    }


    // TC-DEP-06
    // Пустое поле суммы
    @Test
    public void userCannotDepositMoneyWithEmptyAmount() {

        new UserDashboard().open().depositMoneyWithEmptyAmount(senderAccount.getAccountNumber())
                .checkAlertMessageAndAccept(BankAlert.INVALID_DEPOSIT_AMOUNT.getMessage());

        CreateAccountResponse actualSenderAccount =
                userSteps.getAccountByNumber(senderAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance()).isZero();
    }

}