package com.civicfix.platform.workflow.service;

import com.civicfix.platform.common.exception.BusinessException;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.workflow.dto.WorkflowTransitionDto;
import com.civicfix.platform.workflow.entity.WorkflowTransition;
import com.civicfix.platform.workflow.repository.WorkflowTransitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkflowEngine {

    private final WorkflowTransitionRepository transitionRepository;

    // Strict municipal grievance lifecycle state machine
    private static final Map<ComplaintStatus, Set<ComplaintStatus>> ALLOWED_TRANSITIONS = Map.of(
            ComplaintStatus.NEW, Set.of(ComplaintStatus.ACKNOWLEDGED, ComplaintStatus.ASSIGNED, ComplaintStatus.REJECTED),
            ComplaintStatus.SUBMITTED, Set.of(ComplaintStatus.ACKNOWLEDGED, ComplaintStatus.ASSIGNED, ComplaintStatus.REJECTED),
            ComplaintStatus.ACKNOWLEDGED, Set.of(ComplaintStatus.ASSIGNED, ComplaintStatus.REJECTED),
            ComplaintStatus.ASSIGNED, Set.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.ON_HOLD, ComplaintStatus.REJECTED, ComplaintStatus.ASSIGNED, ComplaintStatus.RESOLVED),
            ComplaintStatus.IN_PROGRESS, Set.of(ComplaintStatus.RESOLVED, ComplaintStatus.ON_HOLD, ComplaintStatus.ASSIGNED),
            ComplaintStatus.ON_HOLD, Set.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.ASSIGNED, ComplaintStatus.REJECTED),
            ComplaintStatus.RESOLVED, Set.of(ComplaintStatus.CLOSED, ComplaintStatus.REOPENED),
            ComplaintStatus.CLOSED, Set.of(ComplaintStatus.REOPENED),
            ComplaintStatus.REOPENED, Set.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS, ComplaintStatus.REJECTED),
            ComplaintStatus.REJECTED, Set.of(ComplaintStatus.REOPENED, ComplaintStatus.NEW, ComplaintStatus.SUBMITTED)
    );

    public WorkflowEngine(WorkflowTransitionRepository transitionRepository) {
        this.transitionRepository = transitionRepository;
    }

    public boolean isTransitionAllowed(ComplaintStatus currentStatus, ComplaintStatus targetStatus) {
        if (currentStatus == targetStatus && currentStatus == ComplaintStatus.ASSIGNED) {
            // Reassignment within ASSIGNED state is permitted
            return true;
        }
        Set<ComplaintStatus> allowedTargets = ALLOWED_TRANSITIONS.get(currentStatus);
        return allowedTargets != null && allowedTargets.contains(targetStatus);
    }

    public Set<ComplaintStatus> getAllowedNextStatuses(ComplaintStatus currentStatus) {
        return ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
    }

    @Transactional
    public WorkflowTransition recordTransition(Complaint complaint, ComplaintStatus targetStatus, String remarks, User actionByUser) {
        ComplaintStatus currentStatus = complaint.getStatus();

        if (!isTransitionAllowed(currentStatus, targetStatus)) {
            throw new BusinessException(
                    String.format("Invalid status transition from '%s' to '%s'. Allowed transitions: %s",
                            currentStatus, targetStatus, getAllowedNextStatuses(currentStatus)),
                    "INVALID_STATUS_TRANSITION"
            );
        }

        WorkflowTransition transition = new WorkflowTransition(complaint, currentStatus, targetStatus, remarks, actionByUser);
        return transitionRepository.save(transition);
    }

    @Transactional(readOnly = true)
    public List<WorkflowTransitionDto> getTransitionsForComplaint(Long complaintId) {
        return transitionRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId).stream()
                .map(WorkflowTransitionDto::fromEntity)
                .collect(Collectors.toList());
    }
}
