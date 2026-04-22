package cvut.fel.sit.mojefinance.product.messaging.service;

import cvut.fel.sit.er.openapi.model.ExchangeRate;
import cvut.fel.sit.mojefinance.product.messaging.client.ExchangeRatesApiFeignClient;
import cvut.fel.sit.shared.exception.ServiceException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRatesApiProviderTest {

    @Mock
    private ExchangeRatesApiFeignClient exchangeRatesApiFeignClient;

    @InjectMocks
    private ExchangeRatesApiProvider exchangeRatesApiProvider;

    private static final String API_KEY = "test-exchange-api-key";
    private static final String CURRENCY = "EUR";

    @BeforeEach
    void setUp() {
        // Inject the @Value annotated field manually for unit testing
        ReflectionTestUtils.setField(exchangeRatesApiProvider, "exchangeRatesApiKey", API_KEY);
    }

    @Test
    void fetchExchangeRate_ShouldReturnCashBuyRate_WhenApiCallIsSuccessful() {
        // Given
        BigDecimal expectedRate = new BigDecimal("25.50");
        ExchangeRate mockExchangeRate = new ExchangeRate();
        mockExchangeRate.setCashBuy(expectedRate);

        when(exchangeRatesApiFeignClient.getExchangeRates(anyString(), eq(API_KEY), eq(CURRENCY)))
                .thenReturn(ResponseEntity.ok(mockExchangeRate));

        // When
        BigDecimal actualRate = exchangeRatesApiProvider.fetchExchangeRate(CURRENCY);

        // Then
        assertNotNull(actualRate);
        assertEquals(expectedRate, actualRate);
        verify(exchangeRatesApiFeignClient, times(1))
                .getExchangeRates(anyString(), eq(API_KEY), eq(CURRENCY));
    }

    @Test
    void fetchExchangeRate_ShouldThrowServiceException_WhenResponseHasNoBody() {
        // Given
        when(exchangeRatesApiFeignClient.getExchangeRates(anyString(), eq(API_KEY), eq(CURRENCY)))
                .thenReturn(ResponseEntity.ok().build()); // 200 OK, but no body

        // When & Then
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> exchangeRatesApiProvider.fetchExchangeRate(CURRENCY)
        );

        assertTrue(exception.getMessage().contains("Failed to fetch exchange rates from KB API"));
    }

    @Test
    void fetchExchangeRate_ShouldThrowServiceException_WhenResponseIsNotSuccessful() {
        // Given
        when(exchangeRatesApiFeignClient.getExchangeRates(anyString(), eq(API_KEY), eq(CURRENCY)))
                .thenReturn(ResponseEntity.badRequest().build()); // 400 Bad Request

        // When & Then
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> exchangeRatesApiProvider.fetchExchangeRate(CURRENCY)
        );

        assertTrue(exception.getMessage().contains("Failed to fetch exchange rates from KB API"));
        assertTrue(exception.getMessage().contains("400 BAD_REQUEST"));
    }

    @Test
    void fetchExchangeRate_ShouldThrowServiceException_WhenFeignExceptionIsThrown() {
        // Given
        FeignException mockFeignException = mock(FeignException.class);
        when(mockFeignException.getMessage()).thenReturn("Connection timed out");

        when(exchangeRatesApiFeignClient.getExchangeRates(anyString(), anyString(), anyString()))
                .thenThrow(mockFeignException);

        // When & Then
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> exchangeRatesApiProvider.fetchExchangeRate(CURRENCY)
        );

        assertTrue(exception.getMessage().contains("Error calling KB API: Connection timed out"));
        assertEquals(mockFeignException, exception.getCause());
    }
}