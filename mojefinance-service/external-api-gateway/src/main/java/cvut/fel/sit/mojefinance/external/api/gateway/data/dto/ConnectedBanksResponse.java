package cvut.fel.sit.mojefinance.external.api.gateway.data.dto;

import cvut.fel.sit.mojefinance.external.api.gateway.data.entity.ConnectedBank;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConnectedBanksResponse {
    private List<ConnectedBank> connectedBanks;
}
