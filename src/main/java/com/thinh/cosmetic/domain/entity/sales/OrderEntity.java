package com.thinh.cosmetic.domain.entity.sales;

import com.thinh.cosmetic.domain.entity.account.CustomerEntity;
import com.thinh.cosmetic.domain.entity.inventory.StoreEntity;
import com.thinh.cosmetic.domain.enums.DeliveryMethod;
import com.thinh.cosmetic.domain.enums.OrderStatus;
import com.thinh.cosmetic.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer", columnList = "customer_id"),
    @Index(name = "idx_orders_store", columnList = "fulfillment_store_id"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_created", columnList = "created_at")
})
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Order code cannot be blank")
    @Size(max = 30, message = "Order code must not exceed 30 characters")
    @Column(name = "order_code", nullable = false, unique = true, length = 30)
    private String orderCode;

    @NotNull(message = "Customer cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @NotNull(message = "Fulfillment store cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fulfillment_store_id", nullable = false)
    private StoreEntity assignedStore;

    @NotBlank(message = "Recipient name cannot be blank")
    @Size(max = 150, message = "Recipient name must not exceed 150 characters")
    @Column(name = "receiver_name", nullable = false, length = 150)
    private String recipientName;

    @NotBlank(message = "Recipient phone cannot be blank")
    @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String recipientPhone;

    @NotBlank(message = "Shipping address cannot be blank")
    @Size(max = 500, message = "Shipping address must not exceed 500 characters")
    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Size(max = 100, message = "Ward must not exceed 100 characters")
    @Column(name = "ward", length = 100)
    private String ward;

    @Size(max = 100, message = "District must not exceed 100 characters")
    @Column(name = "district", length = 100)
    private String district;

    @Size(max = 100, message = "Province must not exceed 100 characters")
    @Column(name = "province", length = 100)
    private String province;

    @NotNull(message = "Delivery method cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false, length = 20)
    @Builder.Default
    private DeliveryMethod deliveryMethod = DeliveryMethod.HOME_DELIVERY;

    @NotNull(message = "Subtotal cannot be null")
    @DecimalMin(value = "0.0", message = "Subtotal must be greater than or equal to 0")
    @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private VoucherEntity voucher;

    @NotNull(message = "Discount amount cannot be null")
    @DecimalMin(value = "0.0", message = "Discount amount must be greater than or equal to 0")
    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Shipping fee cannot be null")
    @DecimalMin(value = "0.0", message = "Shipping fee must be greater than or equal to 0")
    @Column(name = "shipping_fee", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0.0", message = "Total amount must be greater than or equal to 0")
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @NotNull(message = "Payment method cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    @Column(name = "note", length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> items;
}
