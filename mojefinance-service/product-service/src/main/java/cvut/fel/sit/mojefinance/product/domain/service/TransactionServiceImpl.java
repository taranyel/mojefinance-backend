package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.domain.helper.TransactionHelper;
import cvut.fel.sit.mojefinance.product.domain.helper.TransactionsGroupingHelper;
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
public class TransactionServiceImpl implements TransactionService {
    private final TransactionsGroupingHelper transactionsGroupingHelper;
    private final ProductService productService;
    private final TransactionHelper transactionHelper;

    @Override
    public TransactionsDomainResponse getTransactions(TransactionsRequest request) {
        log.info("Getting transactions for authorized user.");
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        request.setPrincipalName(principal.getName());

        String clientRegistrationId = request.getBankDetails().getClientRegistrationId();
        String authorization = transactionHelper.constructAuthorizationHeader(clientRegistrationId);
        request.setAuthorization(authorization);

        List<Transaction> transactions = transactionHelper.getTransactionsFromExternalApi(request);
        transactionHelper.enrichTransactions(transactions);

        log.info("Retrieved: {} transactions for client registration id: {} for account id: {}, from date: {}, to date: {}",
                transactions.size(), clientRegistrationId, request.getAccountId(), request.getFromDate(), request.getToDate());
        return transactionsGroupingHelper.groupTransactions(transactions);
    }

    @Override
    public TransactionsDomainResponse getCashFlowSummary() {
        log.info("Getting cash flow summary for authorized user.");
        ProductsResponse productsResponse = productService.getProducts();
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        String clientRegistrationId = null;
        String authorization = null;
        List<Transaction> allTransactions = new ArrayList<>();

        for (Product product : productsResponse.getProducts()) {
            TransactionsRequest request = transactionHelper.buildTransactionsRequest(product.getProductId(), product.getBankDetails());
            request.setPrincipalName(principal.getName());

            if (authorization == null || !clientRegistrationId.equals(product.getBankDetails().getClientRegistrationId())) {
                clientRegistrationId = product.getBankDetails().getClientRegistrationId();
                authorization = transactionHelper.constructAuthorizationHeader(clientRegistrationId);
            }
            request.setAuthorization(authorization);

            List<Transaction> transactions = transactionHelper.getTransactionsFromExternalApi(request);
            transactionHelper.enrichTransactions(transactions);
            allTransactions.addAll(transactions);
        }

        log.info("Retrieved cash flow summary with total: {} transactions for authorized user.", allTransactions.size());
        return transactionsGroupingHelper.groupTransactions(allTransactions);
    }
}
