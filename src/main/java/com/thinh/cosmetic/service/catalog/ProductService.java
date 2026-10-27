package com.thinh.cosmetic.service.catalog;

import com.thinh.cosmetic.domain.dto.request.catalog.ProductRequest;
import com.thinh.cosmetic.domain.dto.request.catalog.ProductSkuRequest;
import com.thinh.cosmetic.domain.dto.response.catalog.ProductResponse;
import com.thinh.cosmetic.domain.dto.response.catalog.ProductSkuResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request) throws Exception;

    ProductResponse getById(Long id) throws Exception;

    List<ProductResponse> getAll();

    List<ProductResponse> getAll(boolean includeInactive);

    List<ProductResponse> getRelatedProducts(Long currentProductId, Long categoryId, Long brandId, int limit);

    List<ProductResponse> getFeaturedProducts(int limit);

    List<ProductResponse> searchAndFilter(
            String keyword,
            Long brandId,
            Long categoryId,
            String skinType,
            String concern,
            String priceRange,
            String sortBy
    );

    List<ProductResponse> searchAndFilter(
            String keyword,
            Long brandId,
            Long categoryId,
            String skinType,
            String concern,
            String priceRange,
            String sortBy,
            boolean includeInactive
    );

    ProductResponse update(Long id, ProductRequest request) throws Exception;

    void delete(Long id) throws Exception;

    void deactivate(Long id) throws Exception;

    void activate(Long id) throws Exception;

    ProductSkuResponse createSku(Long productId, ProductSkuRequest request) throws Exception;

    ProductSkuResponse updateSku(Long productId, Long skuId, ProductSkuRequest request) throws Exception;

    void deleteSku(Long productId, Long skuId) throws Exception;

    List<ProductSkuResponse> getSkusByProductId(Long productId) throws Exception;
}
