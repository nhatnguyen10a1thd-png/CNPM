package com.thinh.cosmetic.service.catalog.impl;

import com.thinh.cosmetic.domain.dto.request.catalog.BrandRequest;
import com.thinh.cosmetic.domain.dto.response.catalog.BrandResponse;
import com.thinh.cosmetic.domain.entity.catalog.BrandEntity;
import com.thinh.cosmetic.mapper.catalog.BrandMapper;
import com.thinh.cosmetic.repository.catalog.BrandRepository;
import com.thinh.cosmetic.service.catalog.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public BrandResponse create(BrandRequest request) throws Exception {
        BrandEntity brand = brandMapper.toEntity(request);

        BrandEntity savedBrand = brandRepository.save(brand);

        return brandMapper.toResponse(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getById(Long id) throws Exception {
        BrandEntity brand = brandRepository.findById(id)
                .orElseThrow(() ->
                        new Exception(
                                "Brand not found: " + id
                        ));

        return brandMapper.toResponse(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAll() {
        return brandRepository.findAll()
                .stream()
                .map(brandMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public BrandResponse update(Long id, BrandRequest request) throws Exception {
        BrandEntity brand = brandRepository.findById(id)
                .orElseThrow(() ->
                        new Exception(
                                "Brand not found: " + id
                        ));
        brandMapper.updateEntity(request, brand);

        BrandEntity savedBrand = brandRepository.save(brand);

        return brandMapper.toResponse(savedBrand);
    }

    @Override
    @PreAuthorize("@permissionPolicy.has('CATALOG_MANAGE')")
    public void delete(Long id) throws Exception {
        BrandEntity brand = brandRepository.findById(id)
                .orElseThrow(() ->
                        new Exception(
                                "Brand not found: " + id
                        ));
        brandRepository.deleteById(id);
    }
}
