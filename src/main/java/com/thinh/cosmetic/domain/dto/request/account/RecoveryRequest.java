package com.thinh.cosmetic.domain.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecoveryRequest(@NotBlank @Size(max = 254) String identifier) { }
