package com.thinh.cosmetic.rest.account;

import com.thinh.cosmetic.domain.dto.request.account.PasswordResetRequest;
import com.thinh.cosmetic.domain.dto.request.account.RecoveryRequest;
import com.thinh.cosmetic.service.account.PasswordRecoveryService;
import com.thinh.cosmetic.service.account.RecoveryDeliveryException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/recovery")
public class RecoveryRestController {
    private final PasswordRecoveryService recovery;
    public RecoveryRestController(PasswordRecoveryService recovery) { this.recovery = recovery; }

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> request(@Valid @RequestBody RecoveryRequest body, HttpServletRequest request) {
        try {
            recovery.request(body.identifier(), request.getRemoteAddr());
        } catch (RecoveryDeliveryException unavailable) {
            // Identical response for unknown, locked, throttled, and delivery-unavailable accounts.
        }
        return ResponseEntity.accepted().body(Map.of("message",
                "Nếu tài khoản hợp lệ, liên kết đặt lại mật khẩu sẽ được gửi đến email đã đăng ký. Nếu chưa nhận được, hãy thử lại sau."));
    }

    @PostMapping("/reset")
    public Map<String, String> reset(@Valid @RequestBody PasswordResetRequest body, HttpServletRequest request) {
        recovery.reset(body, request.getRemoteAddr());
        return Map.of("message", "Đã đặt lại mật khẩu. Hãy đăng nhập bằng mật khẩu mới.");
    }
}
