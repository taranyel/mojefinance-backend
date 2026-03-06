package cvut.fel.sit.mojefinance.bank.data.repository;

import cvut.fel.sit.mojefinance.bank.data.entity.BankConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankConnectionJpaRepository extends JpaRepository<BankConnectionEntity, Long> {
    List<BankConnectionEntity> findAllByCustomerUsername(String customerUsername);
}
