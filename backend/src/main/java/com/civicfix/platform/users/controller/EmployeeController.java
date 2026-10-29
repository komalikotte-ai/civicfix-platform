package com.civicfix.platform.users.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.security.SecurityUtils;
import com.civicfix.platform.users.dto.*;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.users.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR', 'EMPLOYEE')")
    public ApiResponse<PagedResponse<EmployeeDto>> listEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long supervisorId,
            @RequestParam(required = false) RoleType role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(employeeService.getEmployees(departmentId, teamId, supervisorId, role, active, query, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR', 'EMPLOYEE')")
    public ApiResponse<EmployeeDto> getEmployee(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/{id}/subordinates")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR')")
    public ApiResponse<List<EmployeeDto>> getSubordinates(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.getSubordinates(id));
    }

    @GetMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR')")
    public ApiResponse<List<EmployeeAssignmentHistoryDto>> getAssignmentHistory(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.getAssignmentHistory(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<EmployeeDto> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Employee created successfully", employeeService.createEmployee(request, actor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR')")
    public ApiResponse<EmployeeDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Employee profile updated successfully", employeeService.updateEmployee(id, request, actor));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<EmployeeDto> changeStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Employee status updated successfully", employeeService.changeStatus(id, active, actor));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<EmployeeDto> assignDepartmentTeamSupervisor(
            @PathVariable Long id,
            @Valid @RequestBody AssignEmployeeRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Employee reassigned successfully",
                employeeService.assignDepartmentTeamSupervisor(id, request, actor));
    }
}
