package com.thinh.cosmetic.service.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local demo mailbox only. Reset links are kept in memory and removed on expiry/consumption. */
@Service
@Profile("demo")
public class DemoRecoveryOutbox implements RecoveryDelivery {
    public record Message(String email, String resetUrl, Instant expiresAt) {
        @Override public String toString() { return "DemoRecoveryMessage[REDACTED, expiresAt=" + expiresAt + "]"; }
    }
    private final Map<String, Message> messages = new LinkedHashMap<>();
    private final Clock clock;
    private final String baseUrl;

    public DemoRecoveryOutbox(Clock clock,
            @Value("${lunea.recovery.public-base-url:http://localhost:8080}") String baseUrl) {
        this.clock = clock;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    @Override
    public void send(String email, String rawToken, Instant expiresAt) {
        Runnable add = () -> addMessage(new Message(email, baseUrl + "/#reset=" + rawToken, expiresAt));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { add.run(); }
            });
        } else add.run();
    }

    private synchronized void addMessage(Message message) {
        prune();
        if (messages.size() >= 1_000) messages.remove(messages.keySet().iterator().next());
        messages.put(message.email(), message);
    }

    @Override public synchronized void invalidate(String email) { messages.remove(email); }

    public synchronized List<Message> messages() {
        prune();
        return new ArrayList<>(messages.values());
    }

    private void prune() {
        messages.values().removeIf(message -> !message.expiresAt().isAfter(clock.instant()));
    }
}
