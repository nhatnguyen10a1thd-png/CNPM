package com.thinh.cosmetic.repository.account;

import com.thinh.cosmetic.domain.entity.account.PasswordResetTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetTokenEntity t where t.id = :id")
    Optional<PasswordResetTokenEntity> findLockedById(@Param("id") Long id);

    @Modifying
    @Query("delete from PasswordResetTokenEntity t where t.account.id = :accountId")
    void deleteForAccount(@Param("accountId") Long accountId);
}
