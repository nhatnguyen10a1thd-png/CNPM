package com.thinh.cosmetic.repository.catalog;

import com.thinh.cosmetic.domain.entity.catalog.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    @Query("SELECT DISTINCT p FROM ProductEntity p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category")
    List<ProductEntity> findAllWithBrandAndCategory();

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
