package com.civicfix.platform.complaints.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ForbiddenException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.common.exception.UnauthorizedException;
import com.civicfix.platform.complaints.dto.*;
import com.civicfix.platform.complaints.entity.*;
import com.civicfix.platform.complaints.enums.*;
import com.civicfix.platform.complaints.event.ComplaintCreatedEvent;
import com.civicfix.platform.complaints.repository.*;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.security.SecurityUtils;
import com.civicfix.platform.security.UserPrincipal;
import com.civicfix.platform.sla.enums.EscalationLevel;
import com.civicfix.platform.sla.enums.SlaState;
import com.civicfix.platform.sla.service.SlaService;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.repository.EmployeeRepository;
import com.civicfix.platform.users.repository.UserRepository;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.wards.repository.WardRepository;
import com.civicfix.platform.workflow.service.WorkflowEngine;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final WardRepository wardRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ComplaintAttachmentRepository attachmentRepository;
    private final ComplaintCommentRepository commentRepository;
    private final ComplaintAssignmentHistoryRepository assignmentHistoryRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final WorkflowEngine workflowEngine;
    private final SlaService slaService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public ComplaintService(ComplaintRepository complaintRepository,
                            WardRepository wardRepository,
                            DepartmentRepository departmentRepository,
                            UserRepository userRepository,
                            EmployeeRepository employeeRepository,
                            ComplaintAttachmentRepository attachmentRepository,
                            ComplaintCommentRepository commentRepository,
                            ComplaintAssignmentHistoryRepository assignmentHistoryRepository,
                            ComplaintStatusHistoryRepository statusHistoryRepository,
                            WorkflowEngine workflowEngine,
                            SlaService slaService,
                            AuditService auditService,
                            ApplicationEventPublisher eventPublisher) {
        this.complaintRepository = complaintRepository;
        this.wardRepository = wardRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.attachmentRepository = attachmentRepository;
        this.commentRepository = commentRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.workflowEngine = workflowEngine;
        this.slaService = slaService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ComplaintDto createComplaint(CreateComplaintRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("User must be authenticated to lodge a complaint"));

        User citizen = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward", "id", request.getWardId()));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
        } else if (request.getCategory() != null) {
            try {
                ComplaintCategory catEnum = ComplaintCategory.valueOf(request.getCategory().toUpperCase().replace(" ", "_"));
                department = departmentRepository.findByCode(catEnum.getDefaultDepartmentCode()).orElse(null);
            } catch (IllegalArgumentException ignored) {
                // Not standard category enum, left null for routing
            }
        }

        ComplaintPriority priority = request.getPriority();
        if (priority == null) {
            try {
                ComplaintCategory catEnum = ComplaintCategory.valueOf(request.getCategory().toUpperCase().replace(" ", "_"));
                priority = catEnum.getDefaultPriority();
            } catch (IllegalArgumentException ignored) {
                priority = ComplaintPriority.MEDIUM;
            }
        }

        Complaint complaint = new Complaint();
        complaint.setTrackingNumber(generateTrackingNumber());
        complaint.setOrganization(ward.getOrganization());
        complaint.setWard(ward);
        complaint.setDepartment(department);
        complaint.setCitizen(citizen);
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setCategory(request.getCategory());
        complaint.setPriority(priority);
        complaint.setSeverity(request.getSeverity() != null ? request.getSeverity() : ComplaintSeverity.MEDIUM);
        complaint.setStatus(ComplaintStatus.NEW);
        complaint.setSource(request.getSource() != null ? request.getSource() : ComplaintSource.CITIZEN_PORTAL);
        complaint.setAddressLine(request.getAddressLine());
        complaint.setLandmark(request.getLandmark());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());

        Long deptId = department != null ? department.getId() : null;
        Instant now = Instant.now();
        Instant dueDate = slaService.calculateDueDate(
                ward.getOrganization().getId(), deptId, complaint.getCategory(),
                complaint.getPriority(), complaint.getSeverity(), now
        );
        complaint.setSlaDueAt(dueDate);

        Instant warningDate = slaService.calculateWarningDate(
                ward.getOrganization().getId(), deptId, complaint.getCategory(),
                complaint.getPriority(), complaint.getSeverity(), now
        );
        complaint.setSlaWarningAt(warningDate);
        complaint.setSlaState(SlaState.NOT_STARTED);
        complaint.setEscalationLevel(EscalationLevel.LEVEL_0);

        // Add attachments if provided
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (CreateComplaintRequest.AttachmentItem item : request.getAttachments()) {
                ComplaintAttachment attachment = new ComplaintAttachment(
                        complaint,
                        item.getFileName(),
                        item.getFileUrl(),
                        item.getFileType(),
                        item.getFileSize(),
                        citizen.getUsername()
                );
                complaint.getAttachments().add(attachment);
            }
        }

        Complaint saved = complaintRepository.save(complaint);

        // Initial status history
        ComplaintStatusHistory initialStatusHistory = new ComplaintStatusHistory(
                saved,
                null,
                ComplaintStatus.NEW,
                citizen,
                citizen.getUsername(),
                "Complaint lodged"
        );
        statusHistoryRepository.save(initialStatusHistory);

        auditService.record("COMPLAINT", saved.getTrackingNumber(), AuditAction.CREATE, citizen.getUsername(),
                "Complaint lodged with tracking number: " + saved.getTrackingNumber());

        // Publish event for downstream AI duplicate analysis
        eventPublisher.publishEvent(new ComplaintCreatedEvent(this, saved));

        return ComplaintDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintDto> getComplaints(Pageable pageable) {
        return searchComplaints(null, null, null, null, null, null, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintDto> searchComplaints(
            String category,
            ComplaintStatus status,
            ComplaintPriority priority,
            ComplaintSeverity severity,
            Long wardId,
            Long departmentId,
            Long assignedEmployeeId,
            Instant startDate,
            Instant endDate,
            String keyword,
            Pageable pageable) {

        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        Long effectiveCitizenId = null;
        Long effectiveDeptId = departmentId;
        Long effectiveEmpId = assignedEmployeeId;

        if (SecurityUtils.hasRole("ADMIN") || SecurityUtils.hasRole("SUPER_ADMIN")) {
            // Admin can search all
        } else if (SecurityUtils.hasRole("SUPERVISOR") || SecurityUtils.hasRole("DEPARTMENT_MANAGER") || SecurityUtils.hasRole("DEPT_ADMIN")) {
            if (principal.getDepartmentId() != null) {
                effectiveDeptId = principal.getDepartmentId();
            }
        } else if (SecurityUtils.hasRole("EMPLOYEE") || SecurityUtils.hasRole("FIELD_WORKER") || SecurityUtils.hasRole("OFFICER")) {
            Employee emp = employeeRepository.findByUserId(principal.getId()).orElse(null);
            if (emp != null) {
                effectiveEmpId = emp.getId();
            }
        } else {
            // Citizen can only access own complaints
            effectiveCitizenId = principal.getId();
        }

        Page<Complaint> page = complaintRepository.searchComplaints(
                effectiveCitizenId,
                effectiveDeptId,
                wardId,
                effectiveEmpId,
                category,
                status,
                priority,
                severity,
                startDate,
                endDate,
                keyword,
                pageable
        );

        return PagedResponse.from(page.map(ComplaintDto::fromEntity));
    }

    @Transactional(readOnly = true)
    public ComplaintDto getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        if (SecurityUtils.hasRole("ADMIN") || SecurityUtils.hasRole("SUPER_ADMIN")) {
            return ComplaintDto.fromEntity(complaint);
        }

        if (SecurityUtils.hasRole("SUPERVISOR") || SecurityUtils.hasRole("DEPARTMENT_MANAGER") || SecurityUtils.hasRole("DEPT_ADMIN")) {
            if (principal.getDepartmentId() != null && complaint.getDepartment() != null
                    && !principal.getDepartmentId().equals(complaint.getDepartment().getId())) {
                throw new ForbiddenException("Access denied to complaint outside supervisor department");
            }
            return ComplaintDto.fromEntity(complaint);
        }

        if (SecurityUtils.hasRole("EMPLOYEE") || SecurityUtils.hasRole("FIELD_WORKER") || SecurityUtils.hasRole("OFFICER")) {
            boolean isAssignedEmployee = complaint.getAssignedEmployee() != null &&
                    complaint.getAssignedEmployee().getUser() != null &&
                    complaint.getAssignedEmployee().getUser().getId().equals(principal.getId());
            boolean isAssignedOfficer = complaint.getAssignedOfficer() != null &&
                    complaint.getAssignedOfficer().getId().equals(principal.getId());
            boolean isSameDept = principal.getDepartmentId() != null && complaint.getDepartment() != null
                    && principal.getDepartmentId().equals(complaint.getDepartment().getId());

            if (!isAssignedEmployee && !isAssignedOfficer && !isSameDept) {
                throw new ForbiddenException("Access denied to complaint not assigned to you");
            }
            return ComplaintDto.fromEntity(complaint);
        }

        // Citizen check
        if (!complaint.getCitizen().getId().equals(principal.getId())) {
            throw new ForbiddenException("You can only view your own complaints");
        }

        return ComplaintDto.fromEntity(complaint);
    }

    @Transactional(readOnly = true)
    public ComplaintDto getComplaintByTrackingNumber(String trackingNumber) {
        Complaint complaint = complaintRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "trackingNumber", trackingNumber));
        return ComplaintDto.fromEntity(complaint);
    }

    @Transactional
    public ComplaintDto updateStatus(Long id, UpdateComplaintStatusRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        User actor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        validateStatusUpdatePermission(complaint, principal);

        ComplaintStatus fromStatus = complaint.getStatus();
        ComplaintStatus toStatus = request.getTargetStatus();

        // Validate state transition through workflow engine
        workflowEngine.recordTransition(complaint, toStatus, request.getRemarks(), actor);

        complaint.setStatus(toStatus);

        if (request.getAssignedOfficerId() != null) {
            User officer = userRepository.findById(request.getAssignedOfficerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", request.getAssignedOfficerId()));
            complaint.setAssignedOfficer(officer);
        }

        if (toStatus == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(Instant.now());
        }

        Complaint saved = complaintRepository.save(complaint);

        ComplaintStatusHistory statusHistory = new ComplaintStatusHistory(
                saved,
                fromStatus,
                toStatus,
                actor,
                actor.getUsername(),
                request.getRemarks()
        );
        statusHistoryRepository.save(statusHistory);

        auditService.record("COMPLAINT", saved.getTrackingNumber(), AuditAction.STATUS_CHANGE, actor.getUsername(),
                String.format("Status changed from %s to %s. Remarks: %s", fromStatus, toStatus, request.getRemarks()));

        return ComplaintDto.fromEntity(saved);
    }

    @Transactional
    public ComplaintDto assignComplaint(Long id, AssignComplaintRequest request, String performedBy) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        validateAssignmentPermission(principal);

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        User actor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Department prevDept = complaint.getDepartment();
        Employee prevAssignee = complaint.getAssignedEmployee();
        Employee prevSupervisor = complaint.getAssignedSupervisor();

        if (request.getDepartmentId() != null) {
            Department newDept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            complaint.setDepartment(newDept);
        }

        if (request.getAssignedEmployeeId() != null) {
            Employee newAssignee = employeeRepository.findById(request.getAssignedEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getAssignedEmployeeId()));
            complaint.setAssignedEmployee(newAssignee);
            complaint.setAssignedAt(Instant.now());
            if (newAssignee.getUser() != null) {
                complaint.setAssignedOfficer(newAssignee.getUser());
            }
        }

        if (request.getAssignedSupervisorId() != null) {
            Employee newSupervisor = employeeRepository.findById(request.getAssignedSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor", "id", request.getAssignedSupervisorId()));
            complaint.setAssignedSupervisor(newSupervisor);
        }

        // Automatic transition to ASSIGNED if current status is NEW / SUBMITTED / ACKNOWLEDGED
        if (complaint.getStatus() == ComplaintStatus.NEW ||
                complaint.getStatus() == ComplaintStatus.SUBMITTED ||
                complaint.getStatus() == ComplaintStatus.ACKNOWLEDGED) {
            ComplaintStatus oldStatus = complaint.getStatus();
            workflowEngine.recordTransition(complaint, ComplaintStatus.ASSIGNED, "Assigned: " + request.getReason(), actor);
            complaint.setStatus(ComplaintStatus.ASSIGNED);
            statusHistoryRepository.save(new ComplaintStatusHistory(
                    complaint, oldStatus, ComplaintStatus.ASSIGNED, actor, actor.getUsername(), "Auto-transitioned on assignment: " + request.getReason()
            ));
        }

        Complaint saved = complaintRepository.save(complaint);

        ComplaintAssignmentHistory assignmentHistory = new ComplaintAssignmentHistory(
                saved,
                prevDept,
                saved.getDepartment(),
                prevAssignee,
                saved.getAssignedEmployee(),
                prevSupervisor,
                saved.getAssignedSupervisor(),
                performedBy,
                request.getReason()
        );
        assignmentHistoryRepository.save(assignmentHistory);

        auditService.record("COMPLAINT", saved.getTrackingNumber(), AuditAction.UPDATE, performedBy,
                String.format("Complaint assigned/reassigned. Department: %s, Employee: %s, Supervisor: %s. Reason: %s",
                        saved.getDepartment() != null ? saved.getDepartment().getName() : "None",
                        saved.getAssignedEmployee() != null ? saved.getAssignedEmployee().getFullName() : "None",
                        saved.getAssignedSupervisor() != null ? saved.getAssignedSupervisor().getFullName() : "None",
                        request.getReason()));

        return ComplaintDto.fromEntity(saved);
    }

    @Transactional
    public ComplaintDto reopenComplaint(Long id, ReopenComplaintRequest request, String performedBy) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        User actor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        if (!isAdminOrStaff(principal) && !complaint.getCitizen().getId().equals(principal.getId())) {
            throw new ForbiddenException("You can only reopen your own complaints");
        }

        ComplaintStatus fromStatus = complaint.getStatus();
        workflowEngine.recordTransition(complaint, ComplaintStatus.REOPENED, request.getReason(), actor);

        complaint.setStatus(ComplaintStatus.REOPENED);
        complaint.setReopenedCount(complaint.getReopenedCount() + 1);
        complaint.setLastReopenedAt(Instant.now());
        complaint.setResolvedAt(null);

        Complaint saved = complaintRepository.save(complaint);

        ComplaintStatusHistory history = new ComplaintStatusHistory(
                saved,
                fromStatus,
                ComplaintStatus.REOPENED,
                actor,
                actor.getUsername(),
                "Reopened: " + request.getReason()
        );
        statusHistoryRepository.save(history);

        ComplaintComment comment = new ComplaintComment(
                saved,
                actor,
                actor.getFullName(),
                principal.getRoles().stream().findFirst().orElse("CITIZEN"),
                "Complaint reopened: " + request.getReason(),
                false
        );
        commentRepository.save(comment);

        auditService.record("COMPLAINT", saved.getTrackingNumber(), AuditAction.STATUS_CHANGE, performedBy,
                "Complaint reopened. Reason: " + request.getReason());

        return ComplaintDto.fromEntity(saved);
    }

    @Transactional
    public ComplaintCommentDto addComment(Long id, AddComplaintCommentRequest request, String username) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        boolean isStaff = isAdminOrStaff(principal);

        if (!isStaff) {
            if (!complaint.getCitizen().getId().equals(principal.getId())) {
                throw new ForbiddenException("You can only comment on your own complaints");
            }
            if (request.isInternalOnly()) {
                throw new ForbiddenException("Citizens cannot post internal-only notes");
            }
        }

        String authorRole = isStaff ?
                principal.getRoles().stream().findFirst().orElse("STAFF") : "CITIZEN";

        ComplaintComment comment = new ComplaintComment(
                complaint,
                author,
                author.getFullName(),
                authorRole,
                request.getComment(),
                request.isInternalOnly()
        );

        ComplaintComment saved = commentRepository.save(comment);

        auditService.record("COMPLAINT", complaint.getTrackingNumber(), AuditAction.UPDATE, username,
                String.format("Comment added (%s)", request.isInternalOnly() ? "Internal" : "Public"));

        return ComplaintCommentDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ComplaintCommentDto> getComments(Long id) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        boolean isStaff = isAdminOrStaff(principal);
        if (!isStaff && !complaint.getCitizen().getId().equals(principal.getId())) {
            throw new ForbiddenException("Access denied to complaint comments");
        }

        List<ComplaintComment> comments;
        if (isStaff) {
            comments = commentRepository.findByComplaintIdOrderByCreatedAtAsc(id);
        } else {
            comments = commentRepository.findByComplaintIdAndInternalOnlyFalseOrderByCreatedAtAsc(id);
        }

        return comments.stream()
                .map(ComplaintCommentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintAssignmentHistoryDto> getAssignmentHistory(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        if (!isAdminOrStaff(principal) && !complaint.getCitizen().getId().equals(principal.getId())) {
            throw new ForbiddenException("Access denied to assignment history");
        }

        return assignmentHistoryRepository.findByComplaintIdOrderByAssignedAtDesc(id).stream()
                .map(ComplaintAssignmentHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintStatusHistoryDto> getStatusHistory(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        if (!isAdminOrStaff(principal) && !complaint.getCitizen().getId().equals(principal.getId())) {
            throw new ForbiddenException("Access denied to status history");
        }

        return statusHistoryRepository.findByComplaintIdOrderByTransitionTimeAsc(id).stream()
                .map(ComplaintStatusHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ComplaintCategoryDto> getCategories() {
        return Arrays.stream(ComplaintCategory.values())
                .map(ComplaintCategoryDto::fromEnum)
                .collect(Collectors.toList());
    }

    private boolean isAdminOrStaff(UserPrincipal principal) {
        return SecurityUtils.hasRole("ADMIN") ||
                SecurityUtils.hasRole("SUPER_ADMIN") ||
                SecurityUtils.hasRole("SUPERVISOR") ||
                SecurityUtils.hasRole("DEPARTMENT_MANAGER") ||
                SecurityUtils.hasRole("DEPT_ADMIN") ||
                SecurityUtils.hasRole("EMPLOYEE") ||
                SecurityUtils.hasRole("FIELD_WORKER") ||
                SecurityUtils.hasRole("OFFICER") ||
                SecurityUtils.hasRole("OPERATOR");
    }

    private void validateAssignmentPermission(UserPrincipal principal) {
        boolean canAssign = SecurityUtils.hasRole("ADMIN") ||
                SecurityUtils.hasRole("SUPER_ADMIN") ||
                SecurityUtils.hasRole("SUPERVISOR") ||
                SecurityUtils.hasRole("DEPARTMENT_MANAGER") ||
                SecurityUtils.hasRole("DEPT_ADMIN");
        if (!canAssign) {
            throw new ForbiddenException("Only administrators and department supervisors can assign complaints");
        }
    }

    private void validateStatusUpdatePermission(Complaint complaint, UserPrincipal principal) {
        if (!isAdminOrStaff(principal)) {
            throw new ForbiddenException("Citizens cannot manually update complaint status; use the reopen endpoint to request reopening.");
        }

        if (SecurityUtils.hasRole("ADMIN") || SecurityUtils.hasRole("SUPER_ADMIN")) {
            return;
        }

        if (SecurityUtils.hasRole("SUPERVISOR") || SecurityUtils.hasRole("DEPARTMENT_MANAGER") || SecurityUtils.hasRole("DEPT_ADMIN")) {
            if (principal.getDepartmentId() != null && complaint.getDepartment() != null
                    && !principal.getDepartmentId().equals(complaint.getDepartment().getId())) {
                throw new ForbiddenException("Supervisors can only update complaints within their department");
            }
            return;
        }

        if (SecurityUtils.hasRole("EMPLOYEE") || SecurityUtils.hasRole("FIELD_WORKER") || SecurityUtils.hasRole("OFFICER")) {
            boolean isAssignedEmployee = complaint.getAssignedEmployee() != null &&
                    complaint.getAssignedEmployee().getUser() != null &&
                    complaint.getAssignedEmployee().getUser().getId().equals(principal.getId());
            boolean isAssignedOfficer = complaint.getAssignedOfficer() != null &&
                    complaint.getAssignedOfficer().getId().equals(principal.getId());

            if (!isAssignedEmployee && !isAssignedOfficer) {
                throw new ForbiddenException("Employees can only update complaints assigned to them");
            }
        }
    }

    private String generateTrackingNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("CF-%s-%s", datePrefix, randomSuffix);
    }
}
