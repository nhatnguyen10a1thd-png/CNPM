package com.thinh.cosmetic.rest.purchase;

import com.thinh.cosmetic.domain.dto.request.purchase.PurchaseOrderRequest;
import com.thinh.cosmetic.domain.dto.response.purchase.PurchaseOrderResponse;
import com.thinh.cosmetic.service.purchase.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.thinh.cosmetic.security.CurrentAccountResolver;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderRestController {
    private final PurchaseOrderService purchaseOrderService;
    private final CurrentAccountResolver currentAccount;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(
            @Valid @RequestBody PurchaseOrderRequest request,
            @RequestParam(required = false) Long employeeId
    ) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderService.create(request, currentAccount.requireEmployeeId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(purchaseOrderService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getAll() {
        return ResponseEntity.ok(purchaseOrderService.getAll());
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<PurchaseOrderResponse> confirm(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(purchaseOrderService.confirm(id));
    }
}
