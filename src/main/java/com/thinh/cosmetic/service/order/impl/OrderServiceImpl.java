package com.thinh.cosmetic.service.order.impl;

import com.thinh.cosmetic.domain.dto.request.order.OrderRequest;
import com.thinh.cosmetic.domain.dto.response.order.OrderItemResponse;
import com.thinh.cosmetic.domain.dto.response.order.OrderResponse;
import com.thinh.cosmetic.domain.entity.account.CustomerAddressEntity;
import com.thinh.cosmetic.domain.entity.account.CustomerEntity;
import com.thinh.cosmetic.domain.entity.sales.CartEntity;
import com.thinh.cosmetic.domain.entity.sales.CartItemEntity;
import com.thinh.cosmetic.domain.entity.catalog.ProductSkuEntity;
import com.thinh.cosmetic.domain.entity.sales.OrderEntity;
import com.thinh.cosmetic.domain.entity.sales.OrderItemEntity;
import com.thinh.cosmetic.domain.entity.sales.OrderReservationEntity;

import com.thinh.cosmetic.domain.entity.inventory.InventoryEntity;
import com.thinh.cosmetic.domain.entity.inventory.StoreEntity;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.domain.enums.ReservationStatus;
import com.thinh.cosmetic.domain.enums.OrderStatus;
import com.thinh.cosmetic.domain.entity.catalog.ProductImageEntity;
import com.thinh.cosmetic.exception.BusinessException;
import com.thinh.cosmetic.exception.ErrorCode;
import com.thinh.cosmetic.repository.account.CustomerAddressRepository;
import com.thinh.cosmetic.repository.account.CustomerRepository;
import com.thinh.cosmetic.repository.cart.CartItemRepository;
import com.thinh.cosmetic.repository.cart.CartRepository;
import com.thinh.cosmetic.repository.catalog.ProductImageRepository;
import com.thinh.cosmetic.repository.order.OrderItemRepository;
import com.thinh.cosmetic.repository.order.OrderRepository;
import com.thinh.cosmetic.repository.order.OrderReservationRepository;

import com.thinh.cosmetic.repository.store.InventoryRepository;
import com.thinh.cosmetic.repository.store.StoreRepository;
import com.thinh.cosmetic.service.order.OrderService;

