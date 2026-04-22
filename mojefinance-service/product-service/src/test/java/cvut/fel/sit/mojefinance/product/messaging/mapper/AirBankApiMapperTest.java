package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.airbank.openapi.model.*;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static cvut.fel.sit.shared.util.Constants.CLAV_TYPE_CODE;
import static cvut.fel.sit.shared.util.Constants.DEBIT_INDICATOR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirBankApiMapperTest {

    // Instantiate the MapStruct generated implementation
    private AirBankApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(AirBankApiMapper.class);
    }

    @Test
    void toDomainProduct_ShouldMapFieldsCorrectly() {
        // Given
        BankDetails bankDetails = BankDetails.builder()
                .bankName("Air Bank")
                .clientRegistrationId("air-bank")
                .build();

        // Using deep stubs to avoid massive boilerplate for nested OpenAPI models
        AccountListAccountsInner accountMock = mock(AccountListAccountsInner.class, Answers.RETURNS_DEEP_STUBS);

        when(accountMock.getId()).thenReturn("acc-123");
        when(accountMock.getIdentification().getIban()).thenReturn("CZ123456789");
        when(accountMock.getIdentification().getOther()).thenReturn("987654321");
        when(accountMock.getNameI18N()).thenReturn("My Checking Account");
        when(accountMock.getProductI18N()).thenReturn("Everyday Account");
        when(accountMock.getServicer().getBankCode()).thenReturn("3030");
        when(accountMock.getOwnersNames()).thenReturn(List.of("John Doe"));

        // When
        Product result = mapper.toDomainProduct(accountMock, bankDetails);

        // Then
        assertNotNull(result);
        assertEquals("acc-123", result.getProductId());
        assertEquals("CZ123456789", result.getProductIdentification().getIban());
        assertEquals("987654321", result.getProductIdentification().getProductNumber());
        assertEquals("My Checking Account", result.getAccountName());
        assertEquals("Everyday Account", result.getProductName());
        assertFalse(result.getManuallyCreated());
        assertEquals("3030", result.getBankCode());
        assertEquals("Air Bank", result.getBankDetails().getBankName());
        assertEquals(List.of("John Doe"), result.getOwnersNames());
    }

    @Test
    void getAmountValue_WhenClavTypeAndCredit_ShouldReturnPositiveAmount() {
        // Given
        BalanceListBalancesInner balanceMock = mock(BalanceListBalancesInner.class, Answers.RETURNS_DEEP_STUBS);

        when(balanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(balanceMock.getAmount().getValue()).thenReturn(new BigDecimal("1500.50"));
        when(balanceMock.getCreditDebitIndicator().name()).thenReturn("CRDT");

        // When
        BigDecimal result = mapper.getAmountValue(List.of(balanceMock));

        // Then
        assertEquals(0, new BigDecimal("1500.50").compareTo(result));
    }

    @Test
    void getAmountValue_WhenClavTypeAndDebit_ShouldReturnNegativeAmount() {
        // Given
        BalanceListBalancesInner balanceMock = mock(BalanceListBalancesInner.class, Answers.RETURNS_DEEP_STUBS);

        when(balanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(balanceMock.getAmount().getValue()).thenReturn(new BigDecimal("500.00"));
        when(balanceMock.getCreditDebitIndicator().name()).thenReturn(DEBIT_INDICATOR);

        // When
        BigDecimal result = mapper.getAmountValue(List.of(balanceMock));

        // Then
        assertEquals(0, new BigDecimal("-500.00").compareTo(result));
    }

    @Test
    void getAmountValue_WhenNoClavType_ShouldFallbackToFirstElement() {
        // Given
        BalanceListBalancesInner firstBalanceMock = mock(BalanceListBalancesInner.class, Answers.RETURNS_DEEP_STUBS);
        BalanceListBalancesInner secondBalanceMock = mock(BalanceListBalancesInner.class, Answers.RETURNS_DEEP_STUBS);

        // Neither is CLAV
        when(firstBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("OTHER1");
        when(secondBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("OTHER2");

        when(firstBalanceMock.getAmount().getValue()).thenReturn(new BigDecimal("100.00"));
        when(firstBalanceMock.getCreditDebitIndicator().name()).thenReturn("CRDT");

        // When
        BigDecimal result = mapper.getAmountValue(List.of(firstBalanceMock, secondBalanceMock));

        // Then
        assertEquals(0, new BigDecimal("100.00").compareTo(result));
    }

    @Test
    void getCurrency_ShouldReturnCorrectCurrencyFromClavBalance() {
        // Given
        BalanceListBalancesInner otherBalanceMock = mock(BalanceListBalancesInner.class, Answers.RETURNS_DEEP_STUBS);
        BalanceListBalancesInner clavBalanceMock = mock(BalanceListBalancesInner.class, Answers.RETURNS_DEEP_STUBS);

        when(otherBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("OTHER");

        when(clavBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(clavBalanceMock.getAmount().getCurrency()).thenReturn("CZK");

        // When
        String currency = mapper.getCurrency(List.of(otherBalanceMock, clavBalanceMock));

        // Then
        assertEquals("CZK", currency);
    }
}