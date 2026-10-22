package com.thinh.cosmetic.repository.catalog;

import com.thinh.cosmetic.domain.entity.catalog.ProductAttributeValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValueEntity, Long> {
    List<ProductAttributeValueEntity> findByProductId(Long productId);

    @Query("SELECT pav FROM ProductAttributeValueEntity pav JOIN FETCH pav.attribute WHERE pav.product.id IN :productIds")
    List<ProductAttributeValueEntity> findByProductIdInWithAttribute(@Param("productIds") Collection<Long> productIds);

    boolean existsByProductIdAndAttributeId(Long productId, Long attributeId);
    void deleteByProductId(Long productId);
}
