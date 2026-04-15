package cvut.fel.sit.mojefinance.product.domain.helper;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.categorization.CategorizationService;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.RelatedParties;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionDirection;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import cvut.fel.sit.shared.util.entity.TransactionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionHelper {
    private final ExternalApiProvider externalApiProvider;
    private final CategorizationService categorizationService;
    private final AuthorizationService authorizationService;
    private static final String NOT_SPECIFIED_COUNTERPARTY_NAME = "Not specified";
    private static final String BEARER_PREFIX = "Bearer";

    public TransactionsRequest buildTransactionsRequest(String accountId, BankDetails bankDetails) {
        return TransactionsRequest.builder()
                .bankDetails(bankDetails)
                .accountId(accountId)
                .build();
    }

    public List<Transaction> getTransactionsFromExternalApi(TransactionsRequest request) {
        TransactionsMessagingResponse messagingResponse = externalApiProvider.getTransactions(request);
        return messagingResponse.getTransactions();
    }

    public void enrichTransactions(List<Transaction> transactions) {
        setCounterparties(transactions);
        setTransactionCategories(transactions);
    }

    public String constructAuthorizationHeader(String clientRegistrationId) {
        return BEARER_PREFIX + " " + authorizationService.authorizeClient(clientRegistrationId);
    }

    private void setTransactionCategories(List<Transaction> transactions) {
        Map<String, TransactionCategory> categoryMap = getTransactionCategoryMap(transactions);
        transactions.forEach(transaction -> {
            TransactionCategory category = categoryMap.get(transaction.getCounterpartyName() + " " + transaction.getDirection().name());
            if (category == null) {
                category = TransactionCategory.UNCATEGORIZED;
            }
            transaction.setCategory(category);
        });
    }

    private Map<String, TransactionCategory> getTransactionCategoryMap(List<Transaction> transactions) {
        CategorizeTransactionsRequest categorizeTransactionsRequest = buildCategorizeTransactionsRequest(transactions);
        CategorizeTransactionsResponse categorizeTransactionsResponse = categorizationService
                .categorizeTransactions(categorizeTransactionsRequest);
        return categorizeTransactionsResponse.getCategorizedTransactions();
    }

    private CategorizeTransactionsRequest buildCategorizeTransactionsRequest(List<Transaction> transactions) {
        return CategorizeTransactionsRequest.builder()
                .transactionNames(transactions.stream()
                        .map(transaction -> transaction.getCounterpartyName() + " " + transaction.getDirection().name())
                        .collect(Collectors.toSet()))
                .build();
    }

    private void setCounterparties(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            RelatedParties parties = transaction.getRelatedParties();
            if (parties == null || transaction.getDirection() == null) {
                return;
            }
            if (TransactionDirection.OUTCOME.equals(transaction.getDirection())) {
                String counterpartyName = getCounterpartyName(parties.getCreditorName(), parties.getCreditorAccountIban());
                transaction.setCounterpartyName(counterpartyName);
                negateTransactionAmountValue(transaction);

            } else if (TransactionDirection.INCOME.equals(transaction.getDirection())) {
                String counterpartyName = getCounterpartyName(parties.getDebtorName(), parties.getDebtorAccountIban());
                transaction.setCounterpartyName(counterpartyName);
            }
        }
    }

    private String getCounterpartyName(String partyName1, String partyName2) {
        String counterpartyName = NOT_SPECIFIED_COUNTERPARTY_NAME;
        if (partyName1 != null || partyName2 != null) {
            counterpartyName = partyName1 != null ? partyName1 : partyName2;
        }
        return counterpartyName;
    }

    private void negateTransactionAmountValue(Transaction transaction) {
        BigDecimal amountValue = transaction.getAmount().getValue();
        if (amountValue.compareTo(BigDecimal.ZERO) > 0) {
            transaction.getAmount().setValue(amountValue.negate());
        }
    }
}
