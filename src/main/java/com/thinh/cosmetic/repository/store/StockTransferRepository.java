package com.thinh.cosmetic.repository.store;

import com.thinh.cosmetic.domain.entity.store.StockTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Collection;

public interface StockTransferRepository extends JpaRepository<StockTransferEntity, Long> {
    List<StockTransferEntity> findBySourceStoreIdOrDestinationStoreId(Long sourceStoreId, Long destinationStoreId);
    List<StockTransferEntity> findBySourceStoreIdInAndDestinationStoreIdIn(Collection<Long> sourceStoreIds, Collection<Long> destinationStoreIds);
}
