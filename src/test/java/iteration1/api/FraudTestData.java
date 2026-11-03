package iteration1.api;

public final class FraudTestData {

    private FraudTestData() {
    }

    // решения, которые мы передаём в @FraudCheckMock(decision = ...)
    public static final String DECISION_APPROVED = "APPROVED";
    public static final String DECISION_BLOCKED = "BLOCKED";
    public static final String DECISION_REVIEW_REQUIRED = "REVIEW_REQUIRED";
    public static final String DECISION_VERIFICATION_REQUIRED = "VERIFICATION_REQUIRED";

    // статусы, которые возвращает TransferService
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_BLOCKED = "BLOCKED";
    public static final String STATUS_MANUAL_REVIEW = "MANUAL_REVIEW_REQUIRED";
    public static final String STATUS_VERIFICATION = "VERIFICATION_REQUIRED";

    // сообщения сервиса
    public static final String MSG_APPROVED = "Transfer approved and processed immediately";
    public static final String MSG_BLOCKED = "Transfer blocked due to fraud detection";
    public static final String MSG_MANUAL_REVIEW = "Transfer requires manual review";
    public static final String MSG_VERIFICATION = "Additional verification required";

    // риск, который сервис ставит сам при сбое fraud-сервиса
    public static final double FALLBACK_RISK_SCORE = 0.5;

    // допуск при сравнении денег (баланс приходит округлённым до копеек)
    public static final double MONEY_TOLERANCE = 0.01;
}