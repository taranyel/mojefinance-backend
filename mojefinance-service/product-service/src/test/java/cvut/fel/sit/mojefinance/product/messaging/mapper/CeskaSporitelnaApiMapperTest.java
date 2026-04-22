package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.cs.openapi.model.*;
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
class CeskaSporitelnaApiMapperTest {

    private CeskaSporitelnaApiMapper mapper;

    @BeforeEach
    void setUp() {
        // Retrieve the MapStruct-generated implementation
        mapper = Mappers.getMapper(CeskaSporitelnaApiMapper.class);
    }

    @Test
    void toDomainProduct_ShouldMapFieldsCorrectly() {
        // Given
        BankDetails bankDetails = BankDetails.builder()
                .bankName("Česká Spořitelna")
                .clientRegistrationId("ceska-sporitelna")
                .build();

        // Deep stubbing the nested OpenAPI model
        AccountDetail accountMock = mock(AccountDetail.class, Answers.RETURNS_DEEP_STUBS);

        when(accountMock.getId()).thenReturn("acc-789");
        when(accountMock.getIdentification().getIban()).thenReturn("CZ987654321");
        when(accountMock.getIdentification().getOther()).thenReturn("123456789");
        when(accountMock.getNameI18N()).thenReturn("Běžný účet");
        when(accountMock.getProductI18N()).thenReturn("Standard Account");
        when(accountMock.getServicer().getBankCode()).thenReturn("0800");
        when(accountMock.getOwnersNames()).thenReturn(List.of("John Doe"));

        // When
        Product result = mapper.toDomainProduct(accountMock, bankDetails);

        // Then
        assertNotNull(result);
        assertEquals("acc-789", result.getProductId());
        assertEquals("CZ987654321", result.getProductIdentification().getIban());
        assertEquals("123456789", result.getProductIdentification().getProductNumber());
        assertEquals("Běžný účet", result.getAccountName());
        assertEquals("Standard Account", result.getProductName());
        assertFalse(result.getManuallyCreated());
        assertEquals("0800", result.getBankCode());
        assertEquals("Česká Spořitelna", result.getBankDetails().getBankName());
        assertEquals(List.of("John Doe"), result.getOwnersNames());
    }

    @Test
    void getAmountValue_WhenClavTypeAndCredit_ShouldReturnPositiveAmount() {
        // Given
        AccountBalance balanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);

        when(balanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(balanceMock.getAmount().getValue()).thenReturn(new BigDecimal("2500.75"));
        when(balanceMock.getCreditDebitIndicator().name()).thenReturn("CRDT");

        // When
        BigDecimal result = mapper.getAmountValue(List.of(balanceMock));

        // Then
        assertEquals(0, new BigDecimal("2500.75").compareTo(result));
    }

    @Test
    void getAmountValue_WhenClavTypeAndDebit_ShouldReturnNegativeAmount() {
        // Given
        AccountBalance balanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);

        when(balanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(balanceMock.getAmount().getValue()).thenReturn(new BigDecimal("300.00"));
        // DBIT indicator means the value should be negated
        when(balanceMock.getCreditDebitIndicator().name()).thenReturn(DEBIT_INDICATOR);

        // When
        BigDecimal result = mapper.getAmountValue(List.of(balanceMock));

        // Then
        assertEquals(0, new BigDecimal("-300.00").compareTo(result));
    }

    @Test
    void getAmountValue_WhenNoClavType_ShouldFallbackToFirstElement() {
        // Given
        AccountBalance firstBalanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);
        AccountBalance secondBalanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);

        // Simulate a scenario where neither balance is of type CLAV
        when(firstBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("XPCD");
        when(secondBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("ITBD");

        when(firstBalanceMock.getAmount().getValue()).thenReturn(new BigDecimal("1000.00"));
        when(firstBalanceMock.getCreditDebitIndicator().name()).thenReturn("CRDT");

        // When
        BigDecimal result = mapper.getAmountValue(List.of(firstBalanceMock, secondBalanceMock));

        // Then
        // Should take the first element's value since CLAV wasn't found
        assertEquals(0, new BigDecimal("1000.00").compareTo(result));
    }

    @Test
    void getCurrency_ShouldReturnCorrectCurrencyFromClavBalance() {
        // Given
        AccountBalance otherBalanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);
        AccountBalance clavBalanceMock = mock(AccountBalance.class, Answers.RETURNS_DEEP_STUBS);

        when(otherBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("OTHER");

        when(clavBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(clavBalanceMock.getAmount().getCurrency()).thenReturn("EUR");

        // When
        String currency = mapper.getCurrency(List.of(otherBalanceMock, clavBalanceMock));

        // Then
        assertEquals("EUR", currency);
    }
}