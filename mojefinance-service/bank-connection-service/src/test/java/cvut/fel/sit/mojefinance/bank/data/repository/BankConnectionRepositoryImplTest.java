package cvut.fel.sit.mojefinance.bank.data.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionId;
import cvut.fel.sit.mojefinance.bank.data.mapper.BankConnectionDataMapper;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;

class BankConnectionRepositoryImplTest {
    @Mock
    private BankConnectionJpaRepository bankConnectionJpaRepository;
    @Mock
    private BankConnectionDataMapper mapper;
    @InjectMocks
    private BankConnectionRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new BankConnectionRepositoryImpl(bankConnectionJpaRepository, mapper);
    }

    @Test
    void getAllConnectedBanksByPrincipalName_returnsMappedResponse() {
        String principalName = "user1";
        BankConnectionEntity entity = mock(BankConnectionEntity.class);
        BankConnection domain = mock(BankConnection.class);
        when(bankConnectionJpaRepository.findAllById_PrincipalName(principalName)).thenReturn(List.of(entity));
        when(mapper.toBankConnection(entity)).thenReturn(domain);
        ConnectedBanksResponse response = repository.getAllConnectedBanksByPrincipalName(principalName);
        assertNotNull(response);
        assertEquals(1, response.getConnectedBanks().size());
        assertEquals(domain, response.getConnectedBanks().get(0));
    }

    @Test
    void addConnectedBank_savesEntity() {
        BankConnectionEntity entity = mock(BankConnectionEntity.class);
        repository.addConnectedBank(entity);
        verify(bankConnectionJpaRepository).save(entity);
    }

    @Test
    void removeConnectedBankByClientRegistrationIdAndPrincipalName_removesEntity() {
        repository.removeConnectedBankByClientRegistrationIdAndPrincipalName("reg123", "user1");
        verify(bankConnectionJpaRepository).removeBankEntityById(any(BankConnectionId.class));
    }

    @Test
    void updateConnectedBank_savesEntity() {
        BankConnectionEntity entity = mock(BankConnectionEntity.class);
        repository.updateConnectedBank(entity);
        verify(bankConnectionJpaRepository).save(entity);
    }
}
