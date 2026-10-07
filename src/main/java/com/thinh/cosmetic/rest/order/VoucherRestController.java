package com.thinh.cosmetic.rest.order;

import com.thinh.cosmetic.domain.dto.request.order.VoucherRequest;
import com.thinh.cosmetic.domain.dto.response.order.VoucherResponse;
import com.thinh.cosmetic.service.order.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherRestController {
    private final VoucherService voucherService;

    @PostMapping
    @PreAuthorize("@permissionPolicy.has('PROMOTION_MANAGE')")
    public ResponseEntity<VoucherResponse> create(@Valid @RequestBody VoucherRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionPolicy.has('PROMOTION_READ')")
    public ResponseEntity<VoucherResponse> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(voucherService.getById(id));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("@permissionPolicy.has('PROMOTION_READ')")
    public ResponseEntity<VoucherResponse> getByCode(@PathVariable String code) throws Exception {
        return ResponseEntity.ok(voucherService.getByCode(code));
    }

    @GetMapping
    @PreAuthorize("@permissionPolicy.has('PROMOTION_READ')")
    public ResponseEntity<List<VoucherResponse>> getAll() {
        return ResponseEntity.ok(voucherService.getAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionPolicy.has('PROMOTION_MANAGE')")
    public ResponseEntity<VoucherResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody VoucherRequest request
    ) throws Exception {
        return ResponseEntity.ok(voucherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionPolicy.has('PROMOTION_MANAGE')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) throws Exception {
        voucherService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/discount")
    @PreAuthorize("@permissionPolicy.has('PROMOTION_READ')")
    public ResponseEntity<BigDecimal> calculateDiscount(
            @RequestParam String code,
            @RequestParam BigDecimal orderTotal
    ) throws Exception {
        return ResponseEntity.ok(voucherService.calculateDiscount(code, orderTotal));
    }
}
