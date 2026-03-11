package cvut.fel.sit.mojefinance.external.api.gateway.messaging.kb.service;

import cvut.fel.sit.mojefinance.external.api.gateway.data.entity.ConnectedBank;

public interface KBAdapter {
    ConnectedBank connectKB(String code);
}
