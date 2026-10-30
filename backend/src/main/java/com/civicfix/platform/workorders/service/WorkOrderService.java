package com.civicfix.platform.workorders.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.BusinessException;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.complaints.repository.ComplaintRepository;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.users.repository.EmployeeRepository;
import com.civicfix.platform.users.repository.UserRepository;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.wards.repository.WardRepository;
import com.civicfix.platform.workorders.dto.*;
import com.civicfix.platform.workorders.entity.*;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import com.civicfix.platform.workorders.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class WorkOrderService {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderService.class);

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStatusHistoryRepository statusHistoryRepository;
    private final WorkOrderAssignmentHistoryRepository assignmentHistoryRepository;
    private final WorkOrderCommentRepository commentRepository;
    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;
    private final WardRepository wardRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final WorkOrderWorkflowEngine workflowEngine;
    private final WorkOrderSchedulingService schedulingService;
    private final AuditService auditService;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            WorkOrderStatusHistoryRepository statusHistoryRepository,
                            WorkOrderAssignmentHistoryRepository assignmentHistoryRepository,
                            WorkOrderCommentRepository commentRepository,
                            ComplaintRepository complaintRepository,
                            DepartmentRepository departmentRepository,
                            WardRepository wardRepository,
                            EmployeeRepository employeeRepository,
                            UserRepository userRepository,
                            WorkOrderWorkflowEngine workflowEngine,
                            WorkOrderSchedulingService schedulingService,
                            AuditService auditService) {
        this.workOrderRepository = workOrderRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
        this.commentRepository = commentRepository;
        this.complaintRepository = complaintRepository;
        this.departmentRepository = departmentRepository;
        this.wardRepository = wardRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.workflowEngine = workflowEngine;
        this.schedulingService = schedulingService;
        this.auditService = auditService;
    }

    /**
     * Creates a new standalone work order.
     */
    @Transactional
    public WorkOrderDto createWorkOrder(CreateWorkOrderRequest request, String username) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        Organization organization = department.getOrganization();

        Ward ward = null;
        if (request.getWardId() != null) {
            ward = wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + request.getWardId()));
        }

        Complaint complaint = null;
        if (request.getComplaintId() != null) {
            complaint = complaintRepository.findById(request.getComplaintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + request.getComplaintId()));
            if (workOrderRepository.existsByComplaintIdAndStatusNot(complaint.getId(), WorkOrderStatus.CANCELLED)) {
                throw new ConflictException("An active work order already exists for complaint #" + complaint.getTrackingNumber());
            }
        }

        schedulingService.validateScheduleDates(request.getScheduledStartDate(), request.getExpectedCompletionDate());

        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderNumber(generateUniqueWorkOrderNumber());
        workOrder.setTitle(request.getTitle());
        workOrder.setDescription(request.getDescription());
        workOrder.setPriority(request.getPriority() != null ? request.getPriority() : WorkOrderPriority.MEDIUM);
        workOrder.setDepartment(department);
        workOrder.setOrganization(organization);
        workOrder.setWard(ward);
        workOrder.setComplaint(complaint);
        workOrder.setAddressLine(request.getAddressLine());
        workOrder.setLatitude(request.getLatitude());
        workOrder.setLongitude(request.getLongitude());
        workOrder.setScheduledStartDate(request.getScheduledStartDate());
        workOrder.setExpectedCompletionDate(request.getExpectedCompletionDate());
        workOrder.setEstimatedCost(request.getEstimatedCost() != null ? request.getEstimatedCost() : BigDecimal.ZERO);
        workOrder.setCreatedBy(username);
        workOrder.setStatus(WorkOrderStatus.CREATED);

        // Assign worker and supervisor if supplied
        if (request.getAssignedWorkerId() != null) {
            Employee worker = validateAndGetWorker(request.getAssignedWorkerId());
            workOrder.setAssignedWorker(worker);
            workOrder.setAssignedAt(Instant.now());
            workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        }

        if (request.getAssignedSupervisorId() != null) {
            Employee supervisor = validateAndGetSupervisor(request.getAssignedSupervisorId());
            workOrder.setAssignedSupervisor(supervisor);
        }

        WorkOrder saved = workOrderRepository.save(workOrder);

        // Record initial status history
        recordStatusHistory(saved, null, saved.getStatus(), "Work order created", username);

        // Record initial assignment history if worker was assigned
        if (saved.getAssignedWorker() != null) {
            recordAssignmentHistory(saved, null, saved.getAssignedWorker(), null, saved.getAssignedSupervisor(), username, "Initial assignment on creation");
        }

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.CREATE, username,
                "Created work order " + saved.getWorkOrderNumber());

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Converts an eligible complaint into a work order.
     */
    @Transactional
    public WorkOrderDto convertComplaint(ConvertComplaintRequest request, String username) {
        Complaint complaint = complaintRepository.findById(request.getComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + request.getComplaintId()));

        if (complaint.getStatus() == ComplaintStatus.REJECTED || complaint.getStatus() == ComplaintStatus.CLOSED) {
            throw new BusinessException("Cannot convert a " + complaint.getStatus() + " complaint into a work order", "INVALID_COMPLAINT_STATUS");
        }

        if (workOrderRepository.existsByComplaintIdAndStatusNot(complaint.getId(), WorkOrderStatus.CANCELLED)) {
            WorkOrder existing = workOrderRepository.findFirstByComplaintIdAndStatusNot(complaint.getId(), WorkOrderStatus.CANCELLED).orElse(null);
            String woNum = existing != null ? existing.getWorkOrderNumber() : "EXISTING";
            throw new ConflictException("An active work order (" + woNum + ") already exists for complaint #" + complaint.getTrackingNumber());
        }

        Department department;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));
        } else if (complaint.getDepartment() != null) {
            department = complaint.getDepartment();
        } else {
            throw new BadRequestException("Department must be specified either on the complaint or in the conversion request");
        }

        schedulingService.validateScheduleDates(request.getScheduledStartDate(), request.getExpectedCompletionDate());

        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderNumber(generateUniqueWorkOrderNumber());
        workOrder.setComplaint(complaint);
        workOrder.setOrganization(complaint.getOrganization() != null ? complaint.getOrganization() : department.getOrganization());
        workOrder.setDepartment(department);
        workOrder.setWard(complaint.getWard());
        workOrder.setTitle(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle() : complaint.getTitle());
        workOrder.setDescription(request.getDescription() != null && !request.getDescription().isBlank() ? request.getDescription() : complaint.getDescription());

        WorkOrderPriority priority = request.getPriority() != null ? request.getPriority() : mapComplaintPriority(complaint.getPriority());
        workOrder.setPriority(priority);

        workOrder.setAddressLine(complaint.getAddressLine());
        workOrder.setLatitude(complaint.getLatitude());
        workOrder.setLongitude(complaint.getLongitude());
        workOrder.setScheduledStartDate(request.getScheduledStartDate());
        workOrder.setExpectedCompletionDate(request.getExpectedCompletionDate());
        workOrder.setEstimatedCost(request.getEstimatedCost() != null ? request.getEstimatedCost() : BigDecimal.ZERO);
        workOrder.setCreatedBy(username);
        workOrder.setStatus(WorkOrderStatus.CREATED);

        if (request.getAssignedWorkerId() != null) {
            Employee worker = validateAndGetWorker(request.getAssignedWorkerId());
            workOrder.setAssignedWorker(worker);
            workOrder.setAssignedAt(Instant.now());
            workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        } else if (complaint.getAssignedEmployee() != null && complaint.getAssignedEmployee().isActive()) {
            workOrder.setAssignedWorker(complaint.getAssignedEmployee());
            workOrder.setAssignedAt(Instant.now());
            workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        }

        if (request.getAssignedSupervisorId() != null) {
            Employee supervisor = validateAndGetSupervisor(request.getAssignedSupervisorId());
            workOrder.setAssignedSupervisor(supervisor);
        } else if (complaint.getAssignedSupervisor() != null && complaint.getAssignedSupervisor().isActive()) {
            workOrder.setAssignedSupervisor(complaint.getAssignedSupervisor());
        }

        WorkOrder saved = workOrderRepository.save(workOrder);

        recordStatusHistory(saved, null, saved.getStatus(), "Converted from complaint #" + complaint.getTrackingNumber(), username);

        if (saved.getAssignedWorker() != null) {
            recordAssignmentHistory(saved, null, saved.getAssignedWorker(), null, saved.getAssignedSupervisor(), username, "Assigned upon complaint conversion");
        }

        // Align linked complaint status with field work lifecycle
        if (complaint.getStatus() == ComplaintStatus.NEW || complaint.getStatus() == ComplaintStatus.SUBMITTED
                || complaint.getStatus() == ComplaintStatus.ACKNOWLEDGED) {
            complaint.setStatus(ComplaintStatus.IN_PROGRESS);
            complaintRepository.save(complaint);
        }

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.CREATE, username,
                "Converted complaint " + complaint.getTrackingNumber() + " to work order " + saved.getWorkOrderNumber());

        return WorkOrderDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public WorkOrderDto getWorkOrderById(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));
        return WorkOrderDto.fromEntity(workOrder);
    }

    @Transactional(readOnly = true)
    public WorkOrderDto getWorkOrderByNumber(String workOrderNumber) {
        WorkOrder workOrder = workOrderRepository.findByWorkOrderNumber(workOrderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with number: " + workOrderNumber));
        return WorkOrderDto.fromEntity(workOrder);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDto> getWorkOrdersByComplaintId(Long complaintId) {
        return workOrderRepository.findByComplaintId(complaintId).stream()
                .map(WorkOrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<WorkOrderDto> searchWorkOrders(
            String workOrderNumber,
            Long complaintId,
            String complaintTrackingNumber,
            Long departmentId,
            Long wardId,
            Long assignedWorkerId,
            Long assignedSupervisorId,
            WorkOrderStatus status,
            WorkOrderPriority priority,
            Instant startDate,
            Instant endDate,
            String keyword,
            Pageable pageable) {

        Page<WorkOrder> page = workOrderRepository.searchWorkOrders(
                workOrderNumber, complaintId, complaintTrackingNumber, departmentId,
                wardId, assignedWorkerId, assignedSupervisorId, status, priority,
                startDate, endDate, keyword, pageable);

        return PagedResponse.from(page.map(WorkOrderDto::fromEntity));
    }

    @Transactional
    public WorkOrderDto updateWorkOrder(Long id, UpdateWorkOrderRequest request, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        if (workOrder.getStatus() == WorkOrderStatus.VERIFIED || workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new BusinessException("Cannot modify a work order in " + workOrder.getStatus() + " state", "TERMINAL_STATE");
        }

        Instant newStart = request.getScheduledStartDate() != null ? request.getScheduledStartDate() : workOrder.getScheduledStartDate();
        Instant newEnd = request.getExpectedCompletionDate() != null ? request.getExpectedCompletionDate() : workOrder.getExpectedCompletionDate();
        schedulingService.validateScheduleDates(newStart, newEnd);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            workOrder.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            workOrder.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            workOrder.setPriority(request.getPriority());
        }
        if (request.getScheduledStartDate() != null) {
            workOrder.setScheduledStartDate(request.getScheduledStartDate());
        }
        if (request.getExpectedCompletionDate() != null) {
            workOrder.setExpectedCompletionDate(request.getExpectedCompletionDate());
        }
        if (request.getEstimatedCost() != null) {
            workOrder.setEstimatedCost(request.getEstimatedCost());
        }
        if (request.getAddressLine() != null) {
            workOrder.setAddressLine(request.getAddressLine());
        }
        if (request.getLatitude() != null) {
            workOrder.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            workOrder.setLongitude(request.getLongitude());
        }

        workOrder.setModifiedBy(username);
        WorkOrder saved = workOrderRepository.save(workOrder);

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.UPDATE, username,
                "Updated details of work order " + saved.getWorkOrderNumber());

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Assigns or reassigns worker and supervisor to a work order.
     */
    @Transactional
    public WorkOrderDto assignWorkOrder(Long id, AssignWorkOrderRequest request, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        if (workOrder.getStatus() == WorkOrderStatus.VERIFIED || workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new BusinessException("Cannot assign a work order in " + workOrder.getStatus() + " state", "TERMINAL_STATE");
        }

        Employee newWorker = validateAndGetWorker(request.getWorkerId());
        Employee newSupervisor = null;
        if (request.getSupervisorId() != null) {
            newSupervisor = validateAndGetSupervisor(request.getSupervisorId());
        }

        Employee prevWorker = workOrder.getAssignedWorker();
        Employee prevSupervisor = workOrder.getAssignedSupervisor();

        workOrder.setAssignedWorker(newWorker);
        if (newSupervisor != null) {
            workOrder.setAssignedSupervisor(newSupervisor);
        }
        workOrder.setAssignedAt(Instant.now());

        if (request.getScheduledStartDate() != null) {
            workOrder.setScheduledStartDate(request.getScheduledStartDate());
        }
        if (request.getExpectedCompletionDate() != null) {
            workOrder.setExpectedCompletionDate(request.getExpectedCompletionDate());
        }
        schedulingService.validateScheduleDates(workOrder.getScheduledStartDate(), workOrder.getExpectedCompletionDate());

        WorkOrderStatus prevStatus = workOrder.getStatus();
        if (workOrder.getStatus() == WorkOrderStatus.CREATED) {
            workOrder.setStatus(WorkOrderStatus.ASSIGNED);
            recordStatusHistory(workOrder, prevStatus, WorkOrderStatus.ASSIGNED, "Assigned to worker " + newWorker.getFullName(), username);
        }

        workOrder.setModifiedBy(username);
        WorkOrder saved = workOrderRepository.save(workOrder);

        recordAssignmentHistory(saved, prevWorker, newWorker, prevSupervisor, saved.getAssignedSupervisor(),
                username, request.getReason() != null ? request.getReason() : "Assigned by " + username);

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.ASSIGNMENT, username,
                String.format("Assigned work order %s to worker %s", saved.getWorkOrderNumber(), newWorker.getFullName()));

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Updates work order status according to lifecycle rules.
     */
    @Transactional
    public WorkOrderDto updateStatus(Long id, UpdateWorkOrderStatusRequest request, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        WorkOrderStatus currentStatus = workOrder.getStatus();
        WorkOrderStatus targetStatus = request.getTargetStatus();

        workflowEngine.validateTransition(currentStatus, targetStatus);

        if (targetStatus == WorkOrderStatus.IN_PROGRESS) {
            validateCanStart(workOrder);
        }

        workOrder.setStatus(targetStatus);
        workOrder.setModifiedBy(username);

        if (targetStatus == WorkOrderStatus.IN_PROGRESS && workOrder.getActualStartDate() == null) {
            workOrder.setActualStartDate(Instant.now());
        } else if (targetStatus == WorkOrderStatus.COMPLETED && workOrder.getActualCompletionDate() == null) {
            workOrder.setActualCompletionDate(Instant.now());
        }

        WorkOrder saved = workOrderRepository.save(workOrder);

        recordStatusHistory(saved, currentStatus, targetStatus, request.getRemarks(), username);

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                String.format("Changed status of work order %s from %s to %s", saved.getWorkOrderNumber(), currentStatus, targetStatus));

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Field worker accepts assignment.
     */
    @Transactional
    public WorkOrderDto acceptWorkOrder(Long id, String remarks, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        workflowEngine.validateTransition(workOrder.getStatus(), WorkOrderStatus.ACCEPTED);

        WorkOrderStatus prevStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatus.ACCEPTED);
        workOrder.setModifiedBy(username);
        WorkOrder saved = workOrderRepository.save(workOrder);

        recordStatusHistory(saved, prevStatus, WorkOrderStatus.ACCEPTED,
                remarks != null ? remarks : "Work order accepted by field worker", username);

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Accepted work order " + saved.getWorkOrderNumber());

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Field worker starts execution.
     */
    @Transactional
    public WorkOrderDto startWorkOrder(Long id, String remarks, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        workflowEngine.validateTransition(workOrder.getStatus(), WorkOrderStatus.IN_PROGRESS);
        validateCanStart(workOrder);

        WorkOrderStatus prevStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        if (workOrder.getActualStartDate() == null) {
            workOrder.setActualStartDate(Instant.now());
        }
        workOrder.setModifiedBy(username);
        WorkOrder saved = workOrderRepository.save(workOrder);

        recordStatusHistory(saved, prevStatus, WorkOrderStatus.IN_PROGRESS,
                remarks != null ? remarks : "Work commenced on site", username);

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Started work order " + saved.getWorkOrderNumber());

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Puts work order on hold.
     */
    @Transactional
    public WorkOrderDto holdWorkOrder(Long id, String reason, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        workflowEngine.validateTransition(workOrder.getStatus(), WorkOrderStatus.ON_HOLD);

        WorkOrderStatus prevStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatus.ON_HOLD);
        workOrder.setModifiedBy(username);
        WorkOrder saved = workOrderRepository.save(workOrder);

        recordStatusHistory(saved, prevStatus, WorkOrderStatus.ON_HOLD,
                reason != null ? reason : "Work order placed on hold", username);

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Placed work order " + saved.getWorkOrderNumber() + " on hold");

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Completes work order execution and submits for verification.
     */
    @Transactional
    public WorkOrderDto completeWorkOrder(Long id, CompleteWorkOrderRequest request, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        workflowEngine.validateTransition(workOrder.getStatus(), WorkOrderStatus.COMPLETED);

        WorkOrderStatus prevStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        workOrder.setActualCompletionDate(Instant.now());

        if (request != null) {
            if (request.getCompletionNotes() != null) {
                workOrder.setCompletionNotes(request.getCompletionNotes());
            }
            if (request.getActualCost() != null) {
                workOrder.setActualCost(request.getActualCost());
            }
            if (request.getEvidenceMetadata() != null) {
                workOrder.setEvidenceMetadata(request.getEvidenceMetadata());
            }
        }

        workOrder.setModifiedBy(username);
        WorkOrder saved = workOrderRepository.save(workOrder);

        recordStatusHistory(saved, prevStatus, WorkOrderStatus.COMPLETED,
                request != null ? request.getCompletionNotes() : "Work completed and submitted", username);

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Completed work order " + saved.getWorkOrderNumber());

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Supervisor verification: approves to VERIFIED or rejects with rework reason.
     */
    @Transactional
    public WorkOrderDto verifyWorkOrder(Long id, VerifyWorkOrderRequest request, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        if (workOrder.getStatus() != WorkOrderStatus.COMPLETED && workOrder.getStatus() != WorkOrderStatus.PENDING_VERIFICATION) {
            throw new BusinessException("Only completed work orders can be verified. Current status: " + workOrder.getStatus(), "INVALID_VERIFICATION_STATUS");
        }

        User verifier = userRepository.findByUsername(username).orElse(null);
        WorkOrderStatus prevStatus = workOrder.getStatus();

        if (Boolean.TRUE.equals(request.getApproved())) {
            workOrder.setStatus(WorkOrderStatus.VERIFIED);
            workOrder.setVerifiedBy(verifier);
            workOrder.setVerifiedAt(Instant.now());
            workOrder.setVerificationNotes(request.getVerificationNotes());
            workOrder.setRejectionReason(null);

            recordStatusHistory(workOrder, prevStatus, WorkOrderStatus.VERIFIED,
                    request.getVerificationNotes() != null ? request.getVerificationNotes() : "Verified and approved by supervisor", username);

            // Automatically resolve linked complaint upon successful supervisor verification
            Complaint complaint = workOrder.getComplaint();
            if (complaint != null && complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.CLOSED) {
                complaint.setStatus(ComplaintStatus.RESOLVED);
                complaint.setResolvedAt(Instant.now());
                complaintRepository.save(complaint);
            }
        } else {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new BadRequestException("Rejection reason is required when rejecting work order verification");
            }
            workOrder.setStatus(WorkOrderStatus.REJECTED);
            workOrder.setRejectionReason(request.getRejectionReason());
            workOrder.setRejectionCount(workOrder.getRejectionCount() + 1);
            workOrder.setVerificationNotes(request.getVerificationNotes());

            recordStatusHistory(workOrder, prevStatus, WorkOrderStatus.REJECTED,
                    "Rejected by supervisor: " + request.getRejectionReason(), username);
        }

        workOrder.setModifiedBy(username);
        WorkOrder saved = workOrderRepository.save(workOrder);

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                String.format("Verified work order %s: %s", saved.getWorkOrderNumber(), saved.getStatus()));

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Rework handling: resets a REJECTED work order to IN_PROGRESS for rework.
     */
    @Transactional
    public WorkOrderDto reworkWorkOrder(Long id, String remarks, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        if (workOrder.getStatus() != WorkOrderStatus.REJECTED) {
            throw new BusinessException("Rework can only be initiated on REJECTED work orders. Current status: " + workOrder.getStatus(), "INVALID_STATUS");
        }

        WorkOrderStatus prevStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        workOrder.setModifiedBy(username);
        WorkOrder saved = workOrderRepository.save(workOrder);

        recordStatusHistory(saved, prevStatus, WorkOrderStatus.IN_PROGRESS,
                remarks != null ? remarks : "Rework initiated following supervisor rejection", username);

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Initiated rework for work order " + saved.getWorkOrderNumber());

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Cancels a work order.
     */
    @Transactional
    public WorkOrderDto cancelWorkOrder(Long id, String reason, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        workflowEngine.validateTransition(workOrder.getStatus(), WorkOrderStatus.CANCELLED);

        WorkOrderStatus prevStatus = workOrder.getStatus();
        workOrder.setStatus(WorkOrderStatus.CANCELLED);
        workOrder.setModifiedBy(username);
        WorkOrder saved = workOrderRepository.save(workOrder);

        recordStatusHistory(saved, prevStatus, WorkOrderStatus.CANCELLED,
                reason != null ? reason : "Work order cancelled", username);

        auditService.record("WorkOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Cancelled work order " + saved.getWorkOrderNumber());

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Adds a comment to a work order.
     */
    @Transactional
    public WorkOrderCommentDto addComment(Long id, AddWorkOrderCommentRequest request, String username) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + id));

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String role = author.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("ROLE_EMPLOYEE");

        WorkOrderComment comment = new WorkOrderComment(
                workOrder, author, author.getFullName(), role, request.getComment(), request.isInternalOnly()
        );

        WorkOrderComment saved = commentRepository.save(comment);
        return WorkOrderCommentDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderCommentDto> getComments(Long id, boolean includeInternal) {
        List<WorkOrderComment> comments = includeInternal
                ? commentRepository.findByWorkOrderIdOrderByCreatedAtAsc(id)
                : commentRepository.findByWorkOrderIdAndInternalOnlyFalseOrderByCreatedAtAsc(id);

        return comments.stream()
                .map(WorkOrderCommentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderStatusHistoryDto> getStatusHistory(Long id) {
        return statusHistoryRepository.findByWorkOrderIdOrderByCreatedAtAsc(id).stream()
                .map(WorkOrderStatusHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderAssignmentHistoryDto> getAssignmentHistory(Long id) {
        return assignmentHistoryRepository.findByWorkOrderIdOrderByAssignedAtAsc(id).stream()
                .map(WorkOrderAssignmentHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private void validateCanStart(WorkOrder workOrder) {
        if (!schedulingService.arePrerequisitesMet(workOrder.getId())) {
            throw new BusinessException(
                    "Cannot start work order: Prerequisite work orders have not been completed or verified yet",
                    "PREREQUISITES_NOT_MET"
            );
        }
    }

    private Employee validateAndGetWorker(Long workerId) {
        Employee worker = employeeRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Field worker not found with ID: " + workerId));

        if (!worker.isActive()) {
            throw new BusinessException("Assigned field worker is inactive", "EMPLOYEE_INACTIVE");
        }

        return worker;
    }

    private Employee validateAndGetSupervisor(Long supervisorId) {
        Employee supervisor = employeeRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with ID: " + supervisorId));

        if (!supervisor.isActive()) {
            throw new BusinessException("Assigned supervisor is inactive", "EMPLOYEE_INACTIVE");
        }

        return supervisor;
    }

    private void recordStatusHistory(WorkOrder workOrder, WorkOrderStatus from, WorkOrderStatus to, String remarks, String actionBy) {
        WorkOrderStatusHistory history = new WorkOrderStatusHistory(workOrder, from, to, remarks, actionBy);
        statusHistoryRepository.save(history);
    }

    private void recordAssignmentHistory(WorkOrder workOrder, Employee prevWorker, Employee newWorker,
                                         Employee prevSupervisor, Employee newSupervisor,
                                         String assignedBy, String reason) {
        WorkOrderAssignmentHistory history = new WorkOrderAssignmentHistory(
                workOrder, prevWorker, newWorker, prevSupervisor, newSupervisor, assignedBy, reason
        );
        assignmentHistoryRepository.save(history);
    }

    private WorkOrderPriority mapComplaintPriority(ComplaintPriority priority) {
        if (priority == null) {
            return WorkOrderPriority.MEDIUM;
        }
        return switch (priority) {
            case LOW -> WorkOrderPriority.LOW;
            case MEDIUM -> WorkOrderPriority.MEDIUM;
            case HIGH -> WorkOrderPriority.HIGH;
            case URGENT, CRITICAL -> WorkOrderPriority.URGENT;
        };
    }

    private synchronized String generateUniqueWorkOrderNumber() {
        String datePrefix = "WO-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        for (int i = 0; i < 10; i++) {
            int randomSeq = ThreadLocalRandom.current().nextInt(1000, 9999);
            String candidate = datePrefix + randomSeq;
            if (!workOrderRepository.existsByWorkOrderNumber(candidate)) {
                return candidate;
            }
        }
        return datePrefix + System.currentTimeMillis() % 10000;
    }
}
