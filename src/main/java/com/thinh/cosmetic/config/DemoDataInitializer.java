package com.thinh.cosmetic.config;

import com.thinh.cosmetic.domain.entity.account.*;
import com.thinh.cosmetic.domain.entity.store.StoreEntity;
import com.thinh.cosmetic.domain.enums.*;
import com.thinh.cosmetic.repository.account.*;
import com.thinh.cosmetic.repository.store.StoreRepository;
import com.thinh.cosmetic.security.PermissionCatalog;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Fixtures only exist in the loopback-only, disposable demo profile. */
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {
    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final EmployeeRepository employees;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final RolePermissionRepository rolePermissions;
    private final EmployeeRoleRepository employeeRoles;
    private final EmployeeStoreRepository employeeStores;
    private final StoreRepository stores;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (accounts.count() != 0) return;
        Map<String, PermissionEntity> permissionByCode = new LinkedHashMap<>();
        for (String code : PermissionCatalog.ALL) {
            permissionByCode.put(code, permissions.save(PermissionEntity.builder().code(code).name(code).build()));
        }
        Map<String, RoleEntity> roleByName = new LinkedHashMap<>();
        PermissionCatalog.ROLES.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            RoleEntity role = roles.save(RoleEntity.builder().name(entry.getKey()).description("Vai trò demo " + entry.getKey()).build());
            roleByName.put(entry.getKey(), role);
            for (String code : entry.getValue()) {
                rolePermissions.save(RolePermissionEntity.builder().role(role).permission(permissionByCode.get(code)).build());
            }
        });
        StoreEntity store1 = stores.save(StoreEntity.builder().name("LUNEA Quận 1").address("123 Lê Lợi, TP.HCM")
                .phone("02812345678").operatingHours("08:00–21:00").status(ActiveStatus.ACTIVE).build());
        stores.save(StoreEntity.builder().name("LUNEA Quận 3").address("456 Võ Văn Tần, TP.HCM")
                .phone("02887654321").operatingHours("08:00–21:00").status(ActiveStatus.ACTIVE).build());
        employee("admin@lunea.test", "DemoAdmin123", "Quản trị demo", "0900000001", roleByName.get("ADMIN"), store1);
        employee("kho@lunea.test", "KhoDemo123", "Nhân viên kho demo", "0900000002", roleByName.get("QLTK"), store1);
        AccountEntity customer = account("customer@lunea.test", "DemoUser123", "0900000003", AccountType.CUSTOMER);
        customers.save(CustomerEntity.builder().account(customer).fullName("Khách hàng demo")
                .loyaltyPoints(0).joinDate(LocalDate.now()).build());
    }

    private AccountEntity account(String email, String password, String phone, AccountType type) {
        return accounts.save(AccountEntity.builder().username(email).email(email).phone(phone)
                .passwordHash(encoder.encode(password)).accountType(type).status(AccountStatus.ACTIVE).build());
    }

    private void employee(String email, String password, String name, String phone, RoleEntity role, StoreEntity store) {
        EmployeeEntity employee = employees.save(EmployeeEntity.builder()
                .account(account(email, password, phone, AccountType.EMPLOYEE)).fullName(name)
                .internalEmail(email).phone(phone).status(ActiveStatus.ACTIVE).build());
        employeeRoles.save(EmployeeRoleEntity.builder().employee(employee).role(role).build());
        employeeStores.save(EmployeeStoreEntity.builder().employee(employee).store(store).isPrimaryBranch(true).build());
    }
}
