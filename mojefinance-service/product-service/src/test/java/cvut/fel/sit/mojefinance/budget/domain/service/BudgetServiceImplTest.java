package cvut.fel.sit.mojefinance.budget.domain.service;

import cvut.fel.sit.mojefinance.budget.data.entity.BudgetEntity;
import cvut.fel.sit.mojefinance.budget.data.repository.BudgetRepository;
import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetRequest;
import cvut.fel.sit.mojefinance.budget.domain.dto.BudgetsResponse;
import cvut.fel.sit.mojefinance.budget.domain.entity.Budget;
import cvut.fel.sit.mojefinance.budget.domain.entity.BudgetStatus;
import cvut.fel.sit.mojefinance.budget.domain.helper.BudgetHelper;
import cvut.fel.sit.mojefinance.budget.domain.mapper.BudgetDomainMapper;
import cvut.fel.sit.shared.entity.TransactionCategory;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetDomainMapper budgetDomainMapper;

    @Mock
    private BudgetHelper budgetHelper;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private final String PRINCIPAL_NAME = "testUser";

    @BeforeEach
    void setUpSecurityContext() {
        // Mock the Spring Security Context to provide a principal name
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
    void createBudget_ShouldCreateAndSaveBudget() {
        // Arrange
        Budget budget = new Budget();
        budget.setCategory(TransactionCategory.GROCERIES);
        budget.setStartDate(LocalDate.now());

        BudgetRequest request = BudgetRequest.builder().budget(budget).build();
        BudgetEntity mappedEntity = new BudgetEntity();

        doNothing().when(budgetHelper).validateBudgetRequest(request);
        when(budgetHelper.budgetForCategoryExists(PRINCIPAL_NAME, budget)).thenReturn(false);
        doNothing().when(budgetHelper).validateStartDate(budget);
        when(budgetDomainMapper.toBudgetEntity(budget)).thenReturn(mappedEntity);

        // Act
        budgetService.createBudget(request);

        // Assert
        assertEquals(BudgetStatus.ACTIVE, budget.getBudgetStatus());
        assertEquals(PRINCIPAL_NAME, mappedEntity.getPrincipalName());

        verify(budgetRepository, times(1)).saveBudget(mappedEntity, PRINCIPAL_NAME);
    }

    @Test
    void createBudget_WhenCategoryExists_ShouldThrowException() {
        // Arrange
        Budget budget = new Budget();
        BudgetRequest request = BudgetRequest.builder().budget(budget).build();

        doNothing().when(budgetHelper).validateBudgetRequest(request);
        when(budgetHelper.budgetForCategoryExists(PRINCIPAL_NAME, budget)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> budgetService.createBudget(request));

        assertEquals("Budget for this category already exists.", exception.getMessage());
        verify(budgetRepository, never()).saveBudget(any(), any());
    }

    @Test
    void updateBudget_ShouldUpdateAndSaveBudget() {
        // Arrange
        Budget inputBudget = new Budget();
        inputBudget.setBudgetId(1L);
        inputBudget.setCategory(TransactionCategory.ENTERTAINMENT);
        BudgetRequest request = BudgetRequest.builder().budget(inputBudget).build();

        Budget existingBudget = new Budget();
        BudgetEntity mappedEntity = new BudgetEntity();

        doNothing().when(budgetHelper).validateBudgetRequest(request);
        when(budgetRepository.getBudgetById(1L)).thenReturn(existingBudget);
        doNothing().when(budgetHelper).validateExistingBudget(existingBudget, PRINCIPAL_NAME);
        doNothing().when(budgetHelper).updateExistingBudget(existingBudget, inputBudget);
        when(budgetDomainMapper.toBudgetEntity(existingBudget)).thenReturn(mappedEntity);

        // Act
        budgetService.updateBudget(request);

        // Assert
        verify(budgetRepository, times(1)).saveBudget(mappedEntity, PRINCIPAL_NAME);
    }

    @Test
    void updateBudget_WhenIdIsNull_ShouldThrowException() {
        // Arrange
        Budget inputBudget = new Budget();
        inputBudget.setBudgetId(null); // Explicitly null
        BudgetRequest request = BudgetRequest.builder().budget(inputBudget).build();

        doNothing().when(budgetHelper).validateBudgetRequest(request);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> budgetService.updateBudget(request));

        assertEquals("Budget ID must be provided for update.", exception.getMessage());
        verify(budgetRepository, never()).getBudgetById(any());
    }

    @Test
    void getBudgets_ShouldRetrieveAndEnhanceBudgets() {
        // Arrange
        BudgetsResponse mockResponse = BudgetsResponse.builder()
                .budgets(List.of(new Budget()))
                .build();

        when(budgetRepository.getBudgets(PRINCIPAL_NAME)).thenReturn(mockResponse);
        doNothing().when(budgetHelper).updateBudgetStartDate(mockResponse);
        doNothing().when(budgetHelper).calculateSpentAmount(mockResponse);

        // Act
        BudgetsResponse result = budgetService.getBudgets();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getBudgets().size());
        verify(budgetHelper, times(1)).updateBudgetStartDate(mockResponse);
        verify(budgetHelper, times(1)).calculateSpentAmount(mockResponse);
    }

    @Test
    void deleteBudget_ShouldValidateAndDelete() {
        // Arrange
        Long budgetId = 10L;
        Budget existingBudget = new Budget();

        when(budgetRepository.getBudgetById(budgetId)).thenReturn(existingBudget);
        doNothing().when(budgetHelper).validateExistingBudget(existingBudget, PRINCIPAL_NAME);

        // Act
        budgetService.deleteBudget(budgetId);

        // Assert
        verify(budgetRepository, times(1)).deleteBudget(budgetId, PRINCIPAL_NAME);
    }
}