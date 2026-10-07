package com.thinh.cosmetic.service.purchase.impl;

import com.thinh.cosmetic.domain.dto.request.purchase.PurchaseOrderRequest;
import com.thinh.cosmetic.domain.dto.response.purchase.PurchaseOrderResponse;
import com.thinh.cosmetic.domain.entity.catalog.ProductSkuEntity;
import com.thinh.cosmetic.domain.entity.purchase.*;
import com.thinh.cosmetic.domain.entity.store.InventoryEntity;
import com.thinh.cosmetic.domain.entity.store.StoreEntity;
import com.thinh.cosmetic.domain.enums.PurchaseOrderStatus;
import com.thinh.cosmetic.repository.account.EmployeeRepository;
import com.thinh.cosmetic.repository.catalog.ProductSkuRepository;
import com.thinh.cosmetic.repository.purchase.*;
import com.thinh.cosmetic.repository.store.InventoryRepository;
import com.thinh.cosmetic.repository.store.StoreRepository;
import com.thinh.cosmetic.service.purchase.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import com.thinh.cosmetic.security.PermissionPolicy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final StoreRepository storeRepository;
    private final ProductSkuRepository skuRepository;
    private final InventoryRepository inventoryRepository;
    private final EmployeeRepository employeeRepository;
    private final PermissionPolicy policy;

    @Override
    @PreAuthorize("@permissionPolicy.canStore('PURCHASE_MANAGE', #p0.receivingStoreId) and #p1 == @permissionPolicy.employeeId()")
    public PurchaseOrderResponse create(PurchaseOrderRequest request, Long employeeId) throws Exception {
        SupplierEntity supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new Exception("Supplier not found"));
        StoreEntity store = storeRepository.findById(request.getReceivingStoreId())
                .orElseThrow(() -> new Exception("Store not found"));

        PurchaseOrderEntity po = PurchaseOrderEntity.builder()
                .supplier(supplier)
                .receivingStore(store)
                .status(PurchaseOrderStatus.DRAFT)
                .createdBy(employeeRepository.findById(employeeId).orElse(null))
                .totalAmount(BigDecimal.ZERO)
                .build();
        po = purchaseOrderRepository.save(po);

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseOrderItemEntity> items = new ArrayList<>();
        for (PurchaseOrderRequest.PurchaseItemRequest itemReq : request.getItems()) {
            ProductSkuEntity sku = skuRepository.findById(itemReq.getSkuId())
                    .orElseThrow(() -> new Exception("SKU not found: " + itemReq.getSkuId()));
            BigDecimal lineTotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(lineTotal);

            PurchaseOrderItemEntity item = PurchaseOrderItemEntity.builder()
                    .purchaseOrder(po)
                    .sku(sku)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .subtotal(lineTotal)
                    .build();
            items.add(purchaseOrderItemRepository.save(item));
        }

        po.setTotalAmount(total);
        po.setItems(items);
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Override
    @PreAuthorize("@permissionPolicy.canPurchase('PURCHASE_MANAGE', #p0)")
    public PurchaseOrderResponse confirm(Long id) throws Exception {
        PurchaseOrderEntity po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new Exception("Purchase Order not found: " + id));
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new Exception("Can only confirm draft purchase orders");
        }

        List<PurchaseOrderItemEntity> items = purchaseOrderItemRepository.findByPurchaseOrderId(id);
        for (PurchaseOrderItemEntity item : items) {
            InventoryEntity inv = inventoryRepository.findByStoreIdAndSkuId(po.getReceivingStore().getId(), item.getSku().getId())
                    .orElseGet(() -> InventoryEntity.builder()
                            .store(po.getReceivingStore())
                            .sku(item.getSku())
                            .actualStock(0)
                            .heldQuantity(0)
                            .minimumStock(5)
                            .build());
            inv.setActualStock(inv.getActualStock() + item.getQuantity());
            inventoryRepository.save(inv);
        }

        po.setStatus(PurchaseOrderStatus.CONFIRMED);
        po.setConfirmedAt(LocalDateTime.now());
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionPolicy.canPurchase('PURCHASE_READ', #p0)")
    public PurchaseOrderResponse getById(Long id) throws Exception {
        PurchaseOrderEntity po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new Exception("Purchase Order not found: " + id));
        return toResponse(po);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAll() {
        policy.require("PURCHASE_READ");
        var rows = policy.isAdmin() ? purchaseOrderRepository.findAll() : purchaseOrderRepository.findByReceivingStoreIdIn(policy.storeIds());
        return rows.stream().map(this::toResponse).toList();
    }

    private PurchaseOrderResponse toResponse(PurchaseOrderEntity po) {
        List<PurchaseOrderItemEntity> items = po.getItems() != null ? po.getItems() :
                purchaseOrderItemRepository.findByPurchaseOrderId(po.getId());

        List<PurchaseOrderResponse.PurchaseItemResponse> itemResponses = items.stream()
                .map(i -> PurchaseOrderResponse.PurchaseItemResponse.builder()
                        .skuId(i.getSku().getId())
                        .skuCode(i.getSku().getSkuCode())
                        .productName(i.getSku().getProduct().getName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .build())
                .toList();

        return PurchaseOrderResponse.builder()
                .id(po.getId())
                .supplierName(po.getSupplier().getName())
                .receivingStoreName(po.getReceivingStore().getName())
                .status(po.getStatus())
                .createdByName(po.getCreatedBy() != null ? po.getCreatedBy().getFullName() : null)
                .createdAt(po.getCreatedAt())
                .confirmedAt(po.getConfirmedAt())
                .totalAmount(po.getTotalAmount())
                .items(itemResponses)
                .build();
    }
}
