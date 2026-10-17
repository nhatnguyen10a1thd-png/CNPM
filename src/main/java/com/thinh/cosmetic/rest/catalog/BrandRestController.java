package com.thinh.cosmetic.rest.catalog;

import com.thinh.cosmetic.domain.dto.request.catalog.BrandRequest;
import com.thinh.cosmetic.domain.dto.response.catalog.BrandResponse;
import com.thinh.cosmetic.service.catalog.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping(path = "api/brands")
@RequiredArgsConstructor
public class BrandRestController {
    private final BrandService brandService;

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PostMapping
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<BrandResponse> create(
            @Valid @RequestBody BrandRequest request
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(brandService.create(request));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<BrandResponse> getById(
            @PathVariable Long id
    ) throws Exception {
        return ResponseEntity.ok(brandService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAll(@RequestParam(required = false) com.thinh.cosmetic.domain.enums.ActiveStatus status) {
        if (status == com.thinh.cosmetic.domain.enums.ActiveStatus.ACTIVE) {
            return ResponseEntity.ok(brandService.getActiveBrands());
        }
        return ResponseEntity.ok(brandService.getAll());
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PutMapping(path = "/{id}")
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<BrandResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request
    ) throws Exception {
        return ResponseEntity.ok(brandService.update(id, request));
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) throws Exception {
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PutMapping(path = "/{id}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable Long id
    ) throws Exception {
        brandService.activate(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PutMapping(path = "/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) throws Exception {
        brandService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}
