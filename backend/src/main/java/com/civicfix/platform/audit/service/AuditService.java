package com.civicfix.platform.audit.service;

import com.civicfix.platform.audit.dto.AuditLogDto;
import com.civicfix.platform.audit.entity.AuditLog;
import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.repository.AuditLogRepository;
import com.civicfix.platform.common.api.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(String entityName, String entityId, AuditAction action, String performedBy, String details) {
        AuditLog log = new AuditLog(entityName, entityId, action, performedBy, details);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getEntityAuditLogs(String entityName, String entityId, Pageable pageable) {
        Page<AuditLogDto> page = auditLogRepository
                .findByEntityNameAndEntityId(entityName, entityId, pageable)
                .map(AuditLogDto::fromEntity);
        return PagedResponse.from(page);
    }
}
