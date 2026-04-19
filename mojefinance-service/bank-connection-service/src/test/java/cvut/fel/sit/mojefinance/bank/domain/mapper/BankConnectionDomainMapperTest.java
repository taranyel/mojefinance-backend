package cvut.fel.sit.mojefinance.bank.domain.mapper;

import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;

class BankConnectionDomainMapperTest {
    private BankConnectionDomainMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(BankConnectionDomainMapper.class);
    }

    @Test
    void toConnectAuthorizedClientRequest_mapsFieldsCorrectly() {
        BankConnection bankConnection = new BankConnection();
        bankConnection.setClientRegistrationId("reg123");
        ConnectBankDomainRequest domainRequest = ConnectBankDomainRequest.builder()
            .bankConnection(bankConnection)
            .code("authCode")
            .build();
        ConnectAuthorizedClientRequest result = mapper.toConnectAuthorizedClientRequest(domainRequest);
        assertNotNull(result);
        assertEquals("reg123", result.getClientRegistrationId());
        assertEquals("authCode", result.getCode());
    }

    @Test
    void toConnectAuthorizedClientRequest_nullInputReturnsNull() {
        assertNull(mapper.toConnectAuthorizedClientRequest(null));
    }

    @Test
    void toBankConnectionEntity_mapsFieldsCorrectly() {
        BankConnection bankConnection = new BankConnection();
        bankConnection.setClientRegistrationId("regX");
        bankConnection.setBankName("BankX");
        bankConnection.setManuallyCreated(true);
        bankConnection.setBankConnectionStatus(BankConnectionStatus.CONNECTED);
        BankConnectionEntity entity = mapper.toBankConnectionEntity(bankConnection);
        assertNotNull(entity);
        assertNotNull(entity.getId());
        assertEquals("regX", entity.getId().getClientRegistrationId());
        assertEquals("BankX", entity.getBankName());
        assertEquals(true, entity.getManuallyCreated());
        assertEquals(BankConnectionStatus.CONNECTED.name(), entity.getBankConnectionStatus());
    }

    @Test
    void toBankConnectionEntity_nullInputReturnsNull() {
        assertNull(mapper.toBankConnectionEntity(null));
    }
}
