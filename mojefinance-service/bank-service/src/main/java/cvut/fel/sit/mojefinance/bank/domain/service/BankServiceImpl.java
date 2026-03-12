package cvut.fel.sit.mojefinance.bank.domain.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import cvut.fel.sit.mojefinance.bank.data.dto.AuthorizedClientServiceRequest;
import cvut.fel.sit.mojefinance.bank.data.dto.ConnectAuthorizedClientRequest;
import cvut.fel.sit.mojefinance.bank.data.dto.GetConnectedBanksDataResponse;
import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;
import cvut.fel.sit.mojefinance.bank.data.exception.ClientRegistrationNotFoundException;
import cvut.fel.sit.mojefinance.bank.data.repository.AuthorizedClientService;
import cvut.fel.sit.mojefinance.bank.data.repository.BankRepository;
import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectBankDomainRequest;
import cvut.fel.sit.mojefinance.bank.domain.dto.GetConnectedBanksDomainResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnectionStatus;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankDomainEntity;
import cvut.fel.sit.mojefinance.bank.domain.mapper.BankDomainMapper;
import cvut.fel.sit.mojefinance.bank.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {
    private final BankDomainMapper bankDomainMapper;
    private final BankRepository bankRepository;
    private final AuthorizedClientService authorizedClientService;

    @Override
    public BankDomainEntity connectBank(ConnectBankDomainRequest domainRequest) {
        BankDomainEntity bankDomainEntity = domainRequest.getBankDomainEntity();
        validateBankDomainEntity(bankDomainEntity);
        log.info("Connecting to bank: {}", bankDomainEntity.getBankName());

        ConnectAuthorizedClientRequest authorizedClientRequest = bankDomainMapper.toConnectAuthorizedClientRequest(domainRequest);
        boolean manuallyCreated = false;
        try {
            authorizedClientService.connectAuthorizedClient(authorizedClientRequest);
        } catch (ClientRegistrationNotFoundException e) {
            if (!Objects.equals(bankDomainEntity.getClientRegistrationId(), Constants.REIFFEISEN_BANK_CLIENT_REGISTRATION_ID)) {
                log.info("Bank {} not found in authorized client service. Marking as fake connection.", bankDomainEntity.getBankName());
                manuallyCreated = true;
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        bankDomainEntity.setManuallyCreated(manuallyCreated);
        bankDomainEntity.setBankConnectionStatus(BankConnectionStatus.CONNECTED);

        BankEntity bankEntity = bankDomainMapper.toBankEntity(bankDomainEntity);
        bankEntity.getId().setPrincipalName(authentication.getName());
        bankRepository.addConnectedBank(bankEntity);

        log.info("Bank {} connected successfully.", bankDomainEntity.getBankName());
        return bankDomainEntity;
    }

    @Override
    public void disconnectBank(String clientRegistrationId) {
        log.info("Disconnecting bank with client registration ID: {}", clientRegistrationId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        AuthorizedClientServiceRequest authorizedClientServiceRequest = getAuthorizedClientServiceRequest(clientRegistrationId, principalName);
        authorizedClientService.disconnectAuthorizedClient(authorizedClientServiceRequest);

        bankRepository.removeConnectedBankByClientRegistrationIdAndPrincipalName(clientRegistrationId, principalName);
        log.info("Bank with client registration ID {} disconnected successfully.", clientRegistrationId);
    }

    @Override
    public GetConnectedBanksDomainResponse getConnectedBanks() {
        log.info("Retrieving connected banks for authorized user.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();

        GetConnectedBanksDataResponse internalResponse = bankRepository.getAllConnectedBanksByPrincipalName(principalName);
        List<BankEntity> notManuallyCreatedConnectedBanks = getNotManuallyCreatedConnectedBanks(internalResponse);

        updateBankConnectionStatus(notManuallyCreatedConnectedBanks, principalName);

        GetConnectedBanksDomainResponse domainResponse = bankDomainMapper.toConnectedBanksDomainResponse(internalResponse);
        log.info("Connected banks: {}", domainResponse);
        return domainResponse;
    }

    private void updateBankConnectionStatus(List<BankEntity> notManuallyCreatedConnectedBanks, String principalName) {
        for (BankEntity connectedBank : notManuallyCreatedConnectedBanks) {
            String clientRegistrationId = connectedBank.getId().getClientRegistrationId();
            boolean authorizedClientDoesNotExist = authorizedClientDoesNotExist(principalName, clientRegistrationId);
            boolean bankIsNotReiffeisenbank = !Constants.REIFFEISEN_BANK_CLIENT_REGISTRATION_ID.equals(clientRegistrationId);

            if (authorizedClientDoesNotExist && bankIsNotReiffeisenbank) {
                connectedBank.setBankConnectionStatus(BankConnectionStatus.DISCONNECTED.name());
                bankRepository.updateConnectedBank(connectedBank);
            }
        }
    }

    private boolean authorizedClientDoesNotExist(String principalName, String clientRegistrationId) {
        AuthorizedClientServiceRequest authorizedClientRequest = getAuthorizedClientServiceRequest(clientRegistrationId, principalName);
        return !authorizedClientService.authorizedClientExists(authorizedClientRequest);
    }

    private List<BankEntity> getNotManuallyCreatedConnectedBanks(GetConnectedBanksDataResponse banksDataResponse) {
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

    private void validateBankDomainEntity(BankDomainEntity bankDomainEntity) {
        if (bankDomainEntity == null || StringUtils.isBlank(bankDomainEntity.getBankName()) || StringUtils.isBlank(bankDomainEntity.getClientRegistrationId())) {
            throw new IllegalArgumentException("Bank information is required to connect.");
        }
    }
}
