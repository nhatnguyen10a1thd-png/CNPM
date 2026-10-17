package com.thinh.cosmetic.domain.dto.request.account;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
@Data
public class RolePermissionsRequest {
    @NotNull @Size(max = 100)
    private List<Long> permissionIds;
}
