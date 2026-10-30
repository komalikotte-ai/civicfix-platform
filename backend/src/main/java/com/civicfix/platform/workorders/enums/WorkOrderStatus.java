package com.civicfix.platform.workorders.enums;

/**
 * Standard municipal work order lifecycle statuses.
 */
public enum WorkOrderStatus {
    CREATED,
    ASSIGNED,
    ACCEPTED,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED,
    PENDING_VERIFICATION,
    VERIFIED,
    REJECTED,
    CANCELLED
}
