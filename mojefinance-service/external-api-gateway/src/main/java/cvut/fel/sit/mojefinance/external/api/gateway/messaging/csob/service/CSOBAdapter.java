package cvut.fel.sit.mojefinance.external.api.gateway.messaging.csob.service;

import cvut.fel.sit.mojefinance.external.api.gateway.data.entity.ConnectedBank;

public interface CSOBAdapter {
    ConnectedBank connectCSOB(String code);
}
