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
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
    @Autowired InventoryService inventoryService;
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
