package com.thinh.cosmetic.repository.account;

import com.thinh.cosmetic.domain.entity.account.RolePermissionEntity;
import com.thinh.cosmetic.domain.entity.account.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionId> {
    List<RolePermissionEntity> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
}
