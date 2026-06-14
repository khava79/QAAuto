package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class ProfileNameTest extends BaseTest {

    private String getProfileName() {
        return given()
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .get(BASE_URL + "/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getString("name");
    }

    @Test
    public void userCanUpdateProfileName() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .body("""
                        {
                          "name": "Eddie Davidson"
                        }
                        """)
                .put(BASE_URL + "/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        String profileNameAfter = getProfileName();

        MatcherAssert.assertThat(
                profileNameAfter,
                Matchers.equalTo("Eddie Davidson")
        );
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
        String requestBody = String.format(
                """
                        {
                          "name": "%s"
                        }
                        """, name);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .body(requestBody)
                .put(BASE_URL + "/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        String profileNameAfter = getProfileName();

        MatcherAssert.assertThat(
                profileNameAfter,
                Matchers.equalTo(name)
        );
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
        String profileNameBefore = getProfileName();

        String requestBody = String.format(
                """
                        {
                          "name": "%s"
                        }
                        """, name);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", USER_TOKEN)
                .body(requestBody)
                .put(BASE_URL + "/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        String profileNameAfter = getProfileName();

        MatcherAssert.assertThat(
                profileNameAfter,
                Matchers.equalTo(profileNameBefore)
        );
    }
}
