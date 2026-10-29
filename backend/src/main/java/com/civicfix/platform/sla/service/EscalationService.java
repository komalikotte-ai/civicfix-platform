package com.civicfix.platform.sla.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.complaints.repository.ComplaintRepository;
import com.civicfix.platform.sla.dto.ComplaintEscalationHistoryDto;
import com.civicfix.platform.sla.dto.ManualEscalationRequest;
import com.civicfix.platform.sla.entity.ComplaintEscalationHistory;
import com.civicfix.platform.sla.enums.EscalationLevel;
import com.civicfix.platform.sla.enums.SlaState;
import com.civicfix.platform.sla.event.SlaBreachedEvent;
import com.civicfix.platform.sla.event.SlaEscalatedEvent;
import com.civicfix.platform.sla.event.SlaWarningEvent;
import com.civicfix.platform.sla.repository.ComplaintEscalationHistoryRepository;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.users.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EscalationService {

    private static final Logger log = LoggerFactory.getLogger(EscalationService.class);

    private final ComplaintRepository complaintRepository;
    private final ComplaintEscalationHistoryRepository escalationHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final SlaService slaService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public EscalationService(ComplaintRepository complaintRepository,
                             ComplaintEscalationHistoryRepository escalationHistoryRepository,
                             EmployeeRepository employeeRepository,
                             SlaService slaService,
                             AuditService auditService,
                             ApplicationEventPublisher eventPublisher) {
        this.complaintRepository = complaintRepository;
        this.escalationHistoryRepository = escalationHistoryRepository;
        this.employeeRepository = employeeRepository;
        this.slaService = slaService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Evaluates a complaint's SLA deadline and performs automatic escalation if breached.
     * Returns true if an escalation occurred.
     */
    @Transactional
    public boolean evaluateAndEscalate(Complaint complaint, Instant currentTime) {
        if (complaint == null) return false;
        if (currentTime == null) currentTime = Instant.now();

        ComplaintStatus status = complaint.getStatus();
        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.CLOSED || status == ComplaintStatus.REJECTED) {
            SlaState resolvedState = slaService.calculateSlaState(complaint, currentTime);
            if (complaint.getSlaState() != resolvedState) {
                complaint.setSlaState(resolvedState);
                complaintRepository.save(complaint);
            }
            return false;
        }

        SlaState newState = slaService.calculateSlaState(complaint, currentTime);
        SlaState oldState = complaint.getSlaState();
        complaint.setSlaState(newState);

        // Warning state event detection
        if (newState == SlaState.WARNING && oldState != SlaState.WARNING) {
            long remainingMinutes = complaint.getSlaDueAt() != null ?
                    Duration.between(currentTime, complaint.getSlaDueAt()).toMinutes() : 0;
            log.warn("Complaint {} approached SLA warning threshold. {} minutes remaining.",
                    complaint.getTrackingNumber(), remainingMinutes);
            eventPublisher.publishEvent(new SlaWarningEvent(this, complaint, currentTime, remainingMinutes));
        }

        // Breach and Escalation handling
        if (newState == SlaState.BREACHED || newState == SlaState.OVERDUE) {
            if (oldState != SlaState.BREACHED && oldState != SlaState.OVERDUE) {
                log.error("Complaint {} breached SLA due date: {}", complaint.getTrackingNumber(), complaint.getSlaDueAt());
                eventPublisher.publishEvent(new SlaBreachedEvent(this, complaint, currentTime, complaint.getSlaDueAt()));
            }

            // Automatic escalation logic if not at max level
            EscalationLevel currentLevel = complaint.getEscalationLevel() != null ?
                    complaint.getEscalationLevel() : EscalationLevel.LEVEL_0;

            if (currentLevel != EscalationLevel.LEVEL_3) {
                EscalationLevel nextLevel = currentLevel.next();
                Employee targetEmployee = resolveEscalationTarget(complaint, nextLevel);

                // Auto-reassign supervisor if escalating to LEVEL_1
                if (nextLevel == EscalationLevel.LEVEL_1 && targetEmployee != null) {
                    complaint.setAssignedSupervisor(targetEmployee);
                }

                complaint.setEscalationLevel(nextLevel);
                complaint.setLastEscalatedAt(currentTime);

                ComplaintEscalationHistory history = new ComplaintEscalationHistory(
                        complaint,
                        currentLevel,
                        nextLevel,
                        targetEmployee,
                        null,
                        nextLevel.getTargetRole(),
                        "Automatic escalation due to SLA deadline breach",
                        String.format("Escalated to %s (%s)", nextLevel.getTitle(), nextLevel.getTargetRole())
                );
                escalationHistoryRepository.save(history);

                complaintRepository.save(complaint);

                auditService.record("COMPLAINT", complaint.getTrackingNumber(), AuditAction.UPDATE, "SLA_ENGINE",
                        String.format("SLA Breached: Auto-escalated from %s to %s", currentLevel, nextLevel));

                eventPublisher.publishEvent(new SlaEscalatedEvent(
                        this, complaint, currentLevel, nextLevel, targetEmployee,
                        nextLevel.getTargetRole(), history.getReason(), currentTime
                ));

                return true;
            }
        }

        complaintRepository.save(complaint);
        return false;
    }

    /**
     * Resolves the target staff member for the given escalation level.
     */
    public Employee resolveEscalationTarget(Complaint complaint, EscalationLevel level) {
        if (complaint == null) return null;

        if (level == EscalationLevel.LEVEL_1) {
            // Level 1: Field Supervisor
            if (complaint.getAssignedSupervisor() != null) {
                return complaint.getAssignedSupervisor();
            }
            if (complaint.getDepartment() != null) {
                List<Employee> deptEmployees = employeeRepository.findByDepartmentIdAndActiveTrue(complaint.getDepartment().getId());
                return deptEmployees.stream()
                        .filter(e -> e.getRole() == RoleType.ROLE_SUPERVISOR)
                        .findFirst()
                        .orElse(null);
            }
        } else if (level == EscalationLevel.LEVEL_2) {
            // Level 2: Department Manager
            if (complaint.getDepartment() != null) {
                List<Employee> deptEmployees = employeeRepository.findByDepartmentIdAndActiveTrue(complaint.getDepartment().getId());
                return deptEmployees.stream()
                        .filter(e -> e.getRole() == RoleType.ROLE_DEPARTMENT_MANAGER || e.getRole() == RoleType.ROLE_DEPT_ADMIN)
                        .findFirst()
                        .orElse(null);
            }
        }

        return null;
    }

    /**
     * Allows manual operational escalation of a critical complaint by an authorized officer.
     */
    @Transactional
    public ComplaintEscalationHistoryDto manualEscalate(Long complaintId, ManualEscalationRequest request, String performedBy) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", complaintId));

        EscalationLevel currentLevel = complaint.getEscalationLevel() != null ?
                complaint.getEscalationLevel() : EscalationLevel.LEVEL_0;
        EscalationLevel targetLevel = request.getTargetLevel();

        if (targetLevel.getLevel() <= currentLevel.getLevel()) {
            throw new BadRequestException(
                    String.format("Target escalation level (%s) must be higher than current level (%s)", targetLevel, currentLevel));
        }

        Employee targetEmployee = null;
        if (request.getTargetEmployeeId() != null) {
            targetEmployee = employeeRepository.findById(request.getTargetEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getTargetEmployeeId()));
        } else {
            targetEmployee = resolveEscalationTarget(complaint, targetLevel);
        }

        Instant now = Instant.now();
        complaint.setEscalationLevel(targetLevel);
        complaint.setLastEscalatedAt(now);

        if (targetLevel == EscalationLevel.LEVEL_1 && targetEmployee != null) {
            complaint.setAssignedSupervisor(targetEmployee);
        }

        Complaint saved = complaintRepository.save(complaint);

        ComplaintEscalationHistory history = new ComplaintEscalationHistory(
                saved,
                currentLevel,
                targetLevel,
                targetEmployee,
                null,
                targetLevel.getTargetRole(),
                request.getReason(),
                String.format("Manually escalated by %s to %s", performedBy, targetLevel.getTitle())
        );
        ComplaintEscalationHistory savedHistory = escalationHistoryRepository.save(history);

        auditService.record("COMPLAINT", saved.getTrackingNumber(), AuditAction.UPDATE, performedBy,
                String.format("Manual escalation from %s to %s. Reason: %s", currentLevel, targetLevel, request.getReason()));

        eventPublisher.publishEvent(new SlaEscalatedEvent(
                this, saved, currentLevel, targetLevel, targetEmployee,
                targetLevel.getTargetRole(), request.getReason(), now
        ));

        return ComplaintEscalationHistoryDto.fromEntity(savedHistory);
    }

    @Transactional(readOnly = true)
    public List<ComplaintEscalationHistoryDto> getEscalationHistory(Long complaintId) {
        return escalationHistoryRepository.findByComplaintIdOrderByEscalatedAtDesc(complaintId).stream()
                .map(ComplaintEscalationHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }
}
