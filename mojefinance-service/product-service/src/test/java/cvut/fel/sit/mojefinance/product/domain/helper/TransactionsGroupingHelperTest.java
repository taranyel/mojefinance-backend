package cvut.fel.sit.mojefinance.product.domain.helper;

import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.GroupedTransactions;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionDirection;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionStatus;
import cvut.fel.sit.shared.entity.TransactionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static cvut.fel.sit.shared.util.Constants.CZK_CURRENCY_CODE;
import static org.junit.jupiter.api.Assertions.*;

class TransactionsGroupingHelperTest {

    private TransactionsGroupingHelper groupingHelper;

    @BeforeEach
    void setUp() {
        groupingHelper = new TransactionsGroupingHelper();
    }

    @Test
    void groupTransactions_EmptyList_ReturnsEmptyResponse() {
        TransactionsDomainResponse response = groupingHelper.groupTransactions(Collections.emptyList());

        assertNotNull(response);
        assertNotNull(response.getGroupedTransactions());
        assertTrue(response.getGroupedTransactions().isEmpty());
    }

    @Test
    void groupTransactions_CalculatesTotalsAndGroupsCorrectly() {
        // 1. Setup Transactions for a single month (January 2026)
        LocalDate janDate = LocalDate.of(2026, 1, 15);

        Transaction t1 = createTransaction(TransactionStatus.BOOKED, TransactionDirection.OUTCOME, TransactionCategory.GROCERIES, "500", janDate);
        Transaction t2 = createTransaction(TransactionStatus.BOOKED, TransactionDirection.OUTCOME, TransactionCategory.GROCERIES, "200", janDate);
        Transaction t3 = createTransaction(TransactionStatus.BOOKED, TransactionDirection.INCOME, TransactionCategory.EDUCATION, "5000", janDate);

        // 2. Execute
        TransactionsDomainResponse response = groupingHelper.groupTransactions(List.of(t1, t2, t3));

        // 3. Verify Month Level
        List<GroupedTransactions> monthGroups = response.getGroupedTransactions();
        assertEquals(1, monthGroups.size());

        GroupedTransactions janGroup = monthGroups.get(0);
        assertEquals("January 2026", janGroup.getGroupName());
        assertEquals(new BigDecimal("5000"), janGroup.getTotalIncome().getValue());
        assertEquals(new BigDecimal("700"), janGroup.getTotalExpense().getValue()); // 500 + 200

        // 4. Verify Category Level
        List<GroupedTransactions> categoryGroups = janGroup.getGroupedTransactions();
        assertEquals(2, categoryGroups.size()); // Groceries and Salary

        // Categories are sorted alphabetically internally by TreeMap
        GroupedTransactions groceriesGroup = categoryGroups.stream()
                .filter(g -> g.getGroupName().equals(TransactionCategory.GROCERIES.getDisplayName()))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("0"), groceriesGroup.getTotalIncome().getValue());
        assertEquals(new BigDecimal("700"), groceriesGroup.getTotalExpense().getValue());
        assertEquals(2, groceriesGroup.getTransactions().size());

