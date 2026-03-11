package cvut.fel.sit.mojefinance.external.api.gateway.data.mapper;

import cvut.fel.sit.mojefinance.external.api.gateway.data.entity.ConnectedBank;
import cvut.fel.sit.mojefinance.external.api.gateway.util.Constants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConnectedBankMapper {

    public List<ConnectedBank> mapClientRegistrationIdsToConnectedBanks(List<String> clientRegistrationIds) {
        List<ConnectedBank> connectedBanks = new ArrayList<>();
        for (String registrationId: clientRegistrationIds) {
            switch (registrationId) {
                case Constants.CESKA_SPORITELNA_CLIENT_REGISTRATION_ID:
                    connectedBanks.add(ConnectedBank.builder()
                            .name(Constants.CESKA_SPORITELNA_NAME)
                            .build());
                    break;
                case Constants.CSOB_CLIENT_REGISTRATION_ID:
                    connectedBanks.add(ConnectedBank.builder()
                            .name(Constants.CSOB_NAME)
                            .build());
                    break;
                case Constants.KB_CLIENT_REGISTRATION_ID:
                    connectedBanks.add(ConnectedBank.builder()
                            .name(Constants.KB_NAME)
                            .build());
                    break;
                case Constants.AIR_BANK_CLIENT_REGISTRATION_ID:
                    connectedBanks.add(ConnectedBank.builder()
                            .name(Constants.AIR_BANK_NAME)
                            .build());
                    break;
            }
        }
        return connectedBanks;
    }
}
