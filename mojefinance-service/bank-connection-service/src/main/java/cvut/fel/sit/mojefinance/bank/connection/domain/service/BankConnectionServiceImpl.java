package cvut.fel.sit.mojefinance.bank.connection.domain.service;

import cvut.fel.sit.mojefinance.bank.connection.messaging.service.CeskaSporitelnaAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankConnectionServiceImpl implements BankConnectionService {
    private final CeskaSporitelnaAdapter ceskaSporitelnaAdapter;

    @Override
    public void connectCeskaSporitelna(String code) {
        ceskaSporitelnaAdapter.connectCeskaSporitelna(code);
    }
}
