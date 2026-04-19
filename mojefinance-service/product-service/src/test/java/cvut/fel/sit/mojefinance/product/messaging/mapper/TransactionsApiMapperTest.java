package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.airbank.openapi.model.TransactionList;
import cvut.fel.sit.airbank.openapi.model.TransactionListTransactionsInner;
import cvut.fel.sit.cs.openapi.model.MyAccountsIdTransactionsGet200Response;
import cvut.fel.sit.csob.transactions.openapi.model.GetTransactionHistoryRes;
import cvut.fel.sit.kb.openapi.model.GeAccountTransactionsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Transaction;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionDirection;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionStatus;
import cvut.fel.sit.mojefinance.product.messaging.dto.TransactionsMessagingResponse;
import cvut.fel.sit.reif.openapi.model.GetTransactionList200Response;
import cvut.fel.sit.reif.openapi.model.GetTransactionList200ResponseTransactionsInner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionsApiMapperTest {

    private TransactionsApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionsApiMapperImpl();
    }

    @Test
    void toTransactionsResponse_NullInputs_ShouldReturnNull() {
        assertNull(mapper.toTransactionsResponse((TransactionList) null));
        assertNull(mapper.toTransactionsResponse((MyAccountsIdTransactionsGet200Response) null));
        assertNull(mapper.toTransactionsResponse((GetTransactionHistoryRes) null));
        assertNull(mapper.toTransactionsResponse((GeAccountTransactionsResponse) null));
        assertNull(mapper.toTransactionsResponse((GetTransactionList200Response) null));
    }

    @Test
    void customMappers_ShouldMapDirectionsAndStatusesCorrectly() {
        // Test mapTransactionDirection
        assertEquals(TransactionDirection.INCOME, mapper.mapTransactionDirection("CRDT"));
        assertEquals(TransactionDirection.OUTCOME, mapper.mapTransactionDirection("DBIT"));
        assertThrows(IllegalArgumentException.class, () -> mapper.mapTransactionDirection("UNKNOWN"));

        // Test mapTransactionStatus
        assertEquals(TransactionStatus.BOOKED, mapper.mapTransactionStatus("BOOK"));
        assertEquals(TransactionStatus.PENDING, mapper.mapTransactionStatus("PDNG"));
        assertThrows(IllegalArgumentException.class, () -> mapper.mapTransactionStatus("UNKNOWN"));

        // Test Date Mapping
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2026-04-19T10:15:30+02:00");
        assertEquals(LocalDate.of(2026, 4, 19), mapper.mapDateFromOffsetDateTime(offsetDateTime));
    }

    @Test
    void toTransactionsResponse_AirBank_ShouldMapDeeplyNestedFieldsCorrectly() {
        // Arrange
        TransactionList response = mock(TransactionList.class);
        TransactionListTransactionsInner apiTx = mock(TransactionListTransactionsInner.class, RETURNS_DEEP_STUBS);

        // We return the real enums because Mockito cannot mock enums
        when(apiTx.getCreditDebitIndicator()).thenReturn(TransactionListTransactionsInner.CreditDebitIndicatorEnum.CRDT);
        when(apiTx.getStatus()).thenReturn(TransactionListTransactionsInner.StatusEnum.BOOK);

        when(apiTx.getBookingDate().getDate()).thenReturn("2026-04-19");
        when(apiTx.getAmount().getValue()).thenReturn(new BigDecimal("1500.50"));
        when(apiTx.getAmount().getCurrency()).thenReturn("CZK");

        // Deep stubbing the nested RelatedParties tree
        when(apiTx.getEntryDetails().getTransactionDetails().getRelatedParties().getDebtor().getName()).thenReturn("John Doe");
        when(apiTx.getEntryDetails().getTransactionDetails().getRelatedParties().getDebtorAccount().getIdentification().getIban()).thenReturn("CZ1234567890");

        when(response.getTransactions()).thenReturn(List.of(apiTx));

        // Act
        TransactionsMessagingResponse domainResponse = mapper.toTransactionsResponse(response);

        // Assert
        assertNotNull(domainResponse);
        assertEquals(1, domainResponse.getTransactions().size());

        Transaction domainTx = domainResponse.getTransactions().get(0);
        assertEquals(TransactionDirection.INCOME, domainTx.getDirection());
        assertEquals(TransactionStatus.BOOKED, domainTx.getStatus());
        assertEquals(LocalDate.of(2026, 4, 19), domainTx.getBookingDate());
        assertEquals(new BigDecimal("1500.50"), domainTx.getAmount().getValue());
        assertEquals("CZK", domainTx.getAmount().getCurrency());

        // Assert Nested Parties
        assertNotNull(domainTx.getRelatedParties());
        assertEquals("John Doe", domainTx.getRelatedParties().getDebtorName());
        assertEquals("CZ1234567890", domainTx.getRelatedParties().getDebtorAccountIban());
    }

    @Test
    void toTransactionsResponse_KB_ShouldMapDoubleAmountCorrectly() {
        // Arrange
        GeAccountTransactionsResponse response = mock(GeAccountTransactionsResponse.class);
        cvut.fel.sit.kb.openapi.model.AccountTransaction apiTx = mock(cvut.fel.sit.kb.openapi.model.AccountTransaction.class, RETURNS_DEEP_STUBS);

        cvut.fel.sit.kb.openapi.model.CreditDebitIndicator indicator = mock(cvut.fel.sit.kb.openapi.model.CreditDebitIndicator.class);
        when(indicator.getValue()).thenReturn("DBIT");
        when(apiTx.getCreditDebitIndicator()).thenReturn(indicator);

        when(apiTx.getStatus()).thenReturn("PDNG"); // KB passes status as a direct String
        when(apiTx.getBookingDate().getDate()).thenReturn("2026-04-20");

        // KB uses Double for values instead of BigDecimal
        when(apiTx.getAmount().getValue()).thenReturn(250.75);
        when(apiTx.getAmount().getCurrency()).thenReturn("EUR");

        when(response.getTransactions()).thenReturn(List.of(apiTx));

        // Act
        TransactionsMessagingResponse domainResponse = mapper.toTransactionsResponse(response);

        // Assert
        Transaction domainTx = domainResponse.getTransactions().get(0);
        assertEquals(TransactionDirection.OUTCOME, domainTx.getDirection());
        assertEquals(TransactionStatus.PENDING, domainTx.getStatus());
        assertEquals(new BigDecimal("250.75"), domainTx.getAmount().getValue()); // Verifies Double -> BigDecimal mapping
        assertEquals("EUR", domainTx.getAmount().getCurrency());
    }

    @Test
    void toTransactionsResponse_Reiffeisen_ShouldMapOffsetDateTimeAndConstants() {
        // Arrange
        GetTransactionList200Response response = mock(GetTransactionList200Response.class);
        GetTransactionList200ResponseTransactionsInner apiTx = mock(GetTransactionList200ResponseTransactionsInner.class, RETURNS_DEEP_STUBS);

        when(apiTx.getCreditDebitIndication()).thenReturn(GetTransactionList200ResponseTransactionsInner.CreditDebitIndicationEnum.CRDT);
        when(apiTx.getBookingDate()).thenReturn(OffsetDateTime.parse("2026-04-21T08:30:00Z"));
        when(apiTx.getValueDate()).thenReturn(OffsetDateTime.parse("2026-04-21T08:30:00Z"));
        when(apiTx.getAmount().getValue()).thenReturn(100.0);
        when(apiTx.getEntryDetails().getTransactionDetails().getRelatedParties().getCounterParty().getName()).thenReturn("Netflix");
        when(apiTx.getEntryDetails().getTransactionDetails().getRelatedParties().getCounterParty().getAccount().getIban()).thenReturn("IE12BOFI90001122334455");
        when(response.getTransactions()).thenReturn(List.of(apiTx));

        // Act
        TransactionsMessagingResponse domainResponse = mapper.toTransactionsResponse(response);

        // Assert
        Transaction domainTx = domainResponse.getTransactions().get(0);
        assertEquals(TransactionDirection.INCOME, domainTx.getDirection());
        assertEquals(TransactionStatus.BOOKED, domainTx.getStatus());
        assertEquals(LocalDate.of(2026, 4, 21), domainTx.getBookingDate());
        assertEquals(LocalDate.of(2026, 4, 21), domainTx.getValueDate()); // Optional: verify value date too

        // Assert CounterParty mapping
        assertEquals("Netflix", domainTx.getCounterpartyName());
        assertEquals("IE12BOFI90001122334455", domainTx.getRelatedParties().getCreditorAccountIban());
        assertEquals("IE12BOFI90001122334455", domainTx.getRelatedParties().getDebtorAccountIban());
    }
}