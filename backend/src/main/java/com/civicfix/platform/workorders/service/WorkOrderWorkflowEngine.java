package com.civicfix.platform.workorders.service;

import com.civicfix.platform.common.exception.BusinessException;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Service
public class WorkOrderWorkflowEngine {

    /**
     * Strict municipal work order lifecycle state machine:
     * - CREATED -> ASSIGNED, CANCELLED
     * - ASSIGNED -> ACCEPTED, ON_HOLD, CANCELLED, ASSIGNED (reassignment)
     * - ACCEPTED -> IN_PROGRESS, ON_HOLD, CANCELLED
     * - IN_PROGRESS -> COMPLETED, ON_HOLD, CANCELLED
     * - ON_HOLD -> IN_PROGRESS, ASSIGNED, CANCELLED
     * - COMPLETED -> PENDING_VERIFICATION, VERIFIED, REJECTED
     * - PENDING_VERIFICATION -> VERIFIED, REJECTED
     * - REJECTED -> IN_PROGRESS, ASSIGNED, CANCELLED (rework)
     * - VERIFIED -> terminal (no transitions)
     * - CANCELLED -> terminal (no transitions)
     */
    private static final Map<WorkOrderStatus, Set<WorkOrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            WorkOrderStatus.CREATED, Set.of(WorkOrderStatus.ASSIGNED, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.ASSIGNED, Set.of(WorkOrderStatus.ACCEPTED, WorkOrderStatus.ON_HOLD, WorkOrderStatus.CANCELLED, WorkOrderStatus.ASSIGNED),
            WorkOrderStatus.ACCEPTED, Set.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.ON_HOLD, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.IN_PROGRESS, Set.of(WorkOrderStatus.COMPLETED, WorkOrderStatus.ON_HOLD, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.ON_HOLD, Set.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.ASSIGNED, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.COMPLETED, Set.of(WorkOrderStatus.PENDING_VERIFICATION, WorkOrderStatus.VERIFIED, WorkOrderStatus.REJECTED),
            WorkOrderStatus.PENDING_VERIFICATION, Set.of(WorkOrderStatus.VERIFIED, WorkOrderStatus.REJECTED),
            WorkOrderStatus.REJECTED, Set.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.ASSIGNED, WorkOrderStatus.CANCELLED),
            WorkOrderStatus.VERIFIED, Collections.emptySet(),
            WorkOrderStatus.CANCELLED, Collections.emptySet()
    );

    public boolean isTransitionAllowed(WorkOrderStatus currentStatus, WorkOrderStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return false;
        }
        if (currentStatus == WorkOrderStatus.ASSIGNED && targetStatus == WorkOrderStatus.ASSIGNED) {
            // Reassignment in ASSIGNED state is permitted
            return true;
        }
        Set<WorkOrderStatus> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());
        return allowedTargets.contains(targetStatus);
    }

    public Set<WorkOrderStatus> getAllowedNextStatuses(WorkOrderStatus currentStatus) {
        return ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());
    }

    public void validateTransition(WorkOrderStatus currentStatus, WorkOrderStatus targetStatus) {
        if (!isTransitionAllowed(currentStatus, targetStatus)) {
            throw new BusinessException(
                    String.format("Invalid status transition from '%s' to '%s'. Allowed transitions: %s",
                            currentStatus, targetStatus, getAllowedNextStatuses(currentStatus)),
                    "INVALID_STATUS_TRANSITION"
            );
        }
    }
}
