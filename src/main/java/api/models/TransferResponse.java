package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponse extends BaseModel {
    private String status;
    private String message;
    private long transactionId;
    private long senderAccountId;
    private long receiverAccountId;
    private double amount;
    private double fraudRiskScore;
    private String fraudReason;
    private boolean requiresVerification;
    private boolean requiresManualReview;
}