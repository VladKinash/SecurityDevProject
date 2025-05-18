package com.nyc.hosp.util;

import com.nyc.hosp.domain.AuditLog;
import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.repos.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Component
public class AuditLogger {

    private final AuditLogRepository auditLogRepository;

    public AuditLogger(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(Hospuser user, String action, String entityType, Integer entityId) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getRemoteAddr();

        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setIpAddress(ip);
        log.setTimestamp(Instant.now());

        auditLogRepository.save(log);
    }
}
