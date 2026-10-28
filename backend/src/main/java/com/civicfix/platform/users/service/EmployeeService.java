package com.civicfix.platform.users.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.entity.Team;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.departments.repository.TeamRepository;
import com.civicfix.platform.security.service.SecurityAuditService;
import com.civicfix.platform.users.dto.*;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.EmployeeAssignmentHistory;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.users.repository.EmployeeAssignmentHistoryRepository;
import com.civicfix.platform.users.repository.EmployeeRepository;
import com.civicfix.platform.users.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeAssignmentHistoryRepository historyRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final SecurityAuditService auditService;

    public EmployeeService(EmployeeRepository employeeRepository,
                           EmployeeAssignmentHistoryRepository historyRepository,
                           DepartmentRepository departmentRepository,
                           TeamRepository teamRepository,
                           UserRepository userRepository,
                           SecurityAuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.historyRepository = historyRepository;
        this.departmentRepository = departmentRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<EmployeeDto> getEmployees(Long departmentId, Long teamId, Long supervisorId,
                                                  RoleType role, Boolean active, String query, Pageable pageable) {
        Page<EmployeeDto> page = employeeRepository.searchEmployees(departmentId, teamId, supervisorId, role, active, query, pageable)
                .map(EmployeeDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return EmployeeDto.fromEntity(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getSubordinates(Long supervisorId) {
        return employeeRepository.findBySupervisorIdAndActiveTrue(supervisorId).stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request, String performedBy) {
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new ConflictException("Employee code '" + request.getEmployeeCode() + "' already exists");
        }
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Employee email '" + request.getEmail() + "' already exists");
        }

        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        Employee employee = new Employee();
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setDesignation(request.getDesignation());
        employee.setDepartment(dept);
        if (request.getRole() != null) employee.setRole(request.getRole());
        if (request.getJoiningDate() != null) employee.setJoiningDate(request.getJoiningDate());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
            employee.setUser(user);
        }

        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getTeamId()));
            employee.setTeam(team);
        }

        Employee supervisor = null;
        if (request.getSupervisorId() != null) {
            supervisor = employeeRepository.findById(request.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor Employee", "id", request.getSupervisorId()));
            employee.setSupervisor(supervisor);
        }

        Employee saved = employeeRepository.save(employee);

        // Record initial assignment history
        EmployeeAssignmentHistory initialHistory = new EmployeeAssignmentHistory(
                saved, null, dept, null, team, null, supervisor,
                performedBy != null ? performedBy : "SYSTEM", "Initial Department Assignment"
        );
        historyRepository.save(initialHistory);

        auditService.record("Employee", saved.getId().toString(), AuditAction.CREATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Created employee " + saved.getFullName() + " (" + saved.getEmployeeCode() + ")");

        return EmployeeDto.fromEntity(saved);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request, String performedBy) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (request.getFullName() != null) employee.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) employee.setPhoneNumber(request.getPhoneNumber());
        if (request.getDesignation() != null) employee.setDesignation(request.getDesignation());
        if (request.getRole() != null) employee.setRole(request.getRole());
        if (request.getActive() != null) employee.setActive(request.getActive());

        Employee saved = employeeRepository.save(employee);
        auditService.record("Employee", saved.getId().toString(), AuditAction.UPDATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Updated employee profile for " + saved.getFullName());

        return EmployeeDto.fromEntity(saved);
    }

    @Transactional
    public EmployeeDto changeStatus(Long id, boolean active, String performedBy) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        employee.setActive(active);
        Employee saved = employeeRepository.save(employee);

        auditService.record("Employee", saved.getId().toString(), AuditAction.STATUS_CHANGE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Employee status changed to " + (active ? "ACTIVE" : "INACTIVE"));

        return EmployeeDto.fromEntity(saved);
    }

    @Transactional
    public EmployeeDto assignDepartmentTeamSupervisor(Long id, AssignEmployeeRequest request, String performedBy) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        Department newDept = departmentRepository.findById(request.getNewDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getNewDepartmentId()));

        Team newTeam = null;
        if (request.getNewTeamId() != null) {
            newTeam = teamRepository.findById(request.getNewTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getNewTeamId()));
            if (!newTeam.getDepartment().getId().equals(newDept.getId())) {
                throw new ConflictException("Specified team does not belong to the selected department");
            }
        }

        Employee newSupervisor = null;
        if (request.getNewSupervisorId() != null) {
            if (request.getNewSupervisorId().equals(id)) {
                throw new ConflictException("An employee cannot be their own supervisor");
            }
            newSupervisor = employeeRepository.findById(request.getNewSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor Employee", "id", request.getNewSupervisorId()));
        }

        // Close previous active assignment
        historyRepository.findActiveAssignment(id).ifPresent(activeAssignment -> {
            activeAssignment.setEffectiveTo(Instant.now());
            historyRepository.save(activeAssignment);
        });

        Department prevDept = employee.getDepartment();
        Team prevTeam = employee.getTeam();
        Employee prevSupervisor = employee.getSupervisor();

        // Update employee
        employee.setDepartment(newDept);
        employee.setTeam(newTeam);
        employee.setSupervisor(newSupervisor);
        Employee saved = employeeRepository.save(employee);

        // Record new assignment history
        EmployeeAssignmentHistory newHistory = new EmployeeAssignmentHistory(
                saved, prevDept, newDept, prevTeam, newTeam, prevSupervisor, newSupervisor,
                performedBy != null ? performedBy : "SYSTEM",
                request.getReason() != null ? request.getReason() : "Department/Team/Supervisor Reassignment"
        );
        historyRepository.save(newHistory);

        auditService.record("Employee", saved.getId().toString(), AuditAction.ASSIGNMENT,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Reassigned employee " + saved.getFullName() + " to department " + newDept.getName() +
                (newTeam != null ? ", team " + newTeam.getName() : "") +
                (newSupervisor != null ? ", supervisor " + newSupervisor.getFullName() : ""));

        return EmployeeDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeAssignmentHistoryDto> getAssignmentHistory(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        return historyRepository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId).stream()
                .map(EmployeeAssignmentHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }
}
