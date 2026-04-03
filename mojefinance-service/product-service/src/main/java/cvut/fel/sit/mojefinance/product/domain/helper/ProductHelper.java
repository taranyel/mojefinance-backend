package cvut.fel.sit.mojefinance.product.domain.helper;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountInfoRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductHelper {

    public AccountInfoRequest buildAccountBalancesMessagingRequest(String productId, BankDetails bankDetails, String authorization, String principalName) {
        return AccountInfoRequest.builder()
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
        return connectedBanks.getConnectedBankConnections().stream()
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
}
