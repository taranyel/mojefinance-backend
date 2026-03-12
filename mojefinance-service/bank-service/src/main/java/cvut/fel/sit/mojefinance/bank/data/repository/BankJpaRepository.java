package cvut.fel.sit.mojefinance.bank.data.repository;


import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;
import cvut.fel.sit.mojefinance.bank.data.entity.ConnectedBankId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankJpaRepository extends JpaRepository<BankEntity, ConnectedBankId> {
    List<BankEntity> findAllById_PrincipalName(String principalName);

    @Transactional
    void removeBankEntityById(ConnectedBankId id);
}
