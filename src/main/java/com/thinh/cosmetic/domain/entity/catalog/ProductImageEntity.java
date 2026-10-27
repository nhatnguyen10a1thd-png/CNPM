package com.thinh.cosmetic.domain.entity.catalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product_images", indexes = {
    @Index(name = "idx_images_prod_primary", columnList = "product_id, is_primary")
})
public class ProductImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Product cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id")
    private ProductSkuEntity productSku;

    @NotBlank(message = "Image URL cannot be blank")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Column(name = "url", nullable = false, length = 500)
    private String imageUrl;

    @Size(max = 200, message = "Public ID must not exceed 200 characters")
    @Column(name = "public_id", length = 200)
    private String publicId;

    @NotNull(message = "IsPrimary flag cannot be null")
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @NotNull(message = "Sort order cannot be null")
    @Min(value = 0, message = "Sort order must be greater than or equal to 0")
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
