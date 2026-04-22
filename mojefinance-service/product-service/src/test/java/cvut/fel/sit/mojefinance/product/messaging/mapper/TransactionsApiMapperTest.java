package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.mojefinance.product.domain.entity.TransactionDirection;
import cvut.fel.sit.mojefinance.product.domain.entity.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class TransactionsApiMapperTest {

    private TransactionsApiMapper mapper;

    @BeforeEach
    void setUp() {
        // Instantiate an anonymous class to test the default methods directly
        mapper = new TransactionsApiMapper() {};
    }

    // --- mapTransactionDirection Tests ---

    @Test
    void mapTransactionDirection_WhenCRDT_ShouldReturnIncome() {
        assertEquals(TransactionDirection.INCOME, mapper.mapTransactionDirection("CRDT"));
        assertEquals(TransactionDirection.INCOME, mapper.mapTransactionDirection("crdt")); // tests toUpperCase()
    }

    @Test
    void mapTransactionDirection_WhenDBIT_ShouldReturnOutcome() {
        assertEquals(TransactionDirection.OUTCOME, mapper.mapTransactionDirection("DBIT"));
        assertEquals(TransactionDirection.OUTCOME, mapper.mapTransactionDirection("dbit")); // tests toUpperCase()
    }

    @Test
    void mapTransactionDirection_WhenNull_ShouldReturnNull() {
        assertNull(mapper.mapTransactionDirection(null));
    }

    @Test
    void mapTransactionDirection_WhenUnknown_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.mapTransactionDirection("UNKNOWN")
        );
        assertEquals("Unknown credit/debit indicator from bank: UNKNOWN", exception.getMessage());
    }

    // --- mapTransactionStatus Tests ---

    @Test
    void mapTransactionStatus_WhenBOOK_ShouldReturnBooked() {
        assertEquals(TransactionStatus.BOOKED, mapper.mapTransactionStatus("BOOK"));
        assertEquals(TransactionStatus.BOOKED, mapper.mapTransactionStatus("book")); // tests toUpperCase()
    }

    @Test
    void mapTransactionStatus_WhenPDNG_ShouldReturnPending() {
        assertEquals(TransactionStatus.PENDING, mapper.mapTransactionStatus("PDNG"));
        assertEquals(TransactionStatus.PENDING, mapper.mapTransactionStatus("pdng")); // tests toUpperCase()
    }

    @Test
    void mapTransactionStatus_WhenNull_ShouldReturnNull() {
        assertNull(mapper.mapTransactionStatus(null));
    }

    @Test
    void mapTransactionStatus_WhenUnknown_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.mapTransactionStatus("PROCESSING")
        );
        // Note: Your original code has a slight copy-paste typo in the exception message for status ("Unknown credit/debit indicator...").
        // This test expects the exact message currently in your source code.
        assertEquals("Unknown credit/debit indicator from bank: PROCESSING", exception.getMessage());
    }

    // --- mapDateFromOffsetDateTime Tests ---

    @Test
    void mapDateFromOffsetDateTime_ShouldExtractLocalDate() {
        // Given
        OffsetDateTime offsetDateTime = OffsetDateTime.of(2026, 4, 22, 18, 45, 0, 0, ZoneOffset.UTC);

        // When
        LocalDate result = mapper.mapDateFromOffsetDateTime(offsetDateTime);

        // Then
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(4, result.getMonthValue());
        assertEquals(22, result.getDayOfMonth());
    }

    @Test
    void mapDateFromOffsetDateTime_WhenNull_ShouldReturnNull() {
        assertNull(mapper.mapDateFromOffsetDateTime(null));
    }
}