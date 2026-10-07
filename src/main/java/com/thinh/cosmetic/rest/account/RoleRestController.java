package com.thinh.cosmetic.rest.account;
import com.thinh.cosmetic.domain.dto.request.account.RolePermissionsRequest;
import com.thinh.cosmetic.domain.dto.response.account.RoleResponse;
import com.thinh.cosmetic.service.account.RoleGrantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleRestController {
    private final RoleGrantService grants;
    @GetMapping public List<RoleResponse> roles() { return grants.roles(); }
    @PutMapping("/{id}/permissions") public RoleResponse update(@PathVariable Long id, @Valid @RequestBody RolePermissionsRequest request) {
        return grants.replacePermissions(id, request.getPermissionIds());
    }
}
