package com.thinh.cosmetic.security;

import com.thinh.cosmetic.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** In-process abuse guard for the single-node demo; production can replace this adapter. */
@Component
public class AuthAttemptLimiter {
    private final Clock clock;
    private final Map<String, Window> windows = new HashMap<>();
    private record Window(Instant until, int count) { }

    public AuthAttemptLimiter(Clock clock) { this.clock = clock; }

    public synchronized void check(String key, int limit, Duration duration) {
        Instant now = clock.instant();
        windows.entrySet().removeIf(entry -> !entry.getValue().until().isAfter(now));
        Window current = windows.get(key);
        if ((current != null && current.count() >= limit) || (current == null && windows.size() >= 10000)) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_ATTEMPTS",
                    "Thao tác quá nhiều lần. Vui lòng thử lại sau");
        }
        windows.put(key, new Window(current == null ? now.plus(duration) : current.until(),
                current == null ? 1 : current.count() + 1));
    }

    public synchronized void clear(String key) { windows.remove(key); }
}
