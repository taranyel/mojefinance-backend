package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.kb.openapi.model.*;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

import static cvut.fel.sit.shared.util.Constants.CLAV_TYPE_CODE;
import static cvut.fel.sit.shared.util.Constants.DEBIT_INDICATOR;

@Mapper(componentModel = "spring")
public interface KBApiMapper extends TransactionsApiMapper {
    @Mapping(target = "products", source = "accounts")
    ProductsResponse toProductsResponse(GetAccountListResponse getAccountListResponse, @Context BankDetails bankDetails);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "productIdentification.iban", source = "identification.iban")
    @Mapping(target = "productIdentification.productNumber", source = "identification.other")
    @Mapping(target = "accountName", source = "nameI18N")
    @Mapping(target = "productName", source = "productI18N")
    @Mapping(target = "manuallyCreated", constant = "false")
    @Mapping(target = "bankCode", source = "servicer.bankCode")
    @Mapping(target = "bankDetails", expression = "java(bankDetails)")
    Product toDomainProduct(Account account, @Context BankDetails bankDetails);

    TransactionsMessagingResponse toTransactionsResponse(GeAccountTransactionsResponse geAccountTransactionsResponse);

    @Mapping(target = "direction", source = "creditDebitIndicator.value", qualifiedByName = "mapTransactionDirection")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapTransactionStatus")
    @Mapping(target = "bookingDate", source = "bookingDate.date")
    @Mapping(target = "valueDate", source = "valueDate.date")
    @Mapping(target = "relatedParties.debtorName", source = "entryDetails.transactionDetails.relatedParties.debtor.name")
    @Mapping(target = "relatedParties.creditorName", source = "entryDetails.transactionDetails.relatedParties.creditor.name")
    @Mapping(target = "relatedParties.creditorAccountIban", source = "entryDetails.transactionDetails.relatedParties.creditorAccount.identification.iban")
    @Mapping(target = "relatedParties.debtorAccountIban", source = "entryDetails.transactionDetails.relatedParties.debtorAccount.identification.iban")
    Transaction toDomainTransaction(AccountTransaction accountTransaction);

    @Mapping(target = "value", source = "balances", qualifiedByName = "getAmountValue")
    @Mapping(target = "currency", source = "balances", qualifiedByName = "getCurrency")
    Amount toDomainBalance(GetAccountBalanceResponse getAccountBalanceRes);

    @Named("getAmountValue")
    default BigDecimal getAmountValue(List<AccountBalance> balances) {
        AccountBalance selectedBalance = balances.stream()
                .filter(b -> CLAV_TYPE_CODE.equals(b.getType().getCodeOrProprietary().getCode().name()))
                .findFirst()
                .orElse(balances.get(0));

        BigDecimal amount = BigDecimal.ZERO;
        if (selectedBalance.getAmount() != null && selectedBalance.getAmount().getValue() != null) {
            amount = BigDecimal.valueOf(selectedBalance.getAmount().getValue());
        }

        if (DEBIT_INDICATOR.equals(selectedBalance.getCreditDebitIndicator().name())) {
            amount = amount.negate();
        }
        return amount;
    }

    @Named("getCurrency")
    default String getCurrency(List<AccountBalance> balances) {
        AccountBalance selectedBalance = balances.stream()
                .filter(b -> CLAV_TYPE_CODE.equals(b.getType().getCodeOrProprietary().getCode().name()))
                .findFirst()
                .orElse(balances.get(0));
        return selectedBalance.getAmount().getCurrency();
    }
}
