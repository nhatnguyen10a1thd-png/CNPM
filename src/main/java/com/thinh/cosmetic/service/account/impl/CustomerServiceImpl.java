package com.thinh.cosmetic.service.account.impl;

import com.thinh.cosmetic.domain.dto.request.account.BeautyProfileRequest;
import com.thinh.cosmetic.domain.dto.request.account.CustomerAddressRequest;
import com.thinh.cosmetic.domain.dto.request.account.CustomerProfileRequest;
import com.thinh.cosmetic.domain.dto.response.account.BeautyProfileResponse;
import com.thinh.cosmetic.domain.dto.response.account.CustomerAddressResponse;
import com.thinh.cosmetic.domain.dto.response.account.CustomerResponse;
import com.thinh.cosmetic.domain.dto.response.order.OrderResponse;
import com.thinh.cosmetic.domain.entity.account.AccountEntity;
import com.thinh.cosmetic.domain.entity.account.BeautyProfileEntity;
import com.thinh.cosmetic.domain.entity.account.CustomerAddressEntity;
import com.thinh.cosmetic.domain.entity.account.CustomerEntity;
import com.thinh.cosmetic.domain.entity.sales.OrderEntity;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.domain.enums.OrderStatus;
import com.thinh.cosmetic.mapper.account.CustomerAddressMapper;
import com.thinh.cosmetic.mapper.account.CustomerMapper;
import com.thinh.cosmetic.repository.account.AccountRepository;
import com.thinh.cosmetic.repository.account.BeautyProfileRepository;
import com.thinh.cosmetic.repository.account.CustomerAddressRepository;
import com.thinh.cosmetic.repository.account.CustomerRepository;
import com.thinh.cosmetic.repository.order.OrderRepository;
import com.thinh.cosmetic.exception.BusinessException;
import com.thinh.cosmetic.exception.ErrorCode;
import com.thinh.cosmetic.service.account.CustomerService;
import com.thinh.cosmetic.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final BeautyProfileRepository beautyProfileRepository;
    private final CustomerMapper customerMapper;
    private final CustomerAddressMapper addressMapper;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getProfile(Long customerId) throws Exception {
        return customerMapper.toResponse(findCustomer(customerId));
    }

    @Override
    public CustomerResponse updateProfile(Long customerId, CustomerProfileRequest request) throws Exception {
        CustomerEntity customer = findCustomer(customerId);
        if (request.getFullName() != null) customer.setFullName(request.getFullName());
        if (request.getDob() != null) customer.setDob(request.getDob());
        if (request.getGender() != null) customer.setGender(request.getGender());
        if (request.getPhone() != null && customer.getAccount() != null) {
            customer.getAccount().setPhone(request.getPhone());
        }
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> getAddresses(Long customerId) {
        return addressRepository.findByCustomerEntityId(customerId)
                .stream().map(addressMapper::toResponse).toList();
    }

    @Override
    public CustomerAddressResponse addAddress(Long customerId, CustomerAddressRequest request) throws Exception {
        CustomerEntity customer = findCustomer(customerId);
        CustomerAddressEntity address = addressMapper.toEntity(request);
        address.setCustomerEntity(customer);
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.findByCustomerEntityId(customerId)
                    .forEach(a -> { a.setIsDefault(false); addressRepository.save(a); });
        }
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public CustomerAddressResponse updateAddress(Long customerId, Long addressId, CustomerAddressRequest request) throws Exception {
        CustomerAddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND, "Không tìm thấy địa chỉ #" + addressId));
        if (address.getCustomerEntity() == null || !customerId.equals(address.getCustomerEntity().getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Địa chỉ này không thuộc về tài khoản của bạn");
        }
        addressMapper.updateEntity(request, address);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public void deleteAddress(Long customerId, Long addressId) throws Exception {
        CustomerAddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND, "Không tìm thấy địa chỉ #" + addressId));
        if (address.getCustomerEntity() == null || !customerId.equals(address.getCustomerEntity().getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Địa chỉ này không thuộc về tài khoản của bạn");
        }
        addressRepository.delete(address);
    }

    @Override
    @Transactional(readOnly = true)
    public BeautyProfileResponse getBeautyProfile(Long customerId) throws Exception {
        BeautyProfileEntity profile = beautyProfileRepository.findByCustomerId(customerId)
                .orElse(null);
        if (profile == null) return null;
        return BeautyProfileResponse.builder()
                .id(profile.getId()).skinType(profile.getSkinType())
                .skinConcerns(profile.getSkinConcerns()).careNeeds(profile.getCareNeeds())
                .preferences(profile.getPreferences()).priceRangeInterest(profile.getPriceRangeInterest())
                .updatedAt(profile.getUpdatedAt()).build();
    }

    @Override
    public BeautyProfileResponse updateBeautyProfile(Long customerId, BeautyProfileRequest request) throws Exception {
        CustomerEntity customer = findCustomer(customerId);
        BeautyProfileEntity profile = beautyProfileRepository.findByCustomerId(customerId)
                .orElse(BeautyProfileEntity.builder().customer(customer).build());
        profile.setSkinType(request.getSkinType());
        profile.setSkinConcerns(request.getSkinConcerns());
        profile.setCareNeeds(request.getCareNeeds());
        profile.setPreferences(request.getPreferences());
        profile.setPriceRangeInterest(request.getPriceRangeInterest());
        profile.setUpdatedAt(LocalDateTime.now());
        profile = beautyProfileRepository.save(profile);
        return BeautyProfileResponse.builder()
                .id(profile.getId()).skinType(profile.getSkinType())
                .skinConcerns(profile.getSkinConcerns()).careNeeds(profile.getCareNeeds())
                .preferences(profile.getPreferences()).priceRangeInterest(profile.getPriceRangeInterest())
                .updatedAt(profile.getUpdatedAt()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        Map<Long, CustomerStats> statsMap = new HashMap<>();
        if (orderRepository != null) {
            try {
                for (Object[] row : orderRepository.getCustomerOrderStats()) {
                    Long customerId = (Long) row[0];
                    Long totalOrders = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                    BigDecimal totalSpent = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
                    statsMap.put(customerId, new CustomerStats(totalOrders, totalSpent));
                }
            } catch (Exception e) {
                // Fallback to empty stats on failure
            }
        }

        return customerRepository.findAll().stream().map(c -> {
            CustomerResponse res = customerMapper.toResponse(c);
            CustomerStats stats = statsMap.get(c.getId());
            if (stats != null) {
                res.setTotalOrders(stats.totalOrders());
                res.setTotalSpent(stats.totalSpent());
            } else {
                res.setTotalOrders(0L);
                res.setTotalSpent(BigDecimal.ZERO);
            }
            return res;
        }).toList();
    }

    private record CustomerStats(Long totalOrders, BigDecimal totalSpent) {}

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) throws Exception {
        CustomerEntity customer = findCustomer(id);
        CustomerResponse res = customerMapper.toResponse(customer);
        enrichCustomerStats(res, id);
        return res;
    }

    @Override
    public CustomerResponse updateStatus(Long customerId, ActiveStatus status) throws Exception {
        CustomerEntity customer = findCustomer(customerId);
        AccountEntity account = customer.getAccount();
        if (account == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy tài khoản liên kết với khách hàng #" + customerId);
        }
        account.setStatus(status);
        accountRepository.save(account);
        CustomerResponse res = customerMapper.toResponse(customer);
        enrichCustomerStats(res, customerId);
        return res;
    }

    @Override
    public void lockCustomer(Long customerId) throws Exception {
        updateStatus(customerId, ActiveStatus.LOCKED);
    }

    @Override
    public void unlockCustomer(Long customerId) throws Exception {
        updateStatus(customerId, ActiveStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(Long customerId) {
        if (orderService != null) {
            return orderService.getByCustomer(customerId);
        }
        return List.of();
    }

    private void enrichCustomerStats(CustomerResponse res, Long customerId) {
        if (res == null || customerId == null || orderRepository == null) return;
        try {
            List<OrderEntity> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
            res.setTotalOrders((long) orders.size());
            BigDecimal total = orders.stream()
                    .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                    .map(OrderEntity::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            res.setTotalSpent(total);
        } catch (Exception e) {
            // Ignore stats computation error to preserve profile response
        }
    }

    private CustomerEntity findCustomer(Long id) throws Exception {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy khách hàng #" + id));
    }
}
