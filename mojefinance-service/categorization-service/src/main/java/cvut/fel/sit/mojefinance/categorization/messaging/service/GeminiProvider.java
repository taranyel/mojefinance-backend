package cvut.fel.sit.mojefinance.categorization.messaging.service;

import cvut.fel.sit.mojefinance.categorization.domain.entity.TransactionCategory;

public interface GeminiProvider {
    TransactionCategory categorizeTransactionWithAI(String transactionName);
}
