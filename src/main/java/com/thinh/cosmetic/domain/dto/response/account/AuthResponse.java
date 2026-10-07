package com.thinh.cosmetic.domain.dto.response.account;

import com.thinh.cosmetic.domain.enums.AccountStatus;
import com.thinh.cosmetic.domain.enums.AccountType;
import java.util.Set;

public record AuthResponse(Long accountId, String email, String phone, AccountType accountType,
                           AccountStatus status, Long customerId, Long employeeId, String fullName,
                           Set<String> roles, Set<String> permissions, Set<Long> storeIds) {
}
