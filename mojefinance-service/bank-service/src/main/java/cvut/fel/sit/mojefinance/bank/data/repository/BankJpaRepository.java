package cvut.fel.sit.mojefinance.bank.data.repository;


import cvut.fel.sit.mojefinance.bank.data.entity.BankEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankJpaRepository extends JpaRepository<BankEntity, Long> {
    List<BankEntity> findAllByPrincipalName(String principalName);
}
