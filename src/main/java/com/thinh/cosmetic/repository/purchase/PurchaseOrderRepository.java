package com.thinh.cosmetic.repository.purchase;

import com.thinh.cosmetic.domain.entity.purchase.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Collection;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {
    List<PurchaseOrderEntity> findBySupplierId(Long supplierId);
    List<PurchaseOrderEntity> findByReceivingStoreId(Long storeId);
    List<PurchaseOrderEntity> findByReceivingStoreIdIn(Collection<Long> storeIds);
}
