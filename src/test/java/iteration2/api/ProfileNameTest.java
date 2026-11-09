package iteration2.api;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.requests.steps.DataBaseSteps;
import api.generators.ProfileNameRequestGenerator;
import api.models.ProfileNameRequest;
import api.models.ProfileNameResponse;
import api.models.comparison.ModelAssertions;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;
import common.annotations.APIVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@APIVersion("with_database_with_fix")
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
                        profileNameRequest
                );

        ModelAssertions
                .assertThatModels(profileNameRequest, profileNameResponse)
                .match();

        UserDao userAfter = DataBaseSteps.getUserByUsername(
                user.getUsername()
        );

        assertNotNull(userAfter, "Пользователь не найден в БД");

        DaoAndModelAssertions
                .assertThat(profileNameResponse, userAfter)
                .match();

        assertEquals(name, userAfter.getName());
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
        UserDao userBefore = DataBaseSteps.getUserByUsername(
                user.getUsername()
        );

        assertNotNull(userBefore, "Пользователь не найден в БД");

        ProfileNameRequest profileNameRequest =
                ProfileNameRequestGenerator.generate(name);

        new CrudRequester(
                userSpec,
                ResponseSpecs.requestReturnsBadRequest(),
                Endpoint.PROFILE_NAME
        ).put(profileNameRequest);

        UserDao userAfter = DataBaseSteps.getUserByUsername(
                user.getUsername()
        );

        assertNotNull(userAfter, "Пользователь не найден в БД");

        assertEquals(userBefore, userAfter);
    }
}
