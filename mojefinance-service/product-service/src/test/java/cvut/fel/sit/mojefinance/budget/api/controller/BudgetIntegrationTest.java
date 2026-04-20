package cvut.fel.sit.mojefinance.budget.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.budget.config.BudgetTestConfiguration;
import cvut.fel.sit.mojefinance.budget.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.budget.data.repository.BudgetJpaRepository;
import cvut.fel.sit.mojefinance.openapi.model.Amount;
import cvut.fel.sit.mojefinance.openapi.model.Budget;
import cvut.fel.sit.mojefinance.openapi.model.BudgetRequest;
import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.*;
import cvut.fel.sit.mojefinance.product.domain.service.TransactionService;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = BudgetTestConfiguration.class,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create"
        }
)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class BudgetIntegrationTest {

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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BudgetJpaRepository budgetJpaRepository;

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        when(authorizationService.authorizeClient(anyString())).thenReturn("Bearer test-token");
    }

    @Test
    @WithMockUser(username = "testuser")
    void createBudget_ShouldSaveToDatabase() throws Exception {
        BudgetRequest request = buildBudgetRequest("GROCERIES", 5000.0);

        mockMvc.perform(post("/budgets")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(budgetJpaRepository.count()).isEqualTo(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getBudgets_ShouldReturnSavedBudgetsAndCalculatedAmounts() throws Exception {
        BudgetRequest request = buildBudgetRequest("ELECTRONICS", 5000.0);

        mockMvc.perform(post("/budgets")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        TransactionsDomainResponse transactionsDomainResponse = getTransactionsDomainResponse();
        when(transactionService.getCashFlowSummary(any())).thenReturn(transactionsDomainResponse);

        mockMvc.perform(get("/budgets")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgets", hasSize(1)))
                .andExpect(jsonPath("$.budgets[0].category", is("ELECTRONICS")))
                .andExpect(jsonPath("$.budgets[0].amount.value", is(5000.0)))
                .andExpect(jsonPath("$.budgets[0].amount.currency", is("CZK")))
                .andExpect(jsonPath("$.budgets[0].budgetStatus", is("ACTIVE")))
                .andExpect(jsonPath("$.budgets[0].startDate", is("2026-04-01")))
                .andExpect(jsonPath("$.budgets[0].spentAmount.value", is(-10.0)))
                .andExpect(jsonPath("$.budgets[0].spentAmount.currency", is("CZK")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateBudget_ShouldModifyExistingBudget() throws Exception {
        BudgetRequest createRequest = buildBudgetRequest("GROCERIES", 2000.0);
        mockMvc.perform(post("/budgets")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        BudgetEntity savedBudget = budgetJpaRepository.findAll().stream()
                .filter(b -> "GROCERIES".equals(b.getCategory()))
                .findFirst()
                .orElseThrow();
        Integer budgetId = savedBudget.getBudgetId().intValue();

        BudgetRequest updateRequest = buildBudgetRequest("GROCERIES", 3500.0);
        updateRequest.getBudget().setBudgetId(budgetId);

        mockMvc.perform(put("/budgets")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        BudgetEntity updatedBudget = budgetJpaRepository.findById(savedBudget.getBudgetId()).orElseThrow();
        assertThat(updatedBudget.getAmount().doubleValue()).isEqualTo(3500.0);
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteBudget_ShouldRemoveFromDatabase() throws Exception {
        BudgetRequest createRequest = buildBudgetRequest("GROCERIES", 1000.0);
        mockMvc.perform(post("/budgets")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        BudgetEntity savedBudget = budgetJpaRepository.findAll().stream()
                .filter(b -> "GROCERIES".equals(b.getCategory()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/budgets/" + savedBudget.getBudgetId())
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(budgetJpaRepository.count()).isEqualTo(0L);
    }

    private BudgetRequest buildBudgetRequest(String category, double amountValue) {
        Amount amount = new Amount();
        amount.setValue(amountValue);
        amount.setCurrency("CZK");

        Budget budget = new Budget();
        budget.setCategory(category);
        budget.setAmount(amount);
        budget.setStartDate(LocalDate.of(2026, 4, 1));

        BudgetRequest request = new BudgetRequest();
        request.setBudget(budget);
        return request;
    }

    private static TransactionsDomainResponse getTransactionsDomainResponse() {
        Transaction transaction = Transaction.builder()
                .bookingDate(LocalDate.of(2026, 4, 1))
                .amount(cvut.fel.sit.mojefinance.product.domain.entity.Amount.builder().currency("CZK").value(BigDecimal.TEN).build())
                .status(TransactionStatus.BOOKED)
                .category(TransactionCategory.ELECTRONICS)
                .relatedParties(RelatedParties.builder().creditorAccountIban("creditorIban").build())
                .direction(TransactionDirection.OUTCOME)
                .build();

        GroupedTransactions groupedByCategory = GroupedTransactions.builder()
                .transactions(List.of(transaction))
                .totalExpense(cvut.fel.sit.mojefinance.product.domain.entity.Amount.builder()
                        .value(BigDecimal.TEN)
                        .currency("CZK")
                        .build())
                .totalIncome(cvut.fel.sit.mojefinance.product.domain.entity.Amount.builder()
                        .value(BigDecimal.ZERO)
                        .currency("CZK")
                        .build())
                .groupName("Electronics")
                .build();

        GroupedTransactions groupedByMonth = GroupedTransactions.builder()
                .groupedTransactions(List.of(groupedByCategory))
                .groupName("April 2026")
                .build();
        return TransactionsDomainResponse.builder()
                .groupedTransactions(List.of(groupedByMonth))
                .build();
    }
}