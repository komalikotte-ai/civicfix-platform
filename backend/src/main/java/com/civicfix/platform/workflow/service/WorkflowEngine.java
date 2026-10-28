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

    // Allowed transition map for municipal complaints
    private static final Map<ComplaintStatus, Set<ComplaintStatus>> ALLOWED_TRANSITIONS = Map.of(
            ComplaintStatus.SUBMITTED, Set.of(ComplaintStatus.ACKNOWLEDGED, ComplaintStatus.REJECTED),
            ComplaintStatus.ACKNOWLEDGED, Set.of(ComplaintStatus.ASSIGNED, ComplaintStatus.REJECTED),
            ComplaintStatus.ASSIGNED, Set.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.ESCALATED, ComplaintStatus.REJECTED),
            ComplaintStatus.IN_PROGRESS, Set.of(ComplaintStatus.RESOLVED, ComplaintStatus.ESCALATED),
            ComplaintStatus.ESCALATED, Set.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED),
            ComplaintStatus.RESOLVED, Set.of(ComplaintStatus.CLOSED, ComplaintStatus.IN_PROGRESS),
            ComplaintStatus.REJECTED, Set.of(ComplaintStatus.SUBMITTED),
            ComplaintStatus.CLOSED, Set.of()
    );

    public WorkflowEngine(WorkflowTransitionRepository transitionRepository) {
        this.transitionRepository = transitionRepository;
    }

    public boolean isTransitionAllowed(ComplaintStatus currentStatus, ComplaintStatus targetStatus) {
        Set<ComplaintStatus> allowedTargets = ALLOWED_TRANSITIONS.get(currentStatus);
        return allowedTargets != null && allowedTargets.contains(targetStatus);
    }

    @Transactional
    public WorkflowTransition recordTransition(Complaint complaint, ComplaintStatus targetStatus, String remarks, User actionByUser) {
        ComplaintStatus currentStatus = complaint.getStatus();

        if (!isTransitionAllowed(currentStatus, targetStatus)) {
            throw new BusinessException(
                    String.format("Invalid status transition from '%s' to '%s'", currentStatus, targetStatus),
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
