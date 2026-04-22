package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.csob.accounts.openapi.model.AccountInfo;
import cvut.fel.sit.csob.balances.openapi.model.BalanceInfo;
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
class CSOBApiMapperTest {

    private CSOBApiMapper mapper;

    @BeforeEach
    void setUp() {
        // Retrieve the MapStruct-generated implementation
        mapper = Mappers.getMapper(CSOBApiMapper.class);
    }

    @Test
    void toDomainProduct_ShouldMapFieldsCorrectly() {
        // Given
        BankDetails bankDetails = BankDetails.builder()
                .bankName("ČSOB")
                .clientRegistrationId("csob")
                .build();

        // Deep stubbing the nested OpenAPI model
        AccountInfo accountMock = mock(AccountInfo.class, Answers.RETURNS_DEEP_STUBS);

        when(accountMock.getId()).thenReturn("acc-999");
        when(accountMock.getIdentification().getIban()).thenReturn("CZ1122334455");
        when(accountMock.getNameI18N()).thenReturn("Běžný účet ČSOB");
        when(accountMock.getProductI18N()).thenReturn("Plus Konto");
        when(accountMock.getServicer().getBankCode()).thenReturn("0300");
        when(accountMock.getOwnersNames()).thenReturn(List.of("John Doe"));

        // When
        Product result = mapper.toDomainProduct(accountMock, bankDetails);

        // Then
        assertNotNull(result);
        assertEquals("acc-999", result.getProductId());
        assertEquals("CZ1122334455", result.getProductIdentification().getIban());
        assertEquals("Běžný účet ČSOB", result.getAccountName());
        assertEquals("Plus Konto", result.getProductName());
        assertFalse(result.getManuallyCreated());
        assertEquals("0300", result.getBankCode());
        assertEquals("ČSOB", result.getBankDetails().getBankName());
        assertEquals(List.of("John Doe"), result.getOwnersNames());
    }

    @Test
    void getAmountValue_WhenClavTypeAndCredit_ShouldReturnPositiveAmount() {
        // Given
        BalanceInfo balanceMock = mock(BalanceInfo.class, Answers.RETURNS_DEEP_STUBS);

        when(balanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(balanceMock.getAmount().getValue()).thenReturn(new BigDecimal("5000.50"));
        when(balanceMock.getCreditDebitIndicator().name()).thenReturn("CRDT");

        // When
        BigDecimal result = mapper.getAmountValue(List.of(balanceMock));

        // Then
        assertEquals(0, new BigDecimal("5000.50").compareTo(result));
    }

    @Test
    void getAmountValue_WhenClavTypeAndDebit_ShouldReturnNegativeAmount() {
        // Given
        BalanceInfo balanceMock = mock(BalanceInfo.class, Answers.RETURNS_DEEP_STUBS);

        when(balanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(balanceMock.getAmount().getValue()).thenReturn(new BigDecimal("150.00"));
        // DBIT indicator means the value should be negated
        when(balanceMock.getCreditDebitIndicator().name()).thenReturn(DEBIT_INDICATOR);

        // When
        BigDecimal result = mapper.getAmountValue(List.of(balanceMock));

        // Then
        assertEquals(0, new BigDecimal("-150.00").compareTo(result));
    }

    @Test
    void getAmountValue_WhenNoClavType_ShouldFallbackToFirstElement() {
        // Given
        BalanceInfo firstBalanceMock = mock(BalanceInfo.class, Answers.RETURNS_DEEP_STUBS);
        BalanceInfo secondBalanceMock = mock(BalanceInfo.class, Answers.RETURNS_DEEP_STUBS);

        // Simulate a scenario where neither balance is of type CLAV
        when(firstBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("ITBD");
        when(secondBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("XPCD");

        when(firstBalanceMock.getAmount().getValue()).thenReturn(new BigDecimal("1200.00"));
        when(firstBalanceMock.getCreditDebitIndicator().name()).thenReturn("CRDT");

        // When
        BigDecimal result = mapper.getAmountValue(List.of(firstBalanceMock, secondBalanceMock));

        // Then
        // Should take the first element's value since CLAV wasn't found
        assertEquals(0, new BigDecimal("1200.00").compareTo(result));
    }

    @Test
    void getCurrency_ShouldReturnCorrectCurrencyFromClavBalance() {
        // Given
        BalanceInfo otherBalanceMock = mock(BalanceInfo.class, Answers.RETURNS_DEEP_STUBS);
        BalanceInfo clavBalanceMock = mock(BalanceInfo.class, Answers.RETURNS_DEEP_STUBS);

        when(otherBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn("OTHER");

        when(clavBalanceMock.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(clavBalanceMock.getAmount().getCurrency()).thenReturn("USD");

        // When
        String currency = mapper.getCurrency(List.of(otherBalanceMock, clavBalanceMock));

        // Then
        assertEquals("USD", currency);
    }
}