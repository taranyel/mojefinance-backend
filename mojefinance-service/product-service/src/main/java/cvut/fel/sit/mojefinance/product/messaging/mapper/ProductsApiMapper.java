package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.airbank.openapi.model.AccountList;
import cvut.fel.sit.airbank.openapi.model.AccountListAccountsInner;
import cvut.fel.sit.cs.openapi.model.AccountDetail;
import cvut.fel.sit.cs.openapi.model.MyAccountsGet200Response;
import cvut.fel.sit.csob.accounts.openapi.model.AccountInfo;import cvut.fel.sit.csob.accounts.openapi.model.GetAccountsRes;import cvut.fel.sit.kb.openapi.model.Account;
import cvut.fel.sit.kb.openapi.model.GetAccountListResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.reif.openapi.model.GetAccounts200Response;
import cvut.fel.sit.reif.openapi.model.GetAccounts200ResponseAccountsInner;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductsApiMapper {

    @Mapping(target = "products", source = "accounts")
    ProductsDomainResponse toProductsResponse(AccountList accountList, @Context BankDetails bankDetails);

    @Mapping(target = "products", source = "accounts")
    ProductsDomainResponse toProductsResponse(MyAccountsGet200Response myAccountsGet200Response, @Context BankDetails bankDetails);

    @Mapping(target = "products", source = "accounts")
    ProductsDomainResponse toProductsResponse(GetAccountsRes getAccountsRes, @Context BankDetails bankDetails);

    @Mapping(target = "products", source = "accounts")
    ProductsDomainResponse toProductsResponse(GetAccountListResponse getAccountListResponse, @Context BankDetails bankDetails);

    @Mapping(target = "products", source = "accounts")
    ProductsDomainResponse toProductsResponse(GetAccounts200Response getAccounts200Response, @Context BankDetails bankDetails);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "productIdentification.iban", source = "identification.iban")
    @Mapping(target = "productIdentification.productNumber", source = "identification.other")
    @Mapping(target = "accountName", source = "nameI18N")
    @Mapping(target = "productCategory", source = "productI18N")
    @Mapping(target = "manuallyCreated", constant = "false")
    @Mapping(target = "bankCode", source = "servicer.bankCode")
    @Mapping(target = "bankDetails", expression = "java(bankDetails)")
    Product toDomainProduct(AccountListAccountsInner account, @Context BankDetails bankDetails);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "productIdentification.iban", source = "identification.iban")
    @Mapping(target = "productIdentification.productNumber", source = "identification.other")
    @Mapping(target = "accountName", source = "nameI18N")
    @Mapping(target = "productCategory", source = "productI18N")
    @Mapping(target = "manuallyCreated", constant = "false")
    @Mapping(target = "bankCode", source = "servicer.bankCode")
    @Mapping(target = "bankDetails", expression = "java(bankDetails)")
    Product toDomainProduct(AccountDetail accountDetail, @Context BankDetails bankDetails);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "productIdentification.iban", source = "identification.iban")
    @Mapping(target = "productIdentification.productNumber", source = "identification.other")
    @Mapping(target = "accountName", source = "nameI18N")
    @Mapping(target = "productCategory", source = "productI18N")
    @Mapping(target = "manuallyCreated", constant = "false")
    @Mapping(target = "bankCode", source = "servicer.bankCode")
    @Mapping(target = "bankDetails", expression = "java(bankDetails)")
    Product toDomainProduct(Account account, @Context BankDetails bankDetails);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "productIdentification.iban", source = "identification.iban")
    @Mapping(target = "accountName", source = "nameI18N")
    @Mapping(target = "productCategory", source = "productI18N")
    @Mapping(target = "manuallyCreated", constant = "false")
    @Mapping(target = "bankCode", source = "servicer.bankCode")
    @Mapping(target = "bankDetails", expression = "java(bankDetails)")
    Product toDomainProduct(AccountInfo accountInfo, @Context BankDetails bankDetails);

    @Mapping(target = "productId", source = "accountId")
    @Mapping(target = "productIdentification.iban", source = "iban")
    @Mapping(target = "productIdentification.productNumber", source = "accountNumber")
    @Mapping(target = "productCategory", source = "friendlyName")
    @Mapping(target = "currency", source = "mainCurrency")
    @Mapping(target = "manuallyCreated", constant = "false")
    @Mapping(target = "bankDetails", expression = "java(bankDetails)")
    Product toDomainProduct(GetAccounts200ResponseAccountsInner account, @Context BankDetails bankDetails);
}