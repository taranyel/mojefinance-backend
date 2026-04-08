package cvut.fel.sit.mojefinance.categorization.messaging.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class GeminiProviderImpl implements GeminiProvider {
    private static final String GEMINI_MODEL_NAME = "gemini-3.1-flash-lite-preview";
    private static final String EMPTY_STRING = "";

    @Value("${external.api.google.apikey}")
    private String geminiApiKey;

    @Override
    public String askGemini(String prompt) {
        log.info("Asking Gemini with prompt: {}", prompt);

        GenerateContentResponse response;
        try {
            Client client = Client.builder()
                    .apiKey(geminiApiKey)
                    .build();

            response = client.models.generateContent(
                    GEMINI_MODEL_NAME,
                    prompt,
                    null);
        } catch (Exception e) {
            log.error("Error while asking Gemini.", e);
            return EMPTY_STRING;
        }
        log.info("Received response from Gemini: {}", response.text());
        return Objects.requireNonNull(response.text()).trim();
    }
}
