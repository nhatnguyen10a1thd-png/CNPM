package com.thinh.cosmetic.repository.account;

import com.thinh.cosmetic.domain.entity.account.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long>, JpaSpecificationExecutor<EmployeeEntity> {
    Optional<EmployeeEntity> findByAccountId(Long accountId);
    Optional<EmployeeEntity> findByInternalEmailIgnoreCase(String internalEmail);
}
