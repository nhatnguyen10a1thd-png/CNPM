package com.thinh.cosmetic.rest.account;

import com.thinh.cosmetic.domain.dto.request.account.BeautyProfileRequest;
import com.thinh.cosmetic.domain.dto.request.account.CustomerAddressRequest;
import com.thinh.cosmetic.domain.dto.request.account.CustomerProfileRequest;
import com.thinh.cosmetic.domain.dto.response.account.BeautyProfileResponse;
import com.thinh.cosmetic.domain.dto.response.account.CustomerAddressResponse;
import com.thinh.cosmetic.domain.dto.response.account.CustomerResponse;
import com.thinh.cosmetic.service.account.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.thinh.cosmetic.security.CurrentAccountResolver;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerRestController {
    private final CustomerService customerService;
    private final CurrentAccountResolver currentAccount;

    @GetMapping
    @PreAuthorize("@permissionPolicy.has('CUSTOMER_READ')")
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(customerService.getCustomerById(currentAccount.requireCustomerId()));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<CustomerResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody CustomerProfileRequest request
    ) throws Exception {
        return ResponseEntity.ok(customerService.updateProfile(currentAccount.requireCustomerId(), request));
    }

    @GetMapping("/{id}/addresses")
    public ResponseEntity<List<CustomerAddressResponse>> getAddresses(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getAddresses(currentAccount.requireCustomerId()));
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<CustomerAddressResponse> addAddress(
            @PathVariable Long id,
            @Valid @RequestBody CustomerAddressRequest request
    ) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addAddress(currentAccount.requireCustomerId(), request));
    }

    @PutMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<CustomerAddressResponse> updateAddress(
            @PathVariable Long id,
            @PathVariable Long addressId,
            @Valid @RequestBody CustomerAddressRequest request
    ) throws Exception {
        return ResponseEntity.ok(customerService.updateAddress(currentAccount.requireCustomerId(), addressId, request));
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id, @PathVariable Long addressId) throws Exception {
        customerService.deleteAddress(currentAccount.requireCustomerId(), addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/beauty-profile")
    public ResponseEntity<BeautyProfileResponse> getBeautyProfile(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(customerService.getBeautyProfile(currentAccount.requireCustomerId()));
    }

    @PutMapping("/{id}/beauty-profile")
    public ResponseEntity<BeautyProfileResponse> updateBeautyProfile(
            @PathVariable Long id,
            @Valid @RequestBody BeautyProfileRequest request
    ) throws Exception {
        return ResponseEntity.ok(customerService.updateBeautyProfile(currentAccount.requireCustomerId(), request));
    }
}
