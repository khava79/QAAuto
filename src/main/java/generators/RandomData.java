package generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
        private RandomData() {}

        public static String getUsername() {
            return RandomStringUtils.randomAlphabetic(10);
        }

        public static String getPassword() {
            return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                    RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                    RandomStringUtils.randomNumeric(3) + "$" ;
        }

        public static double getBalance() {
            return ThreadLocalRandom.current().nextDouble(1, 5000);
        }

        public static String getFullName() {
            return RandomStringUtils.randomAlphabetic(6)
                    + " "
                    + RandomStringUtils.randomAlphabetic(8);
        }
    }
