package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.product.domain.dto.AccountInfoRequest;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final ExternalApiProvider externalApiProvider;
    private final AuthorizationService authorizationService;
    private final TransactionsGroupingHelper transactionsGroupingHelper;

    @Override
    public TransactionsDomainResponse getTransactions(AccountInfoRequest request) {
        log.info("Getting transactions for authorized user.");
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        request.setPrincipalName(principal.getName());

        String clientRegistrationId = request.getBankDetails().getClientRegistrationId();
        String authorization = "Bearer " + authorizationService.authorizeClient(clientRegistrationId);
        request.setAuthorization(authorization);

        TransactionsMessagingResponse messagingResponse = externalApiProvider.getTransactions(request);
        List<Transaction> transactions = messagingResponse.getTransactions();
        enrichTransactions(transactions);

        log.info("Retrieved: {} transactions for client registration id: {} for account id: {}", transactions.size(), clientRegistrationId, request.getAccountId());
        return transactionsGroupingHelper.groupTransactions(transactions);
    }

    private void enrichTransactions(List<Transaction> transactions) {
        transactions.forEach(this::enrichSingleTransaction);
    }

    private void enrichSingleTransaction(Transaction transaction) {
        RelatedParties parties = transaction.getRelatedParties();
        if (parties == null || transaction.getDirection() == null) {
            return;
        }
        if (TransactionDirection.OUTCOME.equals(transaction.getDirection())) {
            String name = parties.getCreditorName() != null ? parties.getCreditorName() : parties.getCreditorAccountIban();
            transaction.setCounterpartyName(name);
            transaction.getAmount().setValue(transaction.getAmount().getValue().negate());

        } else if (TransactionDirection.INCOME.equals(transaction.getDirection())) {
            String name = parties.getDebtorName() != null ? parties.getDebtorName() : parties.getDebtorAccountIban();
            transaction.setCounterpartyName(name);
        }
    }
}
