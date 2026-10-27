package com.thinh.cosmetic.domain.entity.sales;

import com.thinh.cosmetic.domain.entity.catalog.ProductSkuEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_items_order", columnList = "order_id"),
    @Index(name = "idx_items_sku", columnList = "sku_id")
})
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Order cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @NotNull(message = "SKU cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSkuEntity sku;

    @NotBlank(message = "Product name snapshot cannot be blank")
    @Size(max = 200, message = "Product name snapshot must not exceed 200 characters")
    @Column(name = "product_name", nullable = false, length = 200)
    private String snapshotProductName;

    @Size(max = 150, message = "Variant name snapshot must not exceed 150 characters")
    @Column(name = "variant_name", length = 150)
    private String snapshotVariantName;

    @NotNull(message = "Unit price snapshot cannot be null")
    @DecimalMin(value = "0.0", message = "Unit price snapshot must be greater than or equal to 0")
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal snapshotPrice;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be greater than 0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "Subtotal cannot be null")
    @DecimalMin(value = "0.0", message = "Subtotal must be greater than or equal to 0")
    @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal;
}
