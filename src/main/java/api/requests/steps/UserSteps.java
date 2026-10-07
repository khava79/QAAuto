package api.requests.steps;

import api.specs.RequestSpecs;
import io.restassured.specification.RequestSpecification;
import api.models.CreateAccountResponse;
import api.models.DepositRequest;
import api.models.DepositResponse;
import api.models.ProfileNameRequest;
import api.models.ProfileNameResponse;
import api.models.TransferRequest;
import api.models.TransferResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<CreateAccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                                RequestSpecs.authAsUser(username, password),
                    ResponseSpecs.requestReturnsOK(),
                    Endpoint.CUSTOMER_ACCOUNTS).getAll(CreateAccountResponse[].class);
    }

    public CreateAccountResponse getAccountByNumber(String accountNumber) {
        return getAllAccounts()
                .stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow();
    }

    public static CreateAccountResponse createAccount(
            RequestSpecification userSpec) {

        return new ValidatedCrudRequester<CreateAccountResponse>(
                userSpec,
                ResponseSpecs.entityWasCreated(),
                Endpoint.ACCOUNTS)
                .post(null);
    }

    public static DepositResponse deposit(
            RequestSpecification userSpec,
            DepositRequest depositRequest) {

        return new ValidatedCrudRequester<DepositResponse>(
                userSpec,
                ResponseSpecs.requestReturnsOK(),
                Endpoint.DEPOSIT)
                .post(depositRequest);
    }

    public static TransferResponse transfer(
            RequestSpecification userSpec,
            TransferRequest transferRequest) {

        return new ValidatedCrudRequester<TransferResponse>(
                userSpec,
                ResponseSpecs.requestReturnsOK(),
                Endpoint.TRANSFER)
                .post(transferRequest);
    }

    public static ProfileNameResponse updateProfileName(
            RequestSpecification userSpec,
            ProfileNameRequest profileNameRequest) {

        return new ValidatedCrudRequester<ProfileNameResponse>(
                userSpec,
                ResponseSpecs.requestReturnsOKWithMessage(
                        ResponseSpecs.PROFILE_UPDATED_SUCCESSFULLY),
                Endpoint.PROFILE_NAME)
                .put(profileNameRequest);
    }
}