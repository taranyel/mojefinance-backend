package cvut.fel.sit.mojefinance.categorization;

import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.TransactionMappingEntity;
import cvut.fel.sit.mojefinance.categorization.data.repository.TransactionMappingRepository;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsRequest;
import cvut.fel.sit.mojefinance.categorization.domain.dto.CategorizeTransactionsResponse;
import cvut.fel.sit.mojefinance.categorization.domain.entity.TransactionCategory;
import cvut.fel.sit.mojefinance.categorization.messaging.service.GeminiProviderImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CategorizationServiceImpl implements CategorizationService {
    private final TransactionMappingRepository transactionMappingRepository;
    private final GeminiProviderImpl geminiProvider;

    @Override
    public CategorizeTransactionsResponse categorizeTransactions(CategorizeTransactionsRequest categorizeTransactionsRequest) {
        Set<String> requestedTransactionsNames = categorizeTransactionsRequest.getTransactionsNames();
        List<TransactionMappingEntity> transactionDataMappings = transactionMappingRepository
                .getAllTransactionMappingsByTransactionsNames(requestedTransactionsNames);

        Map<String, TransactionCategory> existingTransactionMappings = mapExistingTransactionMappings(transactionDataMappings);

        Set<String> unmappedTransactions = filterUnmappedTransactions(requestedTransactionsNames, existingTransactionMappings);
        Map<String, TransactionCategory> savedTransactionMappings = saveNewTransactionMappings(unmappedTransactions);
        existingTransactionMappings.putAll(savedTransactionMappings);

        return CategorizeTransactionsResponse.builder()
                .categorizedTransactions(existingTransactionMappings)
                .build();
    }

    private Map<String, TransactionCategory> mapExistingTransactionMappings(List<TransactionMappingEntity> transactionDataMappings) {
        return transactionDataMappings.stream()
                .collect(Collectors.toMap(
                        TransactionMappingEntity::getTransactionName,
                        mapping -> TransactionCategory.valueOf(mapping.getTransactionCategory().getCategoryName())
                ));
    }

    private Map<String, TransactionCategory> saveNewTransactionMappings(Set<String> unmappedTransactions) {
        Map<String, TransactionCategory> transactionMappings = new HashMap<>();

        for (String unmappedTransactionName : unmappedTransactions) {
            TransactionCategory mappedCategory = geminiProvider.categorizeTransactionWithAI(unmappedTransactionName);
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

    private Set<String> filterUnmappedTransactions(Set<String> requestedTransactionsNames, Map<String, TransactionCategory> existingTransactionMappings) {
        return requestedTransactionsNames.stream()
                .filter(requestedTransactionName -> existingTransactionMappings.keySet().stream()
                        .noneMatch(existingTransactionName -> existingTransactionName.equals(requestedTransactionName)))
                .collect(Collectors.toSet());
    }
}
