package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.dto.AccountInfoRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;

public interface TransactionService {
    TransactionsDomainResponse getTransactions(AccountInfoRequest request);
}
