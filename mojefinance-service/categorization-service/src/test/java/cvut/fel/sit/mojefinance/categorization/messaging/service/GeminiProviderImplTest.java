package cvut.fel.sit.mojefinance.categorization.messaging.service;

import com.google.genai.Models;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class GeminiProviderImplTest {
    @InjectMocks
    private GeminiProviderImpl geminiProviderImpl;

    @Mock
    private Client client;
    @Mock
    private GenerateContentResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(geminiProviderImpl, "geminiApiKey", "dummy-key");
    }

    @Test
    void askGemini_returnsTrimmedTextOnSuccess() {
        String prompt = "test prompt";
        String expected = "result";
        Client.Builder builder = mock(Client.Builder.class);
        when(builder.apiKey(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(client);

        Models models = mock(Models.class);
        GeminiProviderImpl spyProvider = spy(geminiProviderImpl);
        doReturn(models).when(spyProvider).getModels(any(Client.class));
        when(models.generateContent(anyString(), anyString(), isNull())).thenReturn(response);
        when(response.text()).thenReturn("  result  ");
        try (MockedStatic<Client> mockedStatic = mockStatic(Client.class)) {
            mockedStatic.when(Client::builder).thenReturn(builder);
            String result = spyProvider.askGemini(prompt);
            assertEquals(expected, result);
        }
    }

    @Test
    void askGemini_returnsEmptyStringOnException() {
        String prompt = "test prompt";
        try (MockedStatic<Client> mockedStatic = mockStatic(Client.class)) {
            mockedStatic.when(Client::builder).thenThrow(new RuntimeException("fail"));
            String result = geminiProviderImpl.askGemini(prompt);
            assertEquals("", result);
        }
    }
}
