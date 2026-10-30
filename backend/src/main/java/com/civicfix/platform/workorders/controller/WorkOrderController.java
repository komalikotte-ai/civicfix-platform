package com.civicfix.platform.workorders.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ForbiddenException;
import com.civicfix.platform.security.SecurityUtils;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.repository.EmployeeRepository;
import com.civicfix.platform.workorders.dto.*;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import com.civicfix.platform.workorders.service.WorkOrderSchedulingService;
import com.civicfix.platform.workorders.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/work-orders")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'FIELD_WORKER', 'EMPLOYEE', 'OFFICER')")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final WorkOrderSchedulingService schedulingService;
    private final EmployeeRepository employeeRepository;

    public WorkOrderController(WorkOrderService workOrderService,
                               WorkOrderSchedulingService schedulingService,
                               EmployeeRepository employeeRepository) {
        this.workOrderService = workOrderService;
        this.schedulingService = schedulingService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<WorkOrderDto> createWorkOrder(@Valid @RequestBody CreateWorkOrderRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Work order created successfully", workOrderService.createWorkOrder(request, username));
    }

    @PostMapping("/from-complaint")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<WorkOrderDto> convertComplaint(@Valid @RequestBody ConvertComplaintRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Complaint successfully converted to work order", workOrderService.convertComplaint(request, username));
    }

    @GetMapping
    public ApiResponse<PagedResponse<WorkOrderDto>> searchWorkOrders(
            @RequestParam(required = false) String workOrderNumber,
            @RequestParam(required = false) Long complaintId,
            @RequestParam(required = false) String complaintTrackingNumber,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Long assignedWorkerId,
            @RequestParam(required = false) Long assignedSupervisorId,
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false) WorkOrderPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // If caller is solely a FIELD_WORKER without management rights, restrict search to their own assigned jobs
        Long effectiveWorkerId = assignedWorkerId;
        if (isOnlyFieldWorker()) {
            effectiveWorkerId = getCurrentEmployeeId();
        }

        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(workOrderService.searchWorkOrders(
                workOrderNumber, complaintId, complaintTrackingNumber, departmentId,
                wardId, effectiveWorkerId, assignedSupervisorId, status, priority,
                startDate, endDate, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkOrderDto> getWorkOrderById(@PathVariable Long id) {
        WorkOrderDto dto = workOrderService.getWorkOrderById(id);
        if (isOnlyFieldWorker()) {
            Long currentEmpId = getCurrentEmployeeId();
            if (currentEmpId != null && !currentEmpId.equals(dto.getAssignedWorkerId())) {
                throw new ForbiddenException("Field workers can only access work orders assigned to them");
            }
        }
        return ApiResponse.ok(dto);
    }

    @GetMapping("/number/{workOrderNumber}")
    public ApiResponse<WorkOrderDto> getWorkOrderByNumber(@PathVariable String workOrderNumber) {
        WorkOrderDto dto = workOrderService.getWorkOrderByNumber(workOrderNumber);
        if (isOnlyFieldWorker()) {
            Long currentEmpId = getCurrentEmployeeId();
            if (currentEmpId != null && !currentEmpId.equals(dto.getAssignedWorkerId())) {
                throw new ForbiddenException("Field workers can only access work orders assigned to them");
            }
        }
        return ApiResponse.ok(dto);
    }

    @GetMapping("/by-complaint/{complaintId}")
    public ApiResponse<List<WorkOrderDto>> getWorkOrdersByComplaintId(@PathVariable Long complaintId) {
        return ApiResponse.ok(workOrderService.getWorkOrdersByComplaintId(complaintId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<WorkOrderDto> updateWorkOrder(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateWorkOrderRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Work order updated successfully", workOrderService.updateWorkOrder(id, request, username));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<WorkOrderDto> assignWorkOrder(@PathVariable Long id,
                                                    @Valid @RequestBody AssignWorkOrderRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Work order assigned successfully", workOrderService.assignWorkOrder(id, request, username));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<WorkOrderDto> updateStatus(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateWorkOrderStatusRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Status updated successfully", workOrderService.updateStatus(id, request, username));
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<WorkOrderDto> acceptWorkOrder(@PathVariable Long id,
                                                    @RequestParam(required = false) String remarks) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Work order accepted", workOrderService.acceptWorkOrder(id, remarks, username));
    }

    @PostMapping("/{id}/start")
    public ApiResponse<WorkOrderDto> startWorkOrder(@PathVariable Long id,
                                                   @RequestParam(required = false) String remarks) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Work order started", workOrderService.startWorkOrder(id, remarks, username));
    }

    @PostMapping("/{id}/hold")
    public ApiResponse<WorkOrderDto> holdWorkOrder(@PathVariable Long id,
                                                  @RequestParam(required = false) String reason) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Work order placed on hold", workOrderService.holdWorkOrder(id, reason, username));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<WorkOrderDto> completeWorkOrder(@PathVariable Long id,
                                                      @RequestBody(required = false) CompleteWorkOrderRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Work order marked as completed", workOrderService.completeWorkOrder(id, request, username));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<WorkOrderDto> verifyWorkOrder(@PathVariable Long id,
                                                    @Valid @RequestBody VerifyWorkOrderRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Verification recorded successfully", workOrderService.verifyWorkOrder(id, request, username));
    }

    @PostMapping("/{id}/rework")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<WorkOrderDto> reworkWorkOrder(@PathVariable Long id,
                                                    @RequestParam(required = false) String remarks) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Rework initiated successfully", workOrderService.reworkWorkOrder(id, remarks, username));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<WorkOrderDto> cancelWorkOrder(@PathVariable Long id,
                                                    @RequestParam(required = false) String reason) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Work order cancelled", workOrderService.cancelWorkOrder(id, reason, username));
    }

    @PostMapping("/{id}/dependencies")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<WorkOrderDependencyDto> addDependency(@PathVariable Long id,
                                                            @Valid @RequestBody AddWorkOrderDependencyRequest request) {
        return ApiResponse.created("Dependency added successfully", schedulingService.addDependency(id, request));
    }

    @DeleteMapping("/{id}/dependencies/{dependsOnId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<Void> removeDependency(@PathVariable Long id, @PathVariable Long dependsOnId) {
        schedulingService.removeDependency(id, dependsOnId);
        return ApiResponse.ok("Dependency removed successfully", null);
    }

    @GetMapping("/{id}/dependencies")
    public ApiResponse<List<WorkOrderDependencyDto>> getDependencies(@PathVariable Long id) {
        return ApiResponse.ok(schedulingService.getDependencies(id));
    }

    @PostMapping("/{id}/comments")
    public ApiResponse<WorkOrderCommentDto> addComment(@PathVariable Long id,
                                                      @Valid @RequestBody AddWorkOrderCommentRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Comment added", workOrderService.addComment(id, request, username));
    }

    @GetMapping("/{id}/comments")
    public ApiResponse<List<WorkOrderCommentDto>> getComments(@PathVariable Long id,
                                                             @RequestParam(defaultValue = "true") boolean includeInternal) {
        return ApiResponse.ok(workOrderService.getComments(id, includeInternal));
    }

    @GetMapping("/{id}/status-history")
    public ApiResponse<List<WorkOrderStatusHistoryDto>> getStatusHistory(@PathVariable Long id) {
        return ApiResponse.ok(workOrderService.getStatusHistory(id));
    }

    @GetMapping("/{id}/assignment-history")
    public ApiResponse<List<WorkOrderAssignmentHistoryDto>> getAssignmentHistory(@PathVariable Long id) {
        return ApiResponse.ok(workOrderService.getAssignmentHistory(id));
    }

    // =========================================================================
    // Role-Checking Helpers
    // =========================================================================

    private boolean isOnlyFieldWorker() {
        return SecurityUtils.hasRole("ROLE_FIELD_WORKER") &&
                !SecurityUtils.hasRole("ROLE_ADMIN") &&
                !SecurityUtils.hasRole("ROLE_SUPER_ADMIN") &&
                !SecurityUtils.hasRole("ROLE_SUPERVISOR") &&
                !SecurityUtils.hasRole("ROLE_DEPARTMENT_MANAGER") &&
                !SecurityUtils.hasRole("ROLE_DEPT_ADMIN");
    }

    private Long getCurrentEmployeeId() {
        return SecurityUtils.getCurrentUserId()
                .flatMap(employeeRepository::findByUserId)
                .map(Employee::getId)
                .orElse(null);
    }
}
