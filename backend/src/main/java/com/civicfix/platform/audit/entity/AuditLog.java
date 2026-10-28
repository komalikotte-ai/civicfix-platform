package com.civicfix.platform.audit.entity;

import com.civicfix.platform.audit.enums.AuditAction;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_entity", columnList = "entity_name, entity_id"),
        @Index(name = "idx_audit_user", columnList = "performed_by")
})
public class AuditLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp = Instant.now();

    public AuditLog() {
    }

    public AuditLog(String entityName, String entityId, AuditAction action, String performedBy, String details) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
        this.timestamp = Instant.now();
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
