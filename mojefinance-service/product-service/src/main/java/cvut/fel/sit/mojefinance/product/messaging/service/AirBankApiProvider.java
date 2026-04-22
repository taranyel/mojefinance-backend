package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.airbank.openapi.model.AccountList;
import cvut.fel.sit.airbank.openapi.model.BalanceList;
import cvut.fel.sit.airbank.openapi.model.TransactionList;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountBalancesMessagingRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.messaging.client.AirBankApiFeignClient;
import cvut.fel.sit.mojefinance.product.messaging.dto.ProductsMessagingRequest;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.mapper.AirBankApiMapper;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AirBankApiProvider {
    private final AirBankApiFeignClient airBankApiFeignClient;
    private final AirBankApiMapper airBankApiMapper;

    public ProductsResponse fetchProducts(ProductsMessagingRequest productsMessagingRequest) {
        String authorization = productsMessagingRequest.getAuthorization();
        BankDetails bankDetails = productsMessagingRequest.getBankDetails();
        log.info("Fetching products from Air Bank");
        try {
            ResponseEntity<AccountList> responseEntity = airBankApiFeignClient.getAccounts(authorization);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return airBankApiMapper.toProductsResponse(responseEntity.getBody(), bankDetails);
            }
            throw new ServiceException("Failed to fetch products from Air Bank API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling Air Bank API: " + e.getMessage(), e);
        }
    }

    public TransactionsMessagingResponse fetchTransactions(TransactionsRequest transactionsRequest, String fromDate, String toDate) {
        String authorization = transactionsRequest.getAuthorization();
        String accountId = transactionsRequest.getAccountId();

        try {
            ResponseEntity<TransactionList> responseEntity = airBankApiFeignClient.getTransactions(authorization, accountId, fromDate, toDate);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return airBankApiMapper.toTransactionsResponse(responseEntity.getBody());
            }
            throw new ServiceException("Failed to fetch transactions from Air Bank API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling Air Bank API: " + e.getMessage(), e);
        }
    }

    public Amount fetchAccountBalance(AccountBalancesMessagingRequest accountBalancesMessagingRequest) {
        String authorization = accountBalancesMessagingRequest.getAuthorization();
        String accountId = accountBalancesMessagingRequest.getAccountId();
        try {
            ResponseEntity<BalanceList> responseEntity = airBankApiFeignClient.getAccountBalance(authorization, accountId);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()) {
                return airBankApiMapper.toDomainBalance(responseEntity.getBody());
            }
            throw new ServiceException("Failed to fetch account balances from Air Bank API. Status: " + responseEntity.getStatusCode());
        } catch (FeignException e) {
            throw new ServiceException("Error calling Air Bank API: " + e.getMessage(), e);
        }
    }
}
