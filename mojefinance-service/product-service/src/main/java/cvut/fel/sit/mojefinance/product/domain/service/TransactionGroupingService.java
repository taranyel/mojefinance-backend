package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.dto.TransactionsDomainResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static cvut.fel.sit.mojefinance.product.domain.entity.TransactionCategory.UNCATEGORIZED;

@Service
public class TransactionGroupingService {

    public TransactionsDomainResponse groupTransactions(List<Transaction> transactions) {
        Map<YearMonth, Map<String, List<Transaction>>> bookedTransactionsMap = getBookedTransactionsMap(transactions);
        Map<YearMonth, Map<String, List<Transaction>>> pendingTransactionsMap = getPendingTransactionsMap(transactions);

        List<TransactionsGroupedByMonth> pendingTransactionsMonthlyGroups = pendingTransactionsMap.entrySet().stream()
                .map(this::buildMonthGroup)
                .toList();
        List<TransactionsGroupedByMonth> result = new ArrayList<>(pendingTransactionsMonthlyGroups);

        List<TransactionsGroupedByMonth> bookedTransactionsMonthlyGroups = bookedTransactionsMap.entrySet().stream()
                .map(this::buildMonthGroup)
                .toList();
        result.addAll(bookedTransactionsMonthlyGroups);

        return TransactionsDomainResponse.builder()
                .groupedByMonth(result)
                .build();
    }

    private TransactionsGroupedByMonth buildMonthGroup(Map.Entry<YearMonth, Map<String, List<Transaction>>> monthEntry) {
        YearMonth yearMonth = monthEntry.getKey();
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.getYear();

        List<TransactionsGroupedByCategory> categoryGroups = monthEntry.getValue().entrySet().stream()
                .map(this::buildCategoryGroup)
                .toList();

        return TransactionsGroupedByMonth.builder()
                .groupName(monthName)
                .groupedByCategory(categoryGroups)
                .build();
    }

    private TransactionsGroupedByCategory buildCategoryGroup(Map.Entry<String, List<Transaction>> categoryEntry) {
        String categoryName = categoryEntry.getKey();
        List<Transaction> categoryTransactions = categoryEntry.getValue();

        BigDecimal total = getTotalGroupAmount(categoryTransactions);
        String currency = categoryTransactions.isEmpty() ? "CZK" : categoryTransactions.get(0).getAmount().getCurrency();

        Amount totalAmount = Amount.builder()
                .value(total)
                .currency(currency)
                .build();

        return TransactionsGroupedByCategory.builder()
                .groupName(categoryName)
                .totalAmount(totalAmount)
                .transactions(categoryTransactions)
                .build();
    }

    private BigDecimal getTotalGroupAmount(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getAmount)
                .filter(Objects::nonNull)
                .map(Amount::getValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<YearMonth, Map<String, List<Transaction>>> getBookedTransactionsMap(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getBookingDate() != null && TransactionStatus.BOOKED.equals(t.getStatus()))
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getBookingDate()),
                        () -> new TreeMap<YearMonth, Map<String, List<Transaction>>>(Comparator.reverseOrder()),
                        groupByCategory()
                ));
    }

    private Collector<Transaction, ?, Map<String, List<Transaction>>> groupByCategory() {
        return Collectors.groupingBy(
                t -> t.getCategory() != null ? t.getCategory().name() : UNCATEGORIZED.name(),
                TreeMap::new,
                Collectors.toList()
        );
    }

    private Map<YearMonth, Map<String, List<Transaction>>> getPendingTransactionsMap(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getValueDate() != null && TransactionStatus.PENDING.equals(t.getStatus()))
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getValueDate()),
                        () -> new TreeMap<YearMonth, Map<String, List<Transaction>>>(Comparator.reverseOrder()),
                        groupByCategory()
                ));
    }
}