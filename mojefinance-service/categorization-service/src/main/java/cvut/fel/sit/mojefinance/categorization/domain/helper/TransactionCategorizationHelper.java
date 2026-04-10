package cvut.fel.sit.mojefinance.categorization.domain.helper;

import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionMappingEntity;
import cvut.fel.sit.mojefinance.categorization.data.repository.TransactionMappingRepository;
import cvut.fel.sit.mojefinance.categorization.messaging.service.GeminiProvider;
import cvut.fel.sit.shared.util.entity.TransactionCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCategorizationHelper {
    private final GeminiProvider geminiProvider;
    private final TransactionMappingRepository transactionMappingRepository;
    private static final List<TransactionCategory> ALL_TRANSACTION_CATEGORIES = List.of(
            TransactionCategory.GROCERIES,
            TransactionCategory.CAFE_AND_RESTAURANT,
            TransactionCategory.UTILITIES,
            TransactionCategory.TRANSPORTATION,
            TransactionCategory.MEDICAL_CARE,
            TransactionCategory.PHARMACY,
            TransactionCategory.HEALTH_AND_BEAUTY,
            TransactionCategory.SPORTS_AND_FITNESS,
            TransactionCategory.SHOPPING,
            TransactionCategory.ELECTRONICS,
            TransactionCategory.ENTERTAINMENT,
            TransactionCategory.EDUCATION,
            TransactionCategory.FEES_AND_CHARGES,
            TransactionCategory.OTHER_INCOME,
            TransactionCategory.OTHER_EXPENSE
    );

    public Map<String, TransactionCategory> mapExistingTransactionMappings(List<TransactionMappingEntity> transactionDataMappings) {
        return transactionDataMappings.stream()
                .collect(Collectors.toMap(
                        TransactionMappingEntity::getTransactionName,
                        mapping -> TransactionCategory.valueOf(mapping.getTransactionCategory().getCategoryName())
                ));
    }

    public Map<String, TransactionCategory> saveNewTransactionMappings(Set<String> unmappedTransactions) {
        Map<String, TransactionCategory> transactionMappings = new HashMap<>();

        for (String unmappedTransactionName : unmappedTransactions) {
            String prompt = buildPrompt(unmappedTransactionName);
            String geminiResponse = geminiProvider.askGemini(prompt);
            TransactionCategory mappedCategory = getTransactionCategory(unmappedTransactionName, geminiResponse);

            TransactionCategoryEntity transactionCategoryDataEntity = transactionMappingRepository
                    .getTransactionCategoryByCategoryName(mappedCategory.name());

            TransactionMappingEntity mappingToSave = TransactionMappingEntity.builder()
                    .transactionCategory(transactionCategoryDataEntity)
                    .transactionName(unmappedTransactionName)
                    .build();

            transactionMappingRepository.addTransactionMapping(mappingToSave);
            transactionMappings.put(unmappedTransactionName, mappedCategory);
        }
        return transactionMappings;
    }

    private TransactionCategory getTransactionCategory(String unmappedTransactionName, String geminiResponse) {
        TransactionCategory mappedCategory = TransactionCategory.UNCATEGORIZED;
        try {
            mappedCategory = TransactionCategory.valueOf(geminiResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to categorize transaction: {}. Response from Gemini: {}", unmappedTransactionName, geminiResponse, e);
        }
        return mappedCategory;
    }

    private String buildPrompt(String unmappedTransactionName) {
        return String.format(
                "Categorize this bank transaction based on counterparty name and transaction direction: '%s'. " +
                        "Respond ONLY with exactly one of the following exact categories, with no quotes, no markdown, and no extra text: " +
                        ALL_TRANSACTION_CATEGORIES,
                unmappedTransactionName
        );
    }

    public Set<String> filterUnmappedTransactions(Set<String> requestedTransactionNames, Map<String, TransactionCategory> existingTransactionMappings) {
        return requestedTransactionNames.stream()
                .filter(requestedTransactionName -> existingTransactionMappings.keySet().stream()
                        .noneMatch(existingTransactionName -> existingTransactionName.equals(requestedTransactionName)))
                .collect(Collectors.toSet());
    }

    public List<TransactionMappingEntity> getAllTransactionMappingsByTransactionNames(Set<String> requestedTransactionNames) {
        return transactionMappingRepository
                .getAllTransactionMappingsByTransactionNames(requestedTransactionNames);
    }
}
