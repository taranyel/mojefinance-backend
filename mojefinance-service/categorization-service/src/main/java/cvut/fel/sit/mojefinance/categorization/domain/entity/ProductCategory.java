package cvut.fel.sit.mojefinance.categorization.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    CHECKING_ACCOUNT("Checking Account"),
    SAVINGS_ACCOUNT("Savings Account"),
    SHORT_TERM_DEPOSIT("Short-term Deposit"),
    CREDIT_CARD("Credit Card"),
    LOAN("Loan"),
    MORTGAGE("Mortgage"),
    INVESTMENT("Investment"),
    PENSION("Pension"),
    INSURANCE("Insurance"),
    OTHER("Other");

    private final String displayName;
}
