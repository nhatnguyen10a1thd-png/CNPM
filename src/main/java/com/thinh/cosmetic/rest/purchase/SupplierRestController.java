package com.thinh.cosmetic.rest.purchase;

import com.thinh.cosmetic.domain.dto.request.purchase.SupplierRequest;
import com.thinh.cosmetic.domain.dto.response.purchase.SupplierResponse;
import com.thinh.cosmetic.service.purchase.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PURCHASE_MANAGE')")
public class SupplierRestController {
    private final SupplierService supplierService;

    @PreAuthorize("hasAuthority('PURCHASE_MANAGE')")
    @PostMapping
    @PreAuthorize("@permissionPolicy.has('SUPPLIER_MANAGE')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(request));
    }

    @PreAuthorize("hasAuthority('PURCHASE_MANAGE')")
    @GetMapping("/{id}")
    @PreAuthorize("@permissionPolicy.has('SUPPLIER_READ')")
    public ResponseEntity<SupplierResponse> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @PreAuthorize("hasAuthority('PURCHASE_MANAGE')")
    @GetMapping
    @PreAuthorize("@permissionPolicy.has('SUPPLIER_READ')")
    public ResponseEntity<List<SupplierResponse>> getAll() {
        return ResponseEntity.ok(supplierService.getAll());
    }

    @PreAuthorize("hasAuthority('PURCHASE_MANAGE')")
    @PutMapping("/{id}")
    @PreAuthorize("@permissionPolicy.has('SUPPLIER_MANAGE')")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request
    ) throws Exception {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @PreAuthorize("hasAuthority('PURCHASE_MANAGE')")
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionPolicy.has('SUPPLIER_MANAGE')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) throws Exception {
        supplierService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
