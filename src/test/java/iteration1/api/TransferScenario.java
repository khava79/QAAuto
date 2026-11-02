package iteration1.api;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.TransferResponse;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.within;

public record TransferScenario(
        AccountSteps senderSteps,
        AccountSteps receiverSteps,
        CreateAccountResponse sender,
        CreateAccountResponse receiver,
        double deposit,
        double amount) {

    /** Два пользователя, два аккаунта, депозит на первом, сумма перевода меньше депозита */
    public static TransferScenario prepare() {
        CreateUserRequest user1 = AdminSteps.createUser();
        AccountSteps steps1 = new AccountSteps(user1.getUsername(), user1.getPassword());
        CreateAccountResponse acc1 = steps1.createAccount();

        double deposit = Math.random() * 4999.9 + 0.1;
        steps1.depositToAccount(acc1.getId(), deposit);

        CreateUserRequest user2 = AdminSteps.createUser();
        AccountSteps steps2 = new AccountSteps(user2.getUsername(), user2.getPassword());
        CreateAccountResponse acc2 = steps2.createAccount();

        double amount = Math.random() * (deposit - 0.1) + 0.1;
        return new TransferScenario(steps1, steps2, acc1, acc2, deposit, amount);
    }

    public TransferResponse transfer() {
        return senderSteps.transferWithFraudCheck(sender.getId(), receiver.getId(), amount);
    }

    public void assertNoMoneyMoved(SoftAssertions softly) {
        softly.assertThat(senderSteps.getBalance(sender.getId())).isCloseTo(deposit, within(FraudTestData.MONEY_TOLERANCE));
        softly.assertThat(receiverSteps.getBalance(receiver.getId())).isCloseTo(0.0, within(FraudTestData.MONEY_TOLERANCE));
    }

    public void assertMoneyMoved(SoftAssertions softly) {
        softly.assertThat(senderSteps.getBalance(sender.getId())).isCloseTo(deposit - amount, within(FraudTestData.MONEY_TOLERANCE));
        softly.assertThat(receiverSteps.getBalance(receiver.getId())).isCloseTo(amount, within(FraudTestData.MONEY_TOLERANCE));
    }
}