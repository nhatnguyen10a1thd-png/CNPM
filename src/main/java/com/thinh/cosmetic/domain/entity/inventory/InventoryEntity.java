package com.thinh.cosmetic.domain.entity.inventory;

import com.thinh.cosmetic.domain.entity.catalog.ProductSkuEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "inventories", indexes = {
    @Index(name = "idx_inv_store_sku", columnList = "store_id, sku_id"),
    @Index(name = "idx_inv_sku", columnList = "sku_id")
})
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Store cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @NotNull(message = "SKU cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSkuEntity sku;

    @NotNull(message = "Actual stock quantity cannot be null")
    @Min(value = 0, message = "Actual stock quantity must be greater than or equal to 0")
    @Column(name = "on_hand_qty", nullable = false)
    @Builder.Default
    private Integer actualStock = 0;

    @NotNull(message = "Held quantity cannot be null")
    @Min(value = 0, message = "Held quantity must be greater than or equal to 0")
    @Column(name = "reserved_qty", nullable = false)
    @Builder.Default
    private Integer heldQuantity = 0;

    @NotNull(message = "Minimum stock threshold cannot be null")
    @Min(value = 0, message = "Minimum stock threshold must be greater than or equal to 0")
    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer minimumStock = 5;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
