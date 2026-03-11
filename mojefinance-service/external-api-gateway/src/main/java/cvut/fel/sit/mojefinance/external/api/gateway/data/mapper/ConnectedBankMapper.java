package cvut.fel.sit.mojefinance.external.api.gateway.data.mapper;

import cvut.fel.sit.mojefinance.external.api.gateway.data.entity.ConnectedBank;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConnectedBankMapper {

    public List<ConnectedBank> mapClientRegistrationIdsToConnectedBanks(List<String> clientRegistrationIds) {
        List<ConnectedBank> connectedBanks = new ArrayList<>();
        for (String registrationId: clientRegistrationIds) {
            switch (registrationId) {
                case "ceska-sporitelna":
                    connectedBanks.add(ConnectedBank.builder()
                            .name("Ceska Sporitelna")
                            .build());
                    break;
                case "csob":
                    connectedBanks.add(ConnectedBank.builder()
                            .name("CSOB")
                            .build());
                    break;
                case "kb":
                    connectedBanks.add(ConnectedBank.builder()
                            .name("KB")
                            .build());
                    break;
                case "air-bank":
                    connectedBanks.add(ConnectedBank.builder()
                            .name("Air Banks")
                            .build());
                    break;
            }
        }
        return connectedBanks;
    }
}
