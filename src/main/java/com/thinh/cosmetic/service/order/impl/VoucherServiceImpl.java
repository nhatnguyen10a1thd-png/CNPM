package com.thinh.cosmetic.service.order.impl;

import com.thinh.cosmetic.domain.dto.request.order.VoucherRequest;
import com.thinh.cosmetic.domain.dto.response.order.VoucherResponse;
import com.thinh.cosmetic.domain.entity.order.VoucherEntity;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.domain.enums.DiscountType;
import com.thinh.cosmetic.mapper.order.VoucherMapper;
import com.thinh.cosmetic.repository.order.VoucherRepository;
import com.thinh.cosmetic.service.order.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;

    @Override
    @PreAuthorize("@permissionPolicy.has('PROMOTION_MANAGE')")
    public VoucherResponse create(VoucherRequest request) throws Exception {
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new Exception("Voucher code already exists: " + request.getCode());
        }
        VoucherEntity entity = voucherMapper.toEntity(request);
        entity.setUsedQuantity(0);
        if (entity.getStatus() == null) entity.setStatus(ActiveStatus.ACTIVE);
        return voucherMapper.toResponse(voucherRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionPolicy.has('PROMOTION_READ')")
    public VoucherResponse getById(Long id) throws Exception {
        return voucherMapper.toResponse(voucherRepository.findById(id)
                .orElseThrow(() -> new Exception("Voucher not found: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionPolicy.has('PROMOTION_READ')")
    public VoucherResponse getByCode(String code) throws Exception {
        return voucherMapper.toResponse(voucherRepository.findByCode(code)
                .orElseThrow(() -> new Exception("Voucher not found: " + code)));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionPolicy.has('PROMOTION_READ')")
    public List<VoucherResponse> getAll() {
        return voucherRepository.findAll().stream().map(voucherMapper::toResponse).toList();
    }

    @Override
    @PreAuthorize("@permissionPolicy.has('PROMOTION_MANAGE')")
    public VoucherResponse update(Long id, VoucherRequest request) throws Exception {
        VoucherEntity entity = voucherRepository.findById(id)
                .orElseThrow(() -> new Exception("Voucher not found: " + id));
        voucherMapper.updateEntity(request, entity);
        return voucherMapper.toResponse(voucherRepository.save(entity));
    }

    @Override
    @PreAuthorize("@permissionPolicy.has('PROMOTION_MANAGE')")
    public void deactivate(Long id) throws Exception {
        VoucherEntity entity = voucherRepository.findById(id)
                .orElseThrow(() -> new Exception("Voucher not found: " + id));
        entity.setStatus(ActiveStatus.INACTIVE);
        voucherRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionPolicy.has('PROMOTION_READ')")
    public BigDecimal calculateDiscount(String code, BigDecimal orderTotal) throws Exception {
        VoucherEntity v = voucherRepository.findByCode(code)
                .orElseThrow(() -> new Exception("Voucher not found: " + code));
        LocalDateTime now = LocalDateTime.now();
        if (v.getStatus() != ActiveStatus.ACTIVE) throw new Exception("Voucher is inactive");
        if (v.getStartDate() != null && now.isBefore(v.getStartDate())) throw new Exception("Voucher not yet valid");
        if (v.getEndDate() != null && now.isAfter(v.getEndDate())) throw new Exception("Voucher expired");
        if (v.getTotalQuantity() != null && v.getUsedQuantity() >= v.getTotalQuantity()) throw new Exception("Voucher usage limit reached");
        if (v.getMinOrderValue() != null && orderTotal.compareTo(v.getMinOrderValue()) < 0) {
            throw new Exception("Order total below minimum: " + v.getMinOrderValue());
        }

        BigDecimal discount;
        if (v.getDiscountType() == DiscountType.PERCENT) {
            discount = orderTotal.multiply(v.getDiscountValue()).divide(BigDecimal.valueOf(100));
            if (v.getMaxDiscount() != null && discount.compareTo(v.getMaxDiscount()) > 0) {
                discount = v.getMaxDiscount();
            }
        } else {
            discount = v.getDiscountValue();
        }
        return discount.min(orderTotal);
    }
}
