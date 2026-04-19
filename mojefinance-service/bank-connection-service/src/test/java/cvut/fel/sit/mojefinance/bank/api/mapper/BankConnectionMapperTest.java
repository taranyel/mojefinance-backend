package cvut.fel.sit.mojefinance.bank.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.Collections;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.openapi.model.ConnectBankRequest;
import cvut.fel.sit.mojefinance.openapi.model.BankConnection.BankConnectionStatusEnum;

class BankConnectionMapperTest {
    private BankConnectionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(BankConnectionMapper.class);
    }

    @Test
    void toConnectBankDomainRequest_mapsFieldsCorrectly() {
        cvut.fel.sit.mojefinance.openapi.model.BankConnection apiBankConnection = new cvut.fel.sit.mojefinance.openapi.model.BankConnection();
        apiBankConnection.setBankName("Test Bank");
        apiBankConnection.setBankConnectionStatus(BankConnectionStatusEnum.CONNECTED);
        apiBankConnection.setClientRegistrationId("reg123");
        ConnectBankRequest apiRequest = new ConnectBankRequest();
        apiRequest.setBankConnection(apiBankConnection);
        String code = "authCode";
        ConnectBankDomainRequest domainRequest = mapper.toConnectBankDomainRequest(apiRequest, code);
        assertNotNull(domainRequest);
        assertEquals("authCode", domainRequest.getCode());
        assertNotNull(domainRequest.getBankConnection());
        assertEquals("Test Bank", domainRequest.getBankConnection().getBankName());
        assertEquals(BankConnectionStatus.CONNECTED, domainRequest.getBankConnection().getBankConnectionStatus());
        assertEquals("reg123", domainRequest.getBankConnection().getClientRegistrationId());
    }

    @Test
    void toConnectedBanksResponse_mapsListCorrectly() {
        BankConnection domainBankConnection = new BankConnection();
        domainBankConnection.setBankName("BankX");
        domainBankConnection.setBankConnectionStatus(BankConnectionStatus.DISCONNECTED);
        domainBankConnection.setClientRegistrationId("regX");
        ConnectedBanksResponse domainResponse = ConnectedBanksResponse.builder()
            .connectedBanks(Collections.singletonList(domainBankConnection))
            .build();
        cvut.fel.sit.mojefinance.openapi.model.ConnectedBanksResponse apiResponse = mapper.toConnectedBanksResponse(domainResponse);
        assertNotNull(apiResponse);
        assertNotNull(apiResponse.getConnectedBanks());
        assertEquals(1, apiResponse.getConnectedBanks().size());
        cvut.fel.sit.mojefinance.openapi.model.BankConnection apiBankConnection = apiResponse.getConnectedBanks().get(0);
        assertEquals("BankX", apiBankConnection.getBankName());
        assertEquals(BankConnectionStatusEnum.DISCONNECTED, apiBankConnection.getBankConnectionStatus());
        assertEquals("regX", apiBankConnection.getClientRegistrationId());
    }

    @Test
    void toConnectBankDomainRequest_nullInputReturnsNull() {
        assertNull(mapper.toConnectBankDomainRequest(null, null));
    }

    @Test
    void toConnectedBanksResponse_nullInputReturnsNull() {
        assertNull(mapper.toConnectedBanksResponse(null));
    }
}
