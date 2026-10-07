package com.thinh.cosmetic.rest.store;

import com.thinh.cosmetic.domain.dto.request.store.StockTransferRequest;
import com.thinh.cosmetic.domain.dto.response.store.StockTransferResponse;
import com.thinh.cosmetic.service.store.StockTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.thinh.cosmetic.security.CurrentAccountResolver;

import java.util.List;

@RestController
@RequestMapping("/api/stock-transfers")
@RequiredArgsConstructor
public class StockTransferRestController {
    private final StockTransferService stockTransferService;
    private final CurrentAccountResolver currentAccount;

    @PostMapping
    public ResponseEntity<StockTransferResponse> create(
            @Valid @RequestBody StockTransferRequest request,
            @RequestParam(required = false) Long employeeId
    ) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockTransferService.create(request, currentAccount.requireEmployeeId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockTransferResponse> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(stockTransferService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<StockTransferResponse>> getAll() {
        return ResponseEntity.ok(stockTransferService.getAll());
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<StockTransferResponse> confirmShipment(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(stockTransferService.confirmShipment(id));
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<StockTransferResponse> confirmReceipt(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(stockTransferService.confirmReceipt(id));
    }
}
