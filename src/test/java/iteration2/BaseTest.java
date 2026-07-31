package iteration2;

import generators.CreateUserRequestGenerator;
import io.restassured.specification.RequestSpecification;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.CreateUserResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class BaseTest {
    protected CreateUserRequest user;
    protected RequestSpecification userSpec;
    protected SoftAssertions softly;
    protected CreateUserResponse createdUser;
    protected CreateAccountResponse senderAccount;
    protected CreateAccountResponse receiverAccount;

    @BeforeEach
    public void setupTest() {
        softly = new SoftAssertions();

        user = CreateUserRequestGenerator.generate();

        createdUser = new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(user)
                .extract()
                .as(CreateUserResponse.class);

        userSpec = RequestSpecs.authAsUser(
                user.getUsername(),
                user.getPassword());


        senderAccount = new CreateAccountRequester(
                userSpec,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        receiverAccount = new CreateAccountRequester(
                userSpec,
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);


    }
}