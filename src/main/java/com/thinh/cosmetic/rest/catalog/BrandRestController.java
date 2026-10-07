package com.thinh.cosmetic.rest.catalog;

import com.thinh.cosmetic.domain.dto.request.catalog.BrandRequest;
import com.thinh.cosmetic.domain.dto.response.catalog.BrandResponse;
import com.thinh.cosmetic.service.catalog.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping(path = "api/brands")
@RequiredArgsConstructor
public class BrandRestController {
    private final BrandService brandService;

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
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(brandService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(brandService.getAll());
    }

    @PutMapping(path = "/{id}")
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<BrandResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(brandService.update(id, request));
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) throws Exception {
        brandService.delete(id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