        GroupedTransactions salaryGroup = categoryGroups.stream()
                .filter(g -> g.getGroupName().equals(TransactionCategory.EDUCATION.getDisplayName()))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("5000"), salaryGroup.getTotalIncome().getValue());
        assertEquals(new BigDecimal("0"), salaryGroup.getTotalExpense().getValue());
        assertEquals(1, salaryGroup.getTransactions().size());
    }

    @Test
    void groupTransactions_SortsMonthsInReverseChronologicalOrder() {
        // Setup Booked transactions in Feb 2026 and Jan 2026
        Transaction febTx = createTransaction(TransactionStatus.BOOKED, TransactionDirection.OUTCOME, TransactionCategory.ELECTRONICS, "100", LocalDate.of(2026, 2, 10));
        Transaction janTx = createTransaction(TransactionStatus.BOOKED, TransactionDirection.OUTCOME, TransactionCategory.ELECTRONICS, "100", LocalDate.of(2026, 1, 10));

        // Execute
        TransactionsDomainResponse response = groupingHelper.groupTransactions(List.of(janTx, febTx));

        // Verify ordering (Newest first due to reverseOrder TreeMap)
        List<GroupedTransactions> monthGroups = response.getGroupedTransactions();
        assertEquals(2, monthGroups.size());
        assertEquals("February 2026", monthGroups.get(0).getGroupName());
        assertEquals("January 2026", monthGroups.get(1).getGroupName());
    }

    @Test
    void groupTransactions_SeparatesPendingAndBooked() {
        // Setup Pending (uses valueDate) and Booked (uses bookingDate)
        Transaction pendingTx = createTransaction(TransactionStatus.PENDING, TransactionDirection.OUTCOME, TransactionCategory.CAFE_AND_RESTAURANT, "100", LocalDate.of(2026, 3, 1));
        Transaction bookedTx = createTransaction(TransactionStatus.BOOKED, TransactionDirection.OUTCOME, TransactionCategory.CAFE_AND_RESTAURANT, "100", LocalDate.of(2026, 3, 1));

        // Execute
        TransactionsDomainResponse response = groupingHelper.groupTransactions(List.of(pendingTx, bookedTx));

        // Verify Pending is processed first and appended before Booked
        List<GroupedTransactions> monthGroups = response.getGroupedTransactions();
        assertEquals(2, monthGroups.size());

        // Even though they are the same month, they form two distinct groups in the result
        // because pending and booked are mapped separately in the helper.
        assertEquals("PENDING", monthGroups.get(0).getGroupName());
        assertEquals("March 2026", monthGroups.get(1).getGroupName());

        // The first group should contain our pending transaction
        assertEquals(TransactionStatus.PENDING, monthGroups.get(0).getGroupedTransactions().get(0).getTransactions().get(0).getStatus());
        // The second group should contain our booked transaction
        assertEquals(TransactionStatus.BOOKED, monthGroups.get(1).getGroupedTransactions().get(0).getTransactions().get(0).getStatus());
    }

    @Test
    void groupTransactions_NullCategory_FallsBackToUncategorized() {
        // Transaction with explicitly null category
        Transaction tx = createTransaction(TransactionStatus.BOOKED, TransactionDirection.OUTCOME, null, "100", LocalDate.of(2026, 1, 1));

        TransactionsDomainResponse response = groupingHelper.groupTransactions(List.of(tx));

        List<GroupedTransactions> categoryGroups = response.getGroupedTransactions().get(0).getGroupedTransactions();
        assertEquals(1, categoryGroups.size());
        assertEquals(TransactionCategory.UNCATEGORIZED.getDisplayName(), categoryGroups.get(0).getGroupName());
    }

    @Test
    void groupTransactions_FiltersOutTransactionsWithoutDates() {
        // Setup one valid, and two invalid missing their respective required dates
        Transaction validTx = createTransaction(TransactionStatus.BOOKED, TransactionDirection.OUTCOME, TransactionCategory.FEES_AND_CHARGES, "100", LocalDate.of(2026, 1, 1));

        Transaction invalidBooked = createTransaction(TransactionStatus.BOOKED, TransactionDirection.OUTCOME, TransactionCategory.FEES_AND_CHARGES, "100", null);
        Transaction invalidPending = createTransaction(TransactionStatus.PENDING, TransactionDirection.OUTCOME, TransactionCategory.FEES_AND_CHARGES, "100", null);

        TransactionsDomainResponse response = groupingHelper.groupTransactions(List.of(validTx, invalidBooked, invalidPending));

        // Only the valid transaction should make it through the filters
        List<GroupedTransactions> monthGroups = response.getGroupedTransactions();
        assertEquals(1, monthGroups.size());
        assertEquals(1, monthGroups.get(0).getGroupedTransactions().get(0).getTransactions().size());
    }

    // --- Helper Methods ---

    private Transaction createTransaction(TransactionStatus status, TransactionDirection direction, TransactionCategory category, String amountValue, LocalDate date) {
        Transaction t = new Transaction();
        t.setStatus(status);
        t.setDirection(direction);
        t.setCategory(category);

        Amount amount = new Amount();
        amount.setValue(new BigDecimal(amountValue));
        amount.setCurrency(CZK_CURRENCY_CODE);
        t.setAmount(amount);

        if (TransactionStatus.BOOKED.equals(status)) {
            t.setBookingDate(date);
        } else if (TransactionStatus.PENDING.equals(status)) {
            t.setValueDate(date);
        }

        return t;
    }
}