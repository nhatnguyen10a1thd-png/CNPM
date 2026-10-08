package com.thinh.cosmetic.service.catalog.impl;

import com.thinh.cosmetic.domain.dto.request.catalog.ProductRequest;
import com.thinh.cosmetic.domain.dto.response.catalog.ProductResponse;
import com.thinh.cosmetic.domain.entity.catalog.BrandEntity;
import com.thinh.cosmetic.domain.entity.catalog.CategoryEntity;
import com.thinh.cosmetic.domain.entity.catalog.ProductEntity;
import com.thinh.cosmetic.mapper.catalog.ProductMapper;
import com.thinh.cosmetic.repository.catalog.BrandRepository;
import com.thinh.cosmetic.repository.catalog.CategoryRepository;
import com.thinh.cosmetic.repository.catalog.ProductRepository;
import com.thinh.cosmetic.service.catalog.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ProductResponse create(ProductRequest request) throws Exception {
        ProductEntity product = productMapper.toEntity(request);

        if (request.getBrandId() != null){
            BrandEntity brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() ->
                            new Exception(
                                    "Brand not found: " + request.getBrandId()
                            ));
            product.setBrand(brand);
        }

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() ->
                            new Exception(
                                    "Category not found: " + request.getCategoryId()
                            ));
            product.setCategory(category);
        }

        ProductEntity savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) throws Exception {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() ->
                        new Exception(
                                "Product not found: " + id
                        ));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public ProductResponse update(Long id, ProductRequest request) throws Exception {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() ->
                        new Exception(
                                "Product not found: " + id
                        ));

        productMapper.updateEntity(request, product);

        if (request.getBrandId() != null){
            BrandEntity brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() ->
                            new Exception(
                                    "Brand not found: " + request.getBrandId()
                            ));
            product.setBrand(brand);
        }

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() ->
                            new Exception(
                                    "Category not found: " + request.getCategoryId()
                            ));
            product.setCategory(category);
        }

        ProductEntity savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public void delete(Long id) throws Exception {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() ->
                        new Exception(
                                "Product not found: " + id
                        ));
        productRepository.deleteById(id);
    }
}
