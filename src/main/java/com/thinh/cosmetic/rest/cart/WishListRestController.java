package com.thinh.cosmetic.rest.cart;

import com.thinh.cosmetic.domain.dto.response.cart.WishListResponse;
import com.thinh.cosmetic.service.cart.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.thinh.cosmetic.security.CurrentAccountResolver;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishListRestController {
    private final WishListService wishListService;
    private final CurrentAccountResolver currentAccount;

    @GetMapping
    public ResponseEntity<WishListResponse> getWishList(
            @RequestParam(required = false) Long customerId
    ) throws Exception {
        return ResponseEntity.ok(wishListService.getWishList(currentAccount.requireCustomerId()));
    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<WishListResponse> addProduct(
            @RequestParam(required = false) Long customerId,
            @PathVariable Long productId
    ) throws Exception {
        return ResponseEntity.ok(wishListService.addProduct(currentAccount.requireCustomerId(), productId));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<WishListResponse> removeProduct(
            @RequestParam(required = false) Long customerId,
            @PathVariable Long productId
    ) throws Exception {
        return ResponseEntity.ok(wishListService.removeProduct(currentAccount.requireCustomerId(), productId));
    }
}
