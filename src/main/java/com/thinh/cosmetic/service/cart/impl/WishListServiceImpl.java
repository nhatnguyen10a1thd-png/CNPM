package com.thinh.cosmetic.service.cart.impl;

import com.thinh.cosmetic.domain.dto.request.cart.CartItemRequest;
import com.thinh.cosmetic.domain.dto.response.cart.WishListResponse;
import com.thinh.cosmetic.domain.entity.account.CustomerEntity;
import com.thinh.cosmetic.domain.entity.catalog.WishListEntity;
import com.thinh.cosmetic.domain.entity.catalog.WishListItemEntity;
import com.thinh.cosmetic.domain.entity.catalog.ProductEntity;
import com.thinh.cosmetic.domain.entity.catalog.ProductImageEntity;
import com.thinh.cosmetic.domain.entity.catalog.ProductSkuEntity;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.exception.BusinessException;
import com.thinh.cosmetic.exception.ErrorCode;
import com.thinh.cosmetic.repository.account.CustomerRepository;
import com.thinh.cosmetic.repository.cart.WishListItemRepository;
import com.thinh.cosmetic.repository.cart.WishListRepository;
import com.thinh.cosmetic.repository.catalog.ProductImageRepository;
import com.thinh.cosmetic.repository.catalog.ProductRepository;
import com.thinh.cosmetic.repository.catalog.ProductSkuRepository;
import com.thinh.cosmetic.service.cart.CartService;
import com.thinh.cosmetic.service.cart.WishListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {
    private final WishListRepository wishListRepository;
    private final WishListItemRepository wishListItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSkuRepository productSkuRepository;
    private final CartService cartService;

    @Override
    @Transactional(readOnly = true)
    public WishListResponse getWishList(Long customerId) throws Exception {
        WishListEntity wishList = getOrCreateWishList(customerId);
        return toResponse(wishList);
    }

    @Override
    public WishListResponse addProduct(Long customerId, Long productId) throws Exception {
        WishListEntity wishList = getOrCreateWishList(customerId);
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Không tìm thấy sản phẩm: " + productId));

        if (wishListItemRepository.findByWishListIdAndProductId(wishList.getId(), productId).isEmpty()) {
            WishListItemEntity item = WishListItemEntity.builder()
                    .wishList(wishList)
                    .product(product)
                    .build();
            wishListItemRepository.save(item);
        }
        return toResponse(wishList);
    }

    @Override
    public WishListResponse removeProduct(Long customerId, Long productId) throws Exception {
        WishListEntity wishList = getOrCreateWishList(customerId);
        wishListItemRepository.deleteByWishListIdAndProductId(wishList.getId(), productId);
        return toResponse(wishList);
    }

    @Override
    public void clearWishList(Long customerId) throws Exception {
        WishListEntity wishList = getOrCreateWishList(customerId);
        wishListItemRepository.deleteByWishListId(wishList.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getWishListProductIds(Long customerId) throws Exception {
        if (customerId == null) {
            return Collections.emptyList();
        }
        WishListEntity wishList = wishListRepository.findByCustomerId(customerId).orElse(null);
        if (wishList == null) {
            return Collections.emptyList();
        }
        return wishListItemRepository.findByWishListId(wishList.getId()).stream()
                .map(item -> item.getProduct() != null ? item.getProduct().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Override
    public WishListResponse moveAllToCart(Long customerId) throws Exception {
        WishListEntity wishList = getOrCreateWishList(customerId);
        List<WishListItemEntity> items = wishListItemRepository.findByWishListId(wishList.getId());

        for (WishListItemEntity item : items) {
            ProductEntity product = item.getProduct();
            if (product == null) continue;

            List<ProductSkuEntity> skus = productSkuRepository.findByProductId(product.getId());
            Long defaultSkuId = skus.stream()
                    .filter(s -> s.getStatus() == ActiveStatus.ACTIVE)
                    .map(ProductSkuEntity::getId)
                    .findFirst()
                    .orElse(skus.isEmpty() ? null : skus.get(0).getId());

            if (defaultSkuId != null) {
                try {
                    cartService.addItem(customerId, CartItemRequest.builder()
                            .skuId(defaultSkuId)
                            .quantity(1)
                            .build());
                } catch (Exception e) {
                    log.warn("Could not add product SKU {} to cart: {}", defaultSkuId, e.getMessage());
                }
            }
        }
        return toResponse(wishList);
    }

    private WishListEntity getOrCreateWishList(Long customerId) throws Exception {
        return wishListRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    CustomerEntity customer = customerRepository.findById(customerId).orElse(null);
                    return wishListRepository.save(WishListEntity.builder().customer(customer).build());
                });
    }

    private WishListResponse toResponse(WishListEntity wishList) {
        List<WishListItemEntity> items = wishListItemRepository.findByWishListId(wishList.getId());
        List<WishListResponse.WishListItemResponse> itemResponses = new ArrayList<>();

        List<Long> productIds = items.stream()
                .map(item -> item.getProduct() != null ? item.getProduct().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> imageMap = new HashMap<>();
        Map<Long, List<ProductSkuEntity>> skuMap = new HashMap<>();

        if (!productIds.isEmpty()) {
            productImageRepository.findByProductIdInAndIsPrimaryTrue(productIds)
                    .forEach(img -> {
                        if (img.getProduct() != null) {
                            imageMap.put(img.getProduct().getId(), img.getImageUrl());
                        }
                    });

            List<ProductSkuEntity> allSkus = productSkuRepository.findByProductIdIn(productIds);
            skuMap = allSkus.stream()
                    .filter(s -> s.getProduct() != null)
                    .collect(Collectors.groupingBy(s -> s.getProduct().getId()));
        }

        for (WishListItemEntity item : items) {
            ProductEntity product = item.getProduct();
            if (product == null) continue;

            String imageUrl = imageMap.get(product.getId());
            List<ProductSkuEntity> skus = skuMap.get(product.getId());
            if (skus == null || skus.isEmpty()) {
                skus = productSkuRepository.findByProductId(product.getId());
            }
            if (skus == null) skus = Collections.emptyList();

            BigDecimal price = skus.stream()
                    .map(ProductSkuEntity::getPrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal listPrice = skus.stream()
                    .map(ProductSkuEntity::getListPrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(null);

            Long defaultSkuId = skus.stream()
                    .filter(s -> s.getStatus() == ActiveStatus.ACTIVE)
                    .map(ProductSkuEntity::getId)
                    .findFirst()
                    .orElse(skus.isEmpty() ? null : skus.get(0).getId());

            boolean inStock = skus.stream()
                    .anyMatch(s -> s.getStatus() == ActiveStatus.ACTIVE);

            String brandName = product.getBrand() != null ? product.getBrand().getName() : null;

            itemResponses.add(WishListResponse.WishListItemResponse.builder()
                    .id(item.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .brandName(brandName)
                    .price(price)
                    .listPrice(listPrice)
                    .imageUrl(imageUrl)
                    .defaultSkuId(defaultSkuId)
                    .inStock(inStock)
                    .addedAt(item.getAddedAt())
                    .build());
        }

        return WishListResponse.builder()
                .id(wishList.getId())
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .build();
    }
}
