package iteration2.api;

import api.dao.AccountDao;
import api.dao.UserDao;
import api.requests.steps.DataBaseSteps;
import common.extensions.APIVersionExtension;
import common.extensions.TimingExtension;
import io.restassured.specification.RequestSpecification;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({TimingExtension.class, APIVersionExtension.class})
public class BaseTest {
    protected static final double DELTA = 0.001;

    protected CreateUserRequest user;
    protected RequestSpecification userSpec;
    protected SoftAssertions softly;
    protected CreateAccountResponse senderAccount;
    protected CreateAccountResponse receiverAccount;
    protected UserSteps userSteps;

    @BeforeEach
    public void setupTest() {

        softly = new SoftAssertions();

        user = AdminSteps.createUser();

        userSpec = RequestSpecs.authAsUser(
                user.getUsername(),
                user.getPassword());

        userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword());

        senderAccount = UserSteps.createAccount(userSpec);

        receiverAccount = UserSteps.createAccount(userSpec);

    }

}
