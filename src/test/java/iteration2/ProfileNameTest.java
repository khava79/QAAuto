package iteration2;

import generators.ProfileNameRequestGenerator;
import models.ProfileNameRequest;
import models.ProfileNameResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.ProfileNameRequester;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class ProfileNameTest extends BaseTest {

    @Test
    public void userCanUpdateProfileName() {

        ProfileNameRequest profileNameRequest =
                ProfileNameRequestGenerator.generate();

        ProfileNameResponse profileNameResponse =
                new ProfileNameRequester(
                        userSpec,
                        ResponseSpecs.requestReturnsOKWithMessage(
                                ResponseSpecs.PROFILE_UPDATED_SUCCESSFULLY))
                        .put(profileNameRequest)
                        .extract()
                        .as(ProfileNameResponse.class);

        softly.assertThat(profileNameRequest.getName())
                .isEqualTo(profileNameResponse.getCustomer().getName());

        softly.assertAll();
    }

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
                new ProfileNameRequester(
                        userSpec,
                        ResponseSpecs.requestReturnsOKWithMessage(
                                ResponseSpecs.PROFILE_UPDATED_SUCCESSFULLY))
                        .put(profileNameRequest)
                        .extract()
                        .as(ProfileNameResponse.class);

        softly.assertThat(profileNameRequest.getName())
                .isEqualTo(profileNameResponse.getCustomer().getName());

        softly.assertAll();
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

        new ProfileNameRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequest())
                .put(profileNameRequest);
    }
}