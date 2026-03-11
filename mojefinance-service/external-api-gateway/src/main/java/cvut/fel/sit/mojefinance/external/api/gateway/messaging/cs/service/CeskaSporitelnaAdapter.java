package cvut.fel.sit.mojefinance.external.api.gateway.messaging.cs.service;

import cvut.fel.sit.mojefinance.external.api.gateway.data.entity.ConnectedBank;

public interface CeskaSporitelnaAdapter {
    ConnectedBank connectCeskaSporitelna(String code);
}
