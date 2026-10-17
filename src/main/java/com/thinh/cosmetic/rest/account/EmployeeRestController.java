package com.thinh.cosmetic.rest.account;

import com.thinh.cosmetic.domain.dto.request.account.EmployeeRequest;
import com.thinh.cosmetic.domain.dto.response.account.EmployeeResponse;
import com.thinh.cosmetic.service.account.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
public class EmployeeRestController {
    private final EmployeeService employeeService;

    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getAll(@RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(employeeService.search(keyword, page, size));
    }

    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) throws Exception {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) throws Exception {
        employeeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) throws Exception {
        employeeService.activate(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('EMPLOYEE_MANAGE')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePut(@PathVariable Long id) throws Exception {
        employeeService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}
