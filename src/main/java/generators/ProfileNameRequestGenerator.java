package generators;

import models.ProfileNameRequest;

public class ProfileNameRequestGenerator {
    private ProfileNameRequestGenerator() {}

    public static ProfileNameRequest generate() {
        return ProfileNameRequest.builder()
                .name(RandomData.getFullName())
                .build();
    }

    public static ProfileNameRequest generate(String name) {
        return ProfileNameRequest.builder()
                .name(name)
                .build();
    }
}
