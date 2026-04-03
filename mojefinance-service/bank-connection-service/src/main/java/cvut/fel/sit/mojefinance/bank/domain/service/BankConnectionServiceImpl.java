package cvut.fel.sit.mojefinance.bank.domain.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import cvut.fel.sit.mojefinance.authorization.AuthorizationService;
import cvut.fel.sit.mojefinance.authorization.data.dto.AuthorizedClientServiceRequest;
import cvut.fel.sit.mojefinance.authorization.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.bank.data.dto.ConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.authorization.data.exception.ClientRegistrationNotFoundException;
import cvut.fel.sit.mojefinance.bank.data.repository.BankConnectionRepository;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.bank.domain.mapper.BankConnectionDomainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cvut.fel.sit.shared.util.Constants.RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankConnectionServiceImpl implements BankConnectionService {
    private final BankConnectionDomainMapper bankConnectionDomainMapper;
    private final BankConnectionRepository bankConnectionRepository;
    private final AuthorizationService authorizationService;

    @Override
    public BankConnection connectBank(ConnectBankDomainRequest domainRequest) {
        BankConnection bankConnection = domainRequest.getBankConnection();
        validateBankDomainEntity(bankConnection);
        log.info("Connecting to bank: {}", bankConnection.getBankName());

        ConnectAuthorizedClientRequest authorizedClientRequest = bankConnectionDomainMapper.toConnectAuthorizedClientRequest(domainRequest);
        boolean manuallyCreated = false;
        try {
            authorizationService.connectAuthorizedClient(authorizedClientRequest);
        } catch (ClientRegistrationNotFoundException e) {
            if (!Objects.equals(bankConnection.getClientRegistrationId(), RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID)) {
                log.info("Bank {} not found in authorized client service. Marking as fake connection.", bankConnection.getBankName());
                manuallyCreated = true;
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        bankConnection.setManuallyCreated(manuallyCreated);
        bankConnection.setBankConnectionStatus(BankConnectionStatus.CONNECTED);

        BankConnectionEntity bankConnectionEntity = bankConnectionDomainMapper.toBankConnectionEntity(bankConnection);
        bankConnectionEntity.getId().setPrincipalName(authentication.getName());
        bankConnectionRepository.addConnectedBank(bankConnectionEntity);

        log.info("Bank {} connected successfully.", bankConnection.getBankName());
        return bankConnection;
    }

    @Override
    public void disconnectBank(String clientRegistrationId) {
        log.info("Disconnecting bank with client registration ID: {}", clientRegistrationId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        AuthorizedClientServiceRequest authorizedClientServiceRequest = getAuthorizedClientServiceRequest(clientRegistrationId, principalName);
        authorizationService.disconnectAuthorizedClient(authorizedClientServiceRequest);

        bankConnectionRepository.removeConnectedBankByClientRegistrationIdAndPrincipalName(clientRegistrationId, principalName);
        log.info("Bank with client registration ID {} disconnected successfully.", clientRegistrationId);
    }

    @Override
    public ConnectedBanksDomainResponse getConnectedBanks() {
        log.info("Retrieving connected banks for authorized user.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        ConnectedBanksDataResponse internalResponse = bankConnectionRepository.getAllConnectedBanksByPrincipalName(principalName);
        List<BankConnectionEntity> notManuallyCreatedConnectedBanks = getNotManuallyCreatedConnectedBanks(internalResponse);

        updateBankConnectionStatus(notManuallyCreatedConnectedBanks, principalName);

        ConnectedBanksDomainResponse domainResponse = bankConnectionDomainMapper.toConnectedBanksDomainResponse(internalResponse);
        log.info("Connected banks: {}", domainResponse);
        return domainResponse;
    }

    private void updateBankConnectionStatus(List<BankConnectionEntity> notManuallyCreatedConnectedBanks, String principalName) {
        for (BankConnectionEntity connectedBank : notManuallyCreatedConnectedBanks) {
            String clientRegistrationId = connectedBank.getId().getClientRegistrationId();
            boolean authorizedClientDoesNotExist = authorizedClientDoesNotExist(principalName, clientRegistrationId);
            boolean bankIsNotReiffeisenbank = !RAIFFEISEN_BANK_CLIENT_REGISTRATION_ID.equals(clientRegistrationId);

            if (authorizedClientDoesNotExist && bankIsNotReiffeisenbank) {
                connectedBank.setBankConnectionStatus(BankConnectionStatus.DISCONNECTED.name());
                bankConnectionRepository.updateConnectedBank(connectedBank);
            }
        }
    }

    private boolean authorizedClientDoesNotExist(String principalName, String clientRegistrationId) {
        AuthorizedClientServiceRequest authorizedClientRequest = getAuthorizedClientServiceRequest(clientRegistrationId, principalName);
        return !authorizationService.authorizedClientExists(authorizedClientRequest);
    }

    private List<BankConnectionEntity> getNotManuallyCreatedConnectedBanks(ConnectedBanksDataResponse banksDataResponse) {
        return banksDataResponse.getConnectedBanks().stream()
                .filter(bankEntity -> !bankEntity.getManuallyCreated())
                .toList();
    }

    private AuthorizedClientServiceRequest getAuthorizedClientServiceRequest(String clientRegistrationId, String principalName) {
        return AuthorizedClientServiceRequest.builder()
                .clientRegistrationId(clientRegistrationId)
                .principalName(principalName)
                .build();
    }

    private void validateBankDomainEntity(BankConnection bankConnection) {
        if (bankConnection == null || StringUtils.isBlank(bankConnection.getBankName()) || StringUtils.isBlank(bankConnection.getClientRegistrationId())) {
            throw new IllegalArgumentException("Bank information is required to connect.");
        }
    }
}
