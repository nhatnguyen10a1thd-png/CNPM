package com.thinh.cosmetic.repository.catalog;

import com.thinh.cosmetic.domain.entity.catalog.ProductSkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductSkuRepository extends JpaRepository<ProductSkuEntity, Long> {
    List<ProductSkuEntity> findByProductId(Long productId);
    List<ProductSkuEntity> findByProductIdIn(java.util.Collection<Long> productIds);
    Optional<ProductSkuEntity> findBySkuCode(String skuCode);
    boolean existsBySkuCode(String skuCode);
    boolean existsBySkuCodeAndIdNot(String skuCode, Long id);
    boolean existsByBarcode(String barcode);
    boolean existsByBarcodeAndIdNot(String barcode, Long id);
}
