package com.thinh.cosmetic.rest.cart;

import com.thinh.cosmetic.domain.dto.request.cart.CartItemRequest;
import com.thinh.cosmetic.domain.dto.response.cart.CartResponse;
import com.thinh.cosmetic.service.cart.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.thinh.cosmetic.security.CurrentAccountResolver;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartRestController {
    private final CartService cartService;
    private final CurrentAccountResolver currentAccount;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestParam(required = false) Long customerId
    ) throws Exception {
        return ResponseEntity.ok(cartService.getCart(currentAccount.requireCustomerId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @RequestParam(required = false) Long customerId,
            @Valid @RequestBody CartItemRequest request
    ) throws Exception {
        return ResponseEntity.ok(cartService.addItem(currentAccount.requireCustomerId(), request));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @RequestParam(required = false) Long customerId,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity
    ) throws Exception {
        return ResponseEntity.ok(cartService.updateItemQuantity(currentAccount.requireCustomerId(), cartItemId, quantity));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(
            @RequestParam(required = false) Long customerId,
            @PathVariable Long cartItemId
    ) throws Exception {
        return ResponseEntity.ok(cartService.removeItem(currentAccount.requireCustomerId(), cartItemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestParam(required = false) Long customerId
    ) throws Exception {
        cartService.clearCart(currentAccount.requireCustomerId());
        return ResponseEntity.noContent().build();
    }
}
