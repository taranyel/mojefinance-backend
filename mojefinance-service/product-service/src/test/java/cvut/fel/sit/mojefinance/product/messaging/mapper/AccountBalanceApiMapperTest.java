package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.airbank.openapi.model.BalanceList;
import cvut.fel.sit.cs.openapi.model.MyAccountsIdBalanceGet200Response;
import cvut.fel.sit.csob.balances.openapi.model.GetAccountBalanceRes;
import cvut.fel.sit.kb.openapi.model.GetAccountBalanceResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Amount;
import cvut.fel.sit.reif.openapi.model.GetBalance200Response;
import cvut.fel.sit.reif.openapi.model.GetBalance200ResponseCurrencyFoldersInner;
import cvut.fel.sit.reif.openapi.model.GetBalance200ResponseCurrencyFoldersInnerBalancesInner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static cvut.fel.sit.shared.util.Constants.CLAV_TYPE_CODE;
import static cvut.fel.sit.shared.util.Constants.CZK_CURRENCY_CODE;
import static cvut.fel.sit.shared.util.Constants.DEBIT_INDICATOR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountBalanceApiMapperTest {

    private final AccountBalanceApiMapper mapper = new AccountBalanceApiMapper();

    @Test
    void toDomainBalance_NullResponses_ShouldReturnNull() {
        assertNull(mapper.toDomainBalance((BalanceList) null));
        assertNull(mapper.toDomainBalance((MyAccountsIdBalanceGet200Response) null));
        assertNull(mapper.toDomainBalance((GetAccountBalanceRes) null));
        assertNull(mapper.toDomainBalance((GetAccountBalanceResponse) null));
        assertNull(mapper.toDomainBalance((GetBalance200Response) null));
    }

    @Test
    void toDomainBalance_AirBank_ShouldMapCreditCorrectly() {
        BalanceList response = mock(BalanceList.class);
        cvut.fel.sit.airbank.openapi.model.BalanceListBalancesInner balance =
                mock(cvut.fel.sit.airbank.openapi.model.BalanceListBalancesInner.class, RETURNS_DEEP_STUBS);

        when(balance.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(balance.getAmount().getValue()).thenReturn(new BigDecimal("1500.50"));
        when(balance.getAmount().getCurrency()).thenReturn(CZK_CURRENCY_CODE);
        when(balance.getCreditDebitIndicator().name()).thenReturn("CRDT");

        when(response.getBalances()).thenReturn(List.of(balance));

        Amount amount = mapper.toDomainBalance(response);

        assertNotNull(amount);
        assertEquals(new BigDecimal("1500.50"), amount.getValue());
        assertEquals(CZK_CURRENCY_CODE, amount.getCurrency());
    }

    @Test
    void toDomainBalance_CS_ShouldMapDebitCorrectly() {
        MyAccountsIdBalanceGet200Response response = mock(MyAccountsIdBalanceGet200Response.class);
        cvut.fel.sit.cs.openapi.model.AccountBalance balance =
                mock(cvut.fel.sit.cs.openapi.model.AccountBalance.class, RETURNS_DEEP_STUBS);

        when(balance.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(balance.getAmount().getValue()).thenReturn(new BigDecimal("2000.00"));
        when(balance.getAmount().getCurrency()).thenReturn(CZK_CURRENCY_CODE);
        when(balance.getCreditDebitIndicator().name()).thenReturn(DEBIT_INDICATOR);
        when(response.getBalances()).thenReturn(List.of(balance));

        Amount amount = mapper.toDomainBalance(response);

        assertNotNull(amount);
        assertEquals(new BigDecimal("-2000.00"), amount.getValue());
        assertEquals(CZK_CURRENCY_CODE, amount.getCurrency());
    }

    @Test
    void toDomainBalance_CSOB_FallbackToFirstBalanceIfClavMissing() {
        GetAccountBalanceRes response = mock(GetAccountBalanceRes.class);
        cvut.fel.sit.csob.balances.openapi.model.BalanceInfo balance =
                mock(cvut.fel.sit.csob.balances.openapi.model.BalanceInfo.class, RETURNS_DEEP_STUBS);

        // Simulate a balance that is NOT the Closing Available (CLAV) code
        when(balance.getType().getCodeOrProprietary().getCode().name()).thenReturn("OTHER_CODE");
        when(balance.getAmount().getValue()).thenReturn(new BigDecimal("3000.00"));
        when(balance.getAmount().getCurrency()).thenReturn("EUR");
        when(balance.getCreditDebitIndicator().name()).thenReturn("CRDT");

        when(response.getBalances()).thenReturn(List.of(balance));

        // It should fall back to the first available balance in the list
        Amount amount = mapper.toDomainBalance(response);

        assertNotNull(amount);
        assertEquals(new BigDecimal("3000.00"), amount.getValue());
        assertEquals("EUR", amount.getCurrency());
    }

    @Test
    void toDomainBalance_KB_ShouldMapCorrectly() {
        GetAccountBalanceResponse response = mock(GetAccountBalanceResponse.class);
        cvut.fel.sit.kb.openapi.model.AccountBalance balance =
                mock(cvut.fel.sit.kb.openapi.model.AccountBalance.class, RETURNS_DEEP_STUBS);

        when(balance.getType().getCodeOrProprietary().getCode().name()).thenReturn(CLAV_TYPE_CODE);
        when(balance.getAmount().getValue()).thenReturn(4000.00); // KB returns double/float
        when(balance.getAmount().getCurrency()).thenReturn(CZK_CURRENCY_CODE);
        when(balance.getCreditDebitIndicator().name()).thenReturn("CRDT");

        when(response.getBalances()).thenReturn(List.of(balance));

        Amount amount = mapper.toDomainBalance(response);

        assertNotNull(amount);
        assertEquals(new BigDecimal("4000.0"), amount.getValue());
        assertEquals(CZK_CURRENCY_CODE, amount.getCurrency());
    }

    @Test
    void toDomainBalance_Reiffeisen_ShouldMapFromCzkFolder() {
        GetBalance200Response response = mock(GetBalance200Response.class);
        GetBalance200ResponseCurrencyFoldersInner folder = mock(GetBalance200ResponseCurrencyFoldersInner.class);
        GetBalance200ResponseCurrencyFoldersInnerBalancesInner balance =
                mock(GetBalance200ResponseCurrencyFoldersInnerBalancesInner.class);

        when(folder.getCurrency()).thenReturn(CZK_CURRENCY_CODE);
        when(balance.getBalanceType()).thenReturn(CLAV_TYPE_CODE);
        when(balance.getValue()).thenReturn(5000.0);
        when(balance.getCurrency()).thenReturn(CZK_CURRENCY_CODE);

        when(folder.getBalances()).thenReturn(List.of(balance));
        when(response.getCurrencyFolders()).thenReturn(List.of(folder));

        Amount amount = mapper.toDomainBalance(response);

        assertNotNull(amount);
        assertEquals(new BigDecimal("5000.0"), amount.getValue());
        assertEquals(CZK_CURRENCY_CODE, amount.getCurrency());
    }

    @Test
    void toDomainBalance_Reiffeisen_NoCzkFolder_ShouldReturnNull() {
        GetBalance200Response response = mock(GetBalance200Response.class);
        GetBalance200ResponseCurrencyFoldersInner folder = mock(GetBalance200ResponseCurrencyFoldersInner.class);

        // Only EUR folder exists
        when(folder.getCurrency()).thenReturn("EUR");
        when(response.getCurrencyFolders()).thenReturn(List.of(folder));

        Amount amount = mapper.toDomainBalance(response);

        assertNull(amount);
    }

    @Test
    void extractBalanceTemplate_EmptyBalancesList_ShouldReturnNull() {
        GetAccountBalanceRes response = mock(GetAccountBalanceRes.class);
        when(response.getBalances()).thenReturn(Collections.emptyList());

        Amount amount = mapper.toDomainBalance(response);

        assertNull(amount);
    }
}