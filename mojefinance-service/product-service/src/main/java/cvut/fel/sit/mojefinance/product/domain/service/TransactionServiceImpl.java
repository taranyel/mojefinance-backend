package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.categorization.CategorizationService;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsResponse;
import cvut.fel.sit.mojefinance.categorization.domain.entity.TransactionCategory;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.RelatedParties;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionDirection;
import cvut.fel.sit.mojefinance.product.domain.helper.TransactionsGroupingHelper;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final ExternalApiProvider externalApiProvider;
    private final AuthorizationService authorizationService;
    private final TransactionsGroupingHelper transactionsGroupingHelper;
    private final CategorizationService categorizationService;

    private static final String NOT_SPECIFIED_COUNTERPARTY_NAME = "Not specified";
    private static final String BEARER_PREFIX = "Bearer";

    @Override
    public TransactionsDomainResponse getTransactions(TransactionsRequest request) {
        log.info("Getting transactions for authorized user.");
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        request.setPrincipalName(principal.getName());

        String clientRegistrationId = request.getBankDetails().getClientRegistrationId();
        String authorization = BEARER_PREFIX + " " + authorizationService.authorizeClient(clientRegistrationId);
        request.setAuthorization(authorization);

        TransactionsMessagingResponse messagingResponse = externalApiProvider.getTransactions(request);
        List<Transaction> transactions = messagingResponse.getTransactions();
        enrichTransactions(transactions);

        log.info("Retrieved: {} transactions for client registration id: {} for account id: {}, from date: {}, to date: {}",
                transactions.size(), clientRegistrationId, request.getAccountId(), request.getFromDate(), request.getToDate());
        return transactionsGroupingHelper.groupTransactions(transactions);
    }

    private void enrichTransactions(List<Transaction> transactions) {
        transactions.forEach(this::enrichSingleTransaction);
        setTransactionCategories(transactions);
    }

    private void setTransactionCategories(List<Transaction> transactions) {
        CategorizeTransactionsRequest categorizeTransactionsRequest = buildCategorizeTransactionsRequest(transactions);
        CategorizeTransactionsResponse categorizeTransactionsResponse = categorizationService.categorizeTransactions(categorizeTransactionsRequest);
        Map<String, TransactionCategory> categoryMap = categorizeTransactionsResponse.getCategorizedTransactions();

        transactions.forEach(transaction -> {
            TransactionCategory category = categoryMap.get(transaction.getCounterpartyName() + " " + transaction.getDirection().name());
            if (category == null) {
                category = TransactionCategory.UNCATEGORIZED;
            }
            transaction.setCategory(category.getDisplayName());
        });
    }

    private CategorizeTransactionsRequest buildCategorizeTransactionsRequest(List<Transaction> transactions) {
        return CategorizeTransactionsRequest.builder()
                .transactionsNames(transactions.stream()
                        .map(transaction -> transaction.getCounterpartyName() + " " + transaction.getDirection().name())
                        .collect(Collectors.toSet()))
                .build();
    }

    private void enrichSingleTransaction(Transaction transaction) {
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
