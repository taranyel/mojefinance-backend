package cvut.fel.sit.mojefinance.product.domain.helper;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.categorization.CategorizationService;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.RelatedParties;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionDirection;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import cvut.fel.sit.shared.entity.TransactionCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionHelperTest {

    @Mock
    private ExternalApiProvider externalApiProvider;

    @Mock
    private CategorizationService categorizationService;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private TransactionHelper transactionHelper;

    @Test
    void buildTransactionsRequest_ShouldReturnCorrectlyBuiltRequest() {
        String accountId = "acc-123";
        BankDetails bankDetails = new BankDetails(); // Assuming a no-arg constructor
        LocalDate fromDate = LocalDate.of(2026, 1, 1);

        TransactionsRequest request = transactionHelper.buildTransactionsRequest(accountId, bankDetails, fromDate);

        assertNotNull(request);
        assertEquals(accountId, request.getAccountId());
        assertEquals(bankDetails, request.getBankDetails());
        assertEquals(fromDate, request.getFromDate());
    }

    @Test
    void getTransactionsFromExternalApi_ShouldReturnTransactionList() {
        TransactionsRequest request = TransactionsRequest.builder().build();
        Transaction mockTransaction = new Transaction();
        TransactionsMessagingResponse mockResponse = new TransactionsMessagingResponse();
        mockResponse.setTransactions(List.of(mockTransaction));

        when(externalApiProvider.getTransactions(request)).thenReturn(mockResponse);

        List<Transaction> result = transactionHelper.getTransactionsFromExternalApi(request);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockTransaction, result.get(0));
        verify(externalApiProvider, times(1)).getTransactions(request);
    }

    @Test
    void constructAuthorizationHeader_ShouldReturnBearerToken() {
        String clientRegId = "test-bank";
        String mockToken = "eyJhbGciOiJIUzI1...";
        when(authorizationService.authorizeClient(clientRegId)).thenReturn(mockToken);

        String header = transactionHelper.constructAuthorizationHeader(clientRegId);

        assertEquals("Bearer " + mockToken, header);
        verify(authorizationService, times(1)).authorizeClient(clientRegId);
    }

    @Test
    void enrichTransactions_Outcome_WithCreditorName_ShouldNegateAmountAndCategorize() {
        // 1. Setup Transaction
        Transaction tx = new Transaction();
        tx.setDirection(TransactionDirection.OUTCOME);

        Amount amount = new Amount();
        amount.setValue(new BigDecimal("100.50"));
        amount.setCurrency("CZK");
        tx.setAmount(amount);

        RelatedParties parties = new RelatedParties();
        parties.setCreditorName("Tesco");
        tx.setRelatedParties(parties);

        // 2. Mock Categorization Service
        CategorizeTransactionsResponse catResponse = new CategorizeTransactionsResponse();
        catResponse.setCategorizedTransactions(Map.of("Tesco OUTCOME", TransactionCategory.GROCERIES));
        when(categorizationService.categorizeTransactions(any(CategorizeTransactionsRequest.class))).thenReturn(catResponse);

        // 3. Execute
        transactionHelper.enrichTransactions(List.of(tx));

        // 4. Verify Counterparty & Amount Logic
        assertEquals("Tesco", tx.getCounterpartyName());
        assertEquals(new BigDecimal("-100.50"), tx.getAmount().getValue());

        // 5. Verify Categorization Logic
        assertEquals(TransactionCategory.GROCERIES, tx.getCategory());
    }

    @Test
    void enrichTransactions_Income_WithDebtorIbanOnly_ShouldNotNegateAndFallbackToUncategorized() {
        // 1. Setup Transaction
        Transaction tx = new Transaction();
        tx.setDirection(TransactionDirection.INCOME);

        Amount amount = new Amount();
        amount.setValue(new BigDecimal("5000.00"));
        amount.setCurrency("CZK");
        tx.setAmount(amount);

        RelatedParties parties = new RelatedParties();
        parties.setDebtorName(null);
        parties.setDebtorAccountIban("CZ1234567890"); // Should fallback to IBAN
        tx.setRelatedParties(parties);

        // 2. Mock Categorization Service (Empty map means no category found)
        CategorizeTransactionsResponse catResponse = new CategorizeTransactionsResponse();
        catResponse.setCategorizedTransactions(Map.of());
        when(categorizationService.categorizeTransactions(any())).thenReturn(catResponse);

        // 3. Execute
        transactionHelper.enrichTransactions(List.of(tx));

        // 4. Verify Counterparty Logic
        assertEquals("CZ1234567890", tx.getCounterpartyName());
        assertEquals(new BigDecimal("5000.00"), tx.getAmount().getValue());

        // 5. Verify Categorization Fallback
        assertEquals(TransactionCategory.UNCATEGORIZED, tx.getCategory());
    }

    @Test
    void enrichTransactions_MissingParties_ShouldFallbackToNotSpecified() {
        // 1. Setup Transaction with empty parties
        Transaction tx = new Transaction();
        tx.setDirection(TransactionDirection.OUTCOME);

        Amount amount = new Amount();
        amount.setValue(new BigDecimal("-50.00"));
        amount.setCurrency("CZK");
        tx.setAmount(amount);

        RelatedParties parties = new RelatedParties(); // Name and IBAN are null
        tx.setRelatedParties(parties);

        // 2. Mock Categorization Service
        CategorizeTransactionsResponse catResponse = new CategorizeTransactionsResponse();
        catResponse.setCategorizedTransactions(Map.of());
        when(categorizationService.categorizeTransactions(any())).thenReturn(catResponse);

        // 3. Execute
        transactionHelper.enrichTransactions(List.of(tx));

        // 4. Verify
        assertEquals("Not specified", tx.getCounterpartyName());
        assertEquals(new BigDecimal("-50.00"), tx.getAmount().getValue());
        assertEquals(TransactionCategory.UNCATEGORIZED, tx.getCategory());
    }

    @Test
    void enrichTransactions_NullRelatedParties_ShouldNotThrowException() {
        Transaction tx = new Transaction();
        tx.setDirection(TransactionDirection.OUTCOME);
        tx.setRelatedParties(null);
        tx.setAmount(new Amount());
        tx.getAmount().setValue(BigDecimal.ONE);
        tx.getAmount().setCurrency("CZK");

        CategorizeTransactionsResponse catResponse = new CategorizeTransactionsResponse();
        catResponse.setCategorizedTransactions(Map.of());
        when(categorizationService.categorizeTransactions(any())).thenReturn(catResponse);

        assertDoesNotThrow(() -> transactionHelper.enrichTransactions(List.of(tx)));
    }

    @Test
    void enrichTransactions_ShouldPassCorrectNamesToCategorizationService() {
        // Setup multiple transactions to ensure the Set logic works
        Transaction tx1 = new Transaction();
        tx1.setDirection(TransactionDirection.OUTCOME);
        RelatedParties parties1 = new RelatedParties();
        parties1.setCreditorName("Netflix");
        tx1.setRelatedParties(parties1);
        tx1.setAmount(new Amount());
        tx1.getAmount().setValue(BigDecimal.ONE);
        tx1.getAmount().setCurrency("CZK");

        Transaction tx2 = new Transaction();
        tx2.setDirection(TransactionDirection.INCOME);
        RelatedParties parties2 = new RelatedParties();
        parties2.setDebtorName("Employer");
        tx2.setRelatedParties(parties2);
        tx2.setAmount(new Amount());
        tx2.getAmount().setValue(BigDecimal.ONE);
        tx2.getAmount().setCurrency("CZK");

        when(categorizationService.categorizeTransactions(any())).thenReturn(new CategorizeTransactionsResponse(Map.of()));

        // Execute
        transactionHelper.enrichTransactions(List.of(tx1, tx2));

        // Verify the argument passed to CategorizationService
        ArgumentCaptor<CategorizeTransactionsRequest> requestCaptor = ArgumentCaptor.forClass(CategorizeTransactionsRequest.class);
        verify(categorizationService).categorizeTransactions(requestCaptor.capture());

        Set<String> sentNames = requestCaptor.getValue().getTransactionNames();
        assertEquals(2, sentNames.size());
        assertTrue(sentNames.contains("Netflix OUTCOME"));
        assertTrue(sentNames.contains("Employer INCOME"));
    }
}