package iteration2.ui;

import api.generators.DepositRequestGenerator;
import api.models.CreateAccountResponse;
import api.models.DepositRequest;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest extends BaseUITest {

    // TC-TRANS-01, TC-TRANS-02
    // Позитивные сценарии: 100 и 10000
    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    public void userCanTransferMoneyTest(double transferAmount) {

        depositMoneyToSenderAccount(transferAmount);

        authAsUser(user);

        new UserDashboard()
                .open()
                .transferMoney(
                        senderAccount.getAccountNumber(),
                        user.getUsername(),
                        receiverAccount.getAccountNumber(),
                        transferAmount
                )
                .checkAlertMessageAndAccept(
                        BankAlert.TRANSFER_SUCCESSFULLY.getMessage()
                                + transferAmount
                );

        CreateAccountResponse actualSenderAccount =
                getAccount(senderAccount.getAccountNumber());

        CreateAccountResponse actualReceiverAccount =
                getAccount(receiverAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance())
                .isZero();

        assertThat(actualReceiverAccount.getBalance())
                .isEqualTo(transferAmount);
    }

    static Stream<Arguments> validTransferAmounts() {
        return Stream.of(
                Arguments.of(100.0),
                Arguments.of(10000.0)
        );
    }


    // TC-TRANS-03, TC-TRANS-06, TC-TRANS-07
    // Некорректные суммы
    @ParameterizedTest
    @MethodSource("invalidTransferAmounts")
    public void userCanNotTransferMoneyWithInvalidAmount(
            double transferAmount,
            String expectedMessage) {

        depositMoneyToSenderAccount(100);

        authAsUser(user);

        new UserDashboard()
                .open()
                .transferMoney(
                        senderAccount.getAccountNumber(),
                        user.getUsername(),
                        receiverAccount.getAccountNumber(),
                        transferAmount
                )
                .checkAlertMessageAndAccept(expectedMessage);

        CreateAccountResponse actualSenderAccount =
                getAccount(senderAccount.getAccountNumber());

        CreateAccountResponse actualReceiverAccount =
                getAccount(receiverAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance())
                .isEqualTo(100);

        assertThat(actualReceiverAccount.getBalance())
                .isZero();
    }

    static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(
                        0.0,
                        BankAlert.TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage()
                ),
                Arguments.of(
                        -100.0,
                        BankAlert.TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage()
                ),
                Arguments.of(
                        10000.01,
                        BankAlert.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage()
                )
        );
    }


    // TC-TRANS-04
    // Недостаточно средств
    @Test
    public void userCanNotTransferMoneyWhenBalanceIsInsufficient() {

        depositMoneyToSenderAccount(100);

        authAsUser(user);

        new UserDashboard()
                .open()
                .transferMoney(
                        senderAccount.getAccountNumber(),
                        user.getUsername(),
                        receiverAccount.getAccountNumber(),
                        101
                )
                .checkAlertMessageAndAccept(
                        BankAlert.INSUFFICIENT_FUNDS.getMessage()
                );

        CreateAccountResponse actualSenderAccount =
                getAccount(senderAccount.getAccountNumber());

        CreateAccountResponse actualReceiverAccount =
                getAccount(receiverAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance())
                .isEqualTo(100);

        assertThat(actualReceiverAccount.getBalance())
                .isZero();
    }


    // TC-TRANS-05
    // Несуществующий счёт
    @Test
    public void userCanNotTransferMoneyToNonexistentAccount() {

        depositMoneyToSenderAccount(100);

        authAsUser(user);

        new UserDashboard()
                .open()
                .transferMoney(
                        senderAccount.getAccountNumber(),
                        user.getUsername(),
                        "ACC999999",
                        100
                )
                .checkAlertMessageAndAccept(
                        BankAlert.NO_USER_FOUND_WITH_THIS_ACCOUNT_NUMBER
                                .getMessage()
                );

        CreateAccountResponse actualSenderAccount =
                getAccount(senderAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance())
                .isEqualTo(100);
    }


    // TC-TRANS-08
    // Пустая сумма
    @Test
    public void userCanNotTransferMoneyWithEmptyAmount() {

        depositMoneyToSenderAccount(100);

        authAsUser(user);

        new UserDashboard()
                .open()
                .transferMoneyWithEmptyAmount(
                        senderAccount.getAccountNumber(),
                        user.getUsername(),
                        receiverAccount.getAccountNumber()
                )
                .checkAlertMessageAndAccept(
                        BankAlert.INVALID_TRANSFER_AMOUNT.getMessage()
                );

        CreateAccountResponse actualSenderAccount =
                getAccount(senderAccount.getAccountNumber());

        CreateAccountResponse actualReceiverAccount =
                getAccount(receiverAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance())
                .isEqualTo(100);

        assertThat(actualReceiverAccount.getBalance())
                .isZero();
    }


    private CreateAccountResponse getAccount(String accountNumber) {

        return new UserSteps(
                user.getUsername(),
                user.getPassword()
        ).getAccountByNumber(accountNumber);
    }


    private void depositMoneyToSenderAccount(double amount) {

        if (amount <= 5000) {
            deposit(amount);
            return;
        }

        deposit(5000);
        deposit(amount - 5000);
    }


    private void deposit(double amount) {

        DepositRequest depositRequest =
                DepositRequestGenerator.generate(
                        senderAccount.getId(),
                        amount
                );

        UserSteps.deposit(
                userSpec,
                depositRequest
        );
    }
}