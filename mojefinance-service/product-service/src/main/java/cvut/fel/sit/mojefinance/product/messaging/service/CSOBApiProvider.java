package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.csob.accounts.openapi.model.GetAccountsRes;
import cvut.fel.sit.csob.balances.openapi.model.GetAccountBalanceRes;
import cvut.fel.sit.csob.transactions.openapi.model.GetTransactionHistoryRes;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.CSOBApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.CSOBApiMapper;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CSOBApiProvider {
    private final CSOBApiFeignClient csobApiFeignClient;
    private final CSOBApiMapper csobApiMapper;
    private static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";

    @Value("${external.api.csob.apikey}")
    private String csobApiKey;

    @Value("${external.api.csob.tpp-name}")
    private String csobTppName;

    public ProductsResponse fetchProducts(ProductsMessagingRequest productsMessagingRequest) {
        String authorization = productsMessagingRequest.getAuthorization();
        BankDetails bankDetails = productsMessagingRequest.getBankDetails();
        String requestId = UUID.randomUUID().toString();

        log.info("Fetching products from CSOB");
        try {
            ResponseEntity<GetAccountsRes> responseEntity = csobApiFeignClient.getAccounts(authorization, requestId, true, csobTppName, csobApiKey, APPLICATION_JSON_CONTENT_TYPE);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return csobApiMapper.toProductsResponse(responseEntity.getBody(), bankDetails);
            }
            throw new ServiceException("Failed to fetch products from CSOB API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling CSOB API: " + e.getMessage(), e);
        }
    }

    public TransactionsMessagingResponse fetchTransactions(TransactionsRequest transactionsRequest, String fromDate, String toDate) {
        String authorization = transactionsRequest.getAuthorization();
        String accountId = transactionsRequest.getAccountId();
        String requestId = UUID.randomUUID().toString();

        try {
            ResponseEntity<GetTransactionHistoryRes> responseEntity = csobApiFeignClient.getTransactions(authorization, requestId, true, csobTppName, csobApiKey, APPLICATION_JSON_CONTENT_TYPE, accountId, fromDate, toDate);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return csobApiMapper.toTransactionsResponse(responseEntity.getBody());
            }
            throw new ServiceException("Failed to fetch transactions from CSOB API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling CSOB API: " + e.getMessage(), e);
        }
    }

    public Amount fetchAccountBalance(AccountBalancesMessagingRequest accountBalancesMessagingRequest) {
        String authorization = accountBalancesMessagingRequest.getAuthorization();
        String accountId = accountBalancesMessagingRequest.getAccountId();
        String requestId = UUID.randomUUID().toString();

        try {
            ResponseEntity<GetAccountBalanceRes> responseEntity = csobApiFeignClient.getAccountBalance(authorization, requestId, true, csobTppName, csobApiKey, APPLICATION_JSON_CONTENT_TYPE, accountId);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return csobApiMapper.toDomainBalance(responseEntity.getBody());
            }
            throw new ServiceException("Failed to fetch account balances from CSOB API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling CSOB API: " + e.getMessage(), e);
        }
    }
}
