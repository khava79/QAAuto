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
    private static final double MAX_DEPOSIT_AMOUNT = 5000.0;
    private static final double INITIAL_BALANCE = 100.0;
    private static final String NONEXISTENT_ACCOUNT_NUMBER = "ACC999999";

    // TC-TRANS-01, TC-TRANS-02
    // Позитивные сценарии: 100 и 10000
    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    public void userCanTransferMoneyTest(double transferAmount) {
        depositMoneyToSenderAccount(transferAmount);

        try {
            new UserDashboard().open().transferMoney(senderAccount.getAccountNumber(), user.getUsername(),
                            receiverAccount.getAccountNumber(), transferAmount)
                    .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.format(transferAmount));
        } catch (AssertionError e) {
            System.out.println("SENDER=" + userSteps.getAccountByNumber(senderAccount.getAccountNumber()).getBalance()
                    + " RECEIVER=" + userSteps.getAccountByNumber(receiverAccount.getAccountNumber()).getBalance());
            throw e;
        }

        CreateAccountResponse actualSenderAccount =
                userSteps.getAccountByNumber(senderAccount.getAccountNumber());

        CreateAccountResponse actualReceiverAccount =
                userSteps.getAccountByNumber(receiverAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance()).isZero();

        assertThat(actualReceiverAccount.getBalance()).isEqualTo(transferAmount);
    }

    static Stream<Arguments> validTransferAmounts() {
        return Stream.of(
                Arguments.of(INITIAL_BALANCE),
                Arguments.of(10000.0)
        );
    }


    // TC-TRANS-03, TC-TRANS-06, TC-TRANS-07
    // Некорректные суммы
    @ParameterizedTest
    @MethodSource("invalidTransferAmounts")
    public void userCanNotTransferMoneyWithInvalidAmount(double transferAmount, String expectedMessage) {
        depositMoneyToSenderAccount(INITIAL_BALANCE);

        new UserDashboard().open().transferMoney(senderAccount.getAccountNumber(), user.getUsername(), receiverAccount.getAccountNumber(), transferAmount)
                .checkAlertMessageAndAccept(expectedMessage);

        CreateAccountResponse actualSenderAccount =
                userSteps.getAccountByNumber(senderAccount.getAccountNumber());

        CreateAccountResponse actualReceiverAccount =
                userSteps.getAccountByNumber(receiverAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance()).isEqualTo(INITIAL_BALANCE);

        assertThat(actualReceiverAccount.getBalance()).isZero();
    }

    static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(
                        0.0,
                        BankAlert.TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage()
                ),
                Arguments.of(
                        -INITIAL_BALANCE,
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
        depositMoneyToSenderAccount(INITIAL_BALANCE);

        new UserDashboard().open().transferMoney(senderAccount.getAccountNumber(), user.getUsername(), receiverAccount.getAccountNumber(), INITIAL_BALANCE + 1)
                .checkAlertMessageAndAccept(BankAlert.INSUFFICIENT_FUNDS.getMessage());

        CreateAccountResponse actualSenderAccount =
                userSteps.getAccountByNumber(senderAccount.getAccountNumber());

        CreateAccountResponse actualReceiverAccount =
                userSteps.getAccountByNumber(receiverAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance()).isEqualTo(INITIAL_BALANCE);

        assertThat(actualReceiverAccount.getBalance()).isZero();
    }


    // TC-TRANS-05
    // Несуществующий счёт
    @Test
    public void userCanNotTransferMoneyToNonexistentAccount() {
        depositMoneyToSenderAccount(INITIAL_BALANCE);

        new UserDashboard().open().transferMoney(senderAccount.getAccountNumber(), user.getUsername(), NONEXISTENT_ACCOUNT_NUMBER,
                        INITIAL_BALANCE)
                .checkAlertMessageAndAccept(BankAlert.NO_USER_FOUND_WITH_THIS_ACCOUNT_NUMBER.getMessage());

        CreateAccountResponse actualSenderAccount =
                userSteps.getAccountByNumber(senderAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance()).isEqualTo(INITIAL_BALANCE);
    }


    // TC-TRANS-08
    // Пустая сумма
    @Test
    public void userCanNotTransferMoneyWithEmptyAmount() {
        depositMoneyToSenderAccount(INITIAL_BALANCE);

        new UserDashboard().open().transferMoneyWithEmptyAmount(senderAccount.getAccountNumber(), user.getUsername(), receiverAccount.getAccountNumber())
                .checkAlertMessageAndAccept(BankAlert.INVALID_TRANSFER_AMOUNT.getMessage());

        CreateAccountResponse actualSenderAccount =
                userSteps.getAccountByNumber(senderAccount.getAccountNumber());

        CreateAccountResponse actualReceiverAccount =
                userSteps.getAccountByNumber(receiverAccount.getAccountNumber());

        assertThat(actualSenderAccount.getBalance()).isEqualTo(INITIAL_BALANCE);

        assertThat(actualReceiverAccount.getBalance()).isZero();
    }



    private void depositMoneyToSenderAccount(double amount) {
        if (amount <= MAX_DEPOSIT_AMOUNT) {
            deposit(amount);
            return;
        }

        deposit(MAX_DEPOSIT_AMOUNT);
        deposit(amount - MAX_DEPOSIT_AMOUNT);
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