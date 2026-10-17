package com.thinh.cosmetic.rest.account;
import com.thinh.cosmetic.domain.dto.response.account.PermissionResponse;
import com.thinh.cosmetic.service.account.RoleGrantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionRestController {
    private final RoleGrantService grants;
    @GetMapping public List<PermissionResponse> permissions() { return grants.permissions(); }
}
