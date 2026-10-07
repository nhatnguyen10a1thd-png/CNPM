package com.thinh.cosmetic.config;

import com.thinh.cosmetic.repository.account.AccountRepository;
import com.thinh.cosmetic.repository.account.EmployeeRepository;
import com.thinh.cosmetic.security.ActiveAccountFilter;
import com.thinh.cosmetic.security.SecurityErrorWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(10); }

    @Bean
    SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }

    @Bean
    CsrfTokenRepository csrfTokenRepository() { return new HttpSessionCsrfTokenRepository(); }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contexts,
                                          CsrfTokenRepository csrfTokens, ObjectMapper mapper,
                                          AccountRepository accounts, EmployeeRepository employees,
                                          Environment environment) throws Exception {
        http.formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .securityContext(context -> context.securityContextRepository(contexts))
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokens))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> SecurityErrorWriter.write(
                                mapper, request, response, 401, "UNAUTHENTICATED", "Vui lòng đăng nhập"))
                        .accessDeniedHandler((request, response, exception) -> SecurityErrorWriter.write(
                                mapper, request, response, 403, "ACCESS_DENIED", "Bạn không có quyền thực hiện thao tác này")))
                .addFilterBefore(new ActiveAccountFilter(accounts, employees), AnonymousAuthenticationFilter.class)
                .authorizeHttpRequests(routes -> {
                    routes.requestMatchers("/", "/index.html", "/app.js", "/app.css", "/styles.css", "/favicon.ico", "/assets/**", "/error").permitAll();
                    routes.requestMatchers(HttpMethod.GET, "/api/auth/csrf", "/api/health").permitAll();
                    routes.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/recovery/request", "/api/auth/recovery/reset").permitAll();
                    routes.requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*", "/api/categories", "/api/categories/*", "/api/brands", "/api/brands/*").permitAll();
                    if (environment.acceptsProfiles(Profiles.of("demo"))) {
                        routes.requestMatchers(HttpMethod.GET, "/api/demo/recovery/messages").permitAll();
                    }
                    routes.requestMatchers("/api/auth/me", "/api/auth/logout").authenticated();
                    // Staff services apply permission AND store scope. Customer domains remain closed until their ownership phases.
                    routes.requestMatchers("/api/employees/**", "/api/roles/**", "/api/permissions/**",
                            "/api/stores/**", "/api/inventory/**", "/api/purchase-orders/**", "/api/stock-transfers/**",
                            "/api/suppliers/**", "/api/vouchers/**").hasRole("EMPLOYEE");
                    routes.requestMatchers("/api/products/**", "/api/categories/**", "/api/brands/**").hasRole("EMPLOYEE");
                    routes.anyRequest().denyAll();
                });
        return http.build();
    }
}
