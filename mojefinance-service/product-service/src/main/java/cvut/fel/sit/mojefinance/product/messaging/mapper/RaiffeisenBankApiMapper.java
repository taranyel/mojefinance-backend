package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.reif.openapi.model.*;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

import static cvut.fel.sit.shared.util.Constants.CLAV_TYPE_CODE;
import static cvut.fel.sit.shared.util.Constants.CZK_CURRENCY_CODE;

@Mapper(componentModel = "spring")
public interface RaiffeisenBankApiMapper extends TransactionsApiMapper {
    @Mapping(target = "products", source = "accounts")
    ProductsResponse toProductsResponse(GetAccounts200Response getAccounts200Response, @Context BankDetails bankDetails);

    @Mapping(target = "productId", source = "accountId")
    @Mapping(target = "productIdentification.iban", source = "iban")
    @Mapping(target = "productIdentification.productNumber", source = "accountNumber")
    @Mapping(target = "productName", source = "friendlyName")
    @Mapping(target = "currency", source = "mainCurrency")
    @Mapping(target = "manuallyCreated", constant = "false")
    @Mapping(target = "bankDetails", expression = "java(bankDetails)")
    Product toDomainProduct(GetAccounts200ResponseAccountsInner account, @Context BankDetails bankDetails);

    TransactionsMessagingResponse toTransactionsResponse(GetTransactionList200Response getTransactionList200Response);

    @Mapping(target = "direction", source = "creditDebitIndication.value", qualifiedByName = "mapTransactionDirection")
    @Mapping(target = "status", constant = "BOOKED")
    @Mapping(target = "bookingDate", source = "bookingDate", qualifiedByName = "mapDateFromOffsetDateTime")
    @Mapping(target = "valueDate", source = "valueDate", qualifiedByName = "mapDateFromOffsetDateTime")
    @Mapping(target = "counterpartyName", source = "entryDetails.transactionDetails.relatedParties.counterParty.name")
    @Mapping(target = "relatedParties.creditorAccountIban", source = "entryDetails.transactionDetails.relatedParties.counterParty.account.iban")
    @Mapping(target = "relatedParties.debtorAccountIban", source = "entryDetails.transactionDetails.relatedParties.counterParty.account.iban")
    Transaction toDomainTransaction(GetTransactionList200ResponseTransactionsInner getTransactionList200ResponseTransactionsInner);

    @Mapping(target = "value", source = "currencyFolders", qualifiedByName = "getAmountValue")
    @Mapping(target = "currency", source = "currencyFolders", qualifiedByName = "getCurrency")
    Amount toDomainBalance(GetBalance200Response getAccountBalanceRes);

    @Named("getAmountValue")
    default BigDecimal getAmountValue(List<GetBalance200ResponseCurrencyFoldersInner> foldersInners) {
        GetBalance200ResponseCurrencyFoldersInnerBalancesInner selectedBalance = getSelectedBalance(foldersInners);

        BigDecimal amount = BigDecimal.ZERO;
        if (selectedBalance.getValue() != null) {
            amount = BigDecimal.valueOf(selectedBalance.getValue());
        }
        return amount;
    }

    @Named("getCurrency")
    default String getCurrency(List<GetBalance200ResponseCurrencyFoldersInner> foldersInners) {
        GetBalance200ResponseCurrencyFoldersInnerBalancesInner selectedBalance = getSelectedBalance(foldersInners);
        return selectedBalance.getCurrency();
    }

    private static GetBalance200ResponseCurrencyFoldersInnerBalancesInner getSelectedBalance(List<GetBalance200ResponseCurrencyFoldersInner> foldersInners) {
        GetBalance200ResponseCurrencyFoldersInner currencyFoldersInner = foldersInners.stream()
                .filter(folder -> CZK_CURRENCY_CODE.equals(folder.getCurrency()))
                .findFirst()
                .orElse(null);

        assert currencyFoldersInner != null;
        return currencyFoldersInner.getBalances().stream()
                .filter(b -> CLAV_TYPE_CODE.equals(b.getBalanceType()))
                .findFirst()
                .orElse(currencyFoldersInner.getBalances().get(0));
    }
}
