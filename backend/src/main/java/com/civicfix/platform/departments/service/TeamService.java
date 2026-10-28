package com.civicfix.platform.departments.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.departments.dto.CreateTeamRequest;
import com.civicfix.platform.departments.dto.TeamDto;
import com.civicfix.platform.departments.dto.UpdateTeamRequest;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.entity.Team;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.departments.repository.TeamRepository;
import com.civicfix.platform.security.service.SecurityAuditService;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityAuditService auditService;

    public TeamService(TeamRepository teamRepository,
                       DepartmentRepository departmentRepository,
                       EmployeeRepository employeeRepository,
                       SecurityAuditService auditService) {
        this.teamRepository = teamRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<TeamDto> getTeams(Long departmentId, Long supervisorId, Boolean active, String query, Pageable pageable) {
        Page<TeamDto> page = teamRepository.searchTeams(departmentId, supervisorId, active, query, pageable)
                .map(TeamDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getTeamsByDepartment(Long departmentId) {
        return teamRepository.findByDepartmentIdAndActiveTrue(departmentId).stream()
                .map(TeamDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamDto getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        return TeamDto.fromEntity(team);
    }

    @Transactional
    public TeamDto createTeam(CreateTeamRequest request, String performedBy) {
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        if (teamRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Team code '" + request.getCode() + "' already exists");
        }

        Team team = new Team();
        team.setDepartment(dept);
        team.setName(request.getName());
        team.setCode(request.getCode().toUpperCase());
        team.setDescription(request.getDescription());

        if (request.getSupervisorId() != null) {
            Employee supervisor = employeeRepository.findById(request.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor Employee", "id", request.getSupervisorId()));
            team.setSupervisor(supervisor);
        }

        Team saved = teamRepository.save(team);
        auditService.record("Team", saved.getId().toString(), AuditAction.CREATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Created team " + saved.getName() + " in department " + dept.getName());

        return TeamDto.fromEntity(saved);
    }

    @Transactional
    public TeamDto updateTeam(Long id, UpdateTeamRequest request, String performedBy) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        if (request.getName() != null) team.setName(request.getName());
        if (request.getDescription() != null) team.setDescription(request.getDescription());

        if (request.getSupervisorId() != null) {
            Employee supervisor = employeeRepository.findById(request.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor Employee", "id", request.getSupervisorId()));
            team.setSupervisor(supervisor);
        }

        if (request.getActive() != null) team.setActive(request.getActive());

        Team saved = teamRepository.save(team);
        auditService.record("Team", saved.getId().toString(), AuditAction.UPDATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Updated team " + saved.getName());

        return TeamDto.fromEntity(saved);
    }

    @Transactional
    public TeamDto assignSupervisor(Long id, Long supervisorId, String performedBy) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        Employee supervisor = null;
        if (supervisorId != null) {
            supervisor = employeeRepository.findById(supervisorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor Employee", "id", supervisorId));
        }

        team.setSupervisor(supervisor);
        Team saved = teamRepository.save(team);
        auditService.record("Team", saved.getId().toString(), AuditAction.ASSIGNMENT,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Assigned supervisor " + (supervisor != null ? supervisor.getFullName() : "None") + " to team " + team.getName());

        return TeamDto.fromEntity(saved);
    }

    @Transactional
    public void deleteTeam(Long id, String performedBy) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        team.setActive(false);
        teamRepository.save(team);
        auditService.record("Team", id.toString(), AuditAction.DELETE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Deactivated team " + team.getName());
    }
}
