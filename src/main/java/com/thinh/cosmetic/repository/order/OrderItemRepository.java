package com.thinh.cosmetic.repository.order;

import com.thinh.cosmetic.domain.entity.sales.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItemEntity oi LEFT JOIN FETCH oi.sku s LEFT JOIN FETCH s.product WHERE oi.order.id IN :orderIds")
    List<OrderItemEntity> findByOrderIdInWithSkuAndProduct(@Param("orderIds") Collection<Long> orderIds);

    boolean existsBySkuIdIn(List<Long> skuIds);
    boolean existsBySkuId(Long skuId);
}
