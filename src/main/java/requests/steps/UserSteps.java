package requests.steps;

import io.restassured.specification.RequestSpecification;
import models.CreateAccountResponse;
import models.DepositRequest;
import models.DepositResponse;
import models.ProfileNameRequest;
import models.ProfileNameResponse;
import models.TransferRequest;
import models.TransferResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.ResponseSpecs;

public class UserSteps {

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