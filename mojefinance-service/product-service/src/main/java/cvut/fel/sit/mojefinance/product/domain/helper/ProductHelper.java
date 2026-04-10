package cvut.fel.sit.mojefinance.product.domain.helper;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.GroupedProducts;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.shared.util.entity.ProductType;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static cvut.fel.sit.shared.util.Constants.CZK_CURRENCY_CODE;

@Component
public class ProductHelper {

    public AccountBalancesMessagingRequest buildAccountBalancesMessagingRequest(String productId, BankDetails bankDetails, String authorization, String principalName) {
        return AccountBalancesMessagingRequest.builder()
                .accountId(productId)
                .bankDetails(bankDetails)
                .authorization(authorization)
                .principalName(principalName)
                .build();
    }

    public BankDetails mapBankDetails(BankConnection realBankConnection) {
        return BankDetails.builder()
                .bankName(realBankConnection.getBankName())
                .clientRegistrationId(realBankConnection.getClientRegistrationId())
                .build();
    }

    public List<BankConnection> filterRealBanks(List<BankConnection> activeBankConnections) {
        return activeBankConnections.stream()
                .filter(bank -> bank.getManuallyCreated() == false)
                .toList();
    }

    public List<BankConnection> filterBanksWithActiveConnection(ConnectedBanksDomainResponse connectedBanks) {
        return connectedBanks.getConnectedBanks().stream()
                .filter(connectedBank -> BankConnectionStatus.CONNECTED.equals(connectedBank.getBankConnectionStatus()))
                .toList();
    }

    public ProductsMessagingRequest buildGetProductsMessagingRequest(BankDetails bankDetails, String authorization, String principalName) {
        return ProductsMessagingRequest.builder()
                .authorization(authorization)
                .bankDetails(bankDetails)
                .principalName(principalName)
                .build();
    }

    public List<GroupedProducts> groupProductsByProductType(List<Product> products, ProductType productType) {
        return products.stream()
                .filter(product -> product.getProductCategory().getProductType() == productType)
                .collect(Collectors.groupingBy(Product::getProductCategory))
                .entrySet().stream()
                .map(entry -> GroupedProducts.builder()
                        .groupName(entry.getKey().getDisplayName())
                        .products(entry.getValue())
                        .totalAmount(Amount.builder()
                                .value(getTotalAmount(entry.getValue()))
                                .currency(CZK_CURRENCY_CODE)
                                .build())
                        .build())
                .sorted(Comparator.comparing(groupedProducts -> groupedProducts.getTotalAmount().getValue(), Comparator.reverseOrder()))
                .toList();
    }

    private BigDecimal getTotalAmount(List<Product> products) {
        return products.stream()
                .map(Product::getBalance)
                .map(Amount::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
