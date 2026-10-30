package com.civicfix.platform.maintenance.entity;

import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.maintenance.enums.ReminderStatus;
import com.civicfix.platform.users.entity.Employee;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "maintenance_reminders", indexes = {
        @Index(name = "idx_rem_status", columnList = "status"),
        @Index(name = "idx_rem_due", columnList = "due_date"),
        @Index(name = "idx_rem_recipient", columnList = "recipient_id")
})
public class MaintenanceReminder extends BaseAuditableEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "reminder_type", nullable = false, length = 50)
    private String reminderType = "UPCOMING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private MaintenanceSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private Employee recipient;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    public MaintenanceReminder() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }
    public ReminderStatus getStatus() { return status; }
    public void setStatus(ReminderStatus status) { this.status = status; }
    public String getReminderType() { return reminderType; }
    public void setReminderType(String reminderType) { this.reminderType = reminderType; }
    public MaintenanceSchedule getSchedule() { return schedule; }
    public void setSchedule(MaintenanceSchedule schedule) { this.schedule = schedule; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public Employee getRecipient() { return recipient; }
    public void setRecipient(Employee recipient) { this.recipient = recipient; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
}
