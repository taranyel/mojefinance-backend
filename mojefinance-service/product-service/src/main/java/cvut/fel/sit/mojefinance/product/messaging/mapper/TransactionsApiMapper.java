package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.airbank.openapi.model.TransactionList;
import cvut.fel.sit.airbank.openapi.model.TransactionListTransactionsInner;
import cvut.fel.sit.cs.openapi.model.AccountTransaction;
import cvut.fel.sit.cs.openapi.model.MyAccountsIdTransactionsGet200Response;
import cvut.fel.sit.csob.transactions.openapi.model.GetTransactionHistoryRes;
import cvut.fel.sit.csob.transactions.openapi.model.TransactionInfo;
import cvut.fel.sit.kb.openapi.model.GeAccountTransactionsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionDirection;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionStatus;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.reif.openapi.model.GetTransactionList200Response;
import cvut.fel.sit.reif.openapi.model.GetTransactionList200ResponseTransactionsInner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface TransactionsApiMapper {

    TransactionsMessagingResponse toTransactionsResponse(TransactionList transactionList);

    TransactionsMessagingResponse toTransactionsResponse(MyAccountsIdTransactionsGet200Response myAccountsIdTransactionsGet200Response);

    TransactionsMessagingResponse toTransactionsResponse(GetTransactionHistoryRes getTransactionHistoryRes);

    TransactionsMessagingResponse toTransactionsResponse(GeAccountTransactionsResponse geAccountTransactionsResponse);

    TransactionsMessagingResponse toTransactionsResponse(GetTransactionList200Response getTransactionList200Response);

    @Mapping(target = "direction", source = "creditDebitIndicator.value", qualifiedByName = "mapTransactionDirection")
    @Mapping(target = "status", source = "status.value", qualifiedByName = "mapTransactionStatus")
    @Mapping(target = "bookingDate", source = "bookingDate.date")
    @Mapping(target = "valueDate", source = "valueDate.date")
    @Mapping(target = "relatedParties.debtorName", source = "entryDetails.transactionDetails.relatedParties.debtor.name")
    @Mapping(target = "relatedParties.creditorName", source = "entryDetails.transactionDetails.relatedParties.creditor.name")
    @Mapping(target = "relatedParties.creditorAccountIban", source = "entryDetails.transactionDetails.relatedParties.creditorAccount.identification.iban")
    @Mapping(target = "relatedParties.debtorAccountIban", source = "entryDetails.transactionDetails.relatedParties.debtorAccount.identification.iban")
    Transaction toDomainTransaction(TransactionListTransactionsInner transactionListTransactionsInner);

    @Mapping(target = "direction", source = "creditDebitIndicator.value", qualifiedByName = "mapTransactionDirection")
    @Mapping(target = "status", source = "status.value", qualifiedByName = "mapTransactionStatus")
    @Mapping(target = "bookingDate", source = "bookingDate.date")
    @Mapping(target = "valueDate", source = "valueDate.date")
    @Mapping(target = "relatedParties.debtorName", source = "entryDetails.transactionDetails.relatedParties.debtor.name")
    @Mapping(target = "relatedParties.creditorName", source = "entryDetails.transactionDetails.relatedParties.creditor.name")
    @Mapping(target = "relatedParties.creditorAccountIban", source = "entryDetails.transactionDetails.relatedParties.creditorAccount.identification.iban")
    @Mapping(target = "relatedParties.debtorAccountIban", source = "entryDetails.transactionDetails.relatedParties.debtorAccount.identification.iban")
    Transaction toDomainTransaction(AccountTransaction accountTransaction);

    @Mapping(target = "direction", source = "creditDebitIndicator.value", qualifiedByName = "mapTransactionDirection")
    @Mapping(target = "status", source = "status.value", qualifiedByName = "mapTransactionStatus")
    @Mapping(target = "bookingDate", source = "bookingDate.date")
    @Mapping(target = "valueDate", source = "valueDate.date")
    @Mapping(target = "relatedParties.debtorName", source = "entryDetails.transactionDetails.relatedParties.debtor.name")
    @Mapping(target = "relatedParties.creditorName", source = "entryDetails.transactionDetails.relatedParties.creditor.name")
    @Mapping(target = "relatedParties.creditorAccountIban", source = "entryDetails.transactionDetails.relatedParties.creditorAccount.identification.iban")
    @Mapping(target = "relatedParties.debtorAccountIban", source = "entryDetails.transactionDetails.relatedParties.debtorAccount.identification.iban")
    Transaction toDomainTransaction(TransactionInfo transactionInfo);

    @Mapping(target = "direction", source = "creditDebitIndicator.value", qualifiedByName = "mapTransactionDirection")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapTransactionStatus")
    @Mapping(target = "bookingDate", source = "bookingDate.date")
    @Mapping(target = "valueDate", source = "valueDate.date")
    @Mapping(target = "relatedParties.debtorName", source = "entryDetails.transactionDetails.relatedParties.debtor.name")
    @Mapping(target = "relatedParties.creditorName", source = "entryDetails.transactionDetails.relatedParties.creditor.name")
    @Mapping(target = "relatedParties.creditorAccountIban", source = "entryDetails.transactionDetails.relatedParties.creditorAccount.identification.iban")
    @Mapping(target = "relatedParties.debtorAccountIban", source = "entryDetails.transactionDetails.relatedParties.debtorAccount.identification.iban")
    Transaction toDomainTransaction(cvut.fel.sit.kb.openapi.model.AccountTransaction accountTransaction);

    @Mapping(target = "direction", source = "creditDebitIndication.value", qualifiedByName = "mapTransactionDirection")
    @Mapping(target = "status", constant = "BOOKED")
    @Mapping(target = "bookingDate", source = "bookingDate", qualifiedByName = "mapDateFromOffsetDateTime")
    @Mapping(target = "valueDate", source = "valueDate", qualifiedByName = "mapDateFromOffsetDateTime")
    @Mapping(target = "counterpartyName", source = "entryDetails.transactionDetails.relatedParties.counterParty.name")
    @Mapping(target = "relatedParties.creditorAccountIban", source = "entryDetails.transactionDetails.relatedParties.counterParty.account.iban")
    @Mapping(target = "relatedParties.debtorAccountIban", source = "entryDetails.transactionDetails.relatedParties.counterParty.account.iban")
    Transaction toDomainTransaction(GetTransactionList200ResponseTransactionsInner getTransactionList200ResponseTransactionsInner);

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
