package com.thinh.cosmetic.service.account;

import com.thinh.cosmetic.domain.dto.response.account.*;
import com.thinh.cosmetic.domain.entity.account.*;
import com.thinh.cosmetic.exception.BadRequestException;
import com.thinh.cosmetic.exception.ConflictException;
import com.thinh.cosmetic.exception.NotFoundException;
import com.thinh.cosmetic.repository.account.*;
import com.thinh.cosmetic.security.PermissionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleGrantService {
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final RolePermissionRepository grants;
    private final EmployeeRoleRepository employeeRoles;
    private final PermissionPolicy policy;
    private final AuditWriter audit;

    @Transactional(readOnly = true)
    public List<RoleResponse> roles() {
        requireLookup();
        return roles.findAll().stream().sorted(Comparator.comparing(RoleEntity::getId)).map(this::response).toList();
    }
    @Transactional(readOnly = true)
    public List<PermissionResponse> permissions() {
        requireLookup();
        return permissions.findAll().stream().map(p -> new PermissionResponse(p.getId(), p.getCode(), p.getName(), p.getDescription())).toList();
    }
    public RoleResponse replacePermissions(Long roleId, List<Long> permissionIds) {
        policy.require("ROLE_MANAGE");
        if (!policy.isAdmin()) throw PermissionPolicy.denied();
        roles.findLockedByName("ADMIN");
        RoleEntity role = roles.findById(roleId).orElseThrow(() -> new NotFoundException("Role not found"));
        if ("ADMIN".equals(role.getName())) throw new ConflictException("Reserved ADMIN permissions cannot be changed");
        if (employeeRoles.findByEmployeeId(policy.employeeId()).stream().anyMatch(r -> r.getRole().getId().equals(roleId))) throw PermissionPolicy.denied();
        if (permissionIds == null || permissionIds.size() > 100 || permissionIds.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(permissionIds).size() != permissionIds.size()) throw new BadRequestException("Permission IDs must be distinct positive IDs");
        var desired = permissionIds.stream().map(id -> permissions.findById(id).orElseThrow(() -> new BadRequestException("Unknown permission ID: " + id))).toList();
        if (desired.stream().anyMatch(p -> "ROLE_MANAGE".equals(p.getCode()))) throw new ConflictException("ROLE_MANAGE is reserved for ADMIN");
        grants.deleteByRoleId(roleId); grants.flush();
        for (PermissionEntity permission : desired) grants.save(RolePermissionEntity.builder().role(role).permission(permission).build());
        audit.write("ROLE_PERMISSIONS_UPDATED", "ROLE", roleId, "permissionIds=" + permissionIds);
        return response(role);
    }
    private void requireLookup() { if (!policy.has("EMPLOYEE_MANAGE") && !policy.has("ROLE_MANAGE")) throw PermissionPolicy.denied(); }
    private RoleResponse response(RoleEntity role) {
        var links = grants.findByRoleId(role.getId());
        return new RoleResponse(role.getId(), role.getName(), role.getDescription(),
                links.stream().map(g -> g.getPermission().getId()).toList(), links.stream().map(g -> g.getPermission().getCode()).toList());
    }
}
