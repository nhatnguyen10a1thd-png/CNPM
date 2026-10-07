package com.thinh.cosmetic.repository.account;

import com.thinh.cosmetic.domain.entity.account.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<AccountEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<AccountEntity> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Optional<AccountEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
