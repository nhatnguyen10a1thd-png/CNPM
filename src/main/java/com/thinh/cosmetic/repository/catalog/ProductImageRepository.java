package com.thinh.cosmetic.repository.catalog;

import com.thinh.cosmetic.domain.entity.catalog.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {
    Optional<ProductImageEntity> findFirstByProductIdAndIsPrimaryTrue(Long productId);
    List<ProductImageEntity> findByProductIdOrderBySortOrderAsc(Long productId);
    List<ProductImageEntity> findByProductIdInOrderBySortOrderAsc(java.util.Collection<Long> productIds);
    List<ProductImageEntity> findByProductIdInAndIsPrimaryTrue(java.util.Collection<Long> productIds);
    boolean existsByProductIdAndImageUrl(Long productId, String imageUrl);
    void deleteByProductId(Long productId);
}
