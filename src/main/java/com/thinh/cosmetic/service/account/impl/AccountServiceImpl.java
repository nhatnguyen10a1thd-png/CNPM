package com.thinh.cosmetic.service.account.impl;

import com.thinh.cosmetic.domain.dto.request.account.LoginRequest;
import com.thinh.cosmetic.domain.dto.request.account.RegisterRequest;
import com.thinh.cosmetic.domain.dto.response.account.AuthResponse;
import com.thinh.cosmetic.domain.dto.response.account.CustomerResponse;
import com.thinh.cosmetic.domain.entity.account.AccountEntity;
import com.thinh.cosmetic.domain.entity.account.CustomerEntity;
import com.thinh.cosmetic.domain.enums.AccountStatus;
import com.thinh.cosmetic.domain.enums.AccountType;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.exception.BadRequestException;
import com.thinh.cosmetic.exception.BusinessException;
import com.thinh.cosmetic.exception.ConflictException;
import com.thinh.cosmetic.mapper.account.CustomerMapper;
import com.thinh.cosmetic.repository.account.AccountRepository;
import com.thinh.cosmetic.repository.account.CustomerRepository;
import com.thinh.cosmetic.repository.account.EmployeeRepository;
import com.thinh.cosmetic.security.AccountPrincipal;
import com.thinh.cosmetic.security.PasswordPolicy;
import com.thinh.cosmetic.security.PermissionPolicy;
import com.thinh.cosmetic.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionPolicy permissionPolicy;
    // A precomputed hash keeps the missing-account path from skipping BCrypt work.
    private static final String DUMMY_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Override
    public CustomerResponse register(RegisterRequest request) {
        PasswordPolicy.validateConfirmation(request.getPassword(), request.getConfirmPassword());
        if (!request.isTermsAccepted()) throw new BadRequestException("Vui lòng đồng ý điều khoản sử dụng");
        String email = PasswordPolicy.normalizeEmail(request.getEmail());
        String phone = PasswordPolicy.normalizePhone(request.getPhone());
        if (email == null || email.isBlank() || request.getFullName() == null || request.getFullName().isBlank()) {
            throw new BadRequestException("Vui lòng nhập họ tên và email hợp lệ");
        }
        if (accountRepository.existsByEmailIgnoreCase(email) || accountRepository.existsByUsername(email)
                || (phone != null && accountRepository.existsByPhone(phone))) {
            throw new ConflictException("Email hoặc số điện thoại đã được sử dụng");
        }
        try {
            AccountEntity account = accountRepository.saveAndFlush(AccountEntity.builder()
                    .username(email).email(email).phone(phone)
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .accountType(AccountType.CUSTOMER).status(AccountStatus.ACTIVE).build());
            CustomerEntity customer = customerRepository.saveAndFlush(CustomerEntity.builder()
                    .account(account).fullName(request.getFullName().trim()).loyaltyPoints(0)
                    .joinDate(LocalDate.now()).build());
            return customerMapper.toResponse(customer);
        } catch (DataIntegrityViolationException exception) {
            // A runtime exception rolls back both inserts even when concurrent callers race.
            throw new ConflictException("Email hoặc số điện thoại đã được sử dụng");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AccountPrincipal login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        AccountEntity account;
        if (identifier.contains("@")) {
            account = accountRepository.findByEmailIgnoreCase(PasswordPolicy.normalizeEmail(identifier)).orElse(null);
        } else {
            try {
                account = accountRepository.findByPhone(PasswordPolicy.normalizePhone(identifier)).orElse(null);
            } catch (BadRequestException invalidPhone) {
                account = null;
            }
        }
        String hash = account == null ? DUMMY_HASH : account.getPasswordHash();
        boolean supportedHash = hash != null && hash.matches("\\$2[aby]\\$[0-9]{2}\\$[./A-Za-z0-9]{53}");
        boolean passwordMatches;
        try {
            passwordMatches = passwordEncoder.matches(request.getPassword(), supportedHash ? hash : DUMMY_HASH);
        } catch (IllegalArgumentException unsupportedCredential) {
            throw invalidCredentials();
        }
        // Legacy plaintext is never compared to the submitted password. It requires recovery.
        if (account == null || !supportedHash || !passwordMatches || account.getStatus() != AccountStatus.ACTIVE) {
            throw invalidCredentials();
        }
        if (account.getAccountType() == AccountType.CUSTOMER) {
            var customer = customerRepository.findByAccountId(account.getId()).orElseThrow(AccountServiceImpl::invalidCredentials);
            return new AccountPrincipal(account.getId(), account.getAccountType(), customer.getId(), null,
                    account.getCredentialsVersion());
        }
        if (account.getAccountType() == AccountType.EMPLOYEE) {
            var employee = employeeRepository.findByAccountId(account.getId()).orElseThrow(AccountServiceImpl::invalidCredentials);
            if (employee.getStatus() != ActiveStatus.ACTIVE) throw invalidCredentials();
            return new AccountPrincipal(account.getId(), account.getAccountType(), null, employee.getId(),
                    account.getCredentialsVersion());
        }
        throw invalidCredentials();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse describe(AccountPrincipal principal) {
        var account = accountRepository.findById(principal.getAccountId()).orElseThrow(AccountServiceImpl::invalidCredentials);
        if (account.getStatus() != AccountStatus.ACTIVE || account.getCredentialsVersion() != principal.getCredentialsVersion()
                || account.getAccountType() != principal.getAccountType()) throw invalidCredentials();
        if (principal.getAccountType() == AccountType.CUSTOMER) {
            var customer = customerRepository.findByAccountId(account.getId()).orElseThrow(AccountServiceImpl::invalidCredentials);
            return new AuthResponse(account.getId(), account.getEmail(), account.getPhone(), account.getAccountType(),
                    account.getStatus(), customer.getId(), null, customer.getFullName(), Set.of(), Set.of(), Set.of());
        }
        var employee = employeeRepository.findByAccountId(account.getId()).orElseThrow(AccountServiceImpl::invalidCredentials);
        var grants = permissionPolicy.describe(employee.getId());
        return new AuthResponse(account.getId(), account.getEmail(), account.getPhone(), account.getAccountType(),
                account.getStatus(), null, employee.getId(), employee.getFullName(), grants.roleNames(),
                grants.permissionCodes(), grants.storeIds());
    }

    private static BusinessException invalidCredentials() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "Thông tin đăng nhập không đúng hoặc tài khoản bị khóa");
    }
}