import com.thinh.cosmetic.domain.entity.sales.VoucherEntity;
import com.thinh.cosmetic.repository.order.VoucherRepository;
import com.thinh.cosmetic.service.order.VoucherService;
import com.thinh.cosmetic.security.AuthPrincipal;
import com.thinh.cosmetic.security.StoreScope;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderReservationRepository stockHoldRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductImageRepository productImageRepository;
    private final StoreScope storeScope;
    private final VoucherService voucherService;
    private final VoucherRepository voucherRepository;


    @Override
    public OrderResponse placeOrder(Long customerId, OrderRequest request) throws Exception {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Không tìm thấy khách hàng: " + customerId));

        CartEntity cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPTY_CART, "Giỏ hàng của bạn đang trống"));

        List<CartItemEntity> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_CART, "Giỏ hàng của bạn đang trống. Vui lòng thêm sản phẩm vào giỏ!");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItemEntity item : cartItems) {
            BigDecimal price = item.getSku().getPrice() != null ? item.getSku().getPrice() : BigDecimal.ZERO;
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // Resolve address details
        CustomerAddressEntity address = null;
        if (request.getCustomerAddressId() != null) {
            address = addressRepository.findById(request.getCustomerAddressId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND, "Không tìm thấy địa chỉ giao hàng: " + request.getCustomerAddressId()));
            if (address.getCustomerEntity() == null || !customer.getId().equals(address.getCustomerEntity().getId())) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED, "Địa chỉ giao hàng không thuộc tài khoản của bạn");
            }
        }

        String recipientName = (address != null && address.getRecipientName() != null && !address.getRecipientName().isBlank())
                ? address.getRecipientName()
                : (request.getRecipientName() != null && !request.getRecipientName().isBlank() ? request.getRecipientName().trim() : (customer.getFullName() != null ? customer.getFullName() : "Khách hàng LUNEA"));

        String recipientPhone = (address != null && address.getPhone() != null && !address.getPhone().isBlank())
                ? address.getPhone()
                : (request.getRecipientPhone() != null && !request.getRecipientPhone().isBlank() ? request.getRecipientPhone().trim() : (customer.getAccount() != null && customer.getAccount().getPhone() != null ? customer.getAccount().getPhone() : "0900000000"));

        String shippingAddress = (address != null && address.getAddressDetail() != null && !address.getAddressDetail().isBlank())
                ? address.getAddressDetail()
                : (request.getShippingAddress() != null ? request.getShippingAddress().trim() : "");

        String ward = (address != null && address.getWard() != null)
                ? address.getWard()
                : (request.getWard() != null ? request.getWard().trim() : "Phường Bến Nghé");

        String district = (address != null && address.getDistrict() != null)
                ? address.getDistrict()
                : (request.getDistrict() != null ? request.getDistrict().trim() : "Quận 1");

        String province = (address != null && address.getProvince() != null)
                ? address.getProvince()
                : (request.getProvince() != null ? request.getProvince().trim() : "TP. Hồ Chí Minh");

        if (shippingAddress.isBlank()) {
            shippingAddress = "Địa chỉ nhận hàng mặc định";
        }

        // Save newly provided address for customer if they didn't pick an existing one
        if (address == null && !shippingAddress.isBlank()) {
            try {
                boolean hasSaved = !addressRepository.findByCustomerEntityId(customer.getId()).isEmpty();
                CustomerAddressEntity newAddr = CustomerAddressEntity.builder()
                        .customerEntity(customer)
                        .recipientName(recipientName)
                        .phone(recipientPhone)
                        .province(province)
                        .district(district)
                        .ward(ward)
                        .addressDetail(shippingAddress)
                        .isDefault(!hasSaved)
                        .build();
                addressRepository.save(newAddr);
            } catch (Exception ignored) {}
        }

        // Resolve dynamic voucher and discounts
        String voucherCode = request.getVoucherCode() != null ? request.getVoucherCode().trim().toUpperCase() : "";
        VoucherEntity appliedVoucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal freeShippingThreshold = new BigDecimal("500000");
        BigDecimal shippingFee = subtotal.compareTo(freeShippingThreshold) >= 0 ? BigDecimal.ZERO : new BigDecimal("30000");

        if (!voucherCode.isEmpty()) {
            appliedVoucher = voucherService.validateAndApply(voucherCode, subtotal);
            if (appliedVoucher != null) {
                discountAmount = voucherService.calculateDiscount(appliedVoucher, subtotal);
                BigDecimal shipDiscount = voucherService.calculateShippingDiscount(appliedVoucher, shippingFee);
                shippingFee = shippingFee.subtract(shipDiscount).max(BigDecimal.ZERO);
            }
        }
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(shippingFee).max(BigDecimal.ZERO);

        // Group required quantities by SKU to handle duplicate SKUs in cart correctly
        Map<Long, Integer> skuQuantities = new HashMap<>();
        for (CartItemEntity item : cartItems) {
            skuQuantities.merge(item.getSku().getId(), item.getQuantity(), Integer::sum);
        }

        // Store selection & stock check (accelerated via idx_inv_store_sku index)
        StoreEntity assignedStore;
        if (request.getFulfillmentStoreId() != null) {
            StoreEntity requestedStore = storeRepository.findById(request.getFulfillmentStoreId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND, "Không tìm thấy chi nhánh: " + request.getFulfillmentStoreId()));

            if (requestedStore.getStatus() == ActiveStatus.INACTIVE) {
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND, "Chi nhánh đã ngừng hoạt động: " + requestedStore.getName());
            }

            for (Map.Entry<Long, Integer> entry : skuQuantities.entrySet()) {
                Long skuId = entry.getKey();
                int requiredQty = entry.getValue();
                var invOpt = inventoryRepository.findByStoreIdAndSkuId(requestedStore.getId(), skuId);
                if (invOpt.isEmpty()) {
                    throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                            String.format("Chi nhánh '%s' không có thông tin tồn kho cho sản phẩm SKU #%d", requestedStore.getName(), skuId));
                }
                var inv = invOpt.get();
                int availableStock = inv.getActualStock() - inv.getHeldQuantity();
                if (availableStock < requiredQty) {
                    throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                            String.format("Chi nhánh '%s' không đủ tồn kho khả dụng cho sản phẩm SKU #%d (cần %d, khả dụng %d)",
                                    requestedStore.getName(), skuId, requiredQty, Math.max(0, availableStock)));
                }
            }
            assignedStore = requestedStore;
        } else {
            List<StoreEntity> stores = storeRepository.findAll();
            List<StoreEntity> activeStores = stores.stream()
                    .filter(s -> s.getStatus() == null || s.getStatus() == ActiveStatus.ACTIVE)
                    .toList();

            List<StoreEntity> capableStores = new ArrayList<>();
            for (StoreEntity store : activeStores) {
                boolean canFulfillAll = true;
                for (Map.Entry<Long, Integer> entry : skuQuantities.entrySet()) {
                    var invOpt = inventoryRepository.findByStoreIdAndSkuId(store.getId(), entry.getKey());
                    if (invOpt.isEmpty() || (invOpt.get().getActualStock() - invOpt.get().getHeldQuantity()) < entry.getValue()) {
                        canFulfillAll = false;
                        break;
                    }
                }
                if (canFulfillAll) {
                    capableStores.add(store);
                }
            }

            if (capableStores.isEmpty()) {
                throw new BusinessException(ErrorCode.NO_STORE_CAN_FULFILL,
                        "Không có chi nhánh nào có đủ hàng để đáp ứng toàn bộ đơn hàng của bạn");
            }

            String provLower = (province != null) ? province.trim().toLowerCase() : "";
            List<StoreEntity> sameProvinceStores = capableStores.stream()
                    .filter(s -> s.getProvince() != null && !provLower.isEmpty() &&
                            (provLower.contains(s.getProvince().toLowerCase()) || s.getProvince().toLowerCase().contains(provLower)))
                    .sorted(Comparator.comparing(StoreEntity::getId))
                    .toList();

            if (!sameProvinceStores.isEmpty()) {
                assignedStore = sameProvinceStores.get(0);
            } else {
                assignedStore = capableStores.stream()
                        .sorted(Comparator.comparing(StoreEntity::getId))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.NO_STORE_CAN_FULFILL, "Không có chi nhánh phù hợp"));
            }
        }

        String orderCode = "LUN-" + (int)(Math.random() * 900000 + 100000);

        OrderEntity order = OrderEntity.builder()
                .orderCode(orderCode)
                .customer(customer)
                .assignedStore(assignedStore)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .shippingAddress(shippingAddress)
                .ward(ward)
                .district(district)
                .province(province)
                .voucher(appliedVoucher)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : com.thinh.cosmetic.domain.enums.PaymentMethod.COD)
                .note(request.getNote())
                .build();
        order = orderRepository.save(order);

        // Record voucher usage if voucher was applied
        if (appliedVoucher != null) {
            voucherService.incrementUsage(appliedVoucher);
        }

        // Sort items by SKU ID to prevent deadlocks across concurrent orders
        List<CartItemEntity> sortedCartItems = new ArrayList<>(cartItems);
        sortedCartItems.sort(Comparator.comparing(item -> item.getSku().getId()));

        List<OrderItemEntity> orderItems = new ArrayList<>();
        for (CartItemEntity item : sortedCartItems) {
            ProductSkuEntity sku = item.getSku();
            BigDecimal price = sku.getPrice() != null ? sku.getPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .sku(sku)
                    .snapshotProductName(sku.getProduct() != null ? sku.getProduct().getName() : "Sản phẩm")
                    .snapshotVariantName(sku.getVariantName())
                    .snapshotPrice(price)
                    .quantity(item.getQuantity())
                    .subtotal(lineTotal)
                    .build();
            orderItems.add(orderItemRepository.save(orderItem));

            // Verify inventory record exists and re-check available stock at reservation time
            InventoryEntity inv = inventoryRepository.findByStoreIdAndSkuId(assignedStore.getId(), sku.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                            String.format("Không tìm thấy bản ghi tồn kho cho sản phẩm SKU #%d tại chi nhánh '%s'", sku.getId(), assignedStore.getName())));

            int availableStock = inv.getActualStock() - inv.getHeldQuantity();
            if (availableStock < item.getQuantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                        String.format("Tồn kho khả dụng của sản phẩm '%s' không đủ (cần %d, khả dụng %d)",
                                sku.getProduct() != null ? sku.getProduct().getName() : sku.getSkuCode(),
                                item.getQuantity(), Math.max(0, availableStock)));
            }

            // Update held stock in inventory first
            inv.setHeldQuantity(inv.getHeldQuantity() + item.getQuantity());
            inv.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inv);

            // Create hold reservation record
            stockHoldRepository.save(OrderReservationEntity.builder()
                    .order(order)
                    .sku(sku)
                    .store(assignedStore)
                    .quantity(item.getQuantity())
                    .status(ReservationStatus.HELD)
                    .build());
        }
        order.setItems(orderItems);

        // Clear cart
        cartItemRepository.deleteByCartId(cart.getId());

        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse previewOrder(Long customerId, OrderRequest request) throws Exception {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Không tìm thấy khách hàng: " + customerId));

        CartEntity cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPTY_CART, "Giỏ hàng của bạn đang trống"));

        List<CartItemEntity> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_CART, "Giỏ hàng của bạn đang trống");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (CartItemEntity item : cartItems) {
            ProductSkuEntity sku = item.getSku();
            BigDecimal price = sku.getPrice() != null ? sku.getPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            String imgUrl = null;
            if (productImageRepository != null && sku.getProduct() != null) {
                imgUrl = productImageRepository.findFirstByProductIdAndIsPrimaryTrue(sku.getProduct().getId())
                        .map(ProductImageEntity::getImageUrl)
                        .orElse(null);
            }

            itemResponses.add(OrderItemResponse.builder()
                    .id(item.getId())
                    .skuId(sku.getId())
                    .productId(sku.getProduct() != null ? sku.getProduct().getId() : null)
                    .productName(sku.getProduct() != null ? sku.getProduct().getName() : "Sản phẩm")
                    .variantName(sku.getVariantName())
                    .imageUrl(imgUrl)
                    .price(price)
                    .quantity(item.getQuantity())
                    .subtotal(lineTotal)
                    .build());
        }

        String voucher = request.getVoucherCode() != null ? request.getVoucherCode().trim().toUpperCase() : "";
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal freeShippingThreshold = new BigDecimal("500000");
        BigDecimal shippingFee = subtotal.compareTo(freeShippingThreshold) >= 0 ? BigDecimal.ZERO : new BigDecimal("30000");
        Long voucherId = null;

        if (!voucher.isEmpty()) {
            var validation = voucherService.validateVoucher(voucher, subtotal, shippingFee);
            if (validation.isValid()) {
                discountAmount = validation.getDiscountAmount();
                shippingFee = shippingFee.subtract(validation.getShippingDiscountAmount()).max(BigDecimal.ZERO);
                var vOpt = voucherRepository.findByCodeIgnoreCase(voucher);
                if (vOpt.isPresent()) {
                    voucherId = vOpt.get().getId();
                }
            } else {
                throw new BusinessException(ErrorCode.VOUCHER_INVALID, validation.getMessage());
            }
        }
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(shippingFee).max(BigDecimal.ZERO);

        return OrderResponse.builder()
                .voucherId(voucherId)
                .voucherCode(!voucher.isEmpty() ? voucher : null)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) throws Exception {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Không tìm thấy đơn hàng #" + id));
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(AuthPrincipal principal, Long id) throws Exception {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Không tìm thấy đơn hàng #" + id));
        if (principal != null && order.getAssignedStore() != null) {
            if (!storeScope.canAccessStore(principal, order.getAssignedStore().getId())) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED, "Không có quyền xem đơn hàng thuộc chi nhánh này");
            }
        }
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getByOrderCode(String orderCode) throws Exception {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Không tìm thấy đơn hàng với mã: " + orderCode));
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getByCustomer(Long customerId) {
        return toResponseBatch(orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return toResponseBatch(orderRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(AuthPrincipal principal, Long requestedStoreId) {
        if (principal == null) {
            return getAll();
        }

        List<Long> accessibleIds = storeScope.getAccessibleStoreIds(principal);
        if (accessibleIds == null) {
            // Admin or ALL_STORES
            if (requestedStoreId != null) {
                return toResponseBatch(orderRepository.findByAssignedStoreIdOrderByCreatedAtDesc(requestedStoreId));
            }
            return toResponseBatch(orderRepository.findAllByOrderByCreatedAtDesc());
        }

        if (accessibleIds.isEmpty()) {
            return List.of();
        }

        if (requestedStoreId != null) {
            if (!accessibleIds.contains(requestedStoreId)) {
                return List.of();
            }
            return toResponseBatch(orderRepository.findByAssignedStoreIdOrderByCreatedAtDesc(requestedStoreId));
        }

        return toResponseBatch(orderRepository.findByAssignedStoreIdInOrderByCreatedAtDesc(accessibleIds));
    }

    @Override
    public OrderResponse updateStatus(Long id, OrderStatus status) throws Exception {
        return updateStatus(null, id, status);
    }

    @Override
    public OrderResponse updateStatus(AuthPrincipal principal, Long id, OrderStatus status) throws Exception {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Không tìm thấy đơn hàng #" + id));

        if (principal != null && order.getAssignedStore() != null) {
            if (!storeScope.canAccessStore(principal, order.getAssignedStore().getId())) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED, "Bạn không có quyền thao tác đơn hàng của chi nhánh: " + order.getAssignedStore().getName());
            }
        }

        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == status) {
            return toResponse(order);
        }

        if (!currentStatus.canTransitionTo(status)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("Không thể chuyển trạng thái đơn hàng từ %s sang %s", currentStatus, status));
        }

        order.setStatus(status);

        if (status == OrderStatus.CONFIRMED) {
            order.setConfirmedAt(LocalDateTime.now());
        } else if (status == OrderStatus.SHIPPING) {
            // Deduct actual stock on shipping (commit hold)
            commitHeldInventory(id);
        } else if (status == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
            // Safety: commit any remaining held stock if somehow not already committed
            commitHeldInventory(id);
        } else if (status == OrderStatus.CANCELLED) {
            order.setCancelledAt(LocalDateTime.now());
            // Release voucher usage count if one was applied
            if (order.getVoucher() != null) {
                voucherService.releaseUsage(order.getVoucher());
            }
            // Release stock hold
            List<OrderReservationEntity> holds = stockHoldRepository.findByOrderId(id);
            for (OrderReservationEntity hold : holds) {
                if (hold.getStatus() == ReservationStatus.HELD) {
                    hold.setStatus(ReservationStatus.RELEASED);
                    stockHoldRepository.save(hold);

                    inventoryRepository.findByStoreIdAndSkuId(hold.getStore().getId(), hold.getSku().getId())
                            .ifPresent(inv -> {
                                inv.setHeldQuantity(Math.max(0, inv.getHeldQuantity() - hold.getQuantity()));
                                inv.setUpdatedAt(LocalDateTime.now());
                                inventoryRepository.save(inv);
                            });
                }
            }
        }

        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse cancelOrder(Long id, Long customerId) throws Exception {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Không tìm thấy đơn hàng #" + id));

        if (order.getCustomer() != null && !order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Bạn không có quyền hủy đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CANCELLABLE, "Chỉ có thể hủy đơn hàng khi đơn còn ở trạng thái Chờ xác nhận");
        }

        return updateStatus(id, OrderStatus.CANCELLED);
    }

    private void commitHeldInventory(Long orderId) {
        List<OrderReservationEntity> holds = stockHoldRepository.findByOrderId(orderId);
        for (OrderReservationEntity hold : holds) {
            if (hold.getStatus() == ReservationStatus.HELD) {
                hold.setStatus(ReservationStatus.COMMITTED);
                stockHoldRepository.save(hold);

                inventoryRepository.findByStoreIdAndSkuId(hold.getStore().getId(), hold.getSku().getId())
                        .ifPresent(inv -> {
                            inv.setActualStock(Math.max(0, inv.getActualStock() - hold.getQuantity()));
                            inv.setHeldQuantity(Math.max(0, inv.getHeldQuantity() - hold.getQuantity()));
                            inv.setUpdatedAt(LocalDateTime.now());
                            inventoryRepository.save(inv);
                        });
            }
        }
    }

    private List<OrderResponse> toResponseBatch(List<OrderEntity> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(OrderEntity::getId).toList();

        // 1. Batch fetch all order items with SKU and Product eagerly fetched in 1 query
        List<OrderItemEntity> allItems = orderItemRepository.findByOrderIdInWithSkuAndProduct(orderIds);
        Map<Long, List<OrderItemEntity>> itemsMap = allItems.stream()
                .filter(i -> i.getOrder() != null)
                .collect(Collectors.groupingBy(i -> i.getOrder().getId()));

        // 2. Collect product IDs from items to batch fetch primary images in 1 query
        Set<Long> productIds = allItems.stream()
                .map(i -> (i.getSku() != null && i.getSku().getProduct() != null) ? i.getSku().getProduct().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> primaryImageMap;
        if (productImageRepository != null && !productIds.isEmpty()) {
            List<ProductImageEntity> primaryImages = productImageRepository.findByProductIdInAndIsPrimaryTrue(productIds);
            primaryImageMap = primaryImages.stream()
                    .filter(img -> img.getProduct() != null)
                    .collect(Collectors.toMap(
                            img -> img.getProduct().getId(),
                            ProductImageEntity::getImageUrl,
                            (a, b) -> a
                    ));
        } else {
            primaryImageMap = Map.of();
        }

        // 3. Map orders in memory without any additional database queries
        return orders.stream().map(o -> {
            List<OrderItemEntity> items = itemsMap.getOrDefault(o.getId(), List.of());

            List<OrderItemResponse> itemResponses = items.stream().map(i -> {
                Long productId = (i.getSku() != null && i.getSku().getProduct() != null) ? i.getSku().getProduct().getId() : null;
                String imgUrl = productId != null ? primaryImageMap.get(productId) : null;

                return OrderItemResponse.builder()
                        .id(i.getId())
                        .skuId(i.getSku() != null ? i.getSku().getId() : null)
                        .productId(productId)
                        .productName(i.getSnapshotProductName())
                        .variantName(i.getSnapshotVariantName())
                        .imageUrl(imgUrl)
                        .price(i.getSnapshotPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .build();
            }).toList();

            String fullAddress = o.getShippingAddress() != null ? o.getShippingAddress() : "";
            if (o.getWard() != null && !o.getWard().isBlank() && !fullAddress.contains(o.getWard())) {
                fullAddress += ", " + o.getWard();
            }
            if (o.getDistrict() != null && !o.getDistrict().isBlank() && !fullAddress.contains(o.getDistrict())) {
                fullAddress += ", " + o.getDistrict();
            }
            if (o.getProvince() != null && !o.getProvince().isBlank() && !fullAddress.contains(o.getProvince())) {
                fullAddress += ", " + o.getProvince();
            }

            return OrderResponse.builder()
                    .id(o.getId())
                    .orderCode(o.getOrderCode())
                    .customerId(o.getCustomer() != null ? o.getCustomer().getId() : null)
                    .customerName(o.getCustomer() != null ? o.getCustomer().getFullName() : null)
                    .fulfillmentStoreId(o.getAssignedStore() != null ? o.getAssignedStore().getId() : null)
                    .fulfillmentStoreName(o.getAssignedStore() != null ? o.getAssignedStore().getName() : null)
                    .recipientName(o.getRecipientName())
                    .recipientPhone(o.getRecipientPhone())
                    .shippingAddress(fullAddress)
                    .ward(o.getWard())
                    .district(o.getDistrict())
                    .province(o.getProvince())
                    .deliveryMethod(o.getDeliveryMethod())
                    .voucherId(o.getVoucher() != null ? o.getVoucher().getId() : null)
                    .voucherCode(o.getVoucher() != null ? o.getVoucher().getCode() : null)
                    .subtotal(o.getSubtotal())
                    .discountAmount(o.getDiscountAmount())
                    .shippingFee(o.getShippingFee())
                    .totalAmount(o.getTotalAmount())
                    .status(o.getStatus())
                    .paymentMethod(o.getPaymentMethod())
                    .note(o.getNote())
                    .createdAt(o.getCreatedAt())
                    .confirmedAt(o.getConfirmedAt())
                    .completedAt(o.getCompletedAt())
                    .cancelledAt(o.getCancelledAt())
                    .items(itemResponses)
                    .build();
        }).toList();
    }

    private OrderResponse toResponse(OrderEntity o) {
        List<OrderItemEntity> items = o.getItems() != null ? o.getItems() :
                orderItemRepository.findByOrderId(o.getId());

        List<OrderItemResponse> itemResponses = items.stream()
                .map(i -> {
                    String imgUrl = null;
                    if (productImageRepository != null && i.getSku() != null && i.getSku().getProduct() != null) {
                        imgUrl = productImageRepository.findFirstByProductIdAndIsPrimaryTrue(i.getSku().getProduct().getId())
                                .map(ProductImageEntity::getImageUrl)
                                .orElse(null);
                    }
                    return OrderItemResponse.builder()
                            .id(i.getId())
                            .skuId(i.getSku() != null ? i.getSku().getId() : null)
                            .productId((i.getSku() != null && i.getSku().getProduct() != null) ? i.getSku().getProduct().getId() : null)
                            .productName(i.getSnapshotProductName())
                            .variantName(i.getSnapshotVariantName())
                            .imageUrl(imgUrl)
                            .price(i.getSnapshotPrice())
                            .quantity(i.getQuantity())
                            .subtotal(i.getSubtotal())
                            .build();
                })
                .toList();

        String fullAddress = o.getShippingAddress() != null ? o.getShippingAddress() : "";
        if (o.getWard() != null && !o.getWard().isBlank() && !fullAddress.contains(o.getWard())) {
            fullAddress += ", " + o.getWard();
        }
        if (o.getDistrict() != null && !o.getDistrict().isBlank() && !fullAddress.contains(o.getDistrict())) {
            fullAddress += ", " + o.getDistrict();
        }
        if (o.getProvince() != null && !o.getProvince().isBlank() && !fullAddress.contains(o.getProvince())) {
            fullAddress += ", " + o.getProvince();
        }

        return OrderResponse.builder()
                .id(o.getId())
                .orderCode(o.getOrderCode())
                .customerId(o.getCustomer() != null ? o.getCustomer().getId() : null)
                .customerName(o.getCustomer() != null ? o.getCustomer().getFullName() : null)
                .fulfillmentStoreId(o.getAssignedStore() != null ? o.getAssignedStore().getId() : null)
                .fulfillmentStoreName(o.getAssignedStore() != null ? o.getAssignedStore().getName() : null)
                .recipientName(o.getRecipientName())
                .recipientPhone(o.getRecipientPhone())
                .shippingAddress(fullAddress)
                .ward(o.getWard())
                .district(o.getDistrict())
                .province(o.getProvince())
                .deliveryMethod(o.getDeliveryMethod())
                .voucherId(o.getVoucher() != null ? o.getVoucher().getId() : null)
                .voucherCode(o.getVoucher() != null ? o.getVoucher().getCode() : null)
                .subtotal(o.getSubtotal())
                .discountAmount(o.getDiscountAmount())
                .shippingFee(o.getShippingFee())
                .totalAmount(o.getTotalAmount())
                .status(o.getStatus())
                .paymentMethod(o.getPaymentMethod())
                .note(o.getNote())
                .createdAt(o.getCreatedAt())
                .confirmedAt(o.getConfirmedAt())
                .completedAt(o.getCompletedAt())
                .cancelledAt(o.getCancelledAt())
                .items(itemResponses)
                .build();
    }
}
