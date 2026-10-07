package com.thinh.cosmetic.service.account;

import com.thinh.cosmetic.domain.entity.account.AuditLogEntity;
import com.thinh.cosmetic.repository.account.AutditLogRepository;
import com.thinh.cosmetic.repository.account.EmployeeRepository;
import com.thinh.cosmetic.security.CurrentAccountResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditWriter {
    private final AutditLogRepository auditLogs;
    private final EmployeeRepository employees;
    private final CurrentAccountResolver currentAccount;

    /** Participates in the admin transaction: an audit persistence failure rolls back the command. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void write(String action, String objectType, Object objectId, String details) {
        long actorId = currentAccount.requireEmployeeId();
        var employee = employees.findById(actorId).orElseThrow(com.thinh.cosmetic.security.PermissionPolicy::denied);
        String remoteAddress = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();
            remoteAddress = request.getRemoteAddr();
        }
        auditLogs.saveAndFlush(AuditLogEntity.builder().action(action).employee(employee)
                .performedBy("employee:" + actorId).objectType(objectType).objectId(String.valueOf(objectId))
                .ipAddress(remoteAddress).details(details).build());
    }
}
