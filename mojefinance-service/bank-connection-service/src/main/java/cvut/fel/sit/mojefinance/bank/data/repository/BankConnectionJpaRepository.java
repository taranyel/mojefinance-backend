package cvut.fel.sit.mojefinance.bank.data.repository;


import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankConnectionJpaRepository extends JpaRepository<BankConnectionEntity, BankConnectionId> {
    List<BankConnectionEntity> findAllById_PrincipalName(String principalName);

    @Transactional
    void removeBankEntityById(BankConnectionId id);
}
