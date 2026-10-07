package com.thinh.cosmetic.rest.order;

import com.thinh.cosmetic.domain.dto.request.order.OrderRequest;
import com.thinh.cosmetic.domain.dto.response.order.OrderResponse;
import com.thinh.cosmetic.domain.enums.OrderStatus;
import com.thinh.cosmetic.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.thinh.cosmetic.security.CurrentAccountResolver;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestController {
    private final OrderService orderService;
    private final CurrentAccountResolver currentAccount;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestParam(required = false) Long customerId,
            @Valid @RequestBody OrderRequest request
    ) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(currentAccount.requireCustomerId(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getByCustomer(currentAccount.requireCustomerId()));
    }

    @GetMapping
    @PreAuthorize("@permissionPolicy.has('ORDER_READ')")
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@permissionPolicy.has('ORDER_MANAGE')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) throws Exception {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) Long customerId
    ) throws Exception {
        return ResponseEntity.ok(orderService.cancelOrder(id, currentAccount.requireCustomerId()));
    }
}
