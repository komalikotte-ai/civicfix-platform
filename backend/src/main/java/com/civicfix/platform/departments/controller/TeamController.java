package com.civicfix.platform.departments.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.departments.dto.CreateTeamRequest;
import com.civicfix.platform.departments.dto.TeamDto;
import com.civicfix.platform.departments.dto.UpdateTeamRequest;
import com.civicfix.platform.departments.service.TeamService;
import com.civicfix.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ApiResponse<PagedResponse<TeamDto>> listTeams(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long supervisorId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(teamService.getTeams(departmentId, supervisorId, active, query, pageable));
    }

    @GetMapping("/department/{departmentId}")
    public ApiResponse<List<TeamDto>> listByDepartment(@PathVariable Long departmentId) {
        return ApiResponse.ok(teamService.getTeamsByDepartment(departmentId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TeamDto> getTeam(@PathVariable Long id) {
        return ApiResponse.ok(teamService.getTeamById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR')")
    public ApiResponse<TeamDto> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Team created successfully", teamService.createTeam(request, actor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR')")
    public ApiResponse<TeamDto> updateTeam(@PathVariable Long id, @Valid @RequestBody UpdateTeamRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Team updated successfully", teamService.updateTeam(id, request, actor));
    }

    @PatchMapping("/{id}/supervisor")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<TeamDto> assignSupervisor(
            @PathVariable Long id,
            @RequestParam(required = false) Long supervisorId) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Team supervisor assigned successfully", teamService.assignSupervisor(id, supervisorId, actor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<Void> deleteTeam(@PathVariable Long id) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        teamService.deleteTeam(id, actor);
        return ApiResponse.ok("Team deactivated successfully", null);
    }
}
