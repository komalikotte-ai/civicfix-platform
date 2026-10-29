package com.civicfix.platform.complaints.enums;

/**
 * Standard municipal grievance lifecycle statuses.
 */
public enum ComplaintStatus {
    NEW,
    ACKNOWLEDGED,
    ASSIGNED,
    IN_PROGRESS,
    ON_HOLD,
    RESOLVED,
    CLOSED,
    REOPENED,
    REJECTED,
    // Backward compatibility aliases
    SUBMITTED,
    ESCALATED
}
