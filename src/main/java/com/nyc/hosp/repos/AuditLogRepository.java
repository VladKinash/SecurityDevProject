package com.nyc.hosp.repos;

import com.nyc.hosp.domain.AuditLog;
import com.nyc.hosp.domain.Hospuser;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    int countByUserAndActionAndTimestampAfter(@NotNull Hospuser user, @Size(max = 20) @NotNull String action, @NotNull Instant timestamp);

}
