package cvut.fel.sit.mojefinance.bank.data.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionId;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;

class BankConnectionDataMapperTest {
    private BankConnectionDataMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(BankConnectionDataMapper.class);
    }

    @Test
    void toBankConnection_mapsFieldsCorrectly() {
        BankConnectionId id = new BankConnectionId();
        id.setClientRegistrationId("reg123");
        id.setPrincipalName("user1");
        BankConnectionEntity entity = BankConnectionEntity.builder()
            .id(id)
            .bankName("Test Bank")
            .bankConnectionStatus("CONNECTED")
            .manuallyCreated(true)
            .build();
        BankConnection domain = mapper.toBankConnection(entity);
        assertNotNull(domain);
        assertEquals("reg123", domain.getClientRegistrationId());
        assertEquals("Test Bank", domain.getBankName());
        assertEquals(BankConnectionStatus.CONNECTED, domain.getBankConnectionStatus());
        assertTrue(domain.getManuallyCreated());
    }

    @Test
    void toBankConnection_nullEntityReturnsNull() {
        assertNull(mapper.toBankConnection(null));
    }

    @Test
    void toBankConnection_nullIdReturnsNullClientRegistrationId() {
        BankConnectionEntity entity = BankConnectionEntity.builder()
            .id(null)
            .bankName("Bank")
            .bankConnectionStatus("DISCONNECTED")
            .manuallyCreated(false)
            .build();
        BankConnection domain = mapper.toBankConnection(entity);
        assertNotNull(domain);
        assertNull(domain.getClientRegistrationId());
        assertEquals(BankConnectionStatus.DISCONNECTED, domain.getBankConnectionStatus());
        assertEquals("Bank", domain.getBankName());
        assertFalse(domain.getManuallyCreated());
    }

    @Test
    void toBankConnection_invalidStatusThrowsException() {
        BankConnectionId id = new BankConnectionId();
        id.setClientRegistrationId("regX");
        id.setPrincipalName("userX");
        BankConnectionEntity entity = BankConnectionEntity.builder()
            .id(id)
            .bankName("Bank")
            .bankConnectionStatus("INVALID_STATUS")
            .manuallyCreated(false)
            .build();
        assertThrows(IllegalArgumentException.class, () -> mapper.toBankConnection(entity));
    }
}
