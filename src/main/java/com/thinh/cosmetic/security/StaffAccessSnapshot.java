package com.thinh.cosmetic.security;

import java.util.Set;

public record StaffAccessSnapshot(Set<String> roleNames, Set<String> permissionCodes, Set<Long> storeIds) {
    public StaffAccessSnapshot {
        roleNames = Set.copyOf(roleNames);
        permissionCodes = Set.copyOf(permissionCodes);
        storeIds = Set.copyOf(storeIds);
    }
}
