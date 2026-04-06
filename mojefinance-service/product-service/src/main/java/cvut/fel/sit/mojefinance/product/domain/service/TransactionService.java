package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;

public interface TransactionService {
    TransactionsDomainResponse getTransactions(TransactionsRequest request);
}
