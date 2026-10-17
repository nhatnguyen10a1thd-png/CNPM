package com.thinh.cosmetic.domain.dto.response.account;
import java.util.List;
public record RoleResponse(Long id, String name, String description, List<Long> permissionIds, List<String> permissions) { }
