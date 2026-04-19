package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsRequest;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.domain.helper.TransactionHelper;
import cvut.fel.sit.mojefinance.product.domain.helper.TransactionsGroupingHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionsGroupingHelper transactionsGroupingHelper;

    @Mock
    private ProductService productService;

    @Mock
    private TransactionHelper transactionHelper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private final String PRINCIPAL_NAME = "testUser";

    @BeforeEach
    void setUpSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(PRINCIPAL_NAME);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getTransactions_ShouldFetchEnrichAndGroupTransactions() {
        // Arrange
        BankDetails bankDetails = new BankDetails();
        bankDetails.setClientRegistrationId("test-bank");

        TransactionsRequest request = TransactionsRequest.builder()
                .bankDetails(bankDetails)
                .accountId("acc-1")
                .build();

        String mockAuthToken = "Bearer test-token";
        List<Transaction> mockTransactions = List.of(new Transaction(), new Transaction());
        TransactionsDomainResponse expectedResponse = new TransactionsDomainResponse();

        when(transactionHelper.constructAuthorizationHeader("test-bank")).thenReturn(mockAuthToken);
        when(transactionHelper.getTransactionsFromExternalApi(request)).thenReturn(mockTransactions);
        doNothing().when(transactionHelper).enrichTransactions(mockTransactions);
        when(transactionsGroupingHelper.groupTransactions(mockTransactions)).thenReturn(expectedResponse);

        // Act
        TransactionsDomainResponse actualResponse = transactionService.getTransactions(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        assertEquals(PRINCIPAL_NAME, request.getPrincipalName());
        assertEquals(mockAuthToken, request.getAuthorization());

        verify(transactionHelper, times(1)).getTransactionsFromExternalApi(request);
        verify(transactionHelper, times(1)).enrichTransactions(mockTransactions);
        verify(transactionsGroupingHelper, times(1)).groupTransactions(mockTransactions);
    }

    @Test
    void getCashFlowSummary_WhenNoProducts_ShouldReturnEmptyGrouping() {
        // Arrange
        LocalDate fromDate = LocalDate.now();
        when(productService.getProducts()).thenReturn(ProductsResponse.builder().products(Collections.emptyList()).build());
        when(transactionsGroupingHelper.groupTransactions(Collections.emptyList())).thenReturn(new TransactionsDomainResponse());

        // Act
        TransactionsDomainResponse response = transactionService.getCashFlowSummary(fromDate);

        // Assert
        assertNotNull(response);
        verify(transactionHelper, never()).getTransactionsFromExternalApi(any());
        verify(transactionsGroupingHelper, times(1)).groupTransactions(Collections.emptyList());
    }

    @Test
    void getCashFlowSummary_WithProductsFromSameBank_ShouldFetchAndGroupUsingCachedToken() {
        // Arrange
        LocalDate fromDate = LocalDate.now();
        String bankId = "same-bank-id";
        String mockAuthToken = "Bearer shared-token";

        BankDetails sharedBankDetails = new BankDetails();
        sharedBankDetails.setClientRegistrationId(bankId);

        Product product1 = new Product();
        product1.setProductId("acc-1");
        product1.setBankDetails(sharedBankDetails);

        Product product2 = new Product();
        product2.setProductId("acc-2");
        product2.setBankDetails(sharedBankDetails);

        when(productService.getProducts()).thenReturn(ProductsResponse.builder().products(List.of(product1, product2)).build());

        TransactionsRequest req1 = TransactionsRequest.builder()
                .accountId("acc-1")
                .build();
        TransactionsRequest req2 = TransactionsRequest.builder()
                .accountId("acc-2")
                .build();
        when(transactionHelper.buildTransactionsRequest("acc-1", sharedBankDetails, fromDate)).thenReturn(req1);
        when(transactionHelper.buildTransactionsRequest("acc-2", sharedBankDetails, fromDate)).thenReturn(req2);

        // This should only be called ONCE because of the caching logic inside the loop
        when(transactionHelper.constructAuthorizationHeader(bankId)).thenReturn(mockAuthToken);

        List<Transaction> txList1 = List.of(new Transaction());
        List<Transaction> txList2 = List.of(new Transaction());
        when(transactionHelper.getTransactionsFromExternalApi(req1)).thenReturn(txList1);
        when(transactionHelper.getTransactionsFromExternalApi(req2)).thenReturn(txList2);

        when(transactionsGroupingHelper.groupTransactions(anyList())).thenReturn(new TransactionsDomainResponse());

        // Act
        transactionService.getCashFlowSummary(fromDate);

        // Assert
        // Verify the auth header was requested exactly ONE time, proving the caching logic works
        verify(transactionHelper, times(1)).constructAuthorizationHeader(bankId);

        verify(transactionHelper, times(1)).getTransactionsFromExternalApi(req1);
        verify(transactionHelper, times(1)).getTransactionsFromExternalApi(req2);

        // Verify we passed 2 total transactions to the grouper (1 from txList1 + 1 from txList2)
        verify(transactionsGroupingHelper, times(1)).groupTransactions(argThat(list -> list.size() == 2));
    }

    @Test
    void getCashFlowSummary_WithProductsFromDifferentBanks_ShouldFetchWithDifferentTokens() {
        // Arrange
        LocalDate fromDate = LocalDate.now();

        BankDetails bank1 = new BankDetails();
        bank1.setClientRegistrationId("bank-A");
        Product product1 = new Product();
        product1.setProductId("acc-1");
        product1.setBankDetails(bank1);

        BankDetails bank2 = new BankDetails();
        bank2.setClientRegistrationId("bank-B");
        Product product2 = new Product();
        product2.setProductId("acc-2");
        product2.setBankDetails(bank2);

        when(productService.getProducts()).thenReturn(ProductsResponse.builder().products(List.of(product1, product2)).build());

        TransactionsRequest req1 = TransactionsRequest.builder().build();
        TransactionsRequest req2 = TransactionsRequest.builder().build();
        when(transactionHelper.buildTransactionsRequest("acc-1", bank1, fromDate)).thenReturn(req1);
        when(transactionHelper.buildTransactionsRequest("acc-2", bank2, fromDate)).thenReturn(req2);

        when(transactionHelper.constructAuthorizationHeader("bank-A")).thenReturn("Token-A");
        when(transactionHelper.constructAuthorizationHeader("bank-B")).thenReturn("Token-B");

        when(transactionHelper.getTransactionsFromExternalApi(any())).thenReturn(List.of(new Transaction()));

        // Act
        transactionService.getCashFlowSummary(fromDate);

        // Assert
        // Verify auth headers were fetched for BOTH banks
        verify(transactionHelper, times(1)).constructAuthorizationHeader("bank-A");
        verify(transactionHelper, times(1)).constructAuthorizationHeader("bank-B");

        assertEquals("Token-A", req1.getAuthorization());
        assertEquals("Token-B", req2.getAuthorization());
    }
}