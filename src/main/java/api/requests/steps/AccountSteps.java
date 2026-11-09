package api.requests.steps;

import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.helpers.StepLogger;

public class AccountSteps {
    private String username;
    private String password;

    public AccountSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public CreateAccountResponse createAccount() {
        return StepLogger.log("User " + username + " creates account", () -> {
            return new ValidatedCrudRequester<CreateAccountResponse>(
                                RequestSpecs.authAsUser(username, password),
                    ResponseSpecs.entityWasCreated(),
                    Endpoint.ACCOUNTS).post(null);
        });
    }

    public DepositResponse depositToAccount(long accountId, double amount) {
        return StepLogger.log("User " + username + " deposits " + amount + " to account " + accountId, () -> {
            DepositRequest depositRequest = DepositRequest.builder()
                    .accountId(accountId)
                    .amount(amount)
                    .description("Test deposit")
                    .build();

            return new ValidatedCrudRequester<DepositResponse>(
                                RequestSpecs.authAsUser(username, password),
                    ResponseSpecs.requestReturnsOK(),
                    Endpoint.DEPOSIT).post(depositRequest);
        });
    }

    public TransferResponse transferWithFraudCheck(long senderAccountId, long receiverAccountId, double amount) {
        return StepLogger.log("User " + username + " transfers " + amount + " to " + receiverAccountId + " with fraud check", () -> {
            TransferRequest transferRequest = TransferRequest.builder()
                    .senderAccountId(senderAccountId)
                    .receiverAccountId(receiverAccountId)
                    .amount(amount)
                    .description("Test transfer with fraud check")
                    .build();

            return new ValidatedCrudRequester<TransferResponse>(
                                RequestSpecs.authAsUser(username, password),
                    ResponseSpecs.requestReturnsOK(),
                    Endpoint.TRANSFER_WITH_FRAUD_CHECK).post(transferRequest);
        });
    }

    public double getBalance(long accountId) {
        return StepLogger.log("User " + username + " gets balance of account " + accountId, () ->
                new CrudRequester(
                        RequestSpecs.authAsUser(username, password),
                        ResponseSpecs.requestReturnsOK(),
                        Endpoint.CUSTOMER_ACCOUNTS)
                        .getAll(CreateAccountResponse.class)
                        .extract().jsonPath()
                        .getDouble("find { it.id == " + accountId + " }.balance"));
    }
}