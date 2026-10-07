package com.fitnessops.modules.auth.service.impl;

import com.fitnessops.modules.auth.entity.AuthAuditLog;
import com.fitnessops.modules.auth.repository.AuthAuditLogRepository;
import com.fitnessops.modules.auth.service.AuthAuditEvent;
import com.fitnessops.modules.auth.service.AuthAuditService;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthAuditServiceImpl implements AuthAuditService {

    private static final int MAX_DETAIL_LENGTH = 1000;
    private static final int MAX_USERNAME_LENGTH = 100;

    private final AuthAuditLogRepository auditLogRepository;
    private final Clock clock;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AuthAuditEvent event) {
        AuthAuditLog entry = AuthAuditLog.builder()
                .eventType(event.type())
                .reasonCode(event.reason())
                .userId(event.userId())
                .username(truncate(event.username(), MAX_USERNAME_LENGTH))
                .sessionId(event.sessionId())
                .clientType(event.clientType())
                .deviceId(event.deviceId())
                .branchId(event.branchId())
                .ipAddress(event.client() == null ? null : event.client().ipAddress())
                .userAgent(event.client() == null ? null : event.client().userAgent())
                .detail(truncate(event.detail(), MAX_DETAIL_LENGTH))
                .occurredAt(Instant.now(clock))
                .build();
        auditLogRepository.saveAndFlush(entry);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
