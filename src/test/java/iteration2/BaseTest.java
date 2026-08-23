package iteration2;

import io.restassured.specification.RequestSpecification;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;

public class BaseTest {

    protected CreateUserRequest user;
    protected RequestSpecification userSpec;
    protected SoftAssertions softly;
    protected CreateAccountResponse senderAccount;
    protected CreateAccountResponse receiverAccount;

    @BeforeEach
    public void setupTest() {

        softly = new SoftAssertions();

        user = AdminSteps.createUser();

        userSpec = RequestSpecs.authAsUser(
                user.getUsername(),
                user.getPassword());

        senderAccount = UserSteps.createAccount(userSpec);

        receiverAccount = UserSteps.createAccount(userSpec);
    }
}