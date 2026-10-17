package com.thinh.cosmetic.rest.account;

import com.thinh.cosmetic.service.account.DemoRecoveryOutbox;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Profile("demo")
@RequestMapping("/api/demo/recovery")
public class DemoRecoveryController {
    private final DemoRecoveryOutbox outbox;
    public DemoRecoveryController(DemoRecoveryOutbox outbox) { this.outbox = outbox; }

    @GetMapping("/messages")
    public List<DemoRecoveryOutbox.Message> messages(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (!"127.0.0.1".equals(ip) && !"::1".equals(ip) && !"0:0:0:0:0:0:0:1".equals(ip)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Local demo mailbox only");
        }
        return outbox.messages();
    }
}
