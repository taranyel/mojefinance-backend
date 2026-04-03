package cvut.fel.sit.mojefinance.categorization.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionCategory {
    GROCERIES("Groceries"),
    CAFE_AND_RESTAURANT("Cafe and Restaurant"),
    UTILITIES("Utilities"),
    TRANSPORTATION("Transportation"),

    MEDICAL_CARE("Medical Care"),
    PHARMACY("Pharmacy"),
    HEALTH_AND_BEAUTY("Health and Beauty"),
    SPORTS_AND_FITNESS("Sports and Fitness"),

    SHOPPING("Shopping"),
    ELECTRONICS("Electronics"),
    ENTERTAINMENT("Entertainment"),
    EDUCATION("Education"),

    FEES_AND_CHARGES("Fees and Charges"),
    OTHER("Other");

    private final String displayName;
}
