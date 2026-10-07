package com.thinh.cosmetic.service.account;

import com.thinh.cosmetic.domain.dto.request.account.EmployeeRequest;
import com.thinh.cosmetic.domain.dto.response.account.EmployeeResponse;

import java.util.List;
import org.springframework.data.domain.Page;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request) throws Exception;
    EmployeeResponse getById(Long id) throws Exception;
    List<EmployeeResponse> getAll();
    Page<EmployeeResponse> search(String keyword, int page, int size);
    EmployeeResponse update(Long id, EmployeeRequest request) throws Exception;
    void deactivate(Long id) throws Exception;
}
