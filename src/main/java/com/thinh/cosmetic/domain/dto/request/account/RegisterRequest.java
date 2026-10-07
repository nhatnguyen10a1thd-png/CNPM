package com.thinh.cosmetic.domain.dto.request.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class RegisterRequest {
    @NotBlank @Size(max = 150) private String fullName;
    @NotBlank @Email @Size(max = 254) private String email;
    @Size(max = 30)
    private String phone;
    @NotBlank @Size(min = 8, max = 72)
    @Pattern(regexp = "(?s)(?=.*[A-Za-z])(?=.*[0-9]).+", message = "Mật khẩu cần có chữ và số")
    @lombok.ToString.Exclude
    private String password;
    @NotBlank @lombok.ToString.Exclude private String confirmPassword;
    @AssertTrue(message = "Vui lòng đồng ý điều khoản sử dụng")
    @NotNull(message = "Vui lòng đồng ý điều khoản sử dụng")
    private Boolean termsAccepted;

    public boolean isTermsAccepted() { return Boolean.TRUE.equals(termsAccepted); }
}
