package cvut.fel.sit.mojefinance.external.api.gateway.messaging.airbank.service;

import cvut.fel.sit.mojefinance.external.api.gateway.data.entity.ConnectedBank;

public interface AirBankAdapter {
    ConnectedBank connectAirBank(String code);
}
