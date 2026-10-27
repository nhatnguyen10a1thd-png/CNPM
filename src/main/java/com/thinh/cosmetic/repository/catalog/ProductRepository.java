package com.thinh.cosmetic.repository.catalog;

import com.thinh.cosmetic.domain.entity.catalog.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category")
    List<ProductEntity> findAllWithBrandAndCategory();

    @Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
           "WHERE p.id != :productId AND p.status = com.thinh.cosmetic.domain.enums.ActiveStatus.ACTIVE " +
           "AND ((:categoryId IS NOT NULL AND p.category.id = :categoryId) OR (:brandId IS NOT NULL AND p.brand.id = :brandId))")
    List<ProductEntity> findRelatedProducts(@Param("productId") Long productId,
                                            @Param("categoryId") Long categoryId,
                                            @Param("brandId") Long brandId,
                                            Pageable pageable);

    @Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
           "WHERE p.status = com.thinh.cosmetic.domain.enums.ActiveStatus.ACTIVE AND p.isFeatured = true ORDER BY p.createdAt DESC")
    List<ProductEntity> findTopFeaturedProducts(Pageable pageable);

    @Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
           "WHERE p.status = com.thinh.cosmetic.domain.enums.ActiveStatus.ACTIVE ORDER BY p.createdAt DESC")
    List<ProductEntity> findTopRecentProducts(Pageable pageable);

    List<ProductEntity> findByCategoryId(Long categoryId);
    List<ProductEntity> findByBrandId(Long brandId);
    List<ProductEntity> findByIsFeaturedTrue();
    List<ProductEntity> findByNameContainingIgnoreCase(String keyword);
    List<ProductEntity> findByStatus(com.thinh.cosmetic.domain.enums.ActiveStatus status);
    boolean existsByBrandId(Long brandId);
    long countByBrandId(Long brandId);
    boolean existsByCategoryId(Long categoryId);
    long countByCategoryId(Long categoryId);
    boolean existsByName(String name);
    java.util.Optional<ProductEntity> findByName(String name);
}
