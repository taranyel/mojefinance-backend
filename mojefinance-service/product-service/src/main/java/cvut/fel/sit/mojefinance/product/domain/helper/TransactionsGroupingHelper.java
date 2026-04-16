package cvut.fel.sit.mojefinance.product.domain.helper;

import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionDirection;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionStatus;
import cvut.fel.sit.mojefinance.product.domain.entity.GroupedTransactions;
import cvut.fel.sit.shared.entity.TransactionCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static cvut.fel.sit.shared.util.Constants.CZK_CURRENCY_CODE;

@Component
public class TransactionsGroupingHelper {

    public TransactionsDomainResponse groupTransactions(List<Transaction> transactions) {
        Map<YearMonth, Map<TransactionCategory, List<Transaction>>> bookedTransactionsMap = getBookedTransactionsMap(transactions);
        Map<YearMonth, Map<TransactionCategory, List<Transaction>>> pendingTransactionsMap = getPendingTransactionsMap(transactions);

        List<GroupedTransactions> pendingTransactionsMonthlyGroups = pendingTransactionsMap.entrySet().stream()
                .map(this::buildMonthGroup)
                .toList();
        List<GroupedTransactions> result = new ArrayList<>(pendingTransactionsMonthlyGroups);

        List<GroupedTransactions> bookedTransactionsMonthlyGroups = bookedTransactionsMap.entrySet().stream()
                .map(this::buildMonthGroup)
                .toList();
        result.addAll(bookedTransactionsMonthlyGroups);

        return TransactionsDomainResponse.builder()
                .groupedTransactions(result)
                .build();
    }

    private GroupedTransactions buildMonthGroup(Map.Entry<YearMonth, Map<TransactionCategory, List<Transaction>>> monthEntry) {
        YearMonth yearMonth = monthEntry.getKey();
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.getYear();

        List<GroupedTransactions> categoryGroups = monthEntry.getValue().entrySet().stream()
                .map(this::buildCategoryGroup)
                .toList();
        String currency = categoryGroups.isEmpty() ? CZK_CURRENCY_CODE : categoryGroups.get(0).getTotalIncome().getCurrency();

        BigDecimal totalIncomeAmount = getTotalIncomeFromTransactionGroups(categoryGroups);
        BigDecimal totalExpenseAmount = getTotalExpenseFromTransactionGroups(categoryGroups);
        Amount totalIncomeAmountObject = getAmountObject(totalIncomeAmount, currency);
        Amount totalExpenseAmountObject = getAmountObject(totalExpenseAmount, currency);

        return GroupedTransactions.builder()
                .groupName(monthName)
                .totalIncome(totalIncomeAmountObject)
                .totalExpense(totalExpenseAmountObject)
                .groupedTransactions(categoryGroups)
                .build();
    }

    private BigDecimal getTotalIncomeFromTransactionGroups(List<GroupedTransactions> categoryGroups) {
        return categoryGroups.stream().
                map(GroupedTransactions::getTotalIncome)
                .filter(Objects::nonNull)
                .map(Amount::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalExpenseFromTransactionGroups(List<GroupedTransactions> categoryGroups) {
        return categoryGroups.stream().
                map(GroupedTransactions::getTotalExpense)
                .filter(Objects::nonNull)
                .map(Amount::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private GroupedTransactions buildCategoryGroup(Map.Entry<TransactionCategory, List<Transaction>> categoryEntry) {
        TransactionCategory categoryName = categoryEntry.getKey();
        List<Transaction> categoryTransactions = categoryEntry.getValue();

        BigDecimal totalIncomeAmount = getTotalAmountFromTransactionList(categoryTransactions, TransactionDirection.INCOME);
        BigDecimal totalExpenseAmount = getTotalAmountFromTransactionList(categoryTransactions, TransactionDirection.OUTCOME);
        String currency = categoryTransactions.isEmpty() ? CZK_CURRENCY_CODE : categoryTransactions.get(0).getAmount().getCurrency();
        Amount totalIncomeAmountObject = getAmountObject(totalIncomeAmount, currency);
        Amount totalExpenseAmountObject = getAmountObject(totalExpenseAmount, currency);

        return GroupedTransactions.builder()
                .groupName(categoryName.getDisplayName())
                .totalIncome(totalIncomeAmountObject)
                .totalExpense(totalExpenseAmountObject)
                .transactions(categoryTransactions)
                .build();
    }

    private Amount getAmountObject(BigDecimal totalAmount, String currency) {
        return Amount.builder()
                .value(totalAmount)
                .currency(currency)
                .build();
    }

    private BigDecimal getTotalAmountFromTransactionList(List<Transaction> transactions, TransactionDirection direction) {
        return transactions.stream()
                .filter(transaction -> direction.equals(transaction.getDirection()))
                .map(Transaction::getAmount)
                .filter(Objects::nonNull)
                .map(Amount::getValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<YearMonth, Map<TransactionCategory, List<Transaction>>> getBookedTransactionsMap(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getBookingDate() != null && TransactionStatus.BOOKED.equals(t.getStatus()))
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getBookingDate()),
                        () -> new TreeMap<YearMonth, Map<TransactionCategory, List<Transaction>>>(Comparator.reverseOrder()),
                        groupByCategory()
                ));
    }

    private Collector<Transaction, ?, Map<TransactionCategory, List<Transaction>>> groupByCategory() {
        return Collectors.groupingBy(
                t -> t.getCategory() != null ? t.getCategory() : TransactionCategory.UNCATEGORIZED,
                TreeMap::new,
                Collectors.toList()
        );
    }

    private Map<YearMonth, Map<TransactionCategory, List<Transaction>>> getPendingTransactionsMap(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getValueDate() != null && TransactionStatus.PENDING.equals(t.getStatus()))
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getValueDate()),
                        () -> new TreeMap<YearMonth, Map<TransactionCategory, List<Transaction>>>(Comparator.reverseOrder()),
                        groupByCategory()
                ));
    }
}