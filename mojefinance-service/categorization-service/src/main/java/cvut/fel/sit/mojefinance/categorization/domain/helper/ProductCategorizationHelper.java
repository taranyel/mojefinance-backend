package cvut.fel.sit.mojefinance.categorization.domain.helper;

import cvut.fel.sit.mojefinance.categorization.data.entity.ProductCategoryEntity;
import cvut.fel.sit.mojefinance.categorization.data.entity.ProductMappingEntity;
import cvut.fel.sit.mojefinance.categorization.data.repository.ProductMappingRepository;
import cvut.fel.sit.mojefinance.categorization.messaging.service.GeminiProvider;
import cvut.fel.sit.shared.util.entity.ProductCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductCategorizationHelper {
    private final ProductMappingRepository productMappingRepository;
    private final GeminiProvider geminiProvider;
    private static final List<ProductCategory> ALL_PRODUCT_CATEGORIES = List.of(
            ProductCategory.CHECKING_ACCOUNT,
            ProductCategory.SAVINGS_ACCOUNT,
            ProductCategory.SHORT_TERM_DEPOSIT,
            ProductCategory.CREDIT_CARD,
            ProductCategory.LOAN,
            ProductCategory.INVESTMENT,
            ProductCategory.INSURANCE,
            ProductCategory.MORTGAGE,
            ProductCategory.PENSION,
            ProductCategory.OTHER
    );

    public List<ProductMappingEntity> getAllProductMappingsByProductNames(Set<String> requestedTransactionsNames) {
        return productMappingRepository
                .getAllProductMappingsByProductNames(requestedTransactionsNames);
    }

    public Map<String, ProductCategory> mapExistingProductMappings(List<ProductMappingEntity> productDataMappings) {
        return productDataMappings.stream()
                .collect(Collectors.toMap(
                        ProductMappingEntity::getProductName,
                        mapping -> ProductCategory.valueOf(mapping.getProductCategory().getCategoryName())
                ));
    }

    public Set<String> filterUnmappedProducts(Set<String> requestedProductNames, Map<String, ProductCategory> existingProductMappings) {
        return requestedProductNames.stream()
                .filter(requestedProductName -> existingProductMappings.keySet().stream()
                        .noneMatch(existingProductName -> existingProductName.equals(requestedProductName)))
                .collect(Collectors.toSet());
    }

    public Map<String, ProductCategory> saveNewProductMappings(Set<String> unmappedProducts) {
        Map<String, ProductCategory> productMappings = new HashMap<>();

        for (String unmappedProductName : unmappedProducts) {
            String prompt = buildPrompt(unmappedProductName);
            String geminiResponse = geminiProvider.askGemini(prompt);
            ProductCategory mappedCategory = getProductCategory(unmappedProductName, geminiResponse);

            ProductCategoryEntity productCategoryDataEntity = productMappingRepository
                    .getProductCategoryByCategoryName(mappedCategory.name());

            ProductMappingEntity mappingToSave = ProductMappingEntity.builder()
                    .productCategory(productCategoryDataEntity)
                    .productName(unmappedProductName)
                    .build();

            productMappingRepository.addProductMapping(mappingToSave);
            productMappings.put(unmappedProductName, mappedCategory);
        }
        return productMappings;
    }

    private String buildPrompt(String unmappedTransactionName) {
        return String.format(
                "Categorize this bank product based on product name: '%s'. " +
                        "Respond ONLY with exactly one of the following exact categories, with no quotes, no markdown, and no extra text: " +
                        ALL_PRODUCT_CATEGORIES,
                unmappedTransactionName
        );
    }

    private ProductCategory getProductCategory(String unmappedProductName, String geminiResponse) {
        ProductCategory mappedCategory = ProductCategory.OTHER;
        try {
            mappedCategory = ProductCategory.valueOf(geminiResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to categorize product: {}. Response from Gemini: {}", unmappedProductName, geminiResponse, e);
        }
        return mappedCategory;
    }
}
