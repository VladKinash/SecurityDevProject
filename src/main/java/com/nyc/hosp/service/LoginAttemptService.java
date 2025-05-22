package com.nyc.hosp.service;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.repos.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class LoginAttemptService {

    private final AuditLogRepository auditLogRepository;

    public LoginAttemptService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public boolean isLocked(Hospuser user) {
        Instant since = Instant.now().minus(15, ChronoUnit.MINUTES);
        int failedAttempts = auditLogRepository.countByUserAndActionAndTimestampAfter(user, "LOGIN_FAILURE", since);
        return failedAttempts >= 5;
    }
}
