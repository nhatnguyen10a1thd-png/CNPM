package com.thinh.cosmetic.repository.account;

import com.thinh.cosmetic.domain.entity.account.EmployeeRoleEntity;
import com.thinh.cosmetic.domain.entity.account.EmployeeRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRoleRepository extends JpaRepository<EmployeeRoleEntity, EmployeeRoleId> {
    List<EmployeeRoleEntity> findByEmployeeId(Long employeeId);
    List<EmployeeRoleEntity> findByRoleId(Long roleId);
    void deleteByEmployeeId(Long employeeId);
}
