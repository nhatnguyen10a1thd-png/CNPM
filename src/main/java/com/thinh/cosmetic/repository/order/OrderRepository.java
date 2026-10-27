package com.thinh.cosmetic.repository.order;

import com.thinh.cosmetic.domain.entity.sales.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<OrderEntity> findByOrderCode(String orderCode);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.assignedStore LEFT JOIN FETCH o.voucher WHERE o.assignedStore.id = :storeId ORDER BY o.createdAt DESC")
    List<OrderEntity> findByAssignedStoreIdOrderByCreatedAtDesc(@Param("storeId") Long storeId);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.assignedStore LEFT JOIN FETCH o.voucher WHERE o.assignedStore.id IN :storeIds ORDER BY o.createdAt DESC")
    List<OrderEntity> findByAssignedStoreIdInOrderByCreatedAtDesc(@Param("storeIds") List<Long> storeIds);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.assignedStore LEFT JOIN FETCH o.voucher ORDER BY o.createdAt DESC")
    List<OrderEntity> findAllByOrderByCreatedAtDesc();

    @Query("SELECT o.customer.id, COUNT(o), SUM(CASE WHEN o.status != com.thinh.cosmetic.domain.enums.OrderStatus.CANCELLED THEN o.totalAmount ELSE 0 END) " +
           "FROM OrderEntity o WHERE o.customer.id IS NOT NULL GROUP BY o.customer.id")
    List<Object[]> getCustomerOrderStats();
}
