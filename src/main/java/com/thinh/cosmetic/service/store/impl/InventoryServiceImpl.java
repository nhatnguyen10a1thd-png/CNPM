package com.thinh.cosmetic.service.store.impl;

import com.thinh.cosmetic.domain.dto.request.store.InventoryAdjustmentRequest;
import com.thinh.cosmetic.domain.dto.response.store.InventoryResponse;
import com.thinh.cosmetic.domain.entity.store.InventoryAdjustmentEntity;
import com.thinh.cosmetic.domain.entity.store.InventoryEntity;
import com.thinh.cosmetic.repository.account.EmployeeRepository;
import com.thinh.cosmetic.repository.store.InventoryAdjustmentRepository;
import com.thinh.cosmetic.repository.store.InventoryRepository;
import com.thinh.cosmetic.service.store.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionPolicy.canStore('INVENTORY_READ', #p0)")
    public List<InventoryResponse> getByStore(Long storeId) {
        return inventoryRepository.findByStoreId(storeId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionPolicy.canStore('INVENTORY_READ', #p0)")
    public InventoryResponse getByStoreAndSku(Long storeId, Long skuId) throws Exception {
        InventoryEntity inv = inventoryRepository.findByStoreIdAndSkuId(storeId, skuId)
                .orElseThrow(() -> new Exception("Inventory not found for store=" + storeId + " sku=" + skuId));
        return toResponse(inv);
    }

    @Override
    @PreAuthorize("@permissionPolicy.canStore('INVENTORY_MANAGE', #p0.storeId) and #p1 == @permissionPolicy.employeeId()")
    public InventoryResponse adjustStock(InventoryAdjustmentRequest request, Long employeeId) throws Exception {
        InventoryEntity inv = inventoryRepository.findByStoreIdAndSkuId(request.getStoreId(), request.getSkuId())
                .orElseThrow(() -> new Exception("Inventory record not found"));

        InventoryAdjustmentEntity adj = InventoryAdjustmentEntity.builder()
                .store(inv.getStore())
                .sku(inv.getSku())
                .quantityBefore(inv.getActualStock())
                .quantityAfter(request.getNewQuantity())
                .reason(request.getReason())
                .performedBy(employeeRepository.findById(employeeId).orElse(null))
                .build();
        adjustmentRepository.save(adj);

        inv.setActualStock(request.getNewQuantity());
        return toResponse(inventoryRepository.save(inv));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionPolicy.canStore('INVENTORY_READ', #p0)")
    public List<InventoryResponse> getLowStockItems(Long storeId) {
        return inventoryRepository.findLowStock(storeId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionPolicy.canStore('INVENTORY_READ', #p0)")
    public Integer getAvailableStock(Long storeId, Long skuId) {
        return inventoryRepository.findByStoreIdAndSkuId(storeId, skuId)
                .map(i -> Math.max(0, i.getActualStock() - i.getHeldQuantity()))
                .orElse(0);
    }

    private InventoryResponse toResponse(InventoryEntity inv) {
        return InventoryResponse.builder()
                .id(inv.getId())
                .storeId(inv.getStore().getId())
                .storeName(inv.getStore().getName())
                .skuId(inv.getSku().getId())
                .skuCode(inv.getSku().getSkuCode())
                .productName(inv.getSku().getProduct().getName())
                .actualStock(inv.getActualStock())
                .heldQuantity(inv.getHeldQuantity())
                .availableStock(Math.max(0, inv.getActualStock() - inv.getHeldQuantity()))
                .minimumStock(inv.getMinimumStock())
                .build();
    }
}
