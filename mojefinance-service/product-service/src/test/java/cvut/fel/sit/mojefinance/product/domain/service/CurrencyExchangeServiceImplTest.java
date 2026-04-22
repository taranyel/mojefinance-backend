package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.mojefinance.product.messaging.service.ExternalApiProvider;
import cvut.fel.sit.shared.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeServiceImplTest {

    @Mock
    private ExternalApiProvider externalApiProvider;

    @InjectMocks
    private CurrencyExchangeServiceImpl currencyExchangeService;

    @Test
    void exchangeAmount_ShouldExchangeSuccessfully() {
        // Given
        Amount inputAmount = Amount.builder()
                .value(BigDecimal.valueOf(100))
                .currency("EUR")
                .build();

        // Assuming an exchange rate of 25.5 CZK per EUR
        when(externalApiProvider.getExchangeRates("EUR")).thenReturn(BigDecimal.valueOf(25.5));

        // When
        Amount result = currencyExchangeService.exchangeAmount(inputAmount);

        // Then
        assertNotNull(result);
        // 100 * 25.5 = 2550.0
        assertEquals(0, BigDecimal.valueOf(2550.0).compareTo(result.getValue()));
        assertEquals(Constants.CZK_CURRENCY_CODE, result.getCurrency());

        verify(externalApiProvider, times(1)).getExchangeRates("EUR");
    }

    @Test
    void exchangeAmount_WhenAmountIsNull_ShouldThrowIllegalArgumentException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> currencyExchangeService.exchangeAmount(null)
        );

        assertEquals("Amount must not be null.", exception.getMessage());
        verifyNoInteractions(externalApiProvider);
    }

    @Test
    void exchangeAmount_WhenAmountValueIsNull_ShouldThrowIllegalArgumentException() {
        // Given
        Amount inputAmount = Amount.builder()
                .value(null)
                .currency("EUR")
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> currencyExchangeService.exchangeAmount(inputAmount)
        );

        assertEquals("Amount must not be null.", exception.getMessage());
        verifyNoInteractions(externalApiProvider);
    }

    @Test
    void exchangeAmount_ShouldHandleZeroValueCorrectly() {
        // Given
        Amount inputAmount = Amount.builder()
                .value(BigDecimal.ZERO)
                .currency("USD")
                .build();

        when(externalApiProvider.getExchangeRates("USD")).thenReturn(BigDecimal.valueOf(23.1));

        // When
        Amount result = currencyExchangeService.exchangeAmount(inputAmount);

        // Then
        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getValue()));
        assertEquals(Constants.CZK_CURRENCY_CODE, result.getCurrency());
    }
}