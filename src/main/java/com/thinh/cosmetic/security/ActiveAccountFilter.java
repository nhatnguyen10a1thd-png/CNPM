package com.thinh.cosmetic.security;

import com.thinh.cosmetic.domain.enums.AccountStatus;
import com.thinh.cosmetic.domain.enums.AccountType;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.repository.account.AccountRepository;
import com.thinh.cosmetic.repository.account.EmployeeRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class ActiveAccountFilter extends OncePerRequestFilter {
    private final AccountRepository accounts;
    private final EmployeeRepository employees;

    public ActiveAccountFilter(AccountRepository accounts, EmployeeRepository employees) {
        this.accounts = accounts;
        this.employees = employees;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AccountPrincipal principal) {
            boolean active = accounts.findById(principal.getAccountId()).map(account ->
                    account.getStatus() == AccountStatus.ACTIVE
                            && account.getCredentialsVersion() == principal.getCredentialsVersion()
                            && account.getAccountType() == principal.getAccountType()).orElse(false);
            if (active && principal.getAccountType() == AccountType.EMPLOYEE) {
                active = employees.findByAccountId(principal.getAccountId())
                        .map(employee -> employee.getStatus() == ActiveStatus.ACTIVE
                                && employee.getId().equals(principal.getEmployeeId())).orElse(false);
            }
            if (!active) {
                SecurityContextHolder.clearContext();
                var session = request.getSession(false);
                if (session != null) session.invalidate();
            }
        }
        chain.doFilter(request, response);
    }
}
