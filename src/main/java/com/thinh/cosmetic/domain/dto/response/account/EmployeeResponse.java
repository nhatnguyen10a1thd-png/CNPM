package com.thinh.cosmetic.domain.dto.response.account;

import com.thinh.cosmetic.domain.enums.ActiveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class EmployeeResponse {
    private Long id;
    private Long accountId;
    private String email;
    private String fullName;
    private String internalEmail;
    private String phone;
    private ActiveStatus status;
    private List<String> roles;
    private List<String> stores;
    private List<Long> roleIds;
    private List<Long> storeIds;
    private Long primaryStoreId;
}
