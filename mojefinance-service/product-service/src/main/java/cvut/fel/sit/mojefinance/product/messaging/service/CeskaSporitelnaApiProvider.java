package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.cs.openapi.model.MyAccountsGet200Response;
import cvut.fel.sit.cs.openapi.model.MyAccountsIdBalanceGet200Response;
import cvut.fel.sit.cs.openapi.model.MyAccountsIdTransactionsGet200Response;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.CeskaSporitelnaApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.CeskaSporitelnaApiMapper;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CeskaSporitelnaApiProvider {
    private final CeskaSporitelnaApiFeignClient ceskaSporitelnaApiFeignClient;
    private final CeskaSporitelnaApiMapper ceskaSporitelnaApiMapper;

    @Value("${external.api.ceska-sporitelna.apikey}")
    private String ceskaSporitelnaApiKey;

    public ProductsResponse fetchProducts(ProductsMessagingRequest productsMessagingRequest) {
        String authorization = productsMessagingRequest.getAuthorization();
        BankDetails bankDetails = productsMessagingRequest.getBankDetails();
        log.info("Fetching products from Ceska Sporitelna");
        try {
            ResponseEntity<MyAccountsGet200Response> responseEntity = ceskaSporitelnaApiFeignClient.getAccounts(authorization, ceskaSporitelnaApiKey);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return ceskaSporitelnaApiMapper.toProductsResponse(responseEntity.getBody(), bankDetails);
            }
            throw new ServiceException("Failed to fetch products from Ceska Sporitelna API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling Ceska Sporitelna API: " + e.getMessage(), e);
        }
    }

    public TransactionsMessagingResponse fetchTransactions(TransactionsRequest transactionsRequest, String fromDate, String toDate) {
        String authorization = transactionsRequest.getAuthorization();
        String accountId = transactionsRequest.getAccountId();

        try {
            ResponseEntity<MyAccountsIdTransactionsGet200Response> responseEntity = ceskaSporitelnaApiFeignClient.getTransactions(authorization, ceskaSporitelnaApiKey, accountId, fromDate, toDate);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return ceskaSporitelnaApiMapper.toTransactionsResponse(responseEntity.getBody());
            }
            throw new ServiceException("Failed to fetch transactions from Ceska Sporitelna API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling Ceska Sporitelna API: " + e.getMessage(), e);
        }
    }

    public Amount fetchAccountBalance(AccountBalancesMessagingRequest accountBalancesMessagingRequest) {
        String authorization = accountBalancesMessagingRequest.getAuthorization();
        String accountId = accountBalancesMessagingRequest.getAccountId();
        try {
            ResponseEntity<MyAccountsIdBalanceGet200Response> responseEntity = ceskaSporitelnaApiFeignClient.getAccountBalance(authorization, ceskaSporitelnaApiKey, accountId);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return ceskaSporitelnaApiMapper.toDomainBalance(responseEntity.getBody());
            }
            throw new ServiceException("Failed to fetch account balances from Ceska Sporitelna API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling Ceska Sporitelna API: " + e.getMessage(), e);
        }
    }
}
