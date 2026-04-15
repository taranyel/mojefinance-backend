package cvut.fel.sit.mojefinance.product.api.controller;

import cvut.fel.sit.mojefinance.openapi.api.ProductsApi;
import cvut.fel.sit.mojefinance.openapi.model.AssetsAndLiabilitiesResponse;
import cvut.fel.sit.mojefinance.openapi.model.TransactionsResponse;
import cvut.fel.sit.mojefinance.product.api.mapper.ProductsMapper;
import cvut.fel.sit.mojefinance.product.api.mapper.TransactionsMapper;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.service.ProductService;
import cvut.fel.sit.mojefinance.product.domain.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductsApi {
    private final ProductService productService;
    private final TransactionService transactionService;
    private final ProductsMapper productsMapper;
    private final TransactionsMapper transactionsMapper;

    @Override
    public ResponseEntity<cvut.fel.sit.mojefinance.openapi.model.ProductsResponse> getProducts(String authorization) {
        ProductsResponse domainResponse = productService.getProducts();
        cvut.fel.sit.mojefinance.openapi.model.ProductsResponse apiResponse = productsMapper.toProductsResponse(domainResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TransactionsResponse> getTransactions(String authorization, String accountId, String clientRegistrationId, LocalDate fromDate, LocalDate toDate) {
        TransactionsRequest request = buildTransactionsRequest(accountId, clientRegistrationId, fromDate, toDate);
        TransactionsDomainResponse domainResponse = transactionService.getTransactions(request);
        TransactionsResponse apiResponse = transactionsMapper.toTransactionsResponse(domainResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AssetsAndLiabilitiesResponse> getAssetsAndLiabilities(String authorization) {
        cvut.fel.sit.mojefinance.product.domain.dto.AssetsAndLiabilitiesResponse domainResponse = productService.getAssetsAndLiabilities();
        AssetsAndLiabilitiesResponse apiResponse = productsMapper.toAssetsAndLiabilitiesResponse(domainResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TransactionsResponse> getCashFlowSummary(String authorization) {
        TransactionsDomainResponse domainResponse = transactionService.getCashFlowSummary();
        TransactionsResponse apiResponse = transactionsMapper.toTransactionsResponse(domainResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    private TransactionsRequest buildTransactionsRequest(String accountId, String clientRegistrationId, LocalDate fromDate, LocalDate toDate) {
        return TransactionsRequest.builder()
                .bankDetails(BankDetails.builder()
                        .clientRegistrationId(clientRegistrationId)
                        .build())
                .accountId(accountId)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
    }
}
