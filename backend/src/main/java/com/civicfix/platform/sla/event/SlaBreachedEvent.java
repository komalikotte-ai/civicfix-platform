package com.civicfix.platform.sla.event;

import com.civicfix.platform.complaints.entity.Complaint;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Event published when a complaint's resolution deadline is breached.
 */
public class SlaBreachedEvent extends ApplicationEvent {

    private final Complaint complaint;
    private final Instant breachedAt;
    private final Instant slaDueAt;

    public SlaBreachedEvent(Object source, Complaint complaint, Instant breachedAt, Instant slaDueAt) {
        super(source);
        this.complaint = complaint;
        this.breachedAt = breachedAt;
        this.slaDueAt = slaDueAt;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public Instant getBreachedAt() {
        return breachedAt;
    }

    public Instant getSlaDueAt() {
        return slaDueAt;
    }
}
