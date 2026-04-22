package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.kb.openapi.model.*;
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
class KBApiMapperTest {

    private KBApiMapper mapper;

    @BeforeEach
    void setUp() {
        // Retrieve the MapStruct-generated implementation
        mapper = Mappers.getMapper(KBApiMapper.class);
    }

    @Test
    void toDomainProduct_ShouldMapFieldsCorrectly() {
        // Given
        BankDetails bankDetails = BankDetails.builder()
                .bankName("Komerční banka")
                .clientRegistrationId("kb")
                .build();

        // Deep stubbing the nested OpenAPI model
        Account accountMock = mock(Account.class, Answers.RETURNS_DEEP_STUBS);

        when(accountMock.getId()).thenReturn("kb-acc-001");
        when(accountMock.getIdentification().getIban()).thenReturn("CZ0101000000000000000000");
        when(accountMock.getIdentification().getOther()).thenReturn("1234567890/0100");
        when(accountMock.getNameI18N()).thenReturn("MůjÚčet");
        when(accountMock.getProductI18N()).thenReturn("MůjÚčet Plus");
        when(accountMock.getServicer().getBankCode()).thenReturn("0100");
        when(accountMock.getOwnersNames()).thenReturn(List.of("John Doe"));

        // When
        Product result = mapper.toDomainProduct(accountMock, bankDetails);

        // Then
        assertNotNull(result);
        assertEquals("kb-acc-001", result.getProductId());
        assertEquals("CZ0101000000000000000000", result.getProductIdentification().getIban());
        assertEquals("1234567890/0100", result.getProductIdentification().getProductNumber());
        assertEquals("MůjÚčet", result.getAccountName());
        assertEquals("MůjÚčet Plus", result.getProductName());
        assertFalse(result.getManuallyCreated());
        assertEquals("0100", result.getBankCode());
        assertEquals("Komerční banka", result.getBankDetails().getBankName());
        assertEquals(List.of("John Doe"), result.getOwnersNames());
    }

    @Test
    void getAmountValue_WhenClavTypeAndCredit_ShouldReturnPositiveAmount() {
        // Given
        AccountBalance balanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);

        when(balanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        // KB OpenAPI model uses a Double/Float for amount value
        when(balanceMock.getAmount().getValue()).thenReturn(10500.50);
        when(balanceMock.getCreditDebitIndicator().name()).thenReturn("CRDT");

        // When
        BigDecimal result = mapper.getAmountValue(List.of(balanceMock));

        // Then
        assertEquals(0, new BigDecimal("10500.5").compareTo(result));
    }

    @Test
    void getAmountValue_WhenClavTypeAndDebit_ShouldReturnNegativeAmount() {
        // Given
        AccountBalance balanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);

        when(balanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(balanceMock.getAmount().getValue()).thenReturn(350.00);
        when(balanceMock.getCreditDebitIndicator().name()).thenReturn(DEBIT_INDICATOR);

        // When
        BigDecimal result = mapper.getAmountValue(List.of(balanceMock));

        // Then
        assertEquals(0, new BigDecimal("-350.0").compareTo(result));
    }

    @Test
    void getAmountValue_WhenNoClavType_ShouldFallbackToFirstElement() {
        // Given
        AccountBalance firstBalanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);
        AccountBalance secondBalanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);

        // Simulate neither balance being of type CLAV
        when(firstBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("XPCD");
        when(secondBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("ITBD");

        when(firstBalanceMock.getAmount().getValue()).thenReturn(1200.0);
        when(firstBalanceMock.getCreditDebitIndicator().name()).thenReturn("CRDT");

        // When
        BigDecimal result = mapper.getAmountValue(List.of(firstBalanceMock, secondBalanceMock));

        // Then
        // Should take the first element's value
        assertEquals(0, new BigDecimal("1200.0").compareTo(result));
    }

    @Test
    void getCurrency_ShouldReturnCorrectCurrencyFromClavBalance() {
        // Given
        AccountBalance otherBalanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);
        AccountBalance clavBalanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);

        when(otherBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("OTHER");

        when(clavBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(clavBalanceMock.getAmount().getCurrency()).thenReturn("CZK");

        // When
        String currency = mapper.getCurrency(List.of(otherBalanceMock, clavBalanceMock));

        // Then
        assertEquals("CZK", currency);
    }
}