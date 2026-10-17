package com.thinh.cosmetic.security;

import com.thinh.cosmetic.domain.entity.account.*;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.domain.enums.AccountStatus;
import com.thinh.cosmetic.repository.account.*;
import com.thinh.cosmetic.repository.purchase.PurchaseOrderRepository;
import com.thinh.cosmetic.repository.store.StockTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashSet;
import java.util.Set;

@Component("permissionPolicy")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionPolicy {
    private final CurrentAccountResolver currentAccount;
    private final EmployeeRepository employees;
    private final EmployeeRoleRepository employeeRoles;
    private final EmployeeStoreRepository employeeStores;
    private final RolePermissionRepository rolePermissions;
    private final PurchaseOrderRepository purchases;
    private final StockTransferRepository transfers;

    /** Read database grants on every request so grant/revoke takes effect in existing sessions. */
    public StaffAccessSnapshot describe(long employeeId) {
        EmployeeEntity employee = employees.findById(employeeId).orElseThrow(() -> denied());
        if (employee.getStatus() != ActiveStatus.ACTIVE || employee.getAccount() == null
                || employee.getAccount().getStatus() != AccountStatus.ACTIVE) throw denied();
        Set<String> roles = new LinkedHashSet<>();
        Set<String> permissions = new LinkedHashSet<>();
        employeeRoles.findByEmployeeId(employeeId).forEach(link -> {
            roles.add(link.getRole().getName());
            rolePermissions.findByRoleId(link.getRole().getId()).forEach(grant -> permissions.add(grant.getPermission().getCode()));
        });
        Set<Long> stores = new LinkedHashSet<>();
        employeeStores.findByEmployeeId(employeeId).stream()
                .filter(link -> link.getStore().getStatus() == ActiveStatus.ACTIVE)
                .forEach(link -> stores.add(link.getStore().getId()));
        return new StaffAccessSnapshot(roles, permissions, stores);
    }

    public StaffAccessSnapshot current() {
        var principal = currentAccount.requireAccount();
        long employeeId = currentAccount.requireEmployeeId();
        var employee = employees.findById(employeeId).orElseThrow(PermissionPolicy::denied);
        if (employee.getAccount() == null || !employee.getAccount().getId().equals(principal.getAccountId())
                || employee.getAccount().getCredentialsVersion() != principal.getCredentialsVersion()) throw denied();
        return describe(employeeId);
    }
    public long employeeId() { return currentAccount.requireEmployeeId(); }
    public boolean has(String permission) { return current().permissionCodes().contains(permission); }
    public void require(String permission) { if (!has(permission)) throw denied(); }
    public boolean isAdmin() { return current().roleNames().contains("ADMIN"); }
    public boolean canStore(String permission, Long storeId) {
        StaffAccessSnapshot access = current();
        return storeId != null && access.permissionCodes().contains(permission)
                && (access.roleNames().contains("ADMIN") || access.storeIds().contains(storeId));
    }
    public void requireStore(String permission, Long storeId) { if (!canStore(permission, storeId)) throw denied(); }
    public Set<Long> storeIds() { return current().storeIds(); }
    public boolean canPurchase(String permission, Long id) {
        require(permission);
        return purchases.findById(id).map(p -> canStore(permission, p.getReceivingStore().getId())).orElse(false);
    }
    public boolean canTransfer(String permission, Long id) {
        require(permission);
        return transfers.findById(id).map(t -> canStore(permission, t.getSourceStore().getId())
                && canStore(permission, t.getDestinationStore().getId())).orElse(false);
    }
    public boolean isCurrentEmployee(Long id) { return id != null && id.equals(currentAccount.requireEmployeeId()); }
    public static AccessDeniedException denied() { return new AccessDeniedException("You do not have permission for this action"); }
}
