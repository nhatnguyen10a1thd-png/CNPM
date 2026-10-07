package com.thinh.cosmetic.security;

import com.thinh.cosmetic.domain.enums.AccountType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentAccountResolver {
    public AccountPrincipal requireAccount() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AccountPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException("Vui lòng đăng nhập");
        }
        return principal;
    }

    public long requireCustomerId() {
        var principal = requireAccount();
        if (principal.getAccountType() != AccountType.CUSTOMER || principal.getCustomerId() == null) {
            throw new AccessDeniedException("Tài khoản khách hàng được yêu cầu");
        }
        return principal.getCustomerId();
    }

    public long requireEmployeeId() {
        var principal = requireAccount();
        if (principal.getAccountType() != AccountType.EMPLOYEE || principal.getEmployeeId() == null) {
            throw new AccessDeniedException("Tài khoản nhân viên được yêu cầu");
        }
        return principal.getEmployeeId();
    }
}
