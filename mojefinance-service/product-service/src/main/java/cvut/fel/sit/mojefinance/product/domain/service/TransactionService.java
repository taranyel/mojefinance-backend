package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;

import java.time.LocalDate;

public interface TransactionService {
    TransactionsDomainResponse getTransactions(TransactionsRequest request);

    TransactionsDomainResponse getCashFlowSummary(LocalDate fromDate);
}
