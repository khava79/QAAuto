package iteration2;

import models.ProfileNameRequest;
import models.ProfileNameResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.ProfileNameRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class ProfileNameTest extends BaseTest {

    @Test
    public void userCanUpdateProfileName() {

        ProfileNameRequest profileNameRequest = ProfileNameRequest.builder()
                .name("Eddie Davidson")
                .build();

        ProfileNameResponse profileNameResponse =
                new ProfileNameRequester(
                        RequestSpecs.authAsUser("kate2026", "Password33$"),
                        ResponseSpecs.requestReturnsOK())
                        .put(profileNameRequest)
                        .extract()
                        .as(ProfileNameResponse.class);

        softly.assertThat(profileNameRequest.getName())
                .isEqualTo(profileNameResponse.getCustomer().getName());

        softly.assertThat(profileNameResponse.getMessage())
                .isEqualTo("Profile updated successfully");
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

        ProfileNameRequest profileNameRequest = ProfileNameRequest.builder()
                .name(name)
                .build();

        ProfileNameResponse profileNameResponse =
                new ProfileNameRequester(
                        RequestSpecs.authAsUser("kate2026", "Password33$"),
                        ResponseSpecs.requestReturnsOK())
                        .put(profileNameRequest)
                        .extract()
                        .as(ProfileNameResponse.class);

        softly.assertThat(profileNameRequest.getName())
                .isEqualTo(profileNameResponse.getCustomer().getName());

        softly.assertThat(profileNameResponse.getMessage())
                .isEqualTo("Profile updated successfully");
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

        ProfileNameRequest profileNameRequest = ProfileNameRequest.builder()
                .name(name)
                .build();

        new ProfileNameRequester(
                RequestSpecs.authAsUser("kate2026", "Password33$"),
                ResponseSpecs.requestReturnsBadRequest())
                .put(profileNameRequest);
    }
}