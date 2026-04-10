package cvut.fel.sit.shared.util.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    CHECKING_ACCOUNT("Checking Account", ProductType.ASSET),
    SAVINGS_ACCOUNT("Savings Account", ProductType.ASSET),
    SHORT_TERM_DEPOSIT("Short-term Deposit", ProductType.ASSET),
    INVESTMENT("Investment", ProductType.ASSET),
    PENSION("Pension", ProductType.ASSET),

    CREDIT_CARD("Credit Card", ProductType.LIABILITY),
    LOAN("Loan", ProductType.LIABILITY),
    MORTGAGE("Mortgage", ProductType.LIABILITY),

    INSURANCE("Insurance", ProductType.NEUTRAL),
    OTHER("Other", ProductType.NEUTRAL);

    private final String displayName;
    private final ProductType productType;
}