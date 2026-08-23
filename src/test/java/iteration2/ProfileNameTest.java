package iteration2;

import generators.ProfileNameRequestGenerator;
import models.ProfileNameRequest;
import models.ProfileNameResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.UserSteps;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class ProfileNameTest extends BaseTest {

    public static Stream<Arguments> profileNameValidData() {
        return Stream.of(
                Arguments.of("John Smith"),
                Arguments.of("Kate Davidson"),
                Arguments.of("Robert Johnson")
        );
    }

    @MethodSource("profileNameValidData")
    @ParameterizedTest
    public void userCanUpdateProfileNameWithValidData(String name) {

        ProfileNameRequest profileNameRequest =
                ProfileNameRequestGenerator.generate(name);

        ProfileNameResponse profileNameResponse =
                UserSteps.updateProfileName(
                        userSpec,
                        profileNameRequest);

        ModelAssertions
                .assertThatModels(profileNameRequest, profileNameResponse)
                .match();
    }

    public static Stream<Arguments> profileNameInvalidData() {
        return Stream.of(
                Arguments.of("Eddie"),
                Arguments.of("Eddie786 Davidson"),
                Arguments.of("Eddie @Davidson"),
                Arguments.of("")
        );
    }

    @MethodSource("profileNameInvalidData")
    @ParameterizedTest
    public void userCanNotUpdateProfileNameWithInvalidData(String name) {

        ProfileNameRequest profileNameRequest =
                ProfileNameRequestGenerator.generate(name);

        new CrudRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequest(),
                Endpoint.PROFILE_NAME)
                .put(profileNameRequest);
    }
}