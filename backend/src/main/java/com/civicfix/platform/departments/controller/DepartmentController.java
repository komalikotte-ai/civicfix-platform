package com.civicfix.platform.departments.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.departments.dto.CreateDepartmentRequest;
import com.civicfix.platform.departments.dto.DepartmentDto;
import com.civicfix.platform.departments.dto.UpdateDepartmentRequest;
import com.civicfix.platform.departments.enums.DepartmentStatus;
import com.civicfix.platform.departments.service.DepartmentService;
import com.civicfix.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ApiResponse<PagedResponse<DepartmentDto>> listDepartments(
            @RequestParam(required = false) Long municipalityId,
            @RequestParam(required = false) DepartmentStatus status,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(departmentService.searchDepartments(municipalityId, status, active, query, pageable));
    }

    @GetMapping("/municipality/{municipalityId}")
    public ApiResponse<List<DepartmentDto>> listByMunicipality(@PathVariable Long municipalityId) {
        return ApiResponse.ok(departmentService.getDepartmentsByMunicipality(municipalityId));
    }

    @GetMapping("/organization/{organizationId}")
    public ApiResponse<List<DepartmentDto>> listByOrganization(@PathVariable Long organizationId) {
        return ApiResponse.ok(departmentService.getDepartmentsByOrganization(organizationId));
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentDto> getDepartment(@PathVariable Long id) {
        return ApiResponse.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPT_ADMIN')")
    public ApiResponse<DepartmentDto> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Department created successfully", departmentService.createDepartment(request, actor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPT_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<DepartmentDto> updateDepartment(@PathVariable Long id, @Valid @RequestBody UpdateDepartmentRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Department updated successfully", departmentService.updateDepartment(id, request, actor));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<DepartmentDto> updateDepartmentStatus(
            @PathVariable Long id,
            @RequestParam DepartmentStatus status) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Department status updated successfully", departmentService.changeStatus(id, status, actor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> deleteDepartment(@PathVariable Long id) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        departmentService.deleteDepartment(id, actor);
        return ApiResponse.ok("Department deactivated successfully", null);
    }
}
