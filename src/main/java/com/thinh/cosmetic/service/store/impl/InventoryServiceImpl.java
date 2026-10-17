package com.thinh.cosmetic.service.store.impl;

import com.thinh.cosmetic.domain.dto.request.store.InventoryAdjustmentRequest;
import com.thinh.cosmetic.domain.dto.response.store.InventoryResponse;
import com.thinh.cosmetic.domain.entity.inventory.InventoryAdjustmentEntity;
import com.thinh.cosmetic.domain.entity.inventory.InventoryEntity;
import com.thinh.cosmetic.repository.account.EmployeeRepository;
import com.thinh.cosmetic.repository.store.InventoryAdjustmentRepository;
import com.thinh.cosmetic.repository.store.InventoryRepository;
import com.thinh.cosmetic.service.store.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import com.thinh.cosmetic.domain.dto.response.store.StoreStockResponse;
import com.thinh.cosmetic.domain.entity.inventory.StoreEntity;
import com.thinh.cosmetic.repository.store.StoreRepository;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;

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

        if (request.getNewQuantity() < inv.getHeldQuantity()) {
            throw new Exception("Số lượng thực tế không được nhỏ hơn số lượng đang giữ cho khách (" + inv.getHeldQuantity() + " sản phẩm)");
        }

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

    @Override
    @Transactional(readOnly = true)
    public List<StoreStockResponse> getStoreStockBySku(Long skuId) {
        List<StoreEntity> stores = storeRepository.findAll().stream()
                .filter(s -> s.getStatus() == null || s.getStatus() == com.thinh.cosmetic.domain.enums.ActiveStatus.ACTIVE)
                .toList();
        Map<Long, InventoryEntity> map = inventoryRepository.findBySkuId(skuId).stream()
                .collect(Collectors.toMap(i -> i.getStore().getId(), i -> i, (a, b) -> a));

        return stores.stream().map(store -> {
            InventoryEntity inv = map.get(store.getId());
            int actual = inv != null ? inv.getActualStock() : 0;
            int held = inv != null ? inv.getHeldQuantity() : 0;
            int available = Math.max(0, actual - held);
            return StoreStockResponse.builder()
                    .storeId(store.getId())
                    .storeName(store.getName())
                    .address(store.getAddress())
                    .province(store.getProvince())
                    .phone(store.getPhone())
                    .actualStock(actual)
                    .heldQuantity(held)
                    .availableStock(available)
                    .inStock(available > 0)
                    .build();
        }).toList();
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
