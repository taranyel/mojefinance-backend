package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankDomainEntity;
import cvut.fel.sit.mojefinance.bank.domain.service.BankService;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountInfoRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.*;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ExternalApiProvider externalApiProvider;
    private final AuthorizationService authorizationService;
    private final BankService bankService;
    private final TransactionGroupingService transactionGroupingService;

    @Override
    public ProductsDomainResponse getProducts() {
        log.info("Getting products for authorized user.");
        List<Product> products = new ArrayList<>();

        ConnectedBanksDomainResponse connectedBanks = bankService.getConnectedBanks();
        List<BankDomainEntity> activeBanks = getBanksWithActiveConnection(connectedBanks);
        List<BankDomainEntity> realBanks = getRealBanks(activeBanks);
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();

        for (BankDomainEntity realBank : realBanks) {
            String clientRegistrationId = realBank.getClientRegistrationId();
            String authorization = "Bearer " + authorizationService.authorizeClient(clientRegistrationId);
            BankDetails bankDetails = getBankDetails(realBank);

            List<Product> retrievedProducts = getProducts(bankDetails, authorization, principal.getName());
            setAccountBalance(retrievedProducts, bankDetails, authorization, principal.getName());

            products.addAll(retrievedProducts);
        }

        log.info("Retrieved {} products for authorized user.", products.size());
        return ProductsDomainResponse.builder()
                .products(products)
                .build();
    }

    @Override
    public TransactionsDomainResponse getTransactions(AccountInfoRequest request) {
        log.info("Getting transactions for authorized user.");
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        request.setPrincipalName(principal.getName());

        String clientRegistrationId = request.getBankDetails().getClientRegistrationId();
        String authorization = "Bearer " + authorizationService.authorizeClient(clientRegistrationId);
        request.setAuthorization(authorization);

        TransactionsMessagingResponse messagingResponse = externalApiProvider.getTransactions(request);
        List<Transaction> transactions = messagingResponse.getTransactions();
        enrichTransactionsInfo(transactions);

        log.info("Retrieved: {} transactions for client registration id: {} for account id: {}", transactions.size(), clientRegistrationId, request.getAccountId());
        return transactionGroupingService.groupTransactions(transactions);
    }

    private void enrichTransactionsInfo(List<Transaction> transactions) {
        transactions.forEach(this::enrichSingleTransaction);
    }

    private void enrichSingleTransaction(Transaction transaction) {
        RelatedParties parties = transaction.getRelatedParties();
        if (parties == null || transaction.getDirection() == null) {
            return;
        }
        if (TransactionDirection.OUTCOME.equals(transaction.getDirection())) {
            String name = parties.getCreditorName() != null ? parties.getCreditorName() : parties.getCreditorAccountIban();
            transaction.setCounterpartyName(name);
            transaction.getAmount().setValue(transaction.getAmount().getValue().negate());

        } else if (TransactionDirection.INCOME.equals(transaction.getDirection())) {
            String name = parties.getDebtorName() != null ? parties.getDebtorName() : parties.getDebtorAccountIban();
            transaction.setCounterpartyName(name);
        }
    }

    private List<Product> getProducts(BankDetails bankDetails, String authorization, String principalName) {
        ProductsMessagingRequest productsMessagingRequest = getGetProductsMessagingRequest(bankDetails, authorization, principalName);
        ProductsDomainResponse messagingResponse = externalApiProvider.getProducts(productsMessagingRequest);
        return messagingResponse.getProducts();
    }

    private void setAccountBalance(List<Product> retrievedProducts, BankDetails bankDetails, String authorization, String principalName) {
        for (Product product : retrievedProducts) {
            String productId = product.getProductId();
            log.info("Retrieving account balance for account id: {} client registration id: {}", productId, bankDetails.getClientRegistrationId());
            AccountInfoRequest accountInfoRequest = getAccountBalancesMessagingRequest(productId, bankDetails, authorization, principalName);
            Amount balance = externalApiProvider.getAccountBalance(accountInfoRequest);
            product.setBalance(balance);
        }
    }

    private AccountInfoRequest getAccountBalancesMessagingRequest(String productId, BankDetails bankDetails, String authorization, String principalName) {
        return AccountInfoRequest.builder()
                .accountId(productId)
                .bankDetails(bankDetails)
                .authorization(authorization)
                .principalName(principalName)
                .build();
    }

    private BankDetails getBankDetails(BankDomainEntity realBank) {
        return BankDetails.builder()
                .bankName(realBank.getBankName())
                .clientRegistrationId(realBank.getClientRegistrationId())
                .build();
    }

    private List<BankDomainEntity> getRealBanks(List<BankDomainEntity> activeBanks) {
        return activeBanks.stream()
                .filter(bank -> bank.getManuallyCreated() == false)
                .toList();
    }

    private List<BankDomainEntity> getBanksWithActiveConnection(ConnectedBanksDomainResponse connectedBanks) {
        return connectedBanks.getConnectedBanks().stream()
                .filter(connectedBank -> BankConnectionStatus.CONNECTED.equals(connectedBank.getBankConnectionStatus()))
                .toList();
    }

    private ProductsMessagingRequest getGetProductsMessagingRequest(BankDetails bankDetails, String authorization, String principalName) {
        return ProductsMessagingRequest.builder()
                .authorization(authorization)
                .bankDetails(bankDetails)
                .principalName(principalName)
                .build();
    }
}
