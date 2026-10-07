package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    DEPOSIT_SUCCESSFULLY("✅ Successfully deposited $"),
    DEPOSIT_AMOUNT_MUST_BE_LESS_OR_EQUAL_5000("❌ Please deposit less or equal to 5000$."),
    INVALID_DEPOSIT_AMOUNT("❌ Please enter a valid amount."),
    PROFILE_NAME_UPDATED("✅ Name updated successfully!"),
    INVALID_PROFILE_NAME("Name must contain two words with letters only"),
    INVALID_PROFILE_NAME_EMPTY("❌ Please enter a valid name."),
    TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001("❌ Error: Transfer amount must be at least 0.01"),
    TRANSFER_AMOUNT_CANNOT_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000"),
    INSUFFICIENT_FUNDS("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    NO_USER_FOUND_WITH_THIS_ACCOUNT_NUMBER("❌ No user found with this account number."),
    INVALID_TRANSFER_AMOUNT("❌ Please fill all fields and confirm."),
    TRANSFER_SUCCESSFULLY("✅ Successfully transferred $");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}