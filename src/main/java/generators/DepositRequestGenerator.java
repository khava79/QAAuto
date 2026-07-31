package generators;

import models.DepositRequest;

public class DepositRequestGenerator {
    private DepositRequestGenerator() {}

    public static DepositRequest generate(int accountId) {
        return DepositRequest.builder()
                .id(accountId)
                .balance(RandomData.getBalance())
                .build();
    }

    public static DepositRequest generate(int accountId, double balance) {
        return DepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();
    }
}
