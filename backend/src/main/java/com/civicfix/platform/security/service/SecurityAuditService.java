package com.civicfix.platform.security.service;

import com.civicfix.platform.audit.entity.AuditLog;
import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityAuditService {

    private final AuditLogRepository auditLogRepository;

    public SecurityAuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(String entityName, String entityId, AuditAction action, String performedBy, String ipAddress, String details) {
        AuditLog log = new AuditLog(entityName, entityId, action, performedBy, details);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }
}
