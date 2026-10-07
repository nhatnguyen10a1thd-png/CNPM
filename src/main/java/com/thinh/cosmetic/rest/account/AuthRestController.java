package com.thinh.cosmetic.rest.account;

import com.thinh.cosmetic.domain.dto.request.account.LoginRequest;
import com.thinh.cosmetic.domain.dto.request.account.RegisterRequest;
import com.thinh.cosmetic.domain.dto.response.account.CustomerResponse;
import com.thinh.cosmetic.domain.dto.response.account.AuthResponse;
import com.thinh.cosmetic.service.account.AccountService;
import com.thinh.cosmetic.security.AuthAttemptLimiter;
import com.thinh.cosmetic.security.CurrentAccountResolver;
import com.thinh.cosmetic.security.PasswordPolicy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {
    private final AccountService accountService;
    private final CurrentAccountResolver currentAccount;
    private final SecurityContextRepository contexts;
    private final CsrfTokenRepository csrfTokens;
    private final AuthAttemptLimiter attempts;
    @Value("${lunea.auth.session-timeout:${server.servlet.session.timeout:30m}}")
    private Duration sessionTimeout;
    @Value("${lunea.auth.remember-timeout:7d}")
    private Duration rememberTimeout;
    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean secureCookie;

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName(), "parameterName", token.getParameterName());
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        attempts.check("register:" + servletRequest.getRemoteAddr(), 10, Duration.ofHours(1));
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        String attemptKey = "login:identifier:" + PasswordPolicy.normalizeLoginIdentifier(request.getIdentifier());
        attempts.check("login:ip:" + servletRequest.getRemoteAddr(), 60, Duration.ofMinutes(15));
        attempts.check(attemptKey, 10, Duration.ofMinutes(15));
        var principal = accountService.login(request);
        attempts.clear(attemptKey);
        AuthResponse result = accountService.describe(principal);
        // Rotate both the session and CSRF secret after successful authentication.
        var previous = servletRequest.getSession(false);
        if (previous != null) previous.invalidate();
        var session = servletRequest.getSession(true);
        Duration timeout = request.isRememberMe() ? rememberTimeout : sessionTimeout;
        session.setMaxInactiveInterval(Math.toIntExact(timeout.toSeconds()));
        var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.getAccountType().name())));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, servletRequest, servletResponse);
        csrfTokens.saveToken(null, servletRequest, servletResponse);
        String path = servletRequest.getContextPath().isEmpty() ? "/" : servletRequest.getContextPath();
        var cookie = ResponseCookie.from("JSESSIONID", session.getId()).path(path).httpOnly(true)
                .secure(secureCookie || servletRequest.isSecure()).sameSite("Lax");
        if (request.isRememberMe()) cookie.maxAge(rememberTimeout);
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.build().toString());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    public AuthResponse me() { return accountService.describe(currentAccount.requireAccount()); }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        String path = request.getContextPath().isEmpty() ? "/" : request.getContextPath();
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from("JSESSIONID", "").path(path)
                .httpOnly(true).secure(secureCookie || request.isSecure()).sameSite("Lax").maxAge(0).build().toString());
        return ResponseEntity.noContent().build();
    }
}
