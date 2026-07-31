package generators;

import models.CreateUserRequest;

public class CreateUserRequestGenerator {
    private CreateUserRequestGenerator() {}

    public static CreateUserRequest generate() {
        return CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role("USER")
                .build();
    }
}
