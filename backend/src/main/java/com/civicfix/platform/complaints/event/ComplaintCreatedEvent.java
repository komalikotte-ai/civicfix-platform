package com.civicfix.platform.complaints.event;

import com.civicfix.platform.complaints.entity.Complaint;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new complaint is lodged.
 * Member 4's AI service can consume this event or integration point
 * to perform duplicate detection analysis asynchronously.
 */
public class ComplaintCreatedEvent extends ApplicationEvent {

    private final Complaint complaint;

    public ComplaintCreatedEvent(Object source, Complaint complaint) {
        super(source);
        this.complaint = complaint;
    }

    public Complaint getComplaint() {
        return complaint;
    }
}
