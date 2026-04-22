package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.kb.openapi.model.GeAccountTransactionsResponse;
import cvut.fel.sit.kb.openapi.model.GetAccountBalanceResponse;
import cvut.fel.sit.kb.openapi.model.GetAccountListResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.KBApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.KBApiMapper;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KBApiProvider {
    private final KBApiFeignClient kbApiFeignClient;
    private final KBApiMapper kbApiMapper;

    public ProductsResponse fetchProducts(ProductsMessagingRequest productsMessagingRequest) {
        String authorization = productsMessagingRequest.getAuthorization();
        BankDetails bankDetails = productsMessagingRequest.getBankDetails();

        log.info("Fetching products from KB");
        try {
            ResponseEntity<GetAccountListResponse> responseEntity = kbApiFeignClient.getAccounts(authorization);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return kbApiMapper.toProductsResponse(responseEntity.getBody(), bankDetails);
            }
            throw new ServiceException("Failed to fetch products from KB API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling KB API: " + e.getMessage(), e);
        }
    }

    public TransactionsMessagingResponse fetchTransactions(TransactionsRequest transactionsRequest, String fromDate, String toDate) {
        String authorization = transactionsRequest.getAuthorization();
        String accountId = transactionsRequest.getAccountId();

        try {
            ResponseEntity<GeAccountTransactionsResponse> responseEntity = kbApiFeignClient.getTransactions(authorization, accountId, fromDate, toDate);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return kbApiMapper.toTransactionsResponse(responseEntity.getBody());
            }
            throw new ServiceException("Failed to fetch transactions from KB API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling KB API: " + e.getMessage(), e);
        }
    }

    public Amount fetchAccountBalance(AccountBalancesMessagingRequest accountBalancesMessagingRequest) {
        String authorization = accountBalancesMessagingRequest.getAuthorization();
        String accountId = accountBalancesMessagingRequest.getAccountId();
        try {
            ResponseEntity<GetAccountBalanceResponse> responseEntity = kbApiFeignClient.getAccountBalance(authorization, accountId);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return kbApiMapper.toDomainBalance(responseEntity.getBody());
            }
            throw new ServiceException("Failed to fetch account balances from KB API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling KB API: " + e.getMessage(), e);
        }
    }
}
