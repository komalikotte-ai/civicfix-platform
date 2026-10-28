package com.civicfix.platform.complaints.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.common.exception.UnauthorizedException;
import com.civicfix.platform.complaints.dto.ComplaintDto;
import com.civicfix.platform.complaints.dto.CreateComplaintRequest;
import com.civicfix.platform.complaints.dto.UpdateComplaintStatusRequest;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.complaints.repository.ComplaintRepository;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.security.SecurityUtils;
import com.civicfix.platform.security.UserPrincipal;
import com.civicfix.platform.sla.service.SlaService;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.repository.UserRepository;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.wards.repository.WardRepository;
import com.civicfix.platform.workflow.service.WorkflowEngine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final WardRepository wardRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final WorkflowEngine workflowEngine;
    private final SlaService slaService;
    private final AuditService auditService;

    public ComplaintService(ComplaintRepository complaintRepository,
                            WardRepository wardRepository,
                            DepartmentRepository departmentRepository,
                            UserRepository userRepository,
                            WorkflowEngine workflowEngine,
                            SlaService slaService,
                            AuditService auditService) {
        this.complaintRepository = complaintRepository;
        this.wardRepository = wardRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.workflowEngine = workflowEngine;
        this.slaService = slaService;
        this.auditService = auditService;
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
        complaint.setPriority(request.getPriority() != null ? request.getPriority() : ComplaintPriority.MEDIUM);
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setAddressLine(request.getAddressLine());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());

        Long deptId = department != null ? department.getId() : null;
        Instant dueDate = slaService.calculateDueDate(ward.getOrganization().getId(), deptId, complaint.getPriority());
        complaint.setSlaDueAt(dueDate);

        Complaint saved = complaintRepository.save(complaint);

        auditService.record("COMPLAINT", saved.getTrackingNumber(), AuditAction.CREATE, citizen.getUsername(),
                "Complaint lodged with tracking number: " + saved.getTrackingNumber());

        return ComplaintDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ComplaintDto> getComplaints(Pageable pageable) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        Page<Complaint> page;
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            page = complaintRepository.findAll(pageable);
        } else if (SecurityUtils.hasRole("DEPT_ADMIN") && principal.getDepartmentId() != null) {
            page = complaintRepository.findByDepartmentId(principal.getDepartmentId(), pageable);
        } else if (SecurityUtils.hasRole("OFFICER")) {
            page = complaintRepository.findByAssignedOfficerId(principal.getId(), pageable);
        } else {
            page = complaintRepository.findByCitizenId(principal.getId(), pageable);
        }

        return PagedResponse.from(page.map(ComplaintDto::fromEntity));
    }

    @Transactional(readOnly = true)
    public ComplaintDto getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));
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

        // Validate state transition through workflow engine
        workflowEngine.recordTransition(complaint, request.getTargetStatus(), request.getRemarks(), actor);

        complaint.setStatus(request.getTargetStatus());

        if (request.getAssignedOfficerId() != null) {
            User officer = userRepository.findById(request.getAssignedOfficerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", request.getAssignedOfficerId()));
            complaint.setAssignedOfficer(officer);
        }

        if (request.getTargetStatus() == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(Instant.now());
        }

        Complaint saved = complaintRepository.save(complaint);

        auditService.record("COMPLAINT", saved.getTrackingNumber(), AuditAction.STATUS_CHANGE, actor.getUsername(),
                String.format("Status changed to %s. Remarks: %s", request.getTargetStatus(), request.getRemarks()));

        return ComplaintDto.fromEntity(saved);
    }

    private String generateTrackingNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("CF-%s-%s", datePrefix, randomSuffix);
    }
}
