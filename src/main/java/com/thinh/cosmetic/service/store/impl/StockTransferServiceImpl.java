package com.thinh.cosmetic.service.store.impl;

import com.thinh.cosmetic.domain.dto.request.store.StockTransferRequest;
import com.thinh.cosmetic.domain.dto.response.store.StockTransferResponse;
import com.thinh.cosmetic.domain.entity.catalog.ProductSkuEntity;
import com.thinh.cosmetic.domain.entity.store.*;
import com.thinh.cosmetic.domain.enums.StockTransferStatus;
import com.thinh.cosmetic.repository.account.EmployeeRepository;
import com.thinh.cosmetic.repository.catalog.ProductSkuRepository;
import com.thinh.cosmetic.repository.store.*;
import com.thinh.cosmetic.service.store.StockTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import com.thinh.cosmetic.security.PermissionPolicy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StockTransferServiceImpl implements StockTransferService {
    private final StockTransferRepository stockTransferRepository;
    private final StockTransferItemRepository stockTransferItemRepository;
    private final StoreRepository storeRepository;
    private final ProductSkuRepository skuRepository;
    private final InventoryRepository inventoryRepository;
    private final EmployeeRepository employeeRepository;
    private final PermissionPolicy policy;

    @Override
    @PreAuthorize("@permissionPolicy.canStore('TRANSFER_MANAGE', #p0.sourceStoreId) and @permissionPolicy.canStore('TRANSFER_MANAGE', #p0.destinationStoreId) and #p1 == @permissionPolicy.employeeId()")
    public StockTransferResponse create(StockTransferRequest request, Long employeeId) throws Exception {
        StoreEntity source = storeRepository.findById(request.getSourceStoreId())
                .orElseThrow(() -> new Exception("Source store not found"));
        StoreEntity destination = storeRepository.findById(request.getDestinationStoreId())
                .orElseThrow(() -> new Exception("Destination store not found"));

        StockTransferEntity transfer = StockTransferEntity.builder()
                .sourceStore(source)
                .destinationStore(destination)
                .status(StockTransferStatus.PENDING)
                .createdBy(employeeRepository.findById(employeeId).orElse(null))
                .build();
        transfer = stockTransferRepository.save(transfer);

        List<StockTransferItemEntity> items = new ArrayList<>();
        for (StockTransferRequest.TransferItemRequest itemReq : request.getItems()) {
            ProductSkuEntity sku = skuRepository.findById(itemReq.getSkuId())
                    .orElseThrow(() -> new Exception("SKU not found: " + itemReq.getSkuId()));
            StockTransferItemEntity item = StockTransferItemEntity.builder()
                    .stockTransfer(transfer)
                    .sku(sku)
                    .quantity(itemReq.getQuantity())
                    .build();
            items.add(stockTransferItemRepository.save(item));
        }
        transfer.setItems(items);

        return toResponse(transfer);
    }

    @Override
    @PreAuthorize("@permissionPolicy.canTransfer('TRANSFER_MANAGE', #p0)")
    public StockTransferResponse confirmShipment(Long id) throws Exception {
        StockTransferEntity transfer = stockTransferRepository.findById(id)
                .orElseThrow(() -> new Exception("Transfer not found: " + id));
        if (transfer.getStatus() != StockTransferStatus.PENDING) {
            throw new Exception("Can only ship pending transfers");
        }

        List<StockTransferItemEntity> items = stockTransferItemRepository.findByStockTransferId(id);
        for (StockTransferItemEntity item : items) {
            InventoryEntity inv = inventoryRepository.findByStoreIdAndSkuId(transfer.getSourceStore().getId(), item.getSku().getId())
                    .orElseThrow(() -> new Exception("Inventory not found at source for SKU: " + item.getSku().getSkuCode()));
            if (inv.getActualStock() < item.getQuantity()) {
                throw new Exception("Insufficient stock for SKU: " + item.getSku().getSkuCode());
            }
            inv.setActualStock(inv.getActualStock() - item.getQuantity());
            inventoryRepository.save(inv);
        }

        transfer.setStatus(StockTransferStatus.IN_TRANSIT);
        transfer.setShippedAt(LocalDateTime.now());
        return toResponse(stockTransferRepository.save(transfer));
    }

    @Override
    @PreAuthorize("@permissionPolicy.canTransfer('TRANSFER_MANAGE', #p0)")
    public StockTransferResponse confirmReceipt(Long id) throws Exception {
        StockTransferEntity transfer = stockTransferRepository.findById(id)
                .orElseThrow(() -> new Exception("Transfer not found: " + id));
        if (transfer.getStatus() != StockTransferStatus.IN_TRANSIT) {
            throw new Exception("Can only receive transfers that are in transit");
        }

        List<StockTransferItemEntity> items = stockTransferItemRepository.findByStockTransferId(id);
        for (StockTransferItemEntity item : items) {
            InventoryEntity inv = inventoryRepository.findByStoreIdAndSkuId(transfer.getDestinationStore().getId(), item.getSku().getId())
                    .orElseGet(() -> InventoryEntity.builder()
                            .store(transfer.getDestinationStore())
                            .sku(item.getSku())
                            .actualStock(0)
                            .heldQuantity(0)
                            .minimumStock(5)
                            .build());
            inv.setActualStock(inv.getActualStock() + item.getQuantity());
            inventoryRepository.save(inv);
        }

        transfer.setStatus(StockTransferStatus.RECEIVED);
        transfer.setReceivedAt(LocalDateTime.now());
        return toResponse(stockTransferRepository.save(transfer));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionPolicy.canTransfer('TRANSFER_READ', #p0)")
    public StockTransferResponse getById(Long id) throws Exception {
        StockTransferEntity transfer = stockTransferRepository.findById(id)
                .orElseThrow(() -> new Exception("Transfer not found: " + id));
        return toResponse(transfer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransferResponse> getAll() {
        policy.require("TRANSFER_READ");
        var rows = policy.isAdmin() ? stockTransferRepository.findAll() : stockTransferRepository.findBySourceStoreIdInAndDestinationStoreIdIn(policy.storeIds(), policy.storeIds());
        return rows.stream().map(this::toResponse).toList();
    }

    private StockTransferResponse toResponse(StockTransferEntity t) {
        List<StockTransferItemEntity> items = t.getItems() != null ? t.getItems() :
                stockTransferItemRepository.findByStockTransferId(t.getId());

        List<StockTransferResponse.TransferItemResponse> itemResponses = items.stream()
                .map(i -> StockTransferResponse.TransferItemResponse.builder()
                        .skuId(i.getSku().getId())
                        .skuCode(i.getSku().getSkuCode())
                        .productName(i.getSku().getProduct().getName())
                        .quantity(i.getQuantity())
                        .build())
                .toList();

        return StockTransferResponse.builder()
                .id(t.getId())
                .sourceStoreName(t.getSourceStore().getName())
                .destinationStoreName(t.getDestinationStore().getName())
                .status(t.getStatus())
                .createdByName(t.getCreatedBy() != null ? t.getCreatedBy().getFullName() : null)
                .createdAt(t.getCreatedAt())
                .shippedAt(t.getShippedAt())
                .receivedAt(t.getReceivedAt())
                .items(itemResponses)
                .build();
    }
}
