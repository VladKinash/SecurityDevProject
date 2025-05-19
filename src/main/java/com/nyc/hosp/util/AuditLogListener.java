package com.nyc.hosp.util;

import com.nyc.hosp.domain.AuditLog;
import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.repos.AuditLogRepository;
import com.nyc.hosp.repos.HospuserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Component
public class AuditLogListener {

    private final AuditLogRepository auditLogRepository;
    private final HospuserRepository hospuserRepository;

    public AuditLogListener(AuditLogRepository auditLogRepository, HospuserRepository hospuserRepository) {
        this.auditLogRepository = auditLogRepository;
        this.hospuserRepository = hospuserRepository;
    }

    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) return;

        Hospuser user = hospuserRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return;

        saveLog(user, "LOGIN_SUCCESS", null, null);
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();

        Hospuser user = hospuserRepository.findByUsername(username).orElse(null);
        if (user != null) {
            saveLog(user, "LOGIN_FAILURE", null, null);
        }
    }

    private void saveLog(Hospuser user, String action, String entityType, Integer entityId) {
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
