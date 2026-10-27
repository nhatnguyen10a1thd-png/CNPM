package com.thinh.cosmetic.repository.review;

import com.thinh.cosmetic.domain.entity.review.ReviewEntity;
import com.thinh.cosmetic.domain.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByProductId(Long productId);

    @Query("SELECT r FROM ReviewEntity r JOIN FETCH r.customer JOIN FETCH r.product LEFT JOIN FETCH r.order WHERE r.product.id = :productId AND r.moderationStatus = :status ORDER BY r.createdAt DESC")
    List<ReviewEntity> findByProductIdAndModerationStatus(@Param("productId") Long productId, @Param("status") ReviewStatus status);

    @Query("SELECT r FROM ReviewEntity r JOIN FETCH r.customer JOIN FETCH r.product LEFT JOIN FETCH r.order WHERE r.customer.id = :customerId ORDER BY r.createdAt DESC")
    List<ReviewEntity> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(r) > 0 FROM ReviewEntity r WHERE r.customer.id = :customerId AND r.order.id = :orderId AND r.product.id = :productId")
    boolean existsByCustomerIdAndOrderIdAndProductId(@Param("customerId") Long customerId, @Param("orderId") Long orderId, @Param("productId") Long productId);

    boolean existsByProductId(Long productId);

    @Query("SELECT r FROM ReviewEntity r JOIN FETCH r.customer JOIN FETCH r.product LEFT JOIN FETCH r.order ORDER BY r.createdAt DESC")
    List<ReviewEntity> findAllByOrderByCreatedAtDesc();
}

