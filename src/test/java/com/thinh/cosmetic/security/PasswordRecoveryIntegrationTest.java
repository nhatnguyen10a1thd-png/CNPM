package com.thinh.cosmetic.security;

import com.thinh.cosmetic.domain.dto.request.account.PasswordResetRequest;
import com.thinh.cosmetic.domain.dto.request.account.RegisterRequest;
import com.thinh.cosmetic.domain.entity.account.AccountEntity;
import com.thinh.cosmetic.domain.enums.AccountStatus;
import com.thinh.cosmetic.domain.enums.AccountType;
import com.thinh.cosmetic.exception.BadRequestException;
import com.thinh.cosmetic.repository.account.AccountRepository;
import com.thinh.cosmetic.repository.account.PasswordResetTokenRepository;
import com.thinh.cosmetic.service.account.AccountService;
import com.thinh.cosmetic.service.account.PasswordRecoveryService;
import com.thinh.cosmetic.service.account.RecoveryDelivery;
import com.thinh.cosmetic.service.account.RecoveryDeliveryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordRecoveryIntegrationTest {
    @Autowired PasswordRecoveryService recovery;
    @Autowired PasswordResetTokenRepository tokens;
    @MockitoSpyBean AccountRepository accounts;
    @Autowired AccountService accountService;
    @Autowired PasswordEncoder encoder;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean RecoveryDelivery delivery;
    @MockitoBean Clock clock;
    private final AtomicReference<String> delivered = new AtomicReference<>();
    private static final Instant NOW = Instant.parse("2026-10-07T12:00:00Z");
    private static final String OLD = "Previous123";
    private static final String NEW = "Changed456";
    private record Csrf(MockHttpSession session, String header, String token) { }

    @BeforeEach
    void prepare() {
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        delivered.set(null);
        doAnswer(invocation -> { delivered.set(invocation.getArgument(1)); return null; })
                .when(delivery).send(anyString(), anyString(), any());
    }

    private String ip() { return UUID.randomUUID().toString(); }
    private AccountEntity account(AccountType type) {
        String email = "recovery-" + UUID.randomUUID() + "@lunea.test";
        return accounts.saveAndFlush(AccountEntity.builder().username(email).email(email)
                .passwordHash(encoder.encode(OLD)).accountType(type).build());
    }
    private String request(AccountEntity account) {
        recovery.request(account.getEmail(), ip());
        assertThat(delivered.get()).isNotBlank();
        return delivered.get();
    }
    private void reset(String token, String password) { recovery.reset(new PasswordResetRequest(token, password, password), ip()); }
    private Csrf csrf(MockHttpSession session) throws Exception {
        var request = get("/api/auth/csrf");
        if (session != null) request.session(session);
        var result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        var data = json.readTree(result.getResponse().getContentAsString());
        return new Csrf((MockHttpSession) result.getRequest().getSession(), data.get("headerName").asText(), data.get("token").asText());
    }
    private MvcResult login(String email, String password, Csrf csrf, int status) throws Exception {
        return mvc.perform(post("/api/auth/login").session(csrf.session()).header(csrf.header(), csrf.token())
                .with(request -> { request.setRemoteAddr(ip()); return request; })
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("identifier", email, "password", password))))
                .andExpect(status().is(status)).andReturn();
    }

    @Test
    void happyRecoveryHashesSecretAndPasswordAndConsumesTokenOnce() {
        var account = account(AccountType.EMPLOYEE);
        String raw = request(account);
        var token = tokens.findByTokenHash(PasswordRecoveryService.hash(raw)).orElseThrow();
        assertThat(token.getTokenHash()).hasSize(64).isNotEqualTo(raw);
        assertThat(token.getExpiresAt()).isEqualTo(NOW.plusSeconds(900));
        reset(raw, NEW);
        var updated = accounts.findById(account.getId()).orElseThrow();
        assertThat(encoder.matches(NEW, updated.getPasswordHash())).isTrue();
        assertThat(encoder.matches(OLD, updated.getPasswordHash())).isFalse();
        assertThat(updated.getCredentialsVersion()).isEqualTo(1);
        assertThat(tokens.findById(token.getId()).orElseThrow().getConsumedAt()).isEqualTo(NOW);
        verify(delivery).invalidate(account.getEmail());
        assertThatThrownBy(() -> reset(raw, "Another789")).isInstanceOf(BadRequestException.class);
        assertThat(encoder.matches(NEW, accounts.findById(account.getId()).orElseThrow().getPasswordHash())).isTrue();
    }

    @Test
    void unknownLockedAndUnavailableAccountsUseSameGenericHttpResponse() throws Exception {
        var active = account(AccountType.CUSTOMER);
        var locked = account(AccountType.EMPLOYEE);
        locked.setStatus(AccountStatus.LOCKED); accounts.saveAndFlush(locked);
        doThrow(new RecoveryDeliveryException()).when(delivery).send(eq(active.getEmail()), anyString(), any());
        String response = null;
        for (String identifier : List.of("missing-" + UUID.randomUUID() + "@lunea.test", locked.getEmail(), active.getEmail())) {
            Csrf csrf = csrf(null);
            var result = mvc.perform(post("/api/auth/recovery/request").session(csrf.session()).header(csrf.header(), csrf.token())
                    .with(request -> { request.setRemoteAddr(ip()); return request; })
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("identifier", identifier))))
                    .andExpect(status().isAccepted()).andReturn();
            String body = result.getResponse().getContentAsString();
            if (response == null) response = body; else assertThat(body).isEqualTo(response);
            assertThat(body).doesNotContain(identifier, "token", "password");
        }
        assertThat(tokens.findAll().stream().filter(token -> token.getAccount().getId().equals(active.getId()) || token.getAccount().getId().equals(locked.getId()))).isEmpty();
    }

    @Test
    void expiredTokenAtExactExpiryCannotChangePassword() {
        var account = account(AccountType.CUSTOMER);
        String raw = request(account);
        when(clock.instant()).thenReturn(NOW.plusSeconds(900));
        assertThatThrownBy(() -> reset(raw, NEW)).isInstanceOf(BadRequestException.class);
        assertThat(encoder.matches(OLD, accounts.findById(account.getId()).orElseThrow().getPasswordHash())).isTrue();
        assertThat(tokens.findByTokenHash(PasswordRecoveryService.hash(raw)).orElseThrow().getConsumedAt()).isNull();
    }

    @Test
    void invalidSecretAndWeakOrMismatchedPasswordDoNotConsumeValidState() {
        var account = account(AccountType.CUSTOMER);
        String raw = request(account);
        assertThatThrownBy(() -> reset("not-valid", NEW)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> reset("a".repeat(43), NEW)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> reset(raw, "short1")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> recovery.reset(new PasswordResetRequest(raw, NEW, "Different789"), ip())).isInstanceOf(BadRequestException.class);
        assertThat(tokens.findByTokenHash(PasswordRecoveryService.hash(raw)).orElseThrow().getConsumedAt()).isNull();
        reset(raw, NEW);
        assertThat(encoder.matches(NEW, accounts.findById(account.getId()).orElseThrow().getPasswordHash())).isTrue();
    }

    @Test
    void requestingNewLinkInvalidatesPreviousLink() {
        var account = account(AccountType.CUSTOMER);
        String first = request(account);
        String second = request(account);
        assertThat(second).isNotEqualTo(first);
        assertThat(tokens.findByTokenHash(PasswordRecoveryService.hash(first))).isEmpty();
        assertThatThrownBy(() -> reset(first, NEW)).isInstanceOf(BadRequestException.class);
        reset(second, NEW);
    }

    @Test
    void accountLockedAfterRequestCannotBeReactivatedByRecovery() {
        var account = account(AccountType.EMPLOYEE);
        String raw = request(account);
        account.setStatus(AccountStatus.LOCKED); accounts.saveAndFlush(account);
        assertThatThrownBy(() -> reset(raw, NEW)).isInstanceOf(BadRequestException.class);
        var unchanged = accounts.findById(account.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(AccountStatus.LOCKED);
        assertThat(encoder.matches(OLD, unchanged.getPasswordHash())).isTrue();
    }

    @Test
    void concurrentResetAllowsExactlyOneCommit() throws Exception {
        var account = account(AccountType.CUSTOMER);
        String raw = request(account);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> command = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Start timed out");
            try { reset(raw, NEW); return true; }
            catch (BadRequestException alreadyConsumed) { return false; }
        };
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(command);
            var second = pool.submit(command);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue(); start.countDown();
            assertThat(List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS))).containsExactlyInAnyOrder(true, false);
        }
        assertThat(accounts.findById(account.getId()).orElseThrow().getCredentialsVersion()).isEqualTo(1);
        assertThat(tokens.findByTokenHash(PasswordRecoveryService.hash(raw)).orElseThrow().getConsumedAt()).isNotNull();
    }

    @Test
    void failedDeliveryRollsBackTokenAndPreservesCredentials() {
        var account = account(AccountType.CUSTOMER);
        long count = tokens.count();
        doThrow(new RecoveryDeliveryException()).when(delivery).send(eq(account.getEmail()), anyString(), any());
        assertThatThrownBy(() -> recovery.request(account.getEmail(), ip())).isInstanceOf(RecoveryDeliveryException.class);
        assertThat(tokens.count()).isEqualTo(count);
        assertThat(encoder.matches(OLD, accounts.findById(account.getId()).orElseThrow().getPasswordHash())).isTrue();
    }

    @Test
    void lateCredentialWriteFailureRollsBackConsumptionAndHash() {
        var account = account(AccountType.CUSTOMER);
        String raw = request(account);
        doThrow(new IllegalStateException("Test persistence failure")).when(accounts).saveAndFlush(argThat(candidate -> candidate != null && account.getId().equals(candidate.getId())));
        assertThatThrownBy(() -> reset(raw, NEW)).isInstanceOf(IllegalStateException.class);
        var persisted = accounts.findById(account.getId()).orElseThrow();
        assertThat(encoder.matches(OLD, persisted.getPasswordHash())).isTrue();
        assertThat(persisted.getCredentialsVersion()).isZero();
        assertThat(tokens.findByTokenHash(PasswordRecoveryService.hash(raw)).orElseThrow().getConsumedAt()).isNull();
        verify(delivery, never()).invalidate(any());
    }

    @Test
    void requestLimitAndResetAttemptLimitAreBoundedAndWindowExpires() {
        var account = account(AccountType.CUSTOMER);
        String ip = ip();
        for (int attempt = 0; attempt < 4; attempt++) recovery.request(account.getEmail(), ip);
        verify(delivery, times(3)).send(eq(account.getEmail()), anyString(), any());
        String raw = delivered.get();
        String resetIp = ip();
        for (int attempt = 0; attempt < 10; attempt++) {
            assertThatThrownBy(() -> recovery.reset(new PasswordResetRequest("x".repeat(43), NEW, NEW), resetIp)).isInstanceOf(BadRequestException.class);
        }
        assertThatThrownBy(() -> recovery.reset(new PasswordResetRequest(raw, NEW, NEW), resetIp)).isInstanceOf(BadRequestException.class).hasMessageContaining("quá nhiều");
        when(clock.instant()).thenReturn(NOW.plusSeconds(901));
        recovery.request(account.getEmail(), ip);
        verify(delivery, times(4)).send(eq(account.getEmail()), anyString(), any());
        recovery.reset(new PasswordResetRequest(delivered.get(), NEW, NEW), resetIp);
    }

    @Test
    void recoveryByPhoneUsesRegisteredEmail() {
        var account = account(AccountType.EMPLOYEE);
        String phone = "09" + Long.toUnsignedString(UUID.randomUUID().getMostSignificantBits()).substring(0, 8);
        account.setPhone(phone); accounts.saveAndFlush(account);
        recovery.request(phone.substring(0, 3) + " " + phone.substring(3), ip());
        verify(delivery).send(eq(account.getEmail()), anyString(), any());
    }

    @Test
    void loggedInResetInvalidatesOldSessionAndAllowsFreshCsrfAndNewLogin() throws Exception {
        String email = "session-recovery-" + UUID.randomUUID() + "@lunea.test";
        var customer = accountService.register(RegisterRequest.builder().fullName("Recovery session test").email(email)
                .password(OLD).confirmPassword(OLD).termsAccepted(true).build());
        var account = accounts.findById(customer.getAccountId()).orElseThrow();
        var loggedIn = login(email, OLD, csrf(null), 200);
        MockHttpSession session = (MockHttpSession) loggedIn.getRequest().getSession();
        Csrf current = csrf(session);
        String raw = request(account);
        mvc.perform(post("/api/auth/recovery/reset").session(session).header(current.header(), current.token())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(new PasswordResetRequest(raw, NEW, NEW))))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(raw))));
        Csrf fresh = csrf(session);
        assertThat(session.isInvalid()).isTrue();
        assertThat(fresh.session()).isNotSameAs(session);
        mvc.perform(get("/api/auth/me").session(fresh.session())).andExpect(status().isUnauthorized());
        login(email, OLD, fresh, 401);
        login(email, NEW, fresh, 200);
    }
}
