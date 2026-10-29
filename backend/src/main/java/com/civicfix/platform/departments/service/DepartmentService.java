package com.civicfix.platform.departments.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.departments.dto.CreateDepartmentRequest;
import com.civicfix.platform.departments.dto.DepartmentDto;
import com.civicfix.platform.departments.dto.UpdateDepartmentRequest;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.enums.DepartmentStatus;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.organizations.entity.Municipality;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.organizations.repository.MunicipalityRepository;
import com.civicfix.platform.organizations.repository.OrganizationRepository;
import com.civicfix.platform.security.service.SecurityAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final OrganizationRepository organizationRepository;
    private final SecurityAuditService auditService;

    public DepartmentService(DepartmentRepository departmentRepository,
                             MunicipalityRepository municipalityRepository,
                             OrganizationRepository organizationRepository,
                             SecurityAuditService auditService) {
        this.departmentRepository = departmentRepository;
        this.municipalityRepository = municipalityRepository;
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getDepartmentsByOrganization(Long organizationId) {
        return departmentRepository.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(DepartmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getDepartmentsByMunicipality(Long municipalityId) {
        return departmentRepository.findByMunicipalityIdAndActiveTrue(municipalityId).stream()
                .map(DepartmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<DepartmentDto> getDepartments(Long organizationId, Pageable pageable) {
        Page<DepartmentDto> page = departmentRepository.findByOrganizationId(organizationId, pageable)
                .map(DepartmentDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DepartmentDto> searchDepartments(Long municipalityId, DepartmentStatus status,
                                                          Boolean active, String query, Pageable pageable) {
        Page<DepartmentDto> page = departmentRepository.searchDepartments(municipalityId, status, active, query, pageable)
                .map(DepartmentDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return DepartmentDto.fromEntity(dept);
    }

    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentRequest request, String performedBy) {
        Department dept = new Department();

        if (request.getMunicipalityId() != null) {
            Municipality m = municipalityRepository.findById(request.getMunicipalityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Municipality", "id", request.getMunicipalityId()));
            if (departmentRepository.existsByMunicipalityIdAndCode(m.getId(), request.getCode())) {
                throw new ConflictException("Department code '" + request.getCode() + "' already exists in municipality");
            }
            dept.setMunicipality(m);
        }

        if (request.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
            if (departmentRepository.existsByOrganizationIdAndCode(org.getId(), request.getCode())) {
                throw new ConflictException("Department code '" + request.getCode() + "' already exists in organization");
            }
            dept.setOrganization(org);
        }

        dept.setName(request.getName());
        dept.setCode(request.getCode().toUpperCase());
        dept.setDescription(request.getDescription());
        dept.setEscalationEmail(request.getEscalationEmail());
        dept.setContactPhone(request.getContactPhone());
        if (request.getStatus() != null) dept.setStatus(request.getStatus());

        Department saved = departmentRepository.save(dept);
        auditService.record("Department", saved.getId().toString(), AuditAction.CREATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Created department " + saved.getName() + " (" + saved.getCode() + ")");

        return DepartmentDto.fromEntity(saved);
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        CreateDepartmentRequest req = new CreateDepartmentRequest();
        req.setMunicipalityId(dto.getMunicipalityId());
        req.setOrganizationId(dto.getOrganizationId());
        req.setName(dto.getName());
        req.setCode(dto.getCode());
        req.setDescription(dto.getDescription());
        req.setEscalationEmail(dto.getEscalationEmail());
        req.setContactPhone(dto.getContactPhone());
        req.setStatus(dto.getStatus() != null ? dto.getStatus() : DepartmentStatus.ACTIVE);
        return createDepartment(req, "LEGACY_API");
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, UpdateDepartmentRequest request, String performedBy) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        if (request.getName() != null) dept.setName(request.getName());
        if (request.getDescription() != null) dept.setDescription(request.getDescription());
        if (request.getEscalationEmail() != null) dept.setEscalationEmail(request.getEscalationEmail());
        if (request.getContactPhone() != null) dept.setContactPhone(request.getContactPhone());
        if (request.getStatus() != null) dept.setStatus(request.getStatus());
        if (request.getActive() != null) dept.setActive(request.getActive());

        Department saved = departmentRepository.save(dept);
        auditService.record("Department", saved.getId().toString(), AuditAction.UPDATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Updated department " + saved.getName());

        return DepartmentDto.fromEntity(saved);
    }

    @Transactional
    public DepartmentDto changeStatus(Long id, DepartmentStatus newStatus, String performedBy) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        DepartmentStatus oldStatus = dept.getStatus();
        dept.setStatus(newStatus);
        if (newStatus == DepartmentStatus.INACTIVE || newStatus == DepartmentStatus.MERGED) {
            dept.setActive(false);
        } else if (newStatus == DepartmentStatus.ACTIVE) {
            dept.setActive(true);
        }

        Department saved = departmentRepository.save(dept);
        auditService.record("Department", saved.getId().toString(), AuditAction.STATUS_CHANGE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Department status changed from " + oldStatus + " to " + newStatus);

        return DepartmentDto.fromEntity(saved);
    }

    @Transactional
    public void deleteDepartment(Long id, String performedBy) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        dept.setActive(false);
        dept.setStatus(DepartmentStatus.INACTIVE);
        departmentRepository.save(dept);
        auditService.record("Department", id.toString(), AuditAction.DELETE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Deactivated department " + dept.getName());
    }
}
