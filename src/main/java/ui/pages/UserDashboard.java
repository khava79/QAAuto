package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {

    private SelenideElement welcomeText =
            $(Selectors.byClassName("welcome-text"));

    private SelenideElement createNewAccount =
            $(Selectors.byText("➕ Create New Account"));

    private SelenideElement depositMoney =
            $(Selectors.byText("💰 Deposit Money"));

    private SelenideElement accountSelector =
            $(".account-selector");

    private SelenideElement depositInput =
            $(".deposit-input");

    private SelenideElement depositButton =
            $(Selectors.byText("💵 Deposit"));

    private SelenideElement profileHeader =
            $(".profile-header");

    private SelenideElement profileNameInput =
            $(Selectors.byAttribute("placeholder", "Enter new name"));

    private SelenideElement saveChangesButton =
            $("button.btn.btn-primary.mt-3");

    private SelenideElement userName =
            $(".user-name");

    private SelenideElement makeTransfer =
            $(Selectors.byText("🔄 Make a Transfer"));

    private SelenideElement recipientNameInput =
            $(Selectors.byAttribute("placeholder", "Enter recipient name"));

    private SelenideElement recipientAccountInput =
            $(Selectors.byAttribute(
                    "placeholder",
                    "Enter recipient account number"
            ));

    private SelenideElement transferAmountInput =
            $(Selectors.byAttribute("placeholder", "Enter amount"));

    private SelenideElement confirmCheck =
            $("#confirmCheck");

    private SelenideElement sendTransferButton =
            $(Selectors.byText("🚀 Send Transfer"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard depositMoney(String accountNumber, double amount) {
        fillDepositForm(accountNumber);

        depositInput.setValue(String.valueOf(amount));
        depositButton.click();

        return this;
    }

    public UserDashboard depositMoneyWithEmptyAmount(String accountNumber) {
        fillDepositForm(accountNumber);

        depositInput.clear();
        depositButton.click();

        return this;
    }

    private void fillDepositForm(String accountNumber) {
        openFormAndSelectAccount(depositMoney, accountNumber);
    }

    private void selectAccount(String accountNumber) {
        accountSelector.shouldBe(Condition.visible);

        // ждём, пока нужный счёт появится в списке
        accountSelector
                .$$("option")
                .findBy(Condition.text(accountNumber))
                .shouldBe(Condition.exist);

        accountSelector.selectOptionContainingText(accountNumber);

        // убеждаемся, что выбор не сбросился после перерисовки списка
        accountSelector
                .getSelectedOption()
                .shouldHave(Condition.text(accountNumber));
    }

    public UserDashboard openProfile() {
        profileHeader
                .shouldBe(visible)
                .click();

        return this;
    }

    public UserDashboard changeProfileName(String name) {
        userName.shouldBe(visible);

        profileNameInput
                .shouldBe(visible)
                .setValue(name);
        profileNameInput.shouldHave(Condition.value(name));

        saveProfileName();
        return this;
    }

    public UserDashboard clearProfileName() {
        userName.shouldBe(visible);

        profileNameInput
                .shouldBe(visible)
                .clear();
        profileNameInput.shouldHave(Condition.exactValue(""));

        saveProfileName();
        return this;
    }

    private void saveProfileName() {
        saveChangesButton
                .shouldBe(visible)
                .click();
    }

    public UserDashboard checkProfileName(String name) {
        userName
                .shouldBe(visible)
                .shouldHave(Condition.text(name));

        return this;
    }


    public UserDashboard transferMoney(String senderAccountNumber, String recipientName,
                                       String recipientAccountNumber, double amount) {
        fillTransferForm(senderAccountNumber, recipientName, recipientAccountNumber);

        transferAmountInput.shouldBe(Condition.visible).setValue(String.valueOf(amount));
        transferAmountInput.shouldNotBe(Condition.empty);

        confirmCheck.shouldBe(Condition.visible).click();
        confirmCheck.shouldBe(Condition.checked);

        sendTransferButton.shouldBe(Condition.enabled).click();
        return this;
    }


    public UserDashboard transferMoneyWithEmptyAmount(
            String senderAccountNumber,
            String recipientName,
            String recipientAccountNumber) {

        fillTransferForm(
                senderAccountNumber,
                recipientName,
                recipientAccountNumber
        );

        confirmCheck.shouldBe(Condition.visible).click();
        confirmCheck.shouldBe(Condition.checked);

        sendTransferButton.shouldBe(Condition.enabled).click();

        return this;
    }
    private void openFormAndSelectAccount(SelenideElement menuItem, String accountNumber) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            menuItem.shouldBe(Condition.visible).click();
            accountSelector.shouldBe(Condition.visible);

            try {
                accountSelector.$$("option")
                        .findBy(Condition.text(accountNumber))
                        .shouldBe(Condition.exist, Duration.ofSeconds(4));
                break; // счёт найден
            } catch (AssertionError e) {
                if (attempt == 3) {
                    throw e;
                }
                Selenide.refresh(); // список мог не загрузиться, перезагружаем
            }
        }

        accountSelector.selectOptionContainingText(accountNumber);
        accountSelector.getSelectedOption().shouldHave(Condition.text(accountNumber));
    }


    private void fillTransferForm(
            String senderAccountNumber,
            String recipientName,
            String recipientAccountNumber) {

        openFormAndSelectAccount(makeTransfer, senderAccountNumber);

        recipientNameInput.shouldBe(Condition.visible).setValue(recipientName);
        recipientNameInput.shouldHave(Condition.value(recipientName));

        recipientAccountInput.shouldBe(Condition.visible).setValue(recipientAccountNumber);
        recipientAccountInput.shouldHave(Condition.value(recipientAccountNumber));
    }
}