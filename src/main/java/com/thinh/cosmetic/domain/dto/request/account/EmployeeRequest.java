package com.thinh.cosmetic.domain.dto.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class EmployeeRequest {
    @NotBlank @Size(max = 150) private String fullName;
    @NotBlank @Email @Size(max = 254) private String email;
    @lombok.ToString.Exclude
    private String password;
    @Email @Size(max = 254) private String internalEmail;
    @Size(max = 30)
    private String phone;
    private List<Long> roleIds;
    private List<Long> storeIds;
    private Long primaryStoreId;
    private ActiveStatus status;
}
