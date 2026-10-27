package com.thinh.cosmetic.controller;

import com.thinh.cosmetic.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import com.thinh.cosmetic.domain.dto.response.catalog.ProductResponse;
import com.thinh.cosmetic.service.catalog.BrandService;
import com.thinh.cosmetic.service.catalog.CategoryService;
import com.thinh.cosmetic.service.catalog.ProductService;
import com.thinh.cosmetic.service.review.ReviewService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProductPageController {

    private final ProductService productService;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;


    @GetMapping({"/products", "/product"})
    public String productList(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "brandId", required = false) Long brandId,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "skinType", required = false) String skinType,
            @RequestParam(name = "concern", required = false) String concern,
            @RequestParam(name = "priceRange", required = false) String priceRange,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @AuthenticationPrincipal AuthPrincipal principal,
            Model model
    ) {
        model.addAttribute("pageTitle", "Danh Sách Sản Phẩm | LUNEA");
        model.addAttribute("activeNav", "products");
        model.addAttribute("keyword", keyword != null ? keyword.trim() : "");
        model.addAttribute("brandId", brandId);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("skinType", skinType);
        model.addAttribute("concern", concern);
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("sortBy", sortBy != null ? sortBy : "newest");
        model.addAttribute("currentPage", page);
        model.addAttribute("brands", brandService.getAll());
        List<com.thinh.cosmetic.domain.dto.response.catalog.CategoryResponse> categories = categoryService.getAll();
        model.addAttribute("categories", categories);

        if (categoryId != null) {
            categories.stream()
                    .filter(c -> categoryId.equals(c.getId()))
                    .findFirst()
                    .ifPresent(c -> model.addAttribute("category", c));
        }

        List<ProductResponse> allProducts = productService.searchAndFilter(
                keyword, brandId, categoryId, skinType, concern, priceRange, sortBy
        );

        int totalElements = allProducts != null ? allProducts.size() : 0;
        int pageSize = 12;
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        if (totalPages == 0) totalPages = 1;
        int validPage = Math.max(0, Math.min(page != null ? page : 0, totalPages - 1));

        int fromIndex = validPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);
        List<ProductResponse> pagedProducts = (fromIndex <= totalElements && allProducts != null)
                ? allProducts.subList(fromIndex, toIndex)
                : List.of();

        model.addAttribute("products", pagedProducts);
        model.addAttribute("totalProducts", totalElements);
        model.addAttribute("currentPage", validPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("hasPrevious", validPage > 0);
        model.addAttribute("hasNext", validPage < totalPages - 1);

        if (principal != null) {
            model.addAttribute("currentUser", principal);
        }
        return "product-list";
    }

    @GetMapping({"/products/{id}", "/product/{id}"})
    public String productDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal AuthPrincipal principal,
            Model model
    ) {
        ProductResponse product = null;
        try {
            product = productService.getById(id);
        } catch (Exception e) {
            log.warn("Product not found with id: {}", id);
        }

        if (product == null) {
            return "redirect:/error/404";
        }

        final Long catId = product.getCategory() != null ? product.getCategory().getId() : null;
        final Long brandId = product.getBrand() != null ? product.getBrand().getId() : null;

        // Suggested / Related Products in same category or brand (optimized direct query)
        List<ProductResponse> relatedProducts = productService.getRelatedProducts(id, catId, brandId, 4);
        model.addAttribute("relatedProducts", relatedProducts);

        model.addAttribute("pageTitle", product.getName() + " | LUNEA");
        model.addAttribute("activeNav", "products");
        model.addAttribute("productId", id);
        model.addAttribute("product", product);
        try {
            model.addAttribute("reviews", reviewService.getByProduct(id));
        } catch (Exception e) {
            log.warn("Failed to load reviews for product id {}: {}", id, e.getMessage());
            model.addAttribute("reviews", List.of());
        }
        if (principal != null) {
            model.addAttribute("currentUser", principal);
        }

        return "product-detail";
    }

    private boolean idMatch(Long id1, Long id2) {
        return id1 != null && id1.equals(id2);
    }
}
