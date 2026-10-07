package com.thinh.cosmetic.service.account;

import com.thinh.cosmetic.domain.dto.request.account.PasswordResetRequest;
import com.thinh.cosmetic.domain.entity.account.AccountEntity;
import com.thinh.cosmetic.domain.entity.account.PasswordResetTokenEntity;
import com.thinh.cosmetic.domain.enums.AccountStatus;
import com.thinh.cosmetic.exception.BadRequestException;
import com.thinh.cosmetic.repository.account.AccountRepository;
import com.thinh.cosmetic.repository.account.PasswordResetTokenRepository;
import com.thinh.cosmetic.security.PasswordPolicy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordRecoveryService {
    private final AccountRepository accounts;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final RecoveryDelivery delivery;
    private final RecoveryRateLimiter limiter;
    private final EntityManager entityManager;
    private final Clock clock;
    private final Duration tokenTtl;
    private final SecureRandom random = new SecureRandom();

    public PasswordRecoveryService(AccountRepository accounts, PasswordResetTokenRepository tokens,
            PasswordEncoder encoder, RecoveryDelivery delivery, RecoveryRateLimiter limiter,
            EntityManager entityManager, Clock clock,
            @Value("${lunea.recovery.ttl-minutes:15}") long ttlMinutes) {
        this.accounts = accounts;
        this.tokens = tokens;
        this.encoder = encoder;
        this.delivery = delivery;
        this.limiter = limiter;
        this.entityManager = entityManager;
        this.clock = clock;
        this.tokenTtl = Duration.ofMinutes(Math.max(1, ttlMinutes));
    }

    @Transactional
    public void request(String identifier, String clientIp) {
        String normalized = identifier.trim().toLowerCase(Locale.ROOT);
        boolean emailIdentifier = normalized.contains("@");
        try {
            normalized = emailIdentifier ? PasswordPolicy.normalizeEmail(normalized) : PasswordPolicy.normalizePhone(normalized);
        } catch (BadRequestException exception) {
            limiter.allowRequest(hash(normalized), clientIp);
            return;
        }
        if (normalized == null || !limiter.allowRequest(hash(normalized), clientIp)) return;
        Optional<AccountEntity> found = emailIdentifier ? accounts.findByEmail(normalized) : accounts.findByPhone(normalized);
        if (found.isEmpty()) return;
        AccountEntity account = found.get();
        entityManager.refresh(account, LockModeType.PESSIMISTIC_WRITE);
        if (account.getStatus() != AccountStatus.ACTIVE) return;

        Instant now = clock.instant();
        // Keep at most one token per account. Global cleanup must use a separate maintenance transaction.
        tokens.deleteForAccount(account.getId());
        byte[] secret = new byte[32];
        random.nextBytes(secret);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setAccount(account);
        token.setTokenHash(hash(rawToken));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(tokenTtl));
        tokens.saveAndFlush(token);
        delivery.send(account.getEmail(), rawToken, token.getExpiresAt());
    }

    @Transactional
    public void reset(PasswordResetRequest request, String clientIp) {
        if (!limiter.allowReset(clientIp)) {
            throw new BadRequestException("RECOVERY_RATE_LIMIT", "Có quá nhiều lần thử. Vui lòng thử lại sau.");
        }
        PasswordPolicy.validatePassword(request.password());
        PasswordPolicy.validateConfirmation(request.password(), request.confirmPassword());
        if (!request.token().matches("[A-Za-z0-9_-]{43}")) throw invalidToken();
        PasswordResetTokenEntity token = tokens.findByTokenHash(hash(request.token())).orElseThrow(this::invalidToken);
        AccountEntity account = token.getAccount();
        // All recovery operations lock the account first, then the token, to serialize resets.
        entityManager.refresh(account, LockModeType.PESSIMISTIC_WRITE);
        token = tokens.findLockedById(token.getId()).orElseThrow(this::invalidToken);
        entityManager.refresh(token, LockModeType.PESSIMISTIC_WRITE);
        Instant now = clock.instant();
        if (token.getConsumedAt() != null || !token.getExpiresAt().isAfter(now)
                || account.getStatus() != AccountStatus.ACTIVE) throw invalidToken();

        account.setPasswordHash(encoder.encode(request.password()));
        account.setCredentialsVersion(account.getCredentialsVersion() + 1);
        token.setConsumedAt(now);
        accounts.saveAndFlush(account);
        tokens.saveAndFlush(token);
        String email = account.getEmail();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { delivery.invalidate(email); }
        });
    }

    private BadRequestException invalidToken() {
        return new BadRequestException("INVALID_RECOVERY_TOKEN", "Liên kết không hợp lệ, đã dùng hoặc đã hết hạn. Vui lòng yêu cầu liên kết mới.");
    }

    public static String hash(String secret) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable");
        }
    }
}
