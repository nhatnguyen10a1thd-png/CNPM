package com.thinh.cosmetic.repository.cart;

import com.thinh.cosmetic.domain.entity.sales.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT ci FROM CartItemEntity ci JOIN FETCH ci.sku s JOIN FETCH s.product p WHERE ci.cart.id = :cartId")
    List<CartItemEntity> findByCartId(@org.springframework.data.repository.query.Param("cartId") Long cartId);

    Optional<CartItemEntity> findByCartIdAndSkuId(Long cartId, Long skuId);
    void deleteByCartId(Long cartId);
    void deleteBySkuIdIn(List<Long> skuIds);
}
