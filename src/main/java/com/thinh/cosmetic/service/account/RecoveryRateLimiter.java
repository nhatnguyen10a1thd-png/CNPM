package com.thinh.cosmetic.service.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** A bounded single-instance limiter. Multi-instance deployments need a shared limiter. */
@Component
public class RecoveryRateLimiter {
    private final Clock clock;
    private final int identifierLimit;
    private final int ipLimit;
    private final int resetLimit;
    private final Duration window;
    private final Map<String, Window> windows = new HashMap<>();

    public RecoveryRateLimiter(Clock clock,
            @Value("${lunea.recovery.request-limit:3}") int identifierLimit,
            @Value("${lunea.recovery.ip-limit:10}") int ipLimit,
            @Value("${lunea.recovery.reset-limit:10}") int resetLimit,
            @Value("${lunea.recovery.request-window-minutes:15}") long windowMinutes) {
        this.clock = clock;
        this.identifierLimit = Math.max(1, identifierLimit);
        this.ipLimit = Math.max(1, ipLimit);
        this.resetLimit = Math.max(1, resetLimit);
        this.window = Duration.ofMinutes(Math.max(1, windowMinutes));
    }

    public synchronized boolean allowRequest(String identifierHash, String ip) {
        return accept("request-ip:" + ip, ipLimit) && accept("identifier:" + identifierHash, identifierLimit);
    }

    public synchronized boolean allowReset(String ip) {
        return accept("reset-ip:" + ip, resetLimit);
    }

    private boolean accept(String key, int limit) {
        Instant now = clock.instant();
        windows.entrySet().removeIf(entry -> !entry.getValue().expiresAt.isAfter(now));
        Window current = windows.get(key);
        if (current == null) {
            if (windows.size() >= 10_000) return false;
            current = new Window(now.plus(window));
            windows.put(key, current);
        }
        if (current.count >= limit) return false;
        current.count++;
        return true;
    }

    private static final class Window {
        private final Instant expiresAt;
        private int count;
        private Window(Instant expiresAt) { this.expiresAt = expiresAt; }
    }
}
