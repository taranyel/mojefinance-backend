package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.RaiffeisenBankApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.RaiffeisenBankApiMapper;
import cvut.fel.sit.reif.openapi.model.GetAccounts200Response;
import cvut.fel.sit.reif.openapi.model.GetBalance200Response;
import cvut.fel.sit.reif.openapi.model.GetTransactionList200Response;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static cvut.fel.sit.shared.util.Constants.CZK_CURRENCY_CODE;

@Service
@Slf4j
@RequiredArgsConstructor
public class RaiffeisenBankApiProvider {
    private final RaiffeisenBankApiFeignClient raiffeisenBankApiFeignClient;
    private final RaiffeisenBankApiMapper raiffeisenBankApiMapper;

    @Value("${external.api.raiffeisen-bank.x-ibm-client-id}")
    private String raiffeisenBankXIbmClientId;

    public ProductsResponse fetchProducts(ProductsMessagingRequest productsMessagingRequest) {
        BankDetails bankDetails = productsMessagingRequest.getBankDetails();
        String requestId = UUID.randomUUID().toString();

        log.info("Fetching products from Raiffeisen Bank");
        try {
            ResponseEntity<GetAccounts200Response> responseEntity = raiffeisenBankApiFeignClient.getAccounts(raiffeisenBankXIbmClientId, requestId);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return raiffeisenBankApiMapper.toProductsResponse(responseEntity.getBody(), bankDetails);
            }
            throw new ServiceException("Failed to fetch products from Raiffeisen Bank API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling Raiffeisen Bank API: " + e.getMessage(), e);
        }
    }

    public TransactionsMessagingResponse fetchTransactions(TransactionsRequest transactionsRequest, String fromDate, String toDate) {
        String accountId = transactionsRequest.getAccountId();
        String requestId = UUID.randomUUID().toString();

        try {
            ResponseEntity<GetTransactionList200Response> responseEntity = raiffeisenBankApiFeignClient.getTransactions(raiffeisenBankXIbmClientId, requestId, accountId, CZK_CURRENCY_CODE, fromDate, toDate);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return raiffeisenBankApiMapper.toTransactionsResponse(responseEntity.getBody());
            }
            throw new ServiceException("Failed to fetch transactions from Raiffeisen Bank API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling Raiffeisen Bank API: " + e.getMessage(), e);
        }
    }

    public Amount fetchAccountBalance(AccountBalancesMessagingRequest accountBalancesMessagingRequest) {
        String accountId = accountBalancesMessagingRequest.getAccountId();
        String requestId = UUID.randomUUID().toString();

        try {
            ResponseEntity<GetBalance200Response> responseEntity = raiffeisenBankApiFeignClient.getAccountBalance(raiffeisenBankXIbmClientId, requestId, accountId);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return raiffeisenBankApiMapper.toDomainBalance(responseEntity.getBody());
            }
            throw new ServiceException("Failed to fetch account balances from Raiffeisen Bank API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling Raiffeisen Bank API: " + e.getMessage(), e);
        }
    }
}
