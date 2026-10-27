package com.thinh.cosmetic.domain.entity.catalog;

import com.thinh.cosmetic.domain.enums.ActiveStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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
@Table(name = "skus", indexes = {
    @Index(name = "idx_skus_product", columnList = "product_id"),
    @Index(name = "idx_skus_code", columnList = "sku_code")
})
public class ProductSkuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Product cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @NotBlank(message = "SKU code cannot be blank")
    @Size(max = 50, message = "SKU code must not exceed 50 characters")
    @Column(name = "sku_code", nullable = false, unique = true, length = 50)
    private String skuCode;

    @Size(max = 150, message = "Variant name must not exceed 150 characters")
    @Column(name = "variant_name", length = 150)
    private String variantName;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "List price must be greater than or equal to 0")
    @Column(name = "list_price", precision = 18, scale = 2)
    private BigDecimal listPrice;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    @Column(name = "barcode", unique = true, length = 100)
    private String barcode;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ActiveStatus status = ActiveStatus.ACTIVE;
}
