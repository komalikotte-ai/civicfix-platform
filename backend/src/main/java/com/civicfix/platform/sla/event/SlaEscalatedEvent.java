package com.civicfix.platform.sla.event;

import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.sla.enums.EscalationLevel;
import com.civicfix.platform.users.entity.Employee;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Event published when an overdue or high-severity complaint is escalated
 * to a higher operational level (Supervisor, Department Manager, Administrator).
 */
public class SlaEscalatedEvent extends ApplicationEvent {

    private final Complaint complaint;
    private final EscalationLevel fromLevel;
    private final EscalationLevel toLevel;
    private final Employee targetEmployee;
    private final String targetRole;
    private final String reason;
    private final Instant escalatedAt;

    public SlaEscalatedEvent(Object source, Complaint complaint, EscalationLevel fromLevel, EscalationLevel toLevel,
                             Employee targetEmployee, String targetRole, String reason, Instant escalatedAt) {
        super(source);
        this.complaint = complaint;
        this.fromLevel = fromLevel;
        this.toLevel = toLevel;
        this.targetEmployee = targetEmployee;
        this.targetRole = targetRole;
        this.reason = reason;
        this.escalatedAt = escalatedAt;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public EscalationLevel getFromLevel() {
        return fromLevel;
    }

    public EscalationLevel getToLevel() {
        return toLevel;
    }

    public Employee getTargetEmployee() {
        return targetEmployee;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public String getReason() {
        return reason;
    }

    public Instant getEscalatedAt() {
        return escalatedAt;
    }
}
