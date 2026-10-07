package com.thinh.cosmetic.repository.account;

import com.thinh.cosmetic.domain.entity.account.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RoleEntity> findLockedByName(String name);
}
