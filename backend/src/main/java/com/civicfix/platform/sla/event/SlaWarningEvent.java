package com.civicfix.platform.sla.event;

import com.civicfix.platform.complaints.entity.Complaint;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Event published when an active complaint enters SLA WARNING state
 * (approaching deadline threshold).
 * Downstream modules (e.g. Member 4 Notifications) can listen to this event.
 */
public class SlaWarningEvent extends ApplicationEvent {

    private final Complaint complaint;
    private final Instant warningTime;
    private final long remainingMinutes;

    public SlaWarningEvent(Object source, Complaint complaint, Instant warningTime, long remainingMinutes) {
        super(source);
        this.complaint = complaint;
        this.warningTime = warningTime;
        this.remainingMinutes = remainingMinutes;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public Instant getWarningTime() {
        return warningTime;
    }

    public long getRemainingMinutes() {
        return remainingMinutes;
    }
}
