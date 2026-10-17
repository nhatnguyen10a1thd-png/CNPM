package com.thinh.cosmetic.security;

import com.thinh.cosmetic.domain.dto.request.account.RegisterRequest;
import com.thinh.cosmetic.domain.entity.account.AccountEntity;
import com.thinh.cosmetic.domain.entity.account.CustomerEntity;
import com.thinh.cosmetic.domain.entity.account.EmployeeEntity;
import com.thinh.cosmetic.domain.enums.AccountStatus;
import com.thinh.cosmetic.domain.enums.AccountType;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.exception.ConflictException;
import com.thinh.cosmetic.repository.account.AccountRepository;
import com.thinh.cosmetic.repository.account.CustomerRepository;
import com.thinh.cosmetic.repository.account.EmployeeRepository;
import com.thinh.cosmetic.service.account.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "server.servlet.session.cookie.secure=true")
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AccountRepository accounts;
    @Autowired CustomerRepository customers;
    @Autowired EmployeeRepository employees;
    @Autowired PasswordEncoder encoder;
    @Autowired AccountService service;
    private static final String PASSWORD = "Password123";
    private record Csrf(MockHttpSession session, String header, String token) { }

    private Csrf csrf(MockHttpSession session) throws Exception {
        var request = get("/api/auth/csrf");
        if (session != null) request.session(session);
        MvcResult result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        var data = json.readTree(result.getResponse().getContentAsString());
        return new Csrf((MockHttpSession) result.getRequest().getSession(),
                data.get("headerName").asText(), data.get("token").asText());
    }

    private String email() { return "auth-" + UUID.randomUUID() + "@lunea.test"; }

    private RegisterRequest registration(String email, String phone) {
        return RegisterRequest.builder().fullName("Khách kiểm thử").email(email).phone(phone)
                .password(PASSWORD).confirmPassword(PASSWORD).termsAccepted(true).build();
    }

    private MvcResult login(String identifier, boolean remember) throws Exception {
        Csrf csrf = csrf(null);
        return mvc.perform(post("/api/auth/login").session(csrf.session()).header(csrf.header(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                                "identifier", identifier, "password", PASSWORD, "rememberMe", remember))))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void registrationHashesPasswordAndCreatesSingleCustomerWithDefaults() throws Exception {
        String email = email();
        Csrf csrf = csrf(null);
        var result = mvc.perform(post("/api/auth/register").session(csrf.session()).header(csrf.header(), csrf.token())
                        .with(request -> { request.setRemoteAddr(UUID.randomUUID().toString()); return request; })
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(registration(email, null))))
                .andExpect(status().isCreated()).andExpect(jsonPath("loyaltyPoints").value(0)).andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("password", PASSWORD);
        var account = accounts.findByEmail(email).orElseThrow();
        assertThat(account.getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, account.getPasswordHash())).isTrue();
        assertThat(customers.findByAccountId(account.getId())).isPresent();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void phoneLoginKeepsIdentityAndDoesNotTrustCustomerId() throws Exception {
        String phone = "09" + Long.toUnsignedString(UUID.randomUUID().getMostSignificantBits()).substring(0, 8);
        var customer = service.register(registration(email(), phone));
        var signedIn = login(phone, false);
        var session = (MockHttpSession) signedIn.getRequest().getSession();
        assertThat(session.getMaxInactiveInterval()).isEqualTo(1800);
        mvc.perform(get("/api/auth/me").session(session).param("customerId", "999999"))
                .andExpect(status().isOk()).andExpect(jsonPath("customerId").value(customer.getId().intValue()))
                .andExpect(jsonPath("accountType").value("CUSTOMER"));
        mvc.perform(get("/api/cart").session(session).param("customerId", "999999"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginRotatesSessionAndRememberPersistsCookie() throws Exception {
        String email = email();
        service.register(registration(email, null));
        Csrf before = csrf(null);
        String oldId = before.session().getId();
        var result = mvc.perform(post("/api/auth/login").session(before.session()).header(before.header(), before.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                                "identifier", email, "password", PASSWORD, "rememberMe", true))))
                .andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) result.getRequest().getSession();
        assertThat(before.session().isInvalid()).isTrue();
        assertThat(session.getId()).isNotEqualTo(oldId);
        assertThat(session.getMaxInactiveInterval()).isEqualTo(604800);
        assertThat(result.getResponse().getHeader("Set-Cookie"))
                .contains("HttpOnly", "SameSite=Lax", "Max-Age=604800", "Secure");
        assertThat(result.getResponse().getContentAsString()).doesNotContain("passwordHash", PASSWORD);
    }

    @Test
    void logoutInvalidatesAuthenticatedSessionAndCookie() throws Exception {
        String email = email();
        service.register(registration(email, null));
        var session = (MockHttpSession) login(email, false).getRequest().getSession();
        Csrf token = csrf(session);
        var logout = mvc.perform(post("/api/auth/logout").session(session).header(token.header(), token.token()))
                .andExpect(status().isNoContent()).andReturn();
        assertThat(session.isInvalid()).isTrue();
        assertThat(logout.getResponse().getHeader("Set-Cookie")).contains("Max-Age=0");
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveOrChangedCredentialsInvalidateExistingSession() throws Exception {
        String email = email();
        var customer = service.register(registration(email, null));
        var session = (MockHttpSession) login(email, false).getRequest().getSession();
        var account = accounts.findById(customer.getAccountId()).orElseThrow();
        account.setCredentialsVersion(account.getCredentialsVersion() + 1);
        accounts.saveAndFlush(account);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
        assertThat(session.isInvalid()).isTrue();
        var fresh = (MockHttpSession) login(email, false).getRequest().getSession();
        account.setStatus(AccountStatus.LOCKED);
        accounts.saveAndFlush(account);
        mvc.perform(get("/api/auth/me").session(fresh)).andExpect(status().isUnauthorized());
        assertThat(fresh.isInvalid()).isTrue();
        Csrf token = csrf(null);
        mvc.perform(post("/api/auth/login").session(token.session()).header(token.header(), token.token())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("identifier", email, "password", PASSWORD))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void employeeLoginWorksWithoutCustomerProfileAndHasNoAutomaticGrants() throws Exception {
        String email = email();
        var account = accounts.saveAndFlush(AccountEntity.builder().username(email).email(email)
                .passwordHash(encoder.encode(PASSWORD)).accountType(AccountType.EMPLOYEE).build());
        var employee = employees.saveAndFlush(EmployeeEntity.builder().account(account).fullName("Nhân viên test")
                .internalEmail(email).status(ActiveStatus.ACTIVE).build());
        var result = login(email, false);
        var data = json.readTree(result.getResponse().getContentAsString());
        assertThat(data.get("employeeId").asLong()).isEqualTo(employee.getId());
        assertThat(data.get("customerId").isNull()).isTrue();
        assertThat(data.get("permissions").size()).isZero();
        mvc.perform(get("/api/employees").session((MockHttpSession) result.getRequest().getSession()))
                .andExpect(status().isForbidden());
    }

    @Test
    void csrfAndAnonymousPrivateAccessAreEnforced() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized()).andExpect(jsonPath("code").value("UNAUTHENTICATED"));
        mvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("identifier", email(), "password", PASSWORD))))
                .andExpect(status().isForbidden());
    }

    @Test
    void badCredentialsHaveSameSafeErrorAndCannotCreateIdentity() throws Exception {
        String email = email();
        service.register(registration(email, null));
        Csrf token = csrf(null);
        var result = mvc.perform(post("/api/auth/login").session(token.session()).header(token.header(), token.token())
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("identifier", email, "password", "Wrong1234"))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("code").value("INVALID_CREDENTIALS")).andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain(email, "Wrong1234", "passwordHash");
        mvc.perform(get("/api/auth/me").session(token.session())).andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedFailedLoginsAreThrottledWithoutCreatingIdentity() throws Exception {
        String unknown = email();
        String remote = UUID.randomUUID().toString();
        Csrf token = csrf(null);
        for (int index = 0; index < 10; index++) {
            mvc.perform(post("/api/auth/login").session(token.session()).header(token.header(), token.token())
                    .with(request -> { request.setRemoteAddr(remote); return request; })
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("identifier", unknown, "password", PASSWORD))))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").session(token.session()).header(token.header(), token.token())
                .with(request -> { request.setRemoteAddr(remote); return request; })
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("identifier", unknown, "password", PASSWORD))))
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("code").value("TOO_MANY_ATTEMPTS"));
        mvc.perform(get("/api/auth/me").session(token.session())).andExpect(status().isUnauthorized());
    }

    @Test
    void emptyCredentialsAndBcryptByteLimitReturnControlledValidationErrors() throws Exception {
        Csrf token = csrf(null);
        mvc.perform(post("/api/auth/login").session(token.session()).header(token.header(), token.token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"identifier\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("fieldErrors.identifier").exists());
        var request = registration(email(), null);
        request.setPassword("é".repeat(40) + "A1");
        request.setConfirmPassword(request.getPassword());
        mvc.perform(post("/api/auth/register").session(token.session()).header(token.header(), token.token())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        assertThat(accounts.findByEmail(request.getEmail())).isEmpty();
    }

    @Test
    void registrationRejectsValidationMismatchAndTermsBeforeWriting() throws Exception {
        long before = accounts.count();
        var request = registration(email(), null);
        request.setPassword("short1");
        request.setTermsAccepted(false);
        Csrf token = csrf(null);
        mvc.perform(post("/api/auth/register").session(token.session()).header(token.header(), token.token())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("fieldErrors.password").exists())
                .andExpect(jsonPath("fieldErrors.termsAccepted").exists());
        assertThat(accounts.count()).isEqualTo(before);
        var mismatch = registration(email(), null);
        mismatch.setConfirmPassword("Different123");
        assertThatThrownBy(() -> service.register(mismatch)).isInstanceOf(RuntimeException.class);
        assertThat(accounts.count()).isEqualTo(before);
    }

    @Test
    void concurrentDuplicateRegistrationCreatesExactlyOneAccountAndProfile() throws Exception {
        long accountCount = accounts.count();
        long customerCount = customers.count();
        String email = email();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> register = () -> {
            ready.countDown();
            if (!start.await(10, java.util.concurrent.TimeUnit.SECONDS)) throw new IllegalStateException("Start latch timed out");
            try { service.register(registration(email, null)); return true; }
            catch (ConflictException conflict) { return false; }
        };
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(register);
            var second = pool.submit(register);
            assertThat(ready.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(java.util.List.of(first.get(30, java.util.concurrent.TimeUnit.SECONDS),
                    second.get(30, java.util.concurrent.TimeUnit.SECONDS))).containsExactlyInAnyOrder(true, false);
        }
        assertThat(accounts.count()).isEqualTo(accountCount + 1);
        assertThat(customers.count()).isEqualTo(customerCount + 1);
    }

    @Test
    void lateCustomerInsertFailureRollsBackAccount() {
        long before = accounts.count();
        var request = registration(email(), null);
        request.setFullName("x".repeat(300)); // exceeds the persisted customer column after account has been flushed
        assertThatThrownBy(() -> service.register(request)).isInstanceOf(ConflictException.class);
        assertThat(accounts.count()).isEqualTo(before);
        assertThat(accounts.findByEmail(request.getEmail())).isEmpty();
    }

    @Test
    void legacyPlaintextPasswordNeverAuthenticates() throws Exception {
        String email = email();
        var account = accounts.saveAndFlush(AccountEntity.builder().email(email).username(email)
                .passwordHash(PASSWORD).accountType(AccountType.CUSTOMER).build());
        customers.saveAndFlush(CustomerEntity.builder().account(account).fullName("Legacy").build());
        Csrf token = csrf(null);
        mvc.perform(post("/api/auth/login").session(token.session()).header(token.header(), token.token())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("identifier", email, "password", PASSWORD))))
                .andExpect(status().isUnauthorized());
        assertThat(accounts.findById(account.getId()).orElseThrow().getPasswordHash()).isEqualTo(PASSWORD);
    }
}
