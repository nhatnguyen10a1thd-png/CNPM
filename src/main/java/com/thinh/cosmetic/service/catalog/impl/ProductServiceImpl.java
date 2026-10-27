package com.thinh.cosmetic.service.catalog.impl;

import com.thinh.cosmetic.domain.dto.request.catalog.ProductRequest;
import com.thinh.cosmetic.domain.dto.response.catalog.ProductResponse;
import com.thinh.cosmetic.domain.entity.catalog.BrandEntity;
import com.thinh.cosmetic.domain.entity.catalog.CategoryEntity;
import com.thinh.cosmetic.domain.entity.catalog.ProductEntity;
import com.thinh.cosmetic.mapper.catalog.ProductMapper;
import com.thinh.cosmetic.repository.catalog.BrandRepository;
import com.thinh.cosmetic.repository.catalog.CategoryRepository;
import com.thinh.cosmetic.repository.catalog.ProductRepository;
import com.thinh.cosmetic.service.catalog.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thinh.cosmetic.domain.dto.response.catalog.ProductSkuResponse;
import com.thinh.cosmetic.domain.entity.catalog.ProductAttributeValueEntity;
import com.thinh.cosmetic.domain.entity.catalog.ProductImageEntity;
import com.thinh.cosmetic.domain.entity.catalog.ProductSkuEntity;
import com.thinh.cosmetic.domain.entity.inventory.InventoryEntity;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.exception.BusinessException;
import com.thinh.cosmetic.exception.ErrorCode;
import com.thinh.cosmetic.repository.cart.CartItemRepository;
import com.thinh.cosmetic.repository.catalog.ProductAttributeValueRepository;
import com.thinh.cosmetic.repository.catalog.ProductImageRepository;
import com.thinh.cosmetic.repository.catalog.ProductSkuRepository;
import com.thinh.cosmetic.repository.order.OrderItemRepository;
import com.thinh.cosmetic.repository.purchase.PurchaseOrderItemRepository;
import com.thinh.cosmetic.repository.review.ReviewRepository;
import com.thinh.cosmetic.repository.store.InventoryRepository;
import com.thinh.cosmetic.repository.store.StockTransferItemRepository;

