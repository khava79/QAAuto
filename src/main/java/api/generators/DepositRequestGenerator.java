package api.generators;

import api.models.DepositRequest;

public class DepositRequestGenerator {
    private DepositRequestGenerator() {
    }

    public static DepositRequest generate(int accountId) {
        return DepositRequest.builder()
                .id(accountId)
                .balance(roundToCents(RandomData.getBalance()))
                .build();
    }

    public static DepositRequest generate(int accountId, double balance) {
        return DepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();
    }

    private static double roundToCents(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}