package cvut.fel.sit.mojefinance.product.api.controller;

import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.bank.domain.service.BankConnectionService;
import cvut.fel.sit.mojefinance.categorization.CategorizationService;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeProductsResponse;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsResponse;
import cvut.fel.sit.mojefinance.product.config.ProductTestConfiguration;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.*;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import cvut.fel.sit.shared.entity.ProductCategory;
import cvut.fel.sit.shared.entity.TransactionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ProductTestConfiguration.class)
@AutoConfigureMockMvc
@Testcontainers
class ProductIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankConnectionService bankConnectionService;

    @MockBean
    private CategorizationService categorizationService;

    @MockBean
    private ExternalApiProvider externalApiProvider;

    @MockBean
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        BankConnection testBankConnection = new BankConnection();
        testBankConnection.setClientRegistrationId("kb");
        testBankConnection.setBankName("KB");
        testBankConnection.setManuallyCreated(false);
        testBankConnection.setBankConnectionStatus(BankConnectionStatus.CONNECTED);

        ConnectedBanksResponse banksResponse = ConnectedBanksResponse.builder()
                .connectedBanks(List.of(testBankConnection))
                .build();

        when(bankConnectionService.getConnectedBanks()).thenReturn(banksResponse);
        when(authorizationService.authorizeClient(anyString())).thenReturn("Bearer test-token");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getProducts_ShouldFetchFromExternalProviderAndMapToApiResponse() throws Exception {
        when(externalApiProvider.getProducts(any())).thenReturn(getProductsResponse());
        when(externalApiProvider.getAccountBalance(any())).thenReturn(getProductBalance());
        when(categorizationService.categorizeProducts(any())).thenReturn(getCategorizeProductsResponse());

        mockMvc.perform(get("/products")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(1)))
                .andExpect(jsonPath("$.products[0].productId", is("acc-9876")))
                .andExpect(jsonPath("$.products[0].productCategory", is("Loan")))
                .andExpect(jsonPath("$.products[0].accountName", is("accountName")))
                .andExpect(jsonPath("$.products[0].ownersNames", is(List.of("John Doe"))))
                .andExpect(jsonPath("$.products[0].productIdentification.iban", is("iban")))
                .andExpect(jsonPath("$.products[0].productIdentification.productNumber", is("productNumber")))
                .andExpect(jsonPath("$.products[0].balance.value", is(10.0)))
                .andExpect(jsonPath("$.products[0].balance.currency", is("CZK")))
                .andExpect(jsonPath("$.products[0].bankDetails.clientRegistrationId", is("kb")))
                .andExpect(jsonPath("$.products[0].bankDetails.bankName", is("KB")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTransactions_ShouldParseDatesAndFetchFromExternalProvider() throws Exception {
        when(externalApiProvider.getTransactions(any())).thenReturn(getTransactionsMessagingResponse());
        when(categorizationService.categorizeTransactions(any())).thenReturn(getCategorizeTransactionsResponse());

        ResultActions result = mockMvc.perform(get("/products/kb/acc-12345/transactions")
                .header("Authorization", "Bearer test-token")
                .with(csrf())
                .param("fromDate", "2026-04-01")
                .param("toDate", "2026-04-20")
                .contentType(MediaType.APPLICATION_JSON));

        assertTransactionResponse(result);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAssetsAndLiabilities_ShouldAggregateProducts() throws Exception {
        when(externalApiProvider.getProducts(any())).thenReturn(getProductsResponse());
        when(externalApiProvider.getAccountBalance(any())).thenReturn(getProductBalance());
        when(categorizationService.categorizeProducts(any())).thenReturn(getCategorizeProductsResponse());

        mockMvc.perform(get("/products/assets-liabilities")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assets").exists())
                .andExpect(jsonPath("$.assets.groupedProducts", hasSize(0)))

                .andExpect(jsonPath("$.liabilities").exists())
                .andExpect(jsonPath("$.liabilities.groupedProducts", hasSize(1)))
                .andExpect(jsonPath("$.liabilities.totalAmount.value", is(10.0)))
                .andExpect(jsonPath("$.liabilities.totalAmount.currency", is("CZK")))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].groupName", is("Loan")))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products", hasSize(1)))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].totalAmount.value", is(10.0)))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].totalAmount.currency", is("CZK")))

                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products[0].productId", is("acc-9876")))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products[0].productCategory", is("Loan")))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products[0].accountName", is("accountName")))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products[0].ownersNames", is(List.of("John Doe"))))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products[0].productIdentification.iban", is("iban")))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products[0].productIdentification.productNumber", is("productNumber")))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products[0].balance.value", is(10.0)))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products[0].balance.currency", is("CZK")))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products[0].bankDetails.clientRegistrationId", is("kb")))
                .andExpect(jsonPath("$.liabilities.groupedProducts[0].products[0].bankDetails.bankName", is("KB")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCashFlowSummary_ShouldFetchProductsAndTransactions() throws Exception {
        when(externalApiProvider.getProducts(any())).thenReturn(getProductsResponse());
        when(externalApiProvider.getTransactions(any())).thenReturn(getTransactionsMessagingResponse());
        when(externalApiProvider.getAccountBalance(any())).thenReturn(getProductBalance());
        when(categorizationService.categorizeProducts(any())).thenReturn(getCategorizeProductsResponse());
        when(categorizationService.categorizeTransactions(any())).thenReturn(getCategorizeTransactionsResponse());

        ResultActions result = mockMvc.perform(get("/products/cash-flow")
                .header("Authorization", "Bearer test-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));
        assertTransactionResponse(result);
    }

    private void assertTransactionResponse(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedTransactions", hasSize(2)))
                .andExpect(jsonPath("$.groupedTransactions[0].groupName", is("PENDING")))
                .andExpect(jsonPath("$.groupedTransactions[0].totalIncome.value", is(0.0)))
                .andExpect(jsonPath("$.groupedTransactions[0].totalExpense.value", is(-10.0)))

                .andExpect(jsonPath("$.groupedTransactions[1].groupName", is("April 2026")))
                .andExpect(jsonPath("$.groupedTransactions[1].totalIncome.value", is(20.0)))
                .andExpect(jsonPath("$.groupedTransactions[1].totalExpense.value", is(0.0)))

                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions", hasSize(1)))
                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions[0].groupName", is("Groceries")))
                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions[0].totalIncome.value", is(0.0)))
                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions[0].totalExpense.value", is(-10.0)))
                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions[0].transactions", hasSize(1)))

                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions[0].transactions[0].amount.value", is(-10.0)))
                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions[0].transactions[0].amount.currency", is("CZK")))
                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions[0].transactions[0].direction", is("OUTCOME")))
                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions[0].transactions[0].status", is("PENDING")))
                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions[0].transactions[0].counterpartyName", is("Tesco")))
                .andExpect(jsonPath("$.groupedTransactions[0].groupedTransactions[0].transactions[0].category", is("GROCERIES")))

                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions", hasSize(1)))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].groupName", is("Electronics")))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].totalIncome.value", is(20.0)))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].totalExpense.value", is(0.0)))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions", hasSize(2)))

                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[0].amount.value", is(10.0)))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[0].amount.currency", is("CZK")))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[0].direction", is("INCOME")))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[0].status", is("BOOKED")))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[0].counterpartyName", is("debtorIban")))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[0].category", is("ELECTRONICS")))

                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[1].amount.value", is(10.0)))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[1].amount.currency", is("CZK")))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[1].direction", is("INCOME")))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[1].status", is("BOOKED")))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[1].counterpartyName", is("debtorIban")))
                .andExpect(jsonPath("$.groupedTransactions[1].groupedTransactions[0].transactions[1].category", is("ELECTRONICS")));
    }

    private static CategorizeTransactionsResponse getCategorizeTransactionsResponse() {
        return CategorizeTransactionsResponse.builder()
                .categorizedTransactions(Map.of(
                        "debtorIban INCOME", TransactionCategory.ELECTRONICS,
                        "Tesco OUTCOME", TransactionCategory.GROCERIES
                ))
                .build();
    }

    private static CategorizeProductsResponse getCategorizeProductsResponse() {
        return CategorizeProductsResponse.builder()
                .categorizedProducts(Map.of("loan", ProductCategory.LOAN))
                .build();
    }

    private static TransactionsMessagingResponse getTransactionsMessagingResponse() {
        Transaction bookedTransaction = Transaction.builder()
                .bookingDate(LocalDate.of(2026, 4, 1))
                .amount(Amount.builder().currency("CZK").value(BigDecimal.TEN).build())
                .status(TransactionStatus.BOOKED)
                .category(TransactionCategory.ELECTRONICS)
                .relatedParties(RelatedParties.builder().debtorAccountIban("debtorIban").build())
                .direction(TransactionDirection.INCOME)
                .build();

        Transaction pendingTransaction = Transaction.builder()
                .valueDate(LocalDate.of(2026, 4, 1))
                .amount(Amount.builder().currency("CZK").value(BigDecimal.TEN).build())
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.GROCERIES)
                .relatedParties(RelatedParties.builder().creditorName("Tesco").build())
                .direction(TransactionDirection.OUTCOME)
                .build();

        return TransactionsMessagingResponse.builder()
                .transactions(List.of(bookedTransaction, bookedTransaction, pendingTransaction)) // Reusing bookedTransaction twice
                .build();
    }

    private static ProductsResponse getProductsResponse() {
        Product product = Product.builder()
                .productId("acc-9876")
                .productName("loan")
                .bankDetails(BankDetails.builder().bankName("KB").clientRegistrationId("kb").build())
                .accountName("accountName")
                .ownersNames(List.of("John Doe"))
                .productIdentification(ProductIdentification.builder().productNumber("productNumber").iban("iban").build())
                .build();

        return ProductsResponse.builder()
                .products(List.of(product))
                .build();
    }

    private static Amount getProductBalance() {
        return Amount.builder()
                .value(BigDecimal.TEN)
                .currency("CZK")
                .build();
    }
}