package com.thinh.cosmetic.rest.store;

import com.thinh.cosmetic.domain.dto.request.store.InventoryAdjustmentRequest;
import com.thinh.cosmetic.domain.dto.response.store.InventoryResponse;
import com.thinh.cosmetic.service.store.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.thinh.cosmetic.security.CurrentAccountResolver;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryRestController {
    private final InventoryService inventoryService;
    private final CurrentAccountResolver currentAccount;

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<InventoryResponse>> getByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(inventoryService.getByStore(storeId));
    }

    @GetMapping("/store/{storeId}/sku/{skuId}")
    public ResponseEntity<InventoryResponse> getByStoreAndSku(
            @PathVariable Long storeId,
            @PathVariable Long skuId
    ) throws Exception {
        return ResponseEntity.ok(inventoryService.getByStoreAndSku(storeId, skuId));
    }

    @GetMapping("/store/{storeId}/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems(@PathVariable Long storeId) {
        return ResponseEntity.ok(inventoryService.getLowStockItems(storeId));
    }

    @PostMapping("/adjust")
    public ResponseEntity<InventoryResponse> adjustStock(
            @Valid @RequestBody InventoryAdjustmentRequest request,
            @RequestParam(required = false) Long employeeId
    ) throws Exception {
        return ResponseEntity.ok(inventoryService.adjustStock(request, currentAccount.requireEmployeeId()));
    }

    @GetMapping("/store/{storeId}/sku/{skuId}/available")
    public ResponseEntity<Integer> getAvailableStock(
            @PathVariable Long storeId,
            @PathVariable Long skuId
    ) {
        return ResponseEntity.ok(inventoryService.getAvailableStock(storeId, skuId));
    }
}
