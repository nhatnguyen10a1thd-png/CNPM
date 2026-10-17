package com.thinh.cosmetic.rest.catalog;

import com.thinh.cosmetic.domain.dto.request.catalog.ProductRequest;
import com.thinh.cosmetic.domain.dto.response.catalog.ProductResponse;
import com.thinh.cosmetic.domain.dto.response.store.StoreStockResponse;
import com.thinh.cosmetic.service.catalog.ProductService;
import com.thinh.cosmetic.service.store.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping(path = "api/products")
@RequiredArgsConstructor
public class ProductRestController {
    private final ProductService productService;
    private final InventoryService inventoryService;

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PostMapping
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ProductResponse> getById(
            @PathVariable Long id
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String skinType,
            @RequestParam(required = false) String concern,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "false") Boolean includeInactive
    ) {
        if (keyword != null || brandId != null || categoryId != null || skinType != null || concern != null || priceRange != null || sortBy != null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(productService.searchAndFilter(keyword, brandId, categoryId, skinType, concern, priceRange, sortBy, includeInactive));
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productService.getAll(includeInactive));
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PutMapping(path = "/{id}")
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productService.update(id, request));
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) throws Exception {
        productService.delete(id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PutMapping(path = "/{id}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable Long id
    ) throws Exception {
        productService.activate(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PutMapping(path = "/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) throws Exception {
        productService.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sku/{skuId}/stores")
    public ResponseEntity<List<StoreStockResponse>> getSkuStoreStock(@PathVariable Long skuId) {
        return ResponseEntity.ok(inventoryService.getStoreStockBySku(skuId));
    }
}
