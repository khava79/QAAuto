package api.generators;

import api.models.TransferRequest;

public class TransferRequestGenerator {
    private TransferRequestGenerator() {}

    public static TransferRequest generate(int senderId,
                                           int receiverId,
                                           double amount) {
        return TransferRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(amount)
                .build();
    }
}
