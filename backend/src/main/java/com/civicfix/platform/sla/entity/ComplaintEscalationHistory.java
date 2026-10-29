package com.civicfix.platform.sla.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.sla.enums.EscalationLevel;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "complaint_escalation_histories", indexes = {
        @Index(name = "idx_esc_complaint", columnList = "complaint_id"),
        @Index(name = "idx_esc_time", columnList = "escalated_at")
})
public class ComplaintEscalationHistory extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_level", nullable = false, length = 30)
    private EscalationLevel fromLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_level", nullable = false, length = 30)
    private EscalationLevel toLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_to_employee_id")
    private Employee escalatedToEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_to_user_id")
    private User escalatedToUser;

    @Column(name = "target_role", length = 50)
    private String targetRole;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "escalated_at", nullable = false)
    private Instant escalatedAt = Instant.now();

    public ComplaintEscalationHistory() {
    }

    public ComplaintEscalationHistory(Complaint complaint, EscalationLevel fromLevel, EscalationLevel toLevel,
                                      Employee escalatedToEmployee, User escalatedToUser, String targetRole,
                                      String reason, String actionTaken) {
        this.complaint = complaint;
        this.fromLevel = fromLevel;
        this.toLevel = toLevel;
        this.escalatedToEmployee = escalatedToEmployee;
        this.escalatedToUser = escalatedToUser;
        this.targetRole = targetRole;
        this.reason = reason;
        this.actionTaken = actionTaken;
        this.escalatedAt = Instant.now();
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public EscalationLevel getFromLevel() {
        return fromLevel;
    }

    public void setFromLevel(EscalationLevel fromLevel) {
        this.fromLevel = fromLevel;
    }

    public EscalationLevel getToLevel() {
        return toLevel;
    }

    public void setToLevel(EscalationLevel toLevel) {
        this.toLevel = toLevel;
    }

    public Employee getEscalatedToEmployee() {
        return escalatedToEmployee;
    }

    public void setEscalatedToEmployee(Employee escalatedToEmployee) {
        this.escalatedToEmployee = escalatedToEmployee;
    }

    public User getEscalatedToUser() {
        return escalatedToUser;
    }

    public void setEscalatedToUser(User escalatedToUser) {
        this.escalatedToUser = escalatedToUser;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public Instant getEscalatedAt() {
        return escalatedAt;
    }

    public void setEscalatedAt(Instant escalatedAt) {
        this.escalatedAt = escalatedAt;
    }
}
