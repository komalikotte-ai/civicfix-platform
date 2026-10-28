package com.civicfix.platform.audit.dto;

import com.civicfix.platform.audit.entity.AuditLog;
import com.civicfix.platform.audit.enums.AuditAction;

import java.time.Instant;

public class AuditLogDto {

    private Long id;
    private String entityName;
    private String entityId;
    private AuditAction action;
    private String performedBy;
    private String ipAddress;
    private String details;
    private Instant timestamp;

    public AuditLogDto() {
    }

    public static AuditLogDto fromEntity(AuditLog entity) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(entity.getId());
        dto.setEntityName(entity.getEntityName());
        dto.setEntityId(entity.getEntityId());
        dto.setAction(entity.getAction());
        dto.setPerformedBy(entity.getPerformedBy());
        dto.setIpAddress(entity.getIpAddress());
        dto.setDetails(entity.getDetails());
        dto.setTimestamp(entity.getTimestamp());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
