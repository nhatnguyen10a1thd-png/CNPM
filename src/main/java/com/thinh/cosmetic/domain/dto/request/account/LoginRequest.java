package com.thinh.cosmetic.domain.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class LoginRequest {
    @NotBlank(message = "Vui lòng nhập email hoặc số điện thoại")
    @Size(max = 254)
    @JsonAlias("email")
    private String identifier;
    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(max = 72)
    @lombok.ToString.Exclude
    private String password;
    @Builder.Default
    private Boolean rememberMe = false;

    public boolean isRememberMe() { return Boolean.TRUE.equals(rememberMe); }
}
