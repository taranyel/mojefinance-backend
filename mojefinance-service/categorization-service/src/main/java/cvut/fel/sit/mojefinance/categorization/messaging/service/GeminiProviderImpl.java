package cvut.fel.sit.mojefinance.categorization.messaging.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import cvut.fel.sit.mojefinance.categorization.domain.entity.TransactionCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class GeminiProviderImpl implements GeminiProvider {
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
            TransactionCategory.OTHER
    );
    private static final String GEMINI_MODEL_NAME = "gemini-3.1-flash-lite-preview";

    @Value("${google.api.key}")
    private String geminiApiKey;

    @Override
    public TransactionCategory categorizeTransactionWithAI(String transactionName) {
        GenerateContentResponse response;

        try {
            Client client = Client.builder()
                    .apiKey(geminiApiKey)
                    .build();

            String prompt = String.format(
                    "Categorize this bank transaction based on counterparty name and transaction direction: '%s'. " +
                            "Respond ONLY with exactly one of the following exact categories, with no quotes, no markdown, and no extra text: " +
                            ALL_TRANSACTION_CATEGORIES,
                    transactionName
            );

            response = client.models.generateContent(
                    GEMINI_MODEL_NAME,
                    prompt,
                    null);
        } catch (Exception e) {
            log.error("Error while categorizing transaction with Gemini.", e);
            return TransactionCategory.OTHER;
        }

        String apiResponse = Objects.requireNonNull(response.text()).trim();
        log.info("Gemini categorized '{}' as: {}", transactionName, apiResponse);

        TransactionCategory transactionCategory = TransactionCategory.OTHER;
        try {
            transactionCategory = TransactionCategory.valueOf(apiResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to categorize transaction: {}. Response from Gemini: {}", transactionName, response.text(), e);
        }
        return transactionCategory;
    }
}
