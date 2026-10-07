package com.thinh.cosmetic.service.account;

import java.time.Instant;

/** Delivery implementations must never log, persist, or return the raw reset secret. */
public interface RecoveryDelivery {
    void send(String email, String rawToken, Instant expiresAt);
    default void invalidate(String email) { }
}
