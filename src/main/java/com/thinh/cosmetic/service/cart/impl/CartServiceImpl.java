package com.thinh.cosmetic.service.cart.impl;

import com.thinh.cosmetic.domain.dto.request.cart.CartItemRequest;
import com.thinh.cosmetic.domain.dto.response.cart.CartItemResponse;
import com.thinh.cosmetic.domain.dto.response.cart.CartResponse;
import com.thinh.cosmetic.domain.entity.account.CustomerEntity;
import com.thinh.cosmetic.domain.entity.sales.CartEntity;
import com.thinh.cosmetic.domain.entity.sales.CartItemEntity;
import com.thinh.cosmetic.domain.entity.catalog.ProductImageEntity;
import com.thinh.cosmetic.domain.entity.catalog.ProductSkuEntity;
import com.thinh.cosmetic.repository.account.CustomerRepository;
import com.thinh.cosmetic.repository.cart.CartItemRepository;
import com.thinh.cosmetic.repository.cart.CartRepository;
import com.thinh.cosmetic.repository.catalog.ProductImageRepository;
import com.thinh.cosmetic.repository.catalog.ProductSkuRepository;
import com.thinh.cosmetic.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.thinh.cosmetic.exception.BusinessException;
import com.thinh.cosmetic.exception.ErrorCode;
import com.thinh.cosmetic.repository.store.InventoryRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductSkuRepository skuRepository;
    private final ProductImageRepository productImageRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public CartResponse getCart(Long customerId) throws Exception {
        CartEntity cart = getOrCreateCart(customerId);
        return toCartResponse(cart);
    }

    @Override
    public CartResponse addItem(Long customerId, CartItemRequest request) throws Exception {
        CartEntity cart = getOrCreateCart(customerId);
        ProductSkuEntity sku = skuRepository.findById(request.getSkuId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SKU_NOT_FOUND, "Product SKU not found: " + request.getSkuId()));

        if (sku.getStatus() == com.thinh.cosmetic.domain.enums.ActiveStatus.INACTIVE ||
                (sku.getProduct() != null && sku.getProduct().getStatus() == com.thinh.cosmetic.domain.enums.ActiveStatus.INACTIVE)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE, "Sản phẩm hoặc biến thể này đã ngừng kinh doanh");
        }

        CartItemEntity item = cartItemRepository.findByCartIdAndSkuId(cart.getId(), sku.getId())
                .orElse(null);

        int totalAvailable = inventoryRepository.findBySkuId(sku.getId())
                .stream()
                .mapToInt(inv -> Math.max(0, inv.getActualStock() - inv.getHeldQuantity()))
                .sum();

        int targetQuantity = (item != null ? item.getQuantity() : 0) + request.getQuantity();
        if (targetQuantity > totalAvailable) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                    "Tồn kho không đủ! Sản phẩm chỉ còn " + totalAvailable + " sản phẩm khả dụng trên toàn hệ thống.");
        }

        if (item != null) {
            item.setQuantity(targetQuantity);
            cartItemRepository.save(item);
        } else {
            item = CartItemEntity.builder()
                    .cart(cart)
                    .sku(sku)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(item);
        }

        return toCartResponse(cart);
    }

    @Override
    public CartResponse updateItemQuantity(Long customerId, Long cartItemId, Integer quantity) throws Exception {
        CartEntity cart = getOrCreateCart(customerId);
        CartItemEntity item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "Không tìm thấy mặt hàng trong giỏ: " + cartItemId));

        if (item.getCart() == null || !cart.getId().equals(item.getCart().getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Mặt hàng này không thuộc giỏ hàng của bạn");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            int totalAvailable = inventoryRepository.findBySkuId(item.getSku().getId())
                    .stream()
                    .mapToInt(inv -> Math.max(0, inv.getActualStock() - inv.getHeldQuantity()))
                    .sum();
            if (quantity > totalAvailable) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                        "Tồn kho không đủ! Sản phẩm chỉ còn " + totalAvailable + " sản phẩm khả dụng trên toàn hệ thống.");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return toCartResponse(cart);
    }

    @Override
    public CartResponse removeItem(Long customerId, Long cartItemId) throws Exception {
        CartEntity cart = getOrCreateCart(customerId);
        CartItemEntity item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "Không tìm thấy mặt hàng trong giỏ: " + cartItemId));

        if (item.getCart() == null || !cart.getId().equals(item.getCart().getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Mặt hàng này không thuộc giỏ hàng của bạn");
        }
        cartItemRepository.delete(item);
        return toCartResponse(cart);
    }

    @Override
    public void clearCart(Long customerId) throws Exception {
        CartEntity cart = getOrCreateCart(customerId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private CartEntity getOrCreateCart(Long customerId) throws Exception {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    CustomerEntity customer = customerRepository.findById(customerId).orElse(null);
                    return cartRepository.save(CartEntity.builder().customer(customer).build());
                });
    }

    private CartResponse toCartResponse(CartEntity cart) {
        List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;

        java.util.Set<Long> productIds = items.stream()
                .map(i -> (i.getSku() != null && i.getSku().getProduct() != null) ? i.getSku().getProduct().getId() : null)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        java.util.Map<Long, String> imageMap = new java.util.HashMap<>();
        if (!productIds.isEmpty()) {
            productImageRepository.findByProductIdInAndIsPrimaryTrue(productIds)
                    .forEach(img -> {
                        if (img.getProduct() != null) {
                            imageMap.put(img.getProduct().getId(), img.getImageUrl());
                        }
                    });
        }

        for (CartItemEntity item : items) {
            ProductSkuEntity sku = item.getSku();
            BigDecimal price = sku.getPrice() != null ? sku.getPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            totalItems += item.getQuantity();

            Long productId = (sku.getProduct() != null) ? sku.getProduct().getId() : null;
            String imageUrl = productId != null ? imageMap.get(productId) : null;

            itemResponses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .skuId(sku.getId())
                    .skuCode(sku.getSkuCode())
                    .productName(sku.getProduct() != null ? sku.getProduct().getName() : "Sản phẩm")
                    .variantName(sku.getVariantName())
                    .price(price)
                    .quantity(item.getQuantity())
                    .subtotal(lineTotal)
                    .imageUrl(imageUrl)
                    .build());
        }

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .subtotal(subtotal)
                .totalItems(totalItems)
                .build();
    }
}
