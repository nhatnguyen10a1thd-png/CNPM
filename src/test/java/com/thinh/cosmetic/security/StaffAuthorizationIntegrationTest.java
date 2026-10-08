package com.thinh.cosmetic.security;

import com.thinh.cosmetic.domain.dto.request.account.EmployeeRequest;
import com.thinh.cosmetic.domain.entity.account.*;
import com.thinh.cosmetic.domain.entity.store.StoreEntity;
import com.thinh.cosmetic.domain.entity.store.StockTransferEntity;
import com.thinh.cosmetic.domain.entity.purchase.PurchaseOrderEntity;
import com.thinh.cosmetic.domain.entity.purchase.SupplierEntity;
import com.thinh.cosmetic.domain.enums.*;
import com.thinh.cosmetic.repository.account.*;
import com.thinh.cosmetic.repository.store.StoreRepository;
import com.thinh.cosmetic.repository.store.StockTransferRepository;
import com.thinh.cosmetic.repository.purchase.PurchaseOrderRepository;
import com.thinh.cosmetic.repository.purchase.SupplierRepository;
import com.thinh.cosmetic.service.account.EmployeeService;
import com.thinh.cosmetic.service.account.RoleGrantService;
import com.thinh.cosmetic.service.catalog.ProductService;
import com.thinh.cosmetic.service.catalog.CategoryService;
import com.thinh.cosmetic.service.catalog.BrandService;
import com.thinh.cosmetic.service.purchase.SupplierService;
import com.thinh.cosmetic.service.order.VoucherService;
import com.thinh.cosmetic.service.store.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StaffAuthorizationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AccountRepository accounts;
    @Autowired EmployeeRepository employees;
    @Autowired CustomerRepository customers;
    @Autowired RoleRepository roles;
    @Autowired PermissionRepository permissions;
    @Autowired RolePermissionRepository rolePermissions;
    @Autowired EmployeeRoleRepository employeeRoles;
    @Autowired EmployeeStoreRepository employeeStores;
    @Autowired StoreRepository stores;
    @Autowired StockTransferRepository transfers;
    @Autowired PurchaseOrderRepository purchases;
    @Autowired SupplierRepository suppliers;
    @Autowired PasswordEncoder encoder;
    @Autowired EmployeeService employeeService;
    @Autowired RoleGrantService roleGrantService;
    @Autowired PermissionPolicy policy;
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired InventoryService inventoryService;
    @Autowired ProductService productService;
    @Autowired CategoryService categoryService;
    @Autowired BrandService brandService;
    @Autowired SupplierService supplierService;
    @Autowired VoucherService voucherService;
    @MockitoSpyBean AutditLogRepository auditLogs;
    private static final String PASSWORD = "StaffPass123";

    private RoleEntity role(String name, String... codes) {
        RoleEntity role = roles.findByName(name).orElseGet(() -> roles.saveAndFlush(RoleEntity.builder().name(name).build()));
        for (String code : codes) {
            PermissionEntity permission = permissions.findByCode(code).orElseGet(() -> permissions.saveAndFlush(PermissionEntity.builder().code(code).name(code).build()));
            var id = new RolePermissionId(role.getId(), permission.getId());
            if (!rolePermissions.existsById(id)) rolePermissions.saveAndFlush(RolePermissionEntity.builder().role(role).permission(permission).build());
        }
        return role;
    }
    private RoleEntity customRole(String... codes) { return role("TEST_" + UUID.randomUUID(), codes); }
    private EmployeeEntity employee(RoleEntity role) {
        String email = "staff-" + UUID.randomUUID() + "@lunea.test";
        var account = accounts.saveAndFlush(AccountEntity.builder().username(email).email(email).passwordHash(encoder.encode(PASSWORD))
                .accountType(AccountType.EMPLOYEE).status(AccountStatus.ACTIVE).build());
        var employee = employees.saveAndFlush(EmployeeEntity.builder().account(account).internalEmail(email).fullName("Staff Test").status(ActiveStatus.ACTIVE).build());
        if (role != null) employeeRoles.saveAndFlush(EmployeeRoleEntity.builder().employee(employee).role(role).build());
        return employee;
    }
    private EmployeeEntity admin() { return employee(role("ADMIN", PermissionCatalog.ALL.toArray(String[]::new))); }
    private StoreEntity store() { return stores.saveAndFlush(StoreEntity.builder().name("Branch " + UUID.randomUUID()).status(ActiveStatus.ACTIVE).build()); }
    private MockHttpSession session(EmployeeEntity employee) {
        var principal = new AccountPrincipal(employee.getAccount().getId(), AccountType.EMPLOYEE, null, employee.getId(), employee.getAccount().getCredentialsVersion());
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        var context = SecurityContextHolder.createEmptyContext(); context.setAuthentication(auth);
        var session = new MockHttpSession(); session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        return session;
    }
    private EmployeeRequest request(RoleEntity role, StoreEntity store) {
        return EmployeeRequest.builder().fullName("New Staff").email("new-" + UUID.randomUUID() + "@lunea.test")
                .password(PASSWORD).roleIds(role == null ? List.of() : List.of(role.getId()))
                .storeIds(store == null ? List.of() : List.of(store.getId())).primaryStoreId(store == null ? null : store.getId()).status(ActiveStatus.ACTIVE).build();
    }
    private EmployeeRequest edit(EmployeeEntity employee, List<Long> roleIds) {
        return EmployeeRequest.builder().fullName(employee.getFullName()).email(employee.getAccount().getEmail())
                .internalEmail(employee.getInternalEmail()).status(employee.getStatus()).roleIds(roleIds).storeIds(List.of()).build();
    }
    private String body(Object value) { return json.writeValueAsString(value); }

    private void as(EmployeeEntity actor) {
        SecurityContextHolder.setContext((org.springframework.security.core.context.SecurityContext)
                session(actor).getAttribute("SPRING_SECURITY_CONTEXT"));
    }

    @Test
    void globalServicesAllowTheirPermissionsWithoutBranchAssignmentsAndKeepPublicReads() throws Exception {
        var worker = employee(customRole("CATALOG_MANAGE", "SUPPLIER_READ", "SUPPLIER_MANAGE", "PROMOTION_READ", "PROMOTION_MANAGE"));
        as(worker);
        try {
            var brandRequest = com.thinh.cosmetic.domain.dto.request.catalog.BrandRequest.builder().name("RBAC brand").status(ActiveStatus.ACTIVE).build();
            var categoryRequest = com.thinh.cosmetic.domain.dto.request.catalog.CategoryRequest.builder().name("RBAC category").status(ActiveStatus.ACTIVE).build();
            var brand = brandService.create(brandRequest);
            var category = categoryService.create(categoryRequest);
            var productRequest = com.thinh.cosmetic.domain.dto.request.catalog.ProductRequest.builder().name("RBAC product")
                    .brandId(brand.getId()).categoryId(category.getId()).status(ActiveStatus.ACTIVE).build();
            var product = productService.create(productRequest);
            assertThat(productService.update(product.getId(), productRequest).getId()).isEqualTo(product.getId());
            productService.delete(product.getId());
            brandService.update(brand.getId(), brandRequest); brandService.delete(brand.getId());
            categoryService.update(category.getId(), categoryRequest); categoryService.delete(category.getId());
            var supplierRequest = com.thinh.cosmetic.domain.dto.request.purchase.SupplierRequest.builder().name("RBAC supplier").build();
            var supplier = supplierService.create(supplierRequest);
            assertThat(supplierService.getById(supplier.getId()).getId()).isEqualTo(supplier.getId());
            assertThat(supplierService.getAll()).extracting(s -> s.getId()).contains(supplier.getId());
            supplierService.update(supplier.getId(), supplierRequest); supplierService.deactivate(supplier.getId());
            var voucherRequest = com.thinh.cosmetic.domain.dto.request.order.VoucherRequest.builder().code("RBAC-" + UUID.randomUUID())
                    .discountType(DiscountType.FIXED).discountValue(java.math.BigDecimal.ONE).build();
            var voucher = voucherService.create(voucherRequest);
            assertThat(voucherService.getById(voucher.getId()).getId()).isEqualTo(voucher.getId());
            assertThat(voucherService.getByCode(voucherRequest.getCode()).getId()).isEqualTo(voucher.getId());
            assertThat(voucherService.getAll()).extracting(v -> v.getId()).contains(voucher.getId());
            assertThat(voucherService.calculateDiscount(voucherRequest.getCode(), java.math.BigDecimal.TEN)).isEqualByComparingTo("1");
            voucherService.update(voucher.getId(), voucherRequest); voucherService.deactivate(voucher.getId());
            assertThat(policy.storeIds()).isEmpty();
        } finally { SecurityContextHolder.clearContext(); }
        assertThat(productService.getAll()).isNotNull();
        assertThat(brandService.getAll()).isNotNull();
        assertThat(categoryService.getAll()).isNotNull();
        assertThatThrownBy(() -> supplierService.getAll()).isInstanceOf(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void invalidStoresAndPermissionSetsPreserveExistingLinksAndAudit() throws Exception {
        var administrator = admin(); var role = customRole("INVENTORY_READ"); var target = employee(role); var assigned = store();
        employeeStores.saveAndFlush(EmployeeStoreEntity.builder().employee(target).store(assigned).isPrimaryBranch(true).build());
        var request = edit(target, List.of(role.getId()));
        request.setEmail("changed-" + UUID.randomUUID() + "@lunea.test");
        long auditCount = auditLogs.count();
        for (List<Long> ids : List.of(List.of(assigned.getId(), Long.MAX_VALUE), List.of(assigned.getId(), assigned.getId()), List.of(0L))) {
            request.setStoreIds(ids); request.setPrimaryStoreId(assigned.getId());
            mvc.perform(put("/api/employees/{id}", target.getId()).session(session(administrator)).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON).content(body(request))).andExpect(status().isBadRequest());
        }
        var inactive = store(); inactive.setStatus(ActiveStatus.INACTIVE); stores.saveAndFlush(inactive);
        request.setStoreIds(List.of(inactive.getId())); request.setPrimaryStoreId(inactive.getId());
        mvc.perform(put("/api/employees/{id}", target.getId()).session(session(administrator)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body(request))).andExpect(status().isBadRequest());
        assertThat(employeeStores.findByEmployeeId(target.getId())).extracting(s -> s.getStore().getId()).containsExactly(assigned.getId());
        assertThat(accounts.findById(target.getAccount().getId()).orElseThrow().getEmail()).isEqualTo(target.getAccount().getEmail());
        var permission = permissions.findByCode("INVENTORY_READ").orElseThrow();
        for (List<Long> ids : List.of(List.of(permission.getId(), Long.MAX_VALUE), List.of(permission.getId(), permission.getId()), List.of(-1L))) {
            mvc.perform(put("/api/roles/{id}/permissions", role.getId()).session(session(administrator)).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("permissionIds", ids)))).andExpect(status().isBadRequest());
        }
        var reserved = permissions.findByCode("ROLE_MANAGE").orElseThrow();
        mvc.perform(put("/api/roles/{id}/permissions", role.getId()).session(session(administrator)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("permissionIds", List.of(reserved.getId()))))).andExpect(status().isConflict());
        assertThat(rolePermissions.findByRoleId(role.getId())).extracting(p -> p.getPermission().getId()).containsExactly(permission.getId());
        assertThat(auditLogs.count()).isEqualTo(auditCount);
    }

    @Test
    void directStaffCommandsRejectWrongPermissionBindingAndRevokedIdentity() {
        var worker = employee(customRole("CATALOG_MANAGE"));
        as(worker);
        try {
            assertThatThrownBy(() -> employeeService.create(request(null, null))).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
            assertThatThrownBy(() -> roleGrantService.replacePermissions(Long.MAX_VALUE, List.of())).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        } finally { SecurityContextHolder.clearContext(); }
        var manager = employee(customRole("EMPLOYEE_MANAGE")); var other = employee(null);
        as(manager);
        var principal = new AccountPrincipal(other.getAccount().getId(), AccountType.EMPLOYEE, null, manager.getId(), manager.getAccount().getCredentialsVersion());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))));
        try { assertThatThrownBy(() -> employeeService.search(null, 0, 10)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class); }
        finally { SecurityContextHolder.clearContext(); }
        as(manager);
        var account = accounts.findById(manager.getAccount().getId()).orElseThrow(); account.setCredentialsVersion(account.getCredentialsVersion() + 1); accounts.saveAndFlush(account);
        try { assertThatThrownBy(() -> employeeService.search(null, 0, 10)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class); }
        finally { SecurityContextHolder.clearContext(); }
    }

    @Test
    void databaseAuditFailureRollsBackProfileScopeCredentialAndDeactivation() throws Exception {
        var administrator = admin(); var role = customRole("INVENTORY_READ"); var target = employee(role); var oldStore = store(); var newStore = store();
        employeeStores.saveAndFlush(EmployeeStoreEntity.builder().employee(target).store(oldStore).isPrimaryBranch(true).build());
        var original = accounts.findById(target.getAccount().getId()).orElseThrow();
        long auditCount = auditLogs.count();
        // Fail inside the real repository flush, after the profile and replacement links have been flushed.
        doAnswer(invocation -> {
            AuditLogEntity event = invocation.getArgument(0);
            event.setAction("X".repeat(300));
            entityManager.persist(event);
            entityManager.flush();
            return event;
        }).when(auditLogs).saveAndFlush(any(AuditLogEntity.class));
        var request = edit(target, List.of()); request.setFullName("Changed profile"); request.setPassword("ChangedPass123");
        request.setStoreIds(List.of(newStore.getId())); request.setPrimaryStoreId(newStore.getId()); request.setStatus(ActiveStatus.INACTIVE);
        var failure = mvc.perform(put("/api/employees/{id}", target.getId()).session(session(administrator)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body(request))).andExpect(status().isInternalServerError()).andReturn();
        assertThat(failure.getResolvedException()).isInstanceOf(jakarta.persistence.PersistenceException.class);
        mvc.perform(delete("/api/employees/{id}", target.getId()).session(session(administrator)).with(csrf())).andExpect(status().isInternalServerError());
        var after = accounts.findById(original.getId()).orElseThrow();
        assertThat(after.getPasswordHash()).isEqualTo(original.getPasswordHash());
        assertThat(after.getCredentialsVersion()).isEqualTo(original.getCredentialsVersion());
        assertThat(after.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(employees.findById(target.getId()).orElseThrow().getFullName()).isEqualTo(target.getFullName());
        assertThat(employees.findById(target.getId()).orElseThrow().getStatus()).isEqualTo(ActiveStatus.ACTIVE);
        assertThat(employeeRoles.findByEmployeeId(target.getId())).extracting(l -> l.getRole().getId()).containsExactly(role.getId());
        assertThat(employeeStores.findByEmployeeId(target.getId())).extracting(l -> l.getStore().getId()).containsExactly(oldStore.getId());
        assertThat(auditLogs.count()).isEqualTo(auditCount);
        mvc.perform(get("/api/auth/me").session(session(target))).andExpect(status().isOk());
    }

    @Test
    void concurrentEmployeeCreationCommitsExactlyOneIdentityAndAudit() throws Exception {
        var administrator = admin(); var request = request(customRole("EMPLOYEE_READ"), store());
        var ready = new CountDownLatch(2); var start = new CountDownLatch(1);
        var payload = body(request);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var results = new ArrayList<java.util.concurrent.Future<Integer>>();
            for (int i = 0; i < 2; i++) {
                var actorSession = session(administrator);
                results.add(pool.submit(() -> {
                    ready.countDown(); if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Concurrent start timed out");
                    return mvc.perform(post("/api/employees").session(actorSession).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(payload)).andReturn().getResponse().getStatus();
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue(); start.countDown();
            assertThat(List.of(results.get(0).get(30, TimeUnit.SECONDS), results.get(1).get(30, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
        }
        var account = accounts.findByEmail(request.getEmail()).orElseThrow();
        var target = employees.findByAccountId(account.getId()).orElseThrow();
        assertThat(employeeRoles.findByEmployeeId(target.getId())).hasSize(1);
        assertThat(employeeStores.findByEmployeeId(target.getId())).hasSize(1);
        assertThat(auditLogs.findAll().stream().filter(a -> "EMPLOYEE_CREATED".equals(a.getAction()) && target.getId().toString().equals(a.getObjectId())).count()).isEqualTo(1);
    }

    @Test
    void globalServicesCannotBypassControllerPermissions() {
        var worker = employee(customRole("INVENTORY_MANAGE"));
        SecurityContextHolder.setContext((org.springframework.security.core.context.SecurityContext)
                session(worker).getAttribute("SPRING_SECURITY_CONTEXT"));
        try {
            org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThatThrownBy(() -> productService.create(null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> productService.update(Long.MAX_VALUE, null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> productService.delete(Long.MAX_VALUE)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> categoryService.create(null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> categoryService.update(Long.MAX_VALUE, null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> categoryService.delete(Long.MAX_VALUE)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> brandService.create(null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> brandService.update(Long.MAX_VALUE, null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> brandService.delete(Long.MAX_VALUE)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> supplierService.create(null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> supplierService.getAll()).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> supplierService.getById(Long.MAX_VALUE)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> supplierService.update(Long.MAX_VALUE, null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> supplierService.deactivate(Long.MAX_VALUE)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> voucherService.create(null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> voucherService.getAll()).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> voucherService.getById(Long.MAX_VALUE)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> voucherService.getByCode("missing")).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> voucherService.update(Long.MAX_VALUE, null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> voucherService.deactivate(Long.MAX_VALUE)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class),
                () -> assertThatThrownBy(() -> voucherService.calculateDiscount("missing", java.math.BigDecimal.TEN)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
            );
        } finally { SecurityContextHolder.clearContext(); }
    }

    @Test
    void liveStoreAssignmentChangesApplyToExistingSessionAndAdminStillNeedsPermission() throws Exception {
        var administrator = admin(); var role = customRole("INVENTORY_READ"); var worker = employee(role); var branch = store();
        var workerSession = session(worker);
        mvc.perform(get("/api/inventory/store/{id}", branch.getId()).session(workerSession)).andExpect(status().isForbidden());
        var request = edit(worker, List.of(role.getId())); request.setStoreIds(List.of(branch.getId())); request.setPrimaryStoreId(branch.getId());
        mvc.perform(put("/api/employees/{id}", worker.getId()).session(session(administrator)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body(request))).andExpect(status().isOk());
        mvc.perform(get("/api/inventory/store/{id}", branch.getId()).session(workerSession)).andExpect(status().isOk());
        request.setStoreIds(List.of()); request.setPrimaryStoreId(null);
        mvc.perform(put("/api/employees/{id}", worker.getId()).session(session(administrator)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body(request))).andExpect(status().isOk());
        mvc.perform(get("/api/inventory/store/{id}", branch.getId()).session(workerSession)).andExpect(status().isForbidden());
        var adminRole = roles.findByName("ADMIN").orElseThrow(); var permission = permissions.findByCode("INVENTORY_READ").orElseThrow();
        var grantId = new RolePermissionId(adminRole.getId(), permission.getId());
        rolePermissions.deleteById(grantId);
        try { mvc.perform(get("/api/inventory/store/{id}", branch.getId()).session(session(administrator))).andExpect(status().isForbidden()); }
        finally { rolePermissions.saveAndFlush(RolePermissionEntity.builder().role(adminRole).permission(permission).build()); }
        mvc.perform(get("/api/inventory/store/{id}", branch.getId()).session(session(administrator))).andExpect(status().isOk());
    }

    @Test
    void employeeManagerCannotModifyPrivilegedEmployeesOrOwnScopeAndStatus() throws Exception {
        var manager = employee(role("QLNV", "EMPLOYEE_READ", "EMPLOYEE_MANAGE")); var administrator = admin(); var branch = store();
        var managerSession = session(manager);
        mvc.perform(put("/api/employees/{id}", administrator.getId()).session(managerSession).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body(edit(administrator, List.of())))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/employees/{id}", administrator.getId()).session(managerSession).with(csrf())).andExpect(status().isForbidden());
        var request = edit(manager, List.of(roles.findByName("QLNV").orElseThrow().getId()));
        request.setStoreIds(List.of(branch.getId())); request.setPrimaryStoreId(branch.getId());
        mvc.perform(put("/api/employees/{id}", manager.getId()).session(managerSession).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body(request))).andExpect(status().isForbidden());
        request.setStoreIds(List.of()); request.setPrimaryStoreId(null); request.setStatus(ActiveStatus.INACTIVE);
        mvc.perform(put("/api/employees/{id}", manager.getId()).session(managerSession).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body(request))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/employees/{id}", administrator.getId()).session(session(administrator)).with(csrf())).andExpect(status().isForbidden());
        assertThat(accounts.findById(manager.getAccount().getId()).orElseThrow().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(employeeStores.findByEmployeeId(manager.getId())).isEmpty();
    }

    @Test
    void createUsesSuppliedCredentialsPrimaryScopeAndPrincipalAudit() throws Exception {
        var admin = admin(); var role = customRole("INVENTORY_READ"); var store = store(); var request = request(role, store);
        var result = mvc.perform(post("/api/employees").session(session(admin)).with(csrf())
                        .param("employeeId", "999999").header("X-Forwarded-For", "8.8.8.8")
                        .with(req -> { req.setRemoteAddr("127.0.0.9"); return req; })
                        .contentType(MediaType.APPLICATION_JSON).content(body(request)))
                .andExpect(status().isCreated()).andExpect(jsonPath("email").value(request.getEmail()))
                .andExpect(jsonPath("primaryStoreId").value(store.getId().intValue())).andReturn();
        var data = json.readTree(result.getResponse().getContentAsString());
        var account = accounts.findByEmail(request.getEmail()).orElseThrow();
        assertThat(encoder.matches(PASSWORD, account.getPasswordHash())).isTrue();
        assertThat(result.getResponse().getContentAsString()).doesNotContain(PASSWORD, "passwordHash");
        var audit = auditLogs.findAll().stream().filter(a -> "EMPLOYEE_CREATED".equals(a.getAction()) && data.get("id").asText().equals(a.getObjectId())).findFirst().orElseThrow();
        assertThat(audit.getEmployee().getId()).isEqualTo(admin.getId());
        assertThat(audit.getIpAddress()).isEqualTo("127.0.0.9");
        assertThat(audit.getDetails()).doesNotContain(PASSWORD, account.getPasswordHash());
    }

    @Test
    void invalidAndDuplicateAssociationsFailBeforeAnyMutation() throws Exception {
        var admin = admin(); var role = customRole(); var store = store(); var request = request(role, store);
        long accountCount = accounts.count(), employeeCount = employees.count();
        request.setRoleIds(List.of(role.getId(), Long.MAX_VALUE));
        mvc.perform(post("/api/employees").session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(request)))
                .andExpect(status().isBadRequest());
        assertThat(accounts.count()).isEqualTo(accountCount); assertThat(employees.count()).isEqualTo(employeeCount);
        request.setRoleIds(List.of(role.getId(), role.getId()));
        mvc.perform(post("/api/employees").session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(request)))
                .andExpect(status().isBadRequest());
        request.setRoleIds(List.of(role.getId())); request.setPrimaryStoreId(null);
        mvc.perform(post("/api/employees").session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidUpdatePreservesOldAssignmentsAndAccountIdentity() throws Exception {
        var admin = admin(); var role = customRole(); var target = employee(role); var request = edit(target, List.of(Long.MAX_VALUE));
        request.setEmail("changed-" + UUID.randomUUID() + "@lunea.test");
        mvc.perform(put("/api/employees/{id}", target.getId()).session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(request)))
                .andExpect(status().isBadRequest());
        assertThat(employeeRoles.findByEmployeeId(target.getId())).extracting(l -> l.getRole().getId()).containsExactly(role.getId());
        assertThat(accounts.findById(target.getAccount().getId()).orElseThrow().getEmail()).isEqualTo(target.getAccount().getEmail());
    }

    @Test
    void missingPasswordAndDuplicateEmailHaveControlledErrors() throws Exception {
        var admin = admin(); var existing = employee(null); var request = request(null, null); request.setPassword(null);
        mvc.perform(post("/api/employees").session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(request)))
                .andExpect(status().isBadRequest());
        request.setPassword(PASSWORD); request.setEmail(existing.getAccount().getEmail().toUpperCase(Locale.ROOT));
        mvc.perform(post("/api/employees").session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void nonManagersAndCustomersCannotGrantOrCreateEmployees() throws Exception {
        var employee = employee(customRole("CATALOG_MANAGE"));
        mvc.perform(post("/api/employees").session(session(employee)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(request(null, null))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/roles").session(session(employee))).andExpect(status().isForbidden());
        mvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
        String email = "customer-" + UUID.randomUUID() + "@lunea.test";
        var account = accounts.saveAndFlush(AccountEntity.builder().username(email).email(email).passwordHash(encoder.encode(PASSWORD))
                .accountType(AccountType.CUSTOMER).status(AccountStatus.ACTIVE).build());
        var customer = customers.saveAndFlush(CustomerEntity.builder().account(account).fullName("Customer Test").build());
        var principal = new AccountPrincipal(account.getId(), AccountType.CUSTOMER, customer.getId(), null, account.getCredentialsVersion());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));
        var customerSession = new MockHttpSession(); customerSession.setAttribute("SPRING_SECURITY_CONTEXT", context);
        mvc.perform(get("/api/employees").session(customerSession)).andExpect(status().isForbidden());
    }

    @Test
    void employeeManagerCannotAssignAdminAndCannotEscalateSelf() throws Exception {
        var qlnv = role("QLNV", "EMPLOYEE_READ", "EMPLOYEE_MANAGE"); var manager = employee(qlnv);
        var adminRole = role("ADMIN", PermissionCatalog.ALL.toArray(String[]::new));
        mvc.perform(post("/api/employees").session(session(manager)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(request(adminRole, null))))
                .andExpect(status().isForbidden());
        var other = customRole("CATALOG_MANAGE");
        mvc.perform(put("/api/employees/{id}", manager.getId()).session(session(manager)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(edit(manager, List.of(qlnv.getId(), other.getId())))))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/employees/{id}", manager.getId()).session(session(manager)).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void administratorCannotGrantAnotherAdminRole() throws Exception {
        var administrator = admin();
        var adminRole = roles.findByName("ADMIN").orElseThrow();
        mvc.perform(post("/api/employees").session(session(administrator)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body(request(adminRole, store()))))
                .andExpect(status().isConflict());
        var staff = employee(customRole("EMPLOYEE_READ"));
        mvc.perform(put("/api/employees/{id}", staff.getId()).session(session(administrator)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body(edit(staff, List.of(adminRole.getId())))))
                .andExpect(status().isConflict());
    }

    @Test
    void branchScopeGuardsRestAndDirectServiceCalls() throws Exception {
        var employee = employee(customRole("INVENTORY_READ")); var assigned = store(); var other = store();
        employeeStores.saveAndFlush(EmployeeStoreEntity.builder().employee(employee).store(assigned).isPrimaryBranch(true).build());
        var session = session(employee);
        mvc.perform(get("/api/inventory/store/{id}", assigned.getId()).session(session)).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/inventory/store/{id}", other.getId()).session(session)).andExpect(status().isForbidden());
        var context = (org.springframework.security.core.context.SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        SecurityContextHolder.setContext(context);
        try { assertThatThrownBy(() -> inventoryService.getByStore(other.getId())).isInstanceOf(org.springframework.security.access.AccessDeniedException.class); }
        finally { SecurityContextHolder.clearContext(); }
    }

    @Test
    void rolePermissionRevocationAndGrantApplyWithoutNewLogin() throws Exception {
        var admin = admin(); var role = customRole("INVENTORY_READ"); var worker = employee(role); var store = store();
        employeeStores.saveAndFlush(EmployeeStoreEntity.builder().employee(worker).store(store).isPrimaryBranch(true).build());
        var workerSession = session(worker);
        mvc.perform(get("/api/inventory/store/{id}", store.getId()).session(workerSession)).andExpect(status().isOk());
        mvc.perform(put("/api/roles/{id}/permissions", role.getId()).session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("permissionIds", List.of())))).andExpect(status().isOk());
        mvc.perform(get("/api/inventory/store/{id}", store.getId()).session(workerSession)).andExpect(status().isForbidden());
        Long permissionId = permissions.findByCode("INVENTORY_READ").orElseThrow().getId();
        mvc.perform(put("/api/roles/{id}/permissions", role.getId()).session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("permissionIds", List.of(permissionId))))).andExpect(status().isOk());
        mvc.perform(get("/api/inventory/store/{id}", store.getId()).session(workerSession)).andExpect(status().isOk());
        var id = new RolePermissionId(role.getId(), permissionId);
        assertThat(rolePermissions.findById(id)).isPresent();
        assertThat(rolePermissions.findById(new RolePermissionId(role.getId(), Long.MAX_VALUE))).isEmpty();
    }

    @Test
    void purchaseAndTransferQueriesExcludeUnassignedBranches() throws Exception {
        var worker = employee(customRole("PURCHASE_READ", "TRANSFER_READ"));
        var source = store(); var destination = store(); var outside = store();
        employeeStores.saveAndFlush(EmployeeStoreEntity.builder().employee(worker).store(source).isPrimaryBranch(true).build());
        employeeStores.saveAndFlush(EmployeeStoreEntity.builder().employee(worker).store(destination).isPrimaryBranch(false).build());
        var supplier = suppliers.saveAndFlush(SupplierEntity.builder().name("Supplier test").status(ActiveStatus.ACTIVE).build());
        var visiblePurchase = purchases.saveAndFlush(PurchaseOrderEntity.builder().supplier(supplier).receivingStore(source).status(PurchaseOrderStatus.DRAFT).build());
        var hiddenPurchase = purchases.saveAndFlush(PurchaseOrderEntity.builder().supplier(supplier).receivingStore(outside).status(PurchaseOrderStatus.DRAFT).build());
        var visibleTransfer = transfers.saveAndFlush(StockTransferEntity.builder().sourceStore(source).destinationStore(destination).status(StockTransferStatus.PENDING).build());
        var hiddenTransfer = transfers.saveAndFlush(StockTransferEntity.builder().sourceStore(source).destinationStore(outside).status(StockTransferStatus.PENDING).build());
        var session = session(worker);
        var purchaseResult = mvc.perform(get("/api/purchase-orders").session(session)).andExpect(status().isOk()).andReturn();
        var purchaseData = json.readTree(purchaseResult.getResponse().getContentAsString());
        assertThat(purchaseData.size()).isEqualTo(1); assertThat(purchaseData.get(0).get("id").asLong()).isEqualTo(visiblePurchase.getId());
        var transferResult = mvc.perform(get("/api/stock-transfers").session(session)).andExpect(status().isOk()).andReturn();
        var transferData = json.readTree(transferResult.getResponse().getContentAsString());
        assertThat(transferData.size()).isEqualTo(1); assertThat(transferData.get(0).get("id").asLong()).isEqualTo(visibleTransfer.getId());
        mvc.perform(get("/api/purchase-orders/{id}", hiddenPurchase.getId()).session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/api/stock-transfers/{id}", hiddenTransfer.getId()).session(session)).andExpect(status().isForbidden());
    }

    @Test
    void deactivationLocksAccountInvalidatesOldSessionAndPreservesHistory() throws Exception {
        var admin = admin(); var role = customRole("EMPLOYEE_READ"); var worker = employee(role); var oldSession = session(worker);
        mvc.perform(delete("/api/employees/{id}", worker.getId()).session(session(admin)).with(csrf())).andExpect(status().isNoContent());
        assertThat(employees.findById(worker.getId()).orElseThrow().getStatus()).isEqualTo(ActiveStatus.INACTIVE);
        assertThat(accounts.findById(worker.getAccount().getId()).orElseThrow().getStatus()).isEqualTo(AccountStatus.LOCKED);
        assertThat(employeeRoles.findByEmployeeId(worker.getId())).hasSize(1);
        mvc.perform(get("/api/auth/me").session(oldSession)).andExpect(status().isUnauthorized());
        assertThat(oldSession.isInvalid()).isTrue();
    }

    @Test
    void mandatoryAuditFailureRollsBackAccountEmployeeAndGrants() throws Exception {
        var admin = admin(); var role = customRole("INVENTORY_READ"); var request = request(role, store());
        long accountCount = accounts.count(), employeeCount = employees.count(), grantCount = employeeRoles.count();
        doThrow(new IllegalStateException("Audit unavailable")).when(auditLogs).saveAndFlush(any(AuditLogEntity.class));
        mvc.perform(post("/api/employees").session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(request)))
                .andExpect(status().isInternalServerError());
        assertThat(accounts.count()).isEqualTo(accountCount); assertThat(employees.count()).isEqualTo(employeeCount);
        assertThat(employeeRoles.count()).isEqualTo(grantCount); assertThat(accounts.findByEmail(request.getEmail())).isEmpty();
        var oldPermissions = rolePermissions.findByRoleId(role.getId()).stream().map(p -> p.getPermission().getId()).toList();
        mvc.perform(put("/api/roles/{id}/permissions", role.getId()).session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("permissionIds", List.of())))).andExpect(status().isInternalServerError());
        assertThat(rolePermissions.findByRoleId(role.getId())).extracting(p -> p.getPermission().getId()).containsExactlyElementsOf(oldPermissions);
    }

    @Test
    void searchReturnsTypedEmptyPageAndReservedAdminCannotBeStripped() throws Exception {
        var admin = admin();
        mvc.perform(get("/api/employees").session(session(admin)).param("keyword", UUID.randomUUID().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("content").isEmpty()).andExpect(jsonPath("totalElements").value(0));
        mvc.perform(get("/api/employees").session(session(admin)).param("size", "101")).andExpect(status().isBadRequest());
        Long adminRole = roles.findByName("ADMIN").orElseThrow().getId();
        mvc.perform(put("/api/roles/{id}/permissions", adminRole).session(session(admin)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("permissionIds", List.of())))).andExpect(status().isConflict());
    }
}
