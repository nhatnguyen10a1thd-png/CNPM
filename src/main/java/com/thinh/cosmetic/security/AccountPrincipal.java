package com.thinh.cosmetic.security;

import com.thinh.cosmetic.domain.enums.AccountType;
import java.io.Serializable;
import java.security.Principal;
import lombok.Getter;

@Getter
public final class AccountPrincipal implements Principal, Serializable {
    private static final long serialVersionUID = 1L;
    private final Long accountId;
    private final AccountType accountType;
    private final Long customerId;
    private final Long employeeId;
    private final long credentialsVersion;

    public AccountPrincipal(Long accountId, AccountType accountType, Long customerId,
                            Long employeeId, long credentialsVersion) {
        this.accountId = accountId;
        this.accountType = accountType;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.credentialsVersion = credentialsVersion;
    }

    @Override
    public String getName() {
        return accountId.toString();
    }
}
