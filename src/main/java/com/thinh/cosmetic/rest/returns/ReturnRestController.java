package com.thinh.cosmetic.rest.returns;

import com.thinh.cosmetic.domain.dto.request.returns.ReturnRequestDto;
import com.thinh.cosmetic.domain.dto.response.returns.ReturnRequestResponse;
import com.thinh.cosmetic.domain.enums.ReturnStatus;
import com.thinh.cosmetic.service.returns.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.thinh.cosmetic.security.CurrentAccountResolver;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnRestController {
    private final ReturnService returnService;
    private final CurrentAccountResolver currentAccount;

    @PostMapping
    public ResponseEntity<ReturnRequestResponse> create(@Valid @RequestBody ReturnRequestDto request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(returnService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequestResponse> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(returnService.getById(id));
    }

    @GetMapping
    @PreAuthorize("@permissionPolicy.has('RETURN_MANAGE')")
    public ResponseEntity<List<ReturnRequestResponse>> getAll() {
        return ResponseEntity.ok(returnService.getAll());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ReturnRequestResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(returnService.getByCustomer(currentAccount.requireCustomerId()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@permissionPolicy.has('RETURN_MANAGE')")
    public ResponseEntity<ReturnRequestResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ReturnStatus status,
            @RequestParam(required = false) Long employeeId
    ) throws Exception {
        return ResponseEntity.ok(returnService.updateStatus(id, status, currentAccount.requireEmployeeId()));
    }
}
