package com.thinh.cosmetic.service.account.impl;

import com.thinh.cosmetic.domain.dto.request.account.EmployeeRequest;
import com.thinh.cosmetic.domain.dto.response.account.EmployeeResponse;
import com.thinh.cosmetic.domain.entity.account.*;
import com.thinh.cosmetic.domain.entity.store.StoreEntity;
import com.thinh.cosmetic.domain.enums.*;
import com.thinh.cosmetic.exception.*;
import com.thinh.cosmetic.repository.account.*;
import com.thinh.cosmetic.repository.store.StoreRepository;
import com.thinh.cosmetic.security.PermissionPolicy;
import com.thinh.cosmetic.security.PasswordPolicy;
import com.thinh.cosmetic.service.account.AuditWriter;
import com.thinh.cosmetic.service.account.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final EmployeeStoreRepository employeeStoreRepository;
    private final RolePermissionRepository rolePermissions;
    private final PasswordEncoder passwordEncoder;
    private final PermissionPolicy policy;
    private final AuditWriter audit;
    private final jakarta.validation.Validator validator;

    @Override
    public EmployeeResponse create(EmployeeRequest request) {
        policy.require("EMPLOYEE_MANAGE");
        lockAdminRole();
        Prepared prepared = prepare(request, null);
        validatePassword(request.getPassword(), true);
        verifyGrantAuthority(null, prepared.roles());
        ActiveStatus status = request.getStatus() == null ? ActiveStatus.ACTIVE : request.getStatus();
        AccountEntity account = accountRepository.save(AccountEntity.builder().username(prepared.email())
                .email(prepared.email()).phone(prepared.phone()).passwordHash(passwordEncoder.encode(request.getPassword()))
                .accountType(AccountType.EMPLOYEE).status(status == ActiveStatus.ACTIVE ? AccountStatus.ACTIVE : AccountStatus.LOCKED)
                .credentialsVersion(0L).build());
        EmployeeEntity employee = employeeRepository.save(EmployeeEntity.builder().account(account)
                .fullName(request.getFullName().trim()).internalEmail(prepared.internalEmail()).phone(prepared.phone()).status(status).build());
        replaceLinks(employee, prepared, request.getPrimaryStoreId());
        audit.write("EMPLOYEE_CREATED", "EMPLOYEE", employee.getId(), auditDetails(prepared, status));
        return toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) { requireRead(); return toResponse(find(id)); }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() { return search(null, 0, 100).getContent(); }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> search(String keyword, int page, int size) {
        requireRead();
        if (page < 0 || size < 1 || size > 100) throw new BadRequestException("Page must be nonnegative and size between 1 and 100");
        String term = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (term.length() > 150) throw new BadRequestException("Search keyword is too long");
        Specification<EmployeeEntity> spec = (root, query, cb) -> {
            if (term.isEmpty()) return cb.conjunction();
            String pattern = "%" + term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            return cb.or(cb.like(cb.lower(root.get("fullName")), pattern, '\\'),
                    cb.like(cb.lower(root.get("internalEmail")), pattern, '\\'),
                    cb.like(cb.lower(root.get("account").get("email")), pattern, '\\'), cb.like(root.get("phone"), pattern, '\\'));
        };
        return employeeRepository.findAll(spec, PageRequest.of(page, size, Sort.by("id").descending())).map(this::toResponse);
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        policy.require("EMPLOYEE_MANAGE");
        lockAdminRole();
        EmployeeEntity employee = find(id);
        Prepared prepared = prepare(request, employee);
        validatePassword(request.getPassword(), false);
        verifyGrantAuthority(employee, prepared.roles());
        ActiveStatus status = request.getStatus() == null ? employee.getStatus() : request.getStatus();
        Set<Long> oldRoles = new HashSet<>(employeeRoleRepository.findByEmployeeId(id).stream().map(l -> l.getRole().getId()).toList());
        Set<Long> oldStores = new HashSet<>(employeeStoreRepository.findByEmployeeId(id).stream().map(l -> l.getStore().getId()).toList());
        Long oldPrimary = employeeStoreRepository.findByEmployeeId(id).stream().filter(l -> Boolean.TRUE.equals(l.getIsPrimaryBranch()))
                .map(l -> l.getStore().getId()).findFirst().orElse(null);
        boolean sensitiveChange = status != employee.getStatus()
                || !oldRoles.equals(ids(prepared.roles())) || !oldStores.equals(storeIds(prepared.stores()))
                || !Objects.equals(oldPrimary, request.getPrimaryStoreId());
        if (policy.isCurrentEmployee(id) && sensitiveChange) throw PermissionPolicy.denied();
        protectLastAdmin(employee, prepared.roles(), status);
        employee.setFullName(request.getFullName().trim());
        employee.setInternalEmail(prepared.internalEmail());
        employee.setPhone(prepared.phone());
        employee.setStatus(status);
        AccountEntity account = employee.getAccount();
        account.setEmail(prepared.email()); account.setUsername(prepared.email()); account.setPhone(prepared.phone());
        AccountStatus accountStatus = status == ActiveStatus.ACTIVE ? AccountStatus.ACTIVE : AccountStatus.LOCKED;
        if (account.getStatus() != accountStatus) account.setCredentialsVersion(account.getCredentialsVersion() + 1);
        account.setStatus(accountStatus);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            account.setCredentialsVersion(account.getCredentialsVersion() + 1);
        }
        accountRepository.save(account);
        replaceLinks(employee, prepared, request.getPrimaryStoreId());
        employeeRepository.save(employee);
        audit.write("EMPLOYEE_UPDATED", "EMPLOYEE", id, auditDetails(prepared, status));
        return toResponse(employee);
    }

    @Override
    public void deactivate(Long id) {
        policy.require("EMPLOYEE_MANAGE");
        lockAdminRole();
        EmployeeEntity employee = find(id);
        if (policy.isCurrentEmployee(id)) throw PermissionPolicy.denied();
        List<RoleEntity> roles = employeeRoleRepository.findByEmployeeId(id).stream().map(EmployeeRoleEntity::getRole).toList();
        verifyGrantAuthority(employee, roles);
        protectLastAdmin(employee, roles, ActiveStatus.INACTIVE);
        employee.setStatus(ActiveStatus.INACTIVE);
        employee.getAccount().setStatus(AccountStatus.LOCKED);
        employee.getAccount().setCredentialsVersion(employee.getAccount().getCredentialsVersion() + 1);
        accountRepository.save(employee.getAccount()); employeeRepository.save(employee);
        audit.write("EMPLOYEE_DEACTIVATED", "EMPLOYEE", id, "status=INACTIVE");
    }

    private Prepared prepare(EmployeeRequest request, EmployeeEntity existing) {
        if (request == null) throw new BadRequestException("Employee details are required");
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new BadRequestException("Invalid employee fields: " + violations.stream()
                .map(v -> v.getPropertyPath().toString()).distinct().sorted().toList());
        if (request.getFullName() == null || request.getFullName().isBlank() || request.getEmail() == null || request.getEmail().isBlank())
            throw new BadRequestException("Full name and login email are required");
        String email = PasswordPolicy.normalizeEmail(request.getEmail());
        String internal = request.getInternalEmail() == null || request.getInternalEmail().isBlank() ? email : PasswordPolicy.normalizeEmail(request.getInternalEmail());
        String phone = PasswordPolicy.normalizePhone(request.getPhone());
        Long accountId = existing == null ? null : existing.getAccount().getId();
        accountRepository.findByEmailIgnoreCase(email).filter(a -> !Objects.equals(a.getId(), accountId))
                .ifPresent(a -> { throw new ConflictException("Login email is already used"); });
        accountRepository.findByUsername(email).filter(a -> !Objects.equals(a.getId(), accountId))
                .ifPresent(a -> { throw new ConflictException("Login identifier is already used"); });
        if (phone != null) accountRepository.findByPhone(phone).filter(a -> !Objects.equals(a.getId(), accountId))
                .ifPresent(a -> { throw new ConflictException("Phone is already used"); });
        employeeRepository.findByInternalEmailIgnoreCase(internal).filter(e -> existing == null || !e.getId().equals(existing.getId()))
                .ifPresent(e -> { throw new ConflictException("Internal email is already used"); });
        List<Long> roleIds = uniqueIds(request.getRoleIds(), "roleIds");
        List<Long> storeIds = uniqueIds(request.getStoreIds(), "storeIds");
        List<RoleEntity> roles = roleIds.stream().map(id -> roleRepository.findById(id).orElseThrow(() -> new BadRequestException("Unknown role ID: " + id))).toList();
        List<StoreEntity> stores = storeIds.stream().map(id -> storeRepository.findById(id).orElseThrow(() -> new BadRequestException("Unknown store ID: " + id))).toList();
        if (stores.stream().anyMatch(s -> s.getStatus() != ActiveStatus.ACTIVE)) throw new BadRequestException("Only active stores can be assigned");
        if ((storeIds.isEmpty() && request.getPrimaryStoreId() != null)
                || (!storeIds.isEmpty() && !storeIds.contains(request.getPrimaryStoreId()))) throw new BadRequestException("Choose exactly one primary store from assigned stores");
        return new Prepared(email, internal, phone, roles, stores);
    }

    private void replaceLinks(EmployeeEntity employee, Prepared prepared, Long primary) {
        employeeRoleRepository.deleteByEmployeeId(employee.getId());
        employeeStoreRepository.deleteByEmployeeId(employee.getId());
        employeeRoleRepository.flush(); employeeStoreRepository.flush();
        for (RoleEntity role : prepared.roles()) employeeRoleRepository.save(EmployeeRoleEntity.builder().employee(employee).role(role).build());
        for (StoreEntity store : prepared.stores()) employeeStoreRepository.save(EmployeeStoreEntity.builder().employee(employee).store(store).isPrimaryBranch(store.getId().equals(primary)).build());
    }
    private void verifyGrantAuthority(EmployeeEntity target, List<RoleEntity> roles) {
        boolean privilegedNew = roles.stream().anyMatch(this::privileged);
        boolean privilegedOld = target != null && employeeRoleRepository.findByEmployeeId(target.getId()).stream().map(EmployeeRoleEntity::getRole).anyMatch(this::privileged);
        if (!policy.isAdmin() && (privilegedNew || privilegedOld)) throw PermissionPolicy.denied();
        boolean requestedAdmin = roles.stream().anyMatch(role -> "ADMIN".equals(role.getName()));
        boolean alreadyAdmin = target != null && employeeRoleRepository.findByEmployeeId(target.getId()).stream()
                .anyMatch(link -> "ADMIN".equals(link.getRole().getName()));
        if (requestedAdmin && !alreadyAdmin) {
            throw new ConflictException("ADMIN is reserved for the initial administrator");
        }
    }
    private boolean privileged(RoleEntity role) { return "ADMIN".equals(role.getName()) || rolePermissions.findByRoleId(role.getId()).stream().anyMatch(p -> "ROLE_MANAGE".equals(p.getPermission().getCode())); }
    private void protectLastAdmin(EmployeeEntity target, List<RoleEntity> newRoles, ActiveStatus status) {
        RoleEntity admin = roleRepository.findByName("ADMIN").orElse(null);
        if (admin == null) return;
        boolean currentlyAdmin = employeeRoleRepository.findByEmployeeId(target.getId()).stream().anyMatch(r -> r.getRole().getId().equals(admin.getId()));
        boolean remainsAdmin = status == ActiveStatus.ACTIVE && newRoles.stream().anyMatch(r -> r.getId().equals(admin.getId()));
        if (currentlyAdmin && !remainsAdmin) {
            long otherAdmins = employeeRoleRepository.findByRoleId(admin.getId()).stream().map(EmployeeRoleEntity::getEmployee)
                    .filter(e -> !e.getId().equals(target.getId()) && e.getStatus() == ActiveStatus.ACTIVE && e.getAccount().getStatus() == AccountStatus.ACTIVE).count();
            if (otherAdmins == 0) throw new ConflictException("At least one active administrator must remain");
        }
    }
    private void lockAdminRole() { roleRepository.findLockedByName("ADMIN"); }
    private void requireRead() { if (!policy.has("EMPLOYEE_READ") && !policy.has("EMPLOYEE_MANAGE")) throw PermissionPolicy.denied(); }
    private EmployeeEntity find(Long id) { return employeeRepository.findById(id).orElseThrow(() -> new NotFoundException("Employee not found")); }
    private void validatePassword(String password, boolean required) {
        if (password == null || password.isBlank()) { if (required) throw new BadRequestException("Password is required when creating an employee"); return; }
        PasswordPolicy.validatePassword(password);
    }
    private List<Long> uniqueIds(List<Long> ids, String field) {
        if (ids == null) return List.of();
        if (ids.size() > 100 || ids.stream().anyMatch(id -> id == null || id <= 0) || new HashSet<>(ids).size() != ids.size()) throw new BadRequestException(field + " must contain distinct positive IDs");
        return ids;
    }
    private Set<Long> ids(List<RoleEntity> roles) { return new HashSet<>(roles.stream().map(RoleEntity::getId).toList()); }
    private Set<Long> storeIds(List<StoreEntity> stores) { return new HashSet<>(stores.stream().map(StoreEntity::getId).toList()); }
    private String auditDetails(Prepared p, ActiveStatus status) { return "roles=" + ids(p.roles()) + "; stores=" + storeIds(p.stores()) + "; status=" + status; }
    private record Prepared(String email, String internalEmail, String phone, List<RoleEntity> roles, List<StoreEntity> stores) { }
    private EmployeeResponse toResponse(EmployeeEntity employee) {
        var roles = employeeRoleRepository.findByEmployeeId(employee.getId());
        var stores = employeeStoreRepository.findByEmployeeId(employee.getId());
        return EmployeeResponse.builder().id(employee.getId()).accountId(employee.getAccount().getId()).email(employee.getAccount().getEmail())
                .fullName(employee.getFullName()).internalEmail(employee.getInternalEmail()).phone(employee.getPhone()).status(employee.getStatus())
                .roles(roles.stream().map(r -> r.getRole().getName()).toList()).roleIds(roles.stream().map(r -> r.getRole().getId()).toList())
                .stores(stores.stream().map(s -> s.getStore().getName()).toList()).storeIds(stores.stream().map(s -> s.getStore().getId()).toList())
                .primaryStoreId(stores.stream().filter(s -> Boolean.TRUE.equals(s.getIsPrimaryBranch())).map(s -> s.getStore().getId()).findFirst().orElse(null)).build();
    }
}