import com.thinh.cosmetic.domain.dto.request.catalog.ProductSkuRequest;
import com.thinh.cosmetic.domain.entity.inventory.StoreEntity;
import com.thinh.cosmetic.repository.store.StoreRepository;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductImageRepository productImageRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final OrderItemRepository orderItemRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final StockTransferItemRepository stockTransferItemRepository;
    private final ReviewRepository reviewRepository;
    private final CartItemRepository cartItemRepository;
    private final StoreRepository storeRepository;

    @Override
    public ProductResponse create(ProductRequest request) throws Exception {
        ProductEntity product = productMapper.toEntity(request);

        if (request.getBrandId() != null){
            BrandEntity brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() ->
                            new Exception(
                                    "Brand not found: " + request.getBrandId()
                            ));
            product.setBrand(brand);
        }

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() ->
                            new Exception(
                                    "Category not found: " + request.getCategoryId()
                            ));
            product.setCategory(category);
        }

        ProductEntity savedProduct = productRepository.save(product);

        String imgUrl = request.getImageUrl() != null && !request.getImageUrl().isBlank()
                ? request.getImageUrl().trim()
                : "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80";

        ProductImageEntity imageEntity = ProductImageEntity.builder()
                .product(savedProduct)
                .imageUrl(imgUrl)
                .isPrimary(true)
                .sortOrder(0)
                .build();
        productImageRepository.save(imageEntity);

        return enrich(savedProduct, productMapper.toResponse(savedProduct));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) throws Exception {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + id));
        return enrich(product, productMapper.toResponse(product));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return getAll(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll(boolean includeInactive) {
        List<ProductEntity> raw = productRepository.findAllWithBrandAndCategory();
        List<ProductEntity> filtered = raw.stream()
                .filter(p -> includeInactive || p.getStatus() == null || p.getStatus() == ActiveStatus.ACTIVE)
                .toList();
        return enrichBatch(filtered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getRelatedProducts(Long currentProductId, Long categoryId, Long brandId, int limit) {
        if (currentProductId == null) return List.of();
        Pageable pageable = PageRequest.of(0, Math.max(1, limit));
        List<ProductEntity> relatedEntities = productRepository.findRelatedProducts(currentProductId, categoryId, brandId, pageable);
        return enrichBatch(relatedEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(1, limit));
        List<ProductEntity> featured = productRepository.findTopFeaturedProducts(pageable);
        if (featured.isEmpty()) {
            featured = productRepository.findTopRecentProducts(pageable);
        }
        return enrichBatch(featured);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchAndFilter(
            String keyword,
            Long brandId,
            Long categoryId,
            String skinType,
            String concern,
            String priceRange,
            String sortBy
    ) {
        return searchAndFilter(keyword, brandId, categoryId, skinType, concern, priceRange, sortBy, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchAndFilter(
            String keyword,
            Long brandId,
            Long categoryId,
            String skinType,
            String concern,
            String priceRange,
            String sortBy,
            boolean includeInactive
    ) {
        List<ProductResponse> all = getAll(includeInactive);

        return all.stream()
                .filter(p -> matchesKeyword(p, keyword))
                .filter(p -> brandId == null || (p.getBrand() != null && Objects.equals(p.getBrand().getId(), brandId)))
                .filter(p -> categoryId == null || (p.getCategory() != null && (
                        Objects.equals(p.getCategory().getId(), categoryId) ||
                        (p.getCategory().getParentId() != null && Objects.equals(p.getCategory().getParentId(), categoryId))
                )))
                .filter(p -> matchesSkinType(p, skinType))
                .filter(p -> matchesConcern(p, concern))
                .filter(p -> matchesPriceRange(p, priceRange))
                .sorted(getComparator(sortBy))
                .toList();
    }

    private boolean matchesKeyword(ProductResponse p, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String kwLower = keyword.trim().toLowerCase();
        String kwNormalized = stripAccents(kwLower);

        List<String> searchTargets = new ArrayList<>();
        if (p.getName() != null) searchTargets.add(p.getName());
        if (p.getBrand() != null && p.getBrand().getName() != null) searchTargets.add(p.getBrand().getName());
        if (p.getCategory() != null && p.getCategory().getName() != null) searchTargets.add(p.getCategory().getName());
        if (p.getDescription() != null) searchTargets.add(p.getDescription());
        if (p.getMainIngredients() != null) searchTargets.add(p.getMainIngredients());
        if (p.getUses() != null) searchTargets.add(p.getUses());
        if (p.getOrigin() != null) searchTargets.add(p.getOrigin());
        if (p.getSkus() != null) {
            for (ProductSkuResponse sku : p.getSkus()) {
                if (sku.getSkuCode() != null) searchTargets.add(sku.getSkuCode());
                if (sku.getVariantName() != null) searchTargets.add(sku.getVariantName());
            }
        }

        for (String target : searchTargets) {
            String targetLower = target.toLowerCase();
            if (targetLower.contains(kwLower)) return true;
            if (stripAccents(targetLower).contains(kwNormalized)) return true;
        }
        return false;
    }

    private String stripAccents(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "").replace("đ", "d").replace("Đ", "d");
    }

    private boolean matchesSkinType(ProductResponse p, String skinType) {
        if (skinType == null || skinType.isBlank()) return true;
        if (p.getSkinTypes() == null || p.getSkinTypes().isEmpty()) return false;
        String stNorm = stripAccents(skinType.toLowerCase());
        return p.getSkinTypes().stream()
                .anyMatch(s -> stripAccents(s.toLowerCase()).contains(stNorm) || stNorm.contains(stripAccents(s.toLowerCase())));
    }

    private boolean matchesConcern(ProductResponse p, String concern) {
        if (concern == null || concern.isBlank()) return true;
        if (p.getConcerns() == null || p.getConcerns().isEmpty()) return false;
        String cNorm = stripAccents(concern.toLowerCase());
        return p.getConcerns().stream()
                .anyMatch(c -> stripAccents(c.toLowerCase()).contains(cNorm) || cNorm.contains(stripAccents(c.toLowerCase())));
    }

    private boolean matchesPriceRange(ProductResponse p, String priceRange) {
        if (priceRange == null || priceRange.isBlank()) return true;
        BigDecimal price = p.getMinPrice() != null ? p.getMinPrice() : BigDecimal.ZERO;
        return switch (priceRange.toLowerCase()) {
            case "under300", "under500" -> price.compareTo(new BigDecimal("500000")) < 0;
            case "300to600", "500to1000" -> price.compareTo(new BigDecimal("500000")) >= 0 && price.compareTo(new BigDecimal("1000000")) <= 0;
            case "above1000" -> price.compareTo(new BigDecimal("1000000")) > 0;
            default -> true;
        };
    }

    private Comparator<ProductResponse> getComparator(String sortBy) {
        if ("price-asc".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(p -> p.getMinPrice() != null ? p.getMinPrice() : BigDecimal.ZERO);
        } else if ("price-desc".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing((ProductResponse p) -> p.getMinPrice() != null ? p.getMinPrice() : BigDecimal.ZERO).reversed();
        } else if ("featured".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing((ProductResponse p) -> Boolean.TRUE.equals(p.getIsFeatured())).reversed()
                    .thenComparing(ProductResponse::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        } else {
            return Comparator.comparing(ProductResponse::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        }
    }

    private ProductResponse enrich(ProductEntity product, ProductResponse res) {
        if (res == null || product == null) return res;

        // 1. SKUs
        List<ProductSkuEntity> skuEntities = productSkuRepository.findByProductId(product.getId());
        List<ProductSkuResponse> skus = skuEntities.stream().map(s -> ProductSkuResponse.builder()
                .id(s.getId())
                .productId(product.getId())
                .productName(product.getName())
                .skuCode(s.getSkuCode())
                .variantName(s.getVariantName())
                .price(s.getPrice())
                .listPrice(s.getListPrice())
                .barcode(s.getBarcode())
                .status(s.getStatus())
                .build()).toList();
        res.setSkus(skus);

        // 2. Images
        Optional<ProductImageEntity> primaryImg = productImageRepository.findFirstByProductIdAndIsPrimaryTrue(product.getId());
        List<ProductImageEntity> allImgs = productImageRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        String defaultFallbackImg = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80";
        String primaryUrl = primaryImg.map(ProductImageEntity::getImageUrl)
                .orElseGet(() -> !allImgs.isEmpty() ? allImgs.get(0).getImageUrl() : defaultFallbackImg);
        res.setImageUrl(primaryUrl);
        res.setImageUrls(allImgs.stream().map(ProductImageEntity::getImageUrl).toList());

        // 3. Price range
        BigDecimal minPrice = skus.stream().map(ProductSkuResponse::getPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = skus.stream().map(ProductSkuResponse::getPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal listPrice = skus.stream().map(ProductSkuResponse::getListPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(minPrice);
        res.setMinPrice(minPrice);
        res.setMaxPrice(maxPrice);
        res.setListPrice(listPrice);

        // 4. Total available inventory across all stores
        int totalStock = 0;
        for (ProductSkuEntity sku : skuEntities) {
            List<InventoryEntity> invs = inventoryRepository.findBySkuId(sku.getId());
            for (InventoryEntity inv : invs) {
                totalStock += Math.max(0, inv.getActualStock() - inv.getHeldQuantity());
            }
        }
        res.setTotalAvailableStock(totalStock);
        res.setInStock(totalStock > 0);

        // 5. Attributes (Skin types & Concerns)
        List<ProductAttributeValueEntity> attrValues = productAttributeValueRepository.findByProductId(product.getId());
        List<String> skinTypes = new ArrayList<>();
        List<String> concerns = new ArrayList<>();
        for (ProductAttributeValueEntity pav : attrValues) {
            if (pav.getAttribute() != null) {
                if ("SKIN_TYPE".equalsIgnoreCase(pav.getAttribute().getGroup())) {
                    skinTypes.add(pav.getAttribute().getName());
                } else if ("NEED".equalsIgnoreCase(pav.getAttribute().getGroup())) {
                    concerns.add(pav.getAttribute().getName());
                } else {
                    concerns.add(pav.getAttribute().getName());
                }
            }
        }
        res.setSkinTypes(skinTypes);
        res.setConcerns(concerns);

        return res;
    }

    private List<ProductResponse> enrichBatch(List<ProductEntity> products) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = products.stream().map(ProductEntity::getId).toList();

        // 1. Batch fetch SKUs in 1 query
        List<ProductSkuEntity> allSkus = productSkuRepository.findByProductIdIn(productIds);
        Map<Long, List<ProductSkuEntity>> skuMap = allSkus.stream()
                .filter(s -> s.getProduct() != null)
                .collect(Collectors.groupingBy(s -> s.getProduct().getId()));

        // 2. Batch fetch Inventories for all SKUs in 1 query
        List<Long> skuIds = allSkus.stream().map(ProductSkuEntity::getId).toList();
        Map<Long, List<InventoryEntity>> invMap;
        if (!skuIds.isEmpty()) {
            List<InventoryEntity> allInvs = inventoryRepository.findBySkuIdIn(skuIds);
            invMap = allInvs.stream()
                    .filter(i -> i.getSku() != null)
                    .collect(Collectors.groupingBy(i -> i.getSku().getId()));
        } else {
            invMap = Map.of();
        }

        // 3. Batch fetch Images in 1 query
        List<ProductImageEntity> allImgs = productImageRepository.findByProductIdInOrderBySortOrderAsc(productIds);
        Map<Long, List<ProductImageEntity>> imgMap = allImgs.stream()
                .filter(i -> i.getProduct() != null)
                .collect(Collectors.groupingBy(i -> i.getProduct().getId()));

        // 4. Batch fetch Attributes with JOIN FETCH in 1 query
        List<ProductAttributeValueEntity> allAttrValues = productAttributeValueRepository.findByProductIdInWithAttribute(productIds);
        Map<Long, List<ProductAttributeValueEntity>> attrMap = allAttrValues.stream()
                .filter(a -> a.getProduct() != null)
                .collect(Collectors.groupingBy(a -> a.getProduct().getId()));

        String defaultFallbackImg = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80";

        // Map and enrich all products in-memory without any subsequent database queries
        return products.stream().map(product -> {
            ProductResponse res = productMapper.toResponse(product);
            if (res == null) return null;

            // 1. SKUs
            List<ProductSkuEntity> productSkus = skuMap.getOrDefault(product.getId(), List.of());
            List<ProductSkuResponse> skus = productSkus.stream().map(s -> ProductSkuResponse.builder()
                    .id(s.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .skuCode(s.getSkuCode())
                    .variantName(s.getVariantName())
                    .price(s.getPrice())
                    .listPrice(s.getListPrice())
                    .barcode(s.getBarcode())
                    .status(s.getStatus())
                    .build()).toList();
            res.setSkus(skus);

            // 2. Images
            List<ProductImageEntity> imgs = imgMap.getOrDefault(product.getId(), List.of());
            Optional<ProductImageEntity> primaryImg = imgs.stream()
                    .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                    .findFirst();
            String primaryUrl = primaryImg.map(ProductImageEntity::getImageUrl)
                    .orElseGet(() -> !imgs.isEmpty() ? imgs.get(0).getImageUrl() : defaultFallbackImg);
            res.setImageUrl(primaryUrl);
            res.setImageUrls(imgs.stream().map(ProductImageEntity::getImageUrl).toList());

            // 3. Price range
            BigDecimal minPrice = skus.stream()
                    .map(ProductSkuResponse::getPrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = skus.stream()
                    .map(ProductSkuResponse::getPrice)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal listPrice = skus.stream()
                    .map(ProductSkuResponse::getListPrice)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(minPrice);
            res.setMinPrice(minPrice);
            res.setMaxPrice(maxPrice);
            res.setListPrice(listPrice);

            // 4. Total available inventory across all stores
            int totalStock = 0;
            for (ProductSkuEntity sku : productSkus) {
                List<InventoryEntity> invs = invMap.getOrDefault(sku.getId(), List.of());
                for (InventoryEntity inv : invs) {
                    totalStock += Math.max(0, inv.getActualStock() - inv.getHeldQuantity());
                }
            }
            res.setTotalAvailableStock(totalStock);
            res.setInStock(totalStock > 0);

            // 5. Attributes (Skin types & Concerns)
            List<ProductAttributeValueEntity> attrValues = attrMap.getOrDefault(product.getId(), List.of());
            List<String> skinTypes = new ArrayList<>();
            List<String> concerns = new ArrayList<>();
            for (ProductAttributeValueEntity pav : attrValues) {
                if (pav.getAttribute() != null) {
                    if ("SKIN_TYPE".equalsIgnoreCase(pav.getAttribute().getGroup())) {
                        skinTypes.add(pav.getAttribute().getName());
                    } else if ("NEED".equalsIgnoreCase(pav.getAttribute().getGroup())) {
                        concerns.add(pav.getAttribute().getName());
                    } else {
                        concerns.add(pav.getAttribute().getName());
                    }
                }
            }
            res.setSkinTypes(skinTypes);
            res.setConcerns(concerns);

            return res;
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) throws Exception {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + id));

        productMapper.updateEntity(request, product);
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        if (request.getBrandId() != null){
            BrandEntity brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BRAND_NOT_FOUND, "Brand not found: " + request.getBrandId()));
            product.setBrand(brand);
        }

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }

        ProductEntity savedProduct = productRepository.save(product);

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            Optional<ProductImageEntity> primaryImgOpt = productImageRepository.findFirstByProductIdAndIsPrimaryTrue(savedProduct.getId());
            if (primaryImgOpt.isPresent()) {
                ProductImageEntity primaryImg = primaryImgOpt.get();
                primaryImg.setImageUrl(request.getImageUrl().trim());
                productImageRepository.save(primaryImg);
            } else {
                ProductImageEntity imageEntity = ProductImageEntity.builder()
                        .product(savedProduct)
                        .imageUrl(request.getImageUrl().trim())
                        .isPrimary(true)
                        .sortOrder(0)
                        .build();
                productImageRepository.save(imageEntity);
            }
        }

        return enrich(savedProduct, productMapper.toResponse(savedProduct));
    }

    @Override
    public void delete(Long id) throws Exception {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + id));

        List<ProductSkuEntity> skus = productSkuRepository.findByProductId(product.getId());
        List<Long> skuIds = skus.stream().map(ProductSkuEntity::getId).toList();

        boolean hasOrderItems = !skuIds.isEmpty() && orderItemRepository.existsBySkuIdIn(skuIds);
        boolean hasPurchaseOrders = !skuIds.isEmpty() && purchaseOrderItemRepository.existsBySkuIdIn(skuIds);
        boolean hasStockTransfers = !skuIds.isEmpty() && stockTransferItemRepository.existsBySkuIdIn(skuIds);
        boolean hasInventory = !skuIds.isEmpty() && inventoryRepository.existsBySkuIdIn(skuIds);
        boolean hasReviews = reviewRepository.existsByProductId(product.getId());

        if (hasOrderItems || hasPurchaseOrders || hasStockTransfers || hasInventory || hasReviews) {
            // Apply soft-delete / deactivation policy to preserve order and transaction history
            product.setStatus(ActiveStatus.INACTIVE);
            for (ProductSkuEntity sku : skus) {
                sku.setStatus(ActiveStatus.INACTIVE);
                productSkuRepository.save(sku);
            }
            productRepository.save(product);
        } else {
            // Clean hard delete of unreferenced product
            if (!skuIds.isEmpty()) {
                cartItemRepository.deleteBySkuIdIn(skuIds);
            }
            productAttributeValueRepository.deleteByProductId(product.getId());
            productImageRepository.deleteByProductId(product.getId());
            productSkuRepository.deleteAll(skus);
            productRepository.delete(product);
        }
    }

    @Override
    public void deactivate(Long id) throws Exception {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + id));
        product.setStatus(ActiveStatus.INACTIVE);
        List<ProductSkuEntity> skus = productSkuRepository.findByProductId(product.getId());
        for (ProductSkuEntity sku : skus) {
            sku.setStatus(ActiveStatus.INACTIVE);
            productSkuRepository.save(sku);
        }
        productRepository.save(product);
    }

    @Override
    public void activate(Long id) throws Exception {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + id));
        product.setStatus(ActiveStatus.ACTIVE);
        List<ProductSkuEntity> skus = productSkuRepository.findByProductId(product.getId());
        for (ProductSkuEntity sku : skus) {
            sku.setStatus(ActiveStatus.ACTIVE);
            productSkuRepository.save(sku);
        }
        productRepository.save(product);
    }

    private ProductSkuResponse toSkuResponse(ProductSkuEntity s) {
        return ProductSkuResponse.builder()
                .id(s.getId())
                .productId(s.getProduct().getId())
                .productName(s.getProduct().getName())
                .skuCode(s.getSkuCode())
                .variantName(s.getVariantName())
                .price(s.getPrice())
                .listPrice(s.getListPrice())
                .barcode(s.getBarcode())
                .status(s.getStatus())
                .build();
    }

    @Override
    @Transactional
    public ProductSkuResponse createSku(Long productId, ProductSkuRequest request) throws Exception {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Không tìm thấy sản phẩm #" + productId));

        String skuCode = request.getSkuCode() != null ? request.getSkuCode().trim() : null;
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mã SKU không được để trống");
        }

        // QLSP-QĐ5: Mã SKU unique toàn hệ thống
        if (productSkuRepository.existsBySkuCode(skuCode)) {
            throw new BusinessException(ErrorCode.DUPLICATE_SKU_CODE, "Mã SKU '" + skuCode + "' đã tồn tại trong hệ thống");
        }

        String barcode = request.getBarcode() != null && !request.getBarcode().isBlank() ? request.getBarcode().trim() : null;
        if (barcode != null && productSkuRepository.existsByBarcode(barcode)) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "Mã vạch (Barcode) '" + barcode + "' đã tồn tại trong hệ thống");
        }

        BigDecimal price = request.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Giá bán SKU phải lớn hơn hoặc bằng 0");
        }

        BigDecimal listPrice = request.getListPrice();
        if (listPrice == null || listPrice.compareTo(BigDecimal.ZERO) < 0) {
            listPrice = price;
        }

        ProductSkuEntity sku = ProductSkuEntity.builder()
                .product(product)
                .skuCode(skuCode)
                .variantName(request.getVariantName() != null && !request.getVariantName().isBlank() ? request.getVariantName().trim() : "Mặc định")
                .price(price)
                .listPrice(listPrice)
                .barcode(barcode)
                .status(request.getStatus() != null ? request.getStatus() : ActiveStatus.ACTIVE)
                .build();

        ProductSkuEntity savedSku = productSkuRepository.save(sku);

        // Khởi tạo bản ghi tồn kho cho tất cả các chi nhánh
        List<StoreEntity> stores = storeRepository.findAll();
        for (StoreEntity store : stores) {
            if (inventoryRepository.findByStoreIdAndSkuId(store.getId(), savedSku.getId()).isEmpty()) {
                inventoryRepository.save(InventoryEntity.builder()
                        .store(store)
                        .sku(savedSku)
                        .actualStock(0)
                        .heldQuantity(0)
                        .minimumStock(5)
                        .updatedAt(LocalDateTime.now())
                        .build());
            }
        }

        return toSkuResponse(savedSku);
    }

    @Override
    @Transactional
    public ProductSkuResponse updateSku(Long productId, Long skuId, ProductSkuRequest request) throws Exception {
        ProductSkuEntity sku = productSkuRepository.findById(skuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy biến thể SKU #" + skuId));

        if (!sku.getProduct().getId().equals(productId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Biến thể SKU không thuộc sản phẩm #" + productId);
        }

        if (request.getSkuCode() != null && !request.getSkuCode().isBlank()) {
            String newCode = request.getSkuCode().trim();
            if (productSkuRepository.existsBySkuCodeAndIdNot(newCode, skuId)) {
                throw new BusinessException(ErrorCode.DUPLICATE_SKU_CODE, "Mã SKU '" + newCode + "' đã tồn tại");
            }
            sku.setSkuCode(newCode);
        }

        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            String newBarcode = request.getBarcode().trim();
            if (productSkuRepository.existsByBarcodeAndIdNot(newBarcode, skuId)) {
                throw new BusinessException(ErrorCode.DATA_CONFLICT, "Mã vạch '" + newBarcode + "' đã tồn tại");
            }
            sku.setBarcode(newBarcode);
        }

        if (request.getVariantName() != null && !request.getVariantName().isBlank()) {
            sku.setVariantName(request.getVariantName().trim());
        }

        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Giá bán SKU phải lớn hơn hoặc bằng 0");
            }
            sku.setPrice(request.getPrice());
        }

        if (request.getListPrice() != null) {
            if (request.getListPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Giá niêm yết SKU phải lớn hơn hoặc bằng 0");
            }
            sku.setListPrice(request.getListPrice());
        }

        if (request.getStatus() != null) {
            sku.setStatus(request.getStatus());
        }

        ProductSkuEntity savedSku = productSkuRepository.save(sku);
        return toSkuResponse(savedSku);
    }

    @Override
    @Transactional
    public void deleteSku(Long productId, Long skuId) throws Exception {
        ProductSkuEntity sku = productSkuRepository.findById(skuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy biến thể SKU #" + skuId));

        if (!sku.getProduct().getId().equals(productId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Biến thể SKU không thuộc sản phẩm #" + productId);
        }

        boolean hasOrderItems = orderItemRepository.existsBySkuIdIn(List.of(skuId));
        boolean hasPurchaseOrders = purchaseOrderItemRepository.existsBySkuIdIn(List.of(skuId));
        boolean hasStockTransfers = stockTransferItemRepository.existsBySkuIdIn(List.of(skuId));

        List<InventoryEntity> inventories = inventoryRepository.findBySkuId(skuId);
        boolean hasInventoryStock = inventories.stream().anyMatch(inv -> (inv.getActualStock() != null && inv.getActualStock() > 0) || (inv.getHeldQuantity() != null && inv.getHeldQuantity() > 0));

        // QLSP-QĐ3, BR-11: Nếu đã phát sinh giao dịch hoặc còn tồn kho, chỉ chuyển INACTIVE (soft-delete)
        if (hasOrderItems || hasPurchaseOrders || hasStockTransfers || hasInventoryStock) {
            sku.setStatus(ActiveStatus.INACTIVE);
            productSkuRepository.save(sku);
        } else {
            cartItemRepository.deleteBySkuIdIn(List.of(skuId));
            inventoryRepository.deleteAll(inventories);
            productSkuRepository.delete(sku);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSkuResponse> getSkusByProductId(Long productId) throws Exception {
        return productSkuRepository.findByProductId(productId)
                .stream().map(this::toSkuResponse).toList();
    }
}
