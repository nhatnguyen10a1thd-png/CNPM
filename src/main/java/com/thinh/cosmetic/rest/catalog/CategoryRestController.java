package com.thinh.cosmetic.rest.catalog;

import com.thinh.cosmetic.domain.dto.request.catalog.CategoryRequest;
import com.thinh.cosmetic.domain.dto.response.catalog.CategoryResponse;
import com.thinh.cosmetic.service.catalog.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping(path = "api/categories")
@RequiredArgsConstructor
public class CategoryRestController {
    private final CategoryService categoryService;

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PostMapping
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.create(request));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @PathVariable Long id
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll(@RequestParam(required = false) com.thinh.cosmetic.domain.enums.ActiveStatus status) {
        if (status == com.thinh.cosmetic.domain.enums.ActiveStatus.ACTIVE) {
            return ResponseEntity.ok(categoryService.getActiveCategories());
        }
        return ResponseEntity.ok(categoryService.getAll());
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PutMapping(path = "/{id}")
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.update(id, request));
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) throws Exception {
        categoryService.delete(id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PutMapping(path = "/{id}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable Long id
    ) throws Exception {
        categoryService.activate(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    @PutMapping(path = "/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) throws Exception {
        categoryService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}
