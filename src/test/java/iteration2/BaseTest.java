package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

public class BaseTest {

    protected static final String BASE_URL = "http://localhost:4111/api/v1";

    protected static final String USER_TOKEN =
            "Basic a2F0ZTIwMjY6UGFzc3dvcmQzMyQ=";

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter()
                )
        );
    }
}