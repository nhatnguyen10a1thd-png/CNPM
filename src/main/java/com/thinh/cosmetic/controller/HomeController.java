package com.thinh.cosmetic.controller;

import com.thinh.cosmetic.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.thinh.cosmetic.service.catalog.BrandService;
import com.thinh.cosmetic.service.catalog.CategoryService;
import com.thinh.cosmetic.service.catalog.ProductService;
import com.thinh.cosmetic.service.store.StoreService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final StoreService storeService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal AuthPrincipal principal, Model model) {
        model.addAttribute("pageTitle", "LUNEA | Thế Giới Mỹ Phẩm & Chăm Sóc Sắc Đẹp");
        model.addAttribute("activeNav", "home");
        model.addAttribute("products", productService.getFeaturedProducts(8));
        model.addAttribute("brands", brandService.getAll());
        model.addAttribute("categories", categoryService.getAll());
        if (principal != null) {
            model.addAttribute("currentUser", principal);
        }
        return "home";
    }

    @GetMapping("/stores")
    public String stores(@AuthenticationPrincipal AuthPrincipal principal, Model model) {
        model.addAttribute("pageTitle", "Hệ Thống Chuỗi Cửa Hàng | LUNEA");
        model.addAttribute("activeNav", "stores");
        model.addAttribute("stores", storeService.getActiveStores());
        if (principal != null) {
            model.addAttribute("currentUser", principal);
        }
        return "stores";
    }

    @GetMapping("/brands")
    public String brands(@AuthenticationPrincipal AuthPrincipal principal, Model model) {
        model.addAttribute("pageTitle", "Thương Hiệu Đồng Hành | LUNEA");
        model.addAttribute("activeNav", "brands");
        model.addAttribute("brands", brandService.getAll());
        if (principal != null) {
            model.addAttribute("currentUser", principal);
        }
        return "brands";
    }
}
