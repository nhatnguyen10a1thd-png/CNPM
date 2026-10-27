package com.thinh.cosmetic.service.order.impl;

import com.thinh.cosmetic.domain.dto.request.promotion.VoucherRequest;
import com.thinh.cosmetic.domain.dto.response.promotion.VoucherResponse;
import com.thinh.cosmetic.domain.dto.response.promotion.VoucherValidationResponse;
import com.thinh.cosmetic.domain.entity.sales.VoucherEntity;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.domain.enums.VoucherType;
import com.thinh.cosmetic.exception.BusinessException;
import com.thinh.cosmetic.exception.ErrorCode;
import com.thinh.cosmetic.repository.order.VoucherRepository;
import com.thinh.cosmetic.service.order.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public VoucherResponse createVoucher(VoucherRequest request) throws Exception {
        if (request == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Dữ liệu tạo voucher không được để trống");
        }
        String cleanCode = request.getCode().trim().toUpperCase();
        if (voucherRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new BusinessException(ErrorCode.DUPLICATE_VOUCHER_CODE, "Mã voucher '" + cleanCode + "' đã tồn tại");
        }
        if (request.getValidTo() != null && request.getValidFrom() != null && request.getValidTo().isBefore(request.getValidFrom())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }

        VoucherEntity entity = VoucherEntity.builder()
                .code(cleanCode)
                .name(request.getName() != null ? request.getName().trim() : cleanCode)
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .validFrom(request.getValidFrom() != null ? request.getValidFrom() : LocalDateTime.now())
                .validTo(request.getValidTo() != null ? request.getValidTo() : LocalDateTime.now().plusMonths(3))
                .usageLimit(request.getUsageLimit() != null ? request.getUsageLimit() : 1000)
                .usedCount(0)
                .status(request.getStatus() != null ? request.getStatus() : ActiveStatus.ACTIVE)
                .build();

        return toResponse(voucherRepository.save(entity));
    }

    @Override
    public VoucherResponse updateVoucher(Long id, VoucherRequest request) throws Exception {
        VoucherEntity entity = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND, "Không tìm thấy voucher #" + id));

        String cleanCode = request.getCode().trim().toUpperCase();
        if (!entity.getCode().equalsIgnoreCase(cleanCode) && voucherRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new BusinessException(ErrorCode.DUPLICATE_VOUCHER_CODE, "Mã voucher '" + cleanCode + "' đã tồn tại");
        }
        if (request.getValidTo() != null && request.getValidFrom() != null && request.getValidTo().isBefore(request.getValidFrom())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }

        entity.setCode(cleanCode);
        if (request.getName() != null) entity.setName(request.getName().trim());
        entity.setDescription(request.getDescription());
        entity.setDiscountType(request.getDiscountType());
        entity.setDiscountValue(request.getDiscountValue());
        entity.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getMinOrderAmount() != null) entity.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getValidFrom() != null) entity.setValidFrom(request.getValidFrom());
        if (request.getValidTo() != null) entity.setValidTo(request.getValidTo());
        if (request.getUsageLimit() != null) entity.setUsageLimit(request.getUsageLimit());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());

        return toResponse(voucherRepository.save(entity));
    }

    @Override
    public VoucherResponse toggleStatus(Long id) throws Exception {
        VoucherEntity entity = voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND, "Không tìm thấy voucher #" + id));

        entity.setStatus(entity.getStatus() == ActiveStatus.ACTIVE ? ActiveStatus.INACTIVE : ActiveStatus.ACTIVE);
        return toResponse(voucherRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getById(Long id) throws Exception {
        return voucherRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND, "Không tìm thấy voucher #" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getByCode(String code) throws Exception {
        return voucherRepository.findByCodeIgnoreCase(code.trim())
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND, "Không tìm thấy voucher với mã: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getActiveVouchers() {
        return voucherRepository.findByStatusOrderByCreatedAtDesc(ActiveStatus.ACTIVE)
                .stream()
                .filter(VoucherEntity::isCurrentlyActive)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherValidationResponse validateVoucher(String code, BigDecimal subtotal, BigDecimal shippingFee) {
        if (code == null || code.trim().isEmpty()) {
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .message("Vui lòng nhập mã ưu đãi")
                    .build();
        }

        var opt = voucherRepository.findByCodeIgnoreCase(code.trim());
        if (opt.isEmpty()) {
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .code(code)
                    .message("Mã ưu đãi '" + code + "' không tồn tại trong hệ thống")
                    .build();
        }

        VoucherEntity v = opt.get();
        if (v.getStatus() != ActiveStatus.ACTIVE) {
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .code(v.getCode())
                    .message("Mã ưu đãi '" + v.getCode() + "' hiện đang tạm ngưng sử dụng")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (v.getValidFrom() != null && now.isBefore(v.getValidFrom())) {
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .code(v.getCode())
                    .message("Chương trình ưu đãi '" + v.getCode() + "' chưa bắt đầu")
                    .build();
        }
        if (v.getValidTo() != null && now.isAfter(v.getValidTo())) {
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .code(v.getCode())
                    .message("Mã ưu đãi '" + v.getCode() + "' đã hết hạn sử dụng")
                    .build();
        }

        if (v.isLimitReached()) {
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .code(v.getCode())
                    .message("Mã ưu đãi '" + v.getCode() + "' đã hết số lượt sử dụng")
                    .build();
        }

        BigDecimal safeSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        if (v.getMinOrderAmount() != null && safeSubtotal.compareTo(v.getMinOrderAmount()) < 0) {
            return VoucherValidationResponse.builder()
                    .valid(false)
                    .code(v.getCode())
                    .message(String.format("Đơn hàng chưa đạt giá trị tối thiểu %,d ₫ để áp dụng mã này", v.getMinOrderAmount().longValue()))
                    .build();
        }

        BigDecimal discount = calculateDiscount(v, safeSubtotal);
        BigDecimal shipDiscount = calculateShippingDiscount(v, shippingFee != null ? shippingFee : BigDecimal.ZERO);
        BigDecimal finalSubtotal = safeSubtotal.subtract(discount).max(BigDecimal.ZERO);

        return VoucherValidationResponse.builder()
                .valid(true)
                .code(v.getCode())
                .discountType(v.getDiscountType())
                .discountAmount(discount)
                .shippingDiscountAmount(shipDiscount)
                .finalSubtotal(finalSubtotal)
                .message("Áp dụng mã ưu đãi " + v.getCode() + " thành công!")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherEntity validateAndApply(String code, BigDecimal subtotal) throws Exception {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        VoucherEntity v = voucherRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND, "Không tìm thấy mã ưu đãi: " + code));

        if (v.getStatus() != ActiveStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.VOUCHER_INVALID, "Mã ưu đãi '" + v.getCode() + "' đang tạm ngưng sử dụng");
        }

        LocalDateTime now = LocalDateTime.now();
        if ((v.getValidFrom() != null && now.isBefore(v.getValidFrom())) ||
            (v.getValidTo() != null && now.isAfter(v.getValidTo()))) {
            throw new BusinessException(ErrorCode.VOUCHER_EXPIRED, "Mã ưu đãi '" + v.getCode() + "' đã hết hạn hoặc chưa đến ngày áp dụng");
        }

        if (v.isLimitReached()) {
            throw new BusinessException(ErrorCode.VOUCHER_LIMIT_REACHED, "Mã ưu đãi '" + v.getCode() + "' đã hết lượt sử dụng");
        }

        BigDecimal safeSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        if (v.getMinOrderAmount() != null && safeSubtotal.compareTo(v.getMinOrderAmount()) < 0) {
            throw new BusinessException(ErrorCode.VOUCHER_MIN_ORDER_NOT_MET,
                    String.format("Đơn hàng chưa đạt giá trị tối thiểu %,d ₫ để áp dụng mã '%s'",
                            v.getMinOrderAmount().longValue(), v.getCode()));
        }

        return v;
    }

    @Override
    public BigDecimal calculateDiscount(VoucherEntity voucher, BigDecimal subtotal) {
        if (voucher == null || subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (voucher.getDiscountType() == VoucherType.PERCENT || voucher.getDiscountType() == VoucherType.PERCENTAGE) {
            BigDecimal rate = voucher.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discount = subtotal.multiply(rate).setScale(0, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.min(voucher.getMaxDiscountAmount());
            }
        } else if (voucher.getDiscountType() == VoucherType.FIXED || voucher.getDiscountType() == VoucherType.FIXED_AMOUNT) {
            discount = voucher.getDiscountValue();
        }

        // QLKM-QĐ5: Giá trị giảm không làm tổng tiền hàng < 0
        return discount.min(subtotal).max(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calculateShippingDiscount(VoucherEntity voucher, BigDecimal shippingFee) {
        if (voucher == null || shippingFee == null || shippingFee.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (voucher.getDiscountType() == VoucherType.FREESHIP || voucher.getDiscountType() == VoucherType.FREE_SHIPPING) {
            if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                return shippingFee.min(voucher.getMaxDiscountAmount());
            }
            return shippingFee;
        }
        return BigDecimal.ZERO;
    }

    @Override
    public void incrementUsage(VoucherEntity voucher) {
        if (voucher == null) return;
        voucher.setUsedCount((voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) + 1);
        voucherRepository.save(voucher);
        log.info("Incremented voucher [{}] usage count to {}", voucher.getCode(), voucher.getUsedCount());
    }

    @Override
    public void releaseUsage(VoucherEntity voucher) {
        if (voucher == null) return;
        if (voucher.getUsedCount() != null && voucher.getUsedCount() > 0) {
            voucher.setUsedCount(voucher.getUsedCount() - 1);
            voucherRepository.save(voucher);
            log.info("Released voucher [{}] usage count to {}", voucher.getCode(), voucher.getUsedCount());
        }
    }

    private VoucherResponse toResponse(VoucherEntity v) {
        return VoucherResponse.builder()
                .id(v.getId())
                .code(v.getCode())
                .name(v.getName())
                .description(v.getDescription())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .maxDiscountAmount(v.getMaxDiscountAmount())
                .minOrderAmount(v.getMinOrderAmount())
                .validFrom(v.getValidFrom())
                .validTo(v.getValidTo())
                .usageLimit(v.getUsageLimit())
                .usedCount(v.getUsedCount())
                .status(v.getStatus())
                .isExpired(v.isExpired())
                .isLimitReached(v.isLimitReached())
                .isCurrentlyActive(v.isCurrentlyActive())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
