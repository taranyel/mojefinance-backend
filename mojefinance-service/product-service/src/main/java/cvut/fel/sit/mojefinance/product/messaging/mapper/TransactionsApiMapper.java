package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.mojefinance.product.domain.entity.TransactionDirection;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface TransactionsApiMapper {

    @Named("mapTransactionDirection")
    default TransactionDirection mapTransactionDirection(String creditDebitIndicator) {
        if (creditDebitIndicator == null) {
            return null;
        }
        return switch (creditDebitIndicator.toUpperCase()) {
            case "CRDT" -> TransactionDirection.INCOME;
            case "DBIT" -> TransactionDirection.OUTCOME;
            default ->
                    throw new IllegalArgumentException("Unknown credit/debit indicator from bank: " + creditDebitIndicator);
        };
    }

    @Named("mapTransactionStatus")
    default TransactionStatus mapTransactionStatus(String transactionStatus) {
        if (transactionStatus == null) {
            return null;
        }
        return switch (transactionStatus.toUpperCase()) {
            case "BOOK" -> TransactionStatus.BOOKED;
            case "PDNG" -> TransactionStatus.PENDING;
            default ->
                    throw new IllegalArgumentException("Unknown credit/debit indicator from bank: " + transactionStatus);
        };
    }

    @Named("mapDateFromOffsetDateTime")
    default LocalDate mapDateFromOffsetDateTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return LocalDate.from(offsetDateTime);
    }
}
