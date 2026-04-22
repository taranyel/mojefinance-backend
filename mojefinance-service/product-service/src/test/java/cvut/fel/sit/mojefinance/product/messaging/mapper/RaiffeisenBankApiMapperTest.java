package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.reif.openapi.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static cvut.fel.sit.shared.util.Constants.CLAV_TYPE_CODE;
import static cvut.fel.sit.shared.util.Constants.CZK_CURRENCY_CODE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaiffeisenBankApiMapperTest {

    private RaiffeisenBankApiMapper mapper;

    @BeforeEach
    void setUp() {
        // Retrieve the MapStruct-generated implementation
        mapper = Mappers.getMapper(RaiffeisenBankApiMapper.class);
    }

    @Test
    void toDomainProduct_ShouldMapFieldsCorrectly() {
        // Given
        BankDetails bankDetails = BankDetails.builder()
                .bankName("Raiffeisen Bank")
                .clientRegistrationId("raiffeisen-bank")
                .build();

        // Mock the nested OpenAPI account object
        GetAccounts200ResponseAccountsInner accountMock = mock(GetAccounts200ResponseAccountsInner.class, Answers.RETURNS_DEEP_STUBS);

        when(accountMock.getAccountId()).thenReturn(1);
        when(accountMock.getIban()).thenReturn("CZ5500000000000000000000");
        when(accountMock.getAccountNumber()).thenReturn("123456789/5500");
        when(accountMock.getFriendlyName()).thenReturn("Chytrý účet");
        when(accountMock.getMainCurrency()).thenReturn("CZK");

        // When
        Product result = mapper.toDomainProduct(accountMock, bankDetails);

        // Then
        assertNotNull(result);
        assertEquals("1", result.getProductId());
        assertEquals("CZ5500000000000000000000", result.getProductIdentification().getIban());
        assertEquals("123456789/5500", result.getProductIdentification().getProductNumber());
        assertEquals("Chytrý účet", result.getProductName());
        assertEquals("CZK", result.getCurrency());
        assertFalse(result.getManuallyCreated());
        assertEquals("Raiffeisen Bank", result.getBankDetails().getBankName());
    }

    @Test
    void getAmountValue_WhenCZKFolderAndClavType_ShouldReturnAmount() {
        // Given
        GetBalance200ResponseCurrencyFoldersInner czkFolder = mock(GetBalance200ResponseCurrencyFoldersInner.class, Answers.RETURNS_DEEP_STUBS);
        when(czkFolder.getCurrency()).thenReturn(CZK_CURRENCY_CODE);

        GetBalance200ResponseCurrencyFoldersInnerBalancesInner nonClavBalance = mock(GetBalance200ResponseCurrencyFoldersInnerBalancesInner.class, Answers.RETURNS_DEEP_STUBS);
        when(nonClavBalance.getBalanceType()).thenReturn("ITBD");

        GetBalance200ResponseCurrencyFoldersInnerBalancesInner clavBalance = mock(GetBalance200ResponseCurrencyFoldersInnerBalancesInner.class, Answers.RETURNS_DEEP_STUBS);
        when(clavBalance.getBalanceType()).thenReturn(CLAV_TYPE_CODE);
        // Raiffeisen API uses Double for value
        when(clavBalance.getValue()).thenReturn(15500.75);

        when(czkFolder.getBalances()).thenReturn(List.of(nonClavBalance, clavBalance));

        // Adding an extra non-CZK folder to ensure it gets filtered out
        GetBalance200ResponseCurrencyFoldersInner eurFolder = mock(GetBalance200ResponseCurrencyFoldersInner.class, Answers.RETURNS_DEEP_STUBS);
        when(eurFolder.getCurrency()).thenReturn("EUR");

        // When
        BigDecimal result = mapper.getAmountValue(List.of(eurFolder, czkFolder));

        // Then
        assertEquals(0, new BigDecimal("15500.75").compareTo(result));
    }

    @Test
    void getAmountValue_WhenCZKFolderButNoClavType_ShouldFallbackToFirstBalance() {
        // Given
        GetBalance200ResponseCurrencyFoldersInner czkFolder = mock(GetBalance200ResponseCurrencyFoldersInner.class, Answers.RETURNS_DEEP_STUBS);
        when(czkFolder.getCurrency()).thenReturn(CZK_CURRENCY_CODE);

        GetBalance200ResponseCurrencyFoldersInnerBalancesInner firstBalance = mock(GetBalance200ResponseCurrencyFoldersInnerBalancesInner.class, Answers.RETURNS_DEEP_STUBS);
        when(firstBalance.getBalanceType()).thenReturn("XPCD");
        when(firstBalance.getValue()).thenReturn(2000.0);

        GetBalance200ResponseCurrencyFoldersInnerBalancesInner secondBalance = mock(GetBalance200ResponseCurrencyFoldersInnerBalancesInner.class, Answers.RETURNS_DEEP_STUBS);
        when(secondBalance.getBalanceType()).thenReturn("ITBD");

        // Neither is CLAV
        when(czkFolder.getBalances()).thenReturn(List.of(firstBalance, secondBalance));

        // When
        BigDecimal result = mapper.getAmountValue(List.of(czkFolder));

        // Then
        // Should fallback to the first balance in the list
        assertEquals(0, new BigDecimal("2000.0").compareTo(result));
    }

    @Test
    void getCurrency_ShouldReturnCorrectCurrencyFromClavBalanceInCZKFolder() {
        // Given
        GetBalance200ResponseCurrencyFoldersInner czkFolder = mock(GetBalance200ResponseCurrencyFoldersInner.class, Answers.RETURNS_DEEP_STUBS);
        when(czkFolder.getCurrency()).thenReturn(CZK_CURRENCY_CODE);

        GetBalance200ResponseCurrencyFoldersInnerBalancesInner clavBalance = mock(GetBalance200ResponseCurrencyFoldersInnerBalancesInner.class, Answers.RETURNS_DEEP_STUBS);
        when(clavBalance.getBalanceType()).thenReturn(CLAV_TYPE_CODE);
        when(clavBalance.getCurrency()).thenReturn(CZK_CURRENCY_CODE);

        when(czkFolder.getBalances()).thenReturn(List.of(clavBalance));

        // When
        String currency = mapper.getCurrency(List.of(czkFolder));

        // Then
        assertEquals(CZK_CURRENCY_CODE, currency);
    }
}