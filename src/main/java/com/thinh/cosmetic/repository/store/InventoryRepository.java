package com.thinh.cosmetic.repository.store;

import com.thinh.cosmetic.domain.entity.inventory.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    @Query("SELECT i FROM InventoryEntity i JOIN FETCH i.sku s JOIN FETCH s.product p JOIN FETCH i.store st WHERE st.id = :storeId")
    List<InventoryEntity> findByStoreId(@Param("storeId") Long storeId);

    Optional<InventoryEntity> findByStoreIdAndSkuId(Long storeId, Long skuId);
    List<InventoryEntity> findBySkuId(Long skuId);
    List<InventoryEntity> findBySkuIdIn(java.util.Collection<Long> skuIds);

    @Query("SELECT i FROM InventoryEntity i JOIN FETCH i.sku s JOIN FETCH s.product p JOIN FETCH i.store st WHERE st.id = :storeId AND i.actualStock <= i.minimumStock")
    List<InventoryEntity> findLowStock(@Param("storeId") Long storeId);

    boolean existsBySkuIdIn(List<Long> skuIds);
}
