package com.thinh.cosmetic.config;

import com.thinh.cosmetic.domain.entity.account.*;
import com.thinh.cosmetic.domain.entity.store.StoreEntity;
import com.thinh.cosmetic.domain.enums.AccountStatus;
import com.thinh.cosmetic.domain.enums.AccountType;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.repository.account.*;
import com.thinh.cosmetic.repository.store.StoreRepository;
import com.thinh.cosmetic.security.PasswordPolicy;
import com.thinh.cosmetic.security.PermissionCatalog;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Explicit one-time bootstrap for a fresh real database; never part of normal startup. */
@Component
@Profile("bootstrap-admin & !demo")
@RequiredArgsConstructor
public class InitialAdminBootstrap implements ApplicationRunner {
    private final AccountRepository accounts;
    private final EmployeeRepository employees;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final RolePermissionRepository rolePermissions;
    private final EmployeeRoleRepository employeeRoles;
    private final EmployeeStoreRepository employeeStores;
    private final StoreRepository stores;
    private final PasswordEncoder encoder;

    @Value("${INITIAL_ADMIN_NAME:}") private String adminName;
    @Value("${INITIAL_ADMIN_EMAIL:}") private String adminEmail;
    @Value("${INITIAL_ADMIN_PHONE:}") private String adminPhone;
    @Value("${INITIAL_ADMIN_PASSWORD:}") private String adminPassword;
    @Value("${INITIAL_STORE_NAME:}") private String storeName;
    @Value("${INITIAL_STORE_ADDRESS:}") private String storeAddress;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (roles.count() != 0 || permissions.count() != 0 || employees.count() != 0 || stores.count() != 0) {
            throw new IllegalStateException("Initial ADMIN bootstrap requires empty staff, role, permission and store tables.");
        }
        String email = PasswordPolicy.normalizeEmail(adminEmail);
        if (email == null || email.length() > 254 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Set a valid INITIAL_ADMIN_EMAIL.");
        }
        if (adminName == null || adminName.isBlank() || adminName.trim().length() > 150) {
            throw new IllegalArgumentException("Set INITIAL_ADMIN_NAME (maximum 150 characters).");
        }
        if (storeName == null || storeName.isBlank() || storeName.trim().length() > 255
                || storeAddress == null || storeAddress.isBlank()) {
            throw new IllegalArgumentException("Set INITIAL_STORE_NAME and INITIAL_STORE_ADDRESS.");
        }
        PasswordPolicy.validatePassword(adminPassword);
        String phone = PasswordPolicy.normalizePhone(adminPhone);
        if (accounts.existsByEmailIgnoreCase(email) || accounts.existsByUsername(email)
                || (phone != null && accounts.existsByPhone(phone))) {
            throw new IllegalStateException("Initial ADMIN email or phone already belongs to an account.");
        }

        Map<String, PermissionEntity> permissionByCode = new LinkedHashMap<>();
        for (String code : PermissionCatalog.ALL) {
            permissionByCode.put(code, permissions.save(PermissionEntity.builder().code(code).name(code).build()));
        }
        Map<String, RoleEntity> roleByName = new LinkedHashMap<>();
        PermissionCatalog.ROLES.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            RoleEntity role = roles.save(RoleEntity.builder().name(entry.getKey())
                    .description("Vai trò " + entry.getKey()).build());
            roleByName.put(entry.getKey(), role);
            for (String code : entry.getValue()) {
                rolePermissions.save(RolePermissionEntity.builder().role(role)
                        .permission(permissionByCode.get(code)).build());
            }
        });
        StoreEntity store = stores.save(StoreEntity.builder().name(storeName.trim())
                .address(storeAddress.trim()).status(ActiveStatus.ACTIVE).build());
        AccountEntity account = accounts.save(AccountEntity.builder().username(email).email(email).phone(phone)
                .passwordHash(encoder.encode(adminPassword)).accountType(AccountType.EMPLOYEE)
                .status(AccountStatus.ACTIVE).build());
        EmployeeEntity employee = employees.save(EmployeeEntity.builder().account(account)
                .fullName(adminName.trim()).internalEmail(email).phone(phone)
                .status(ActiveStatus.ACTIVE).build());
        employeeRoles.save(EmployeeRoleEntity.builder().employee(employee).role(roleByName.get("ADMIN")).build());
        employeeStores.save(EmployeeStoreEntity.builder().employee(employee).store(store)
                .isPrimaryBranch(true).build());
        System.out.println("Initial LUNEA ADMIN and store created.");
    }
}
