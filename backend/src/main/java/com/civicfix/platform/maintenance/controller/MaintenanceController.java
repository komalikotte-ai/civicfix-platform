package com.civicfix.platform.maintenance.controller;

import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.maintenance.dto.*;
import com.civicfix.platform.maintenance.enums.PlanType;
import com.civicfix.platform.maintenance.enums.ScheduleStatus;
import com.civicfix.platform.maintenance.service.PreventiveMaintenanceService;
import com.civicfix.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/maintenance")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'FIELD_WORKER', 'EMPLOYEE')")
public class MaintenanceController {

    private final PreventiveMaintenanceService maintenanceService;

    public MaintenanceController(PreventiveMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    // =========================================================================
    // 1. INSPECTION PLANS
    // =========================================================================

    @PostMapping("/inspection-plans")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<InspectionPlanDto> createInspectionPlan(@Valid @RequestBody CreateInspectionPlanRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Inspection plan created successfully",
                maintenanceService.createInspectionPlan(request, username));
    }

    @GetMapping("/inspection-plans/{id}")
    public ApiResponse<InspectionPlanDto> getInspectionPlanById(@PathVariable Long id) {
        return ApiResponse.ok(maintenanceService.getInspectionPlanById(id));
    }

    @GetMapping("/inspection-plans")
    public ApiResponse<PagedResponse<InspectionPlanDto>> searchInspectionPlans(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) PlanType planType,
            @RequestParam(required = false) AssetType targetAssetType,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(maintenanceService.searchInspectionPlans(keyword, departmentId, planType, targetAssetType, active, pageable));
    }

    // =========================================================================
    // 2. MAINTENANCE SCHEDULES
    // =========================================================================

    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'SUPERVISOR')")
    public ApiResponse<MaintenanceScheduleDto> createMaintenanceSchedule(@Valid @RequestBody CreateMaintenanceScheduleRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Maintenance schedule created successfully",
                maintenanceService.createMaintenanceSchedule(request, username));
    }

    @GetMapping("/schedules/{id}")
    public ApiResponse<MaintenanceScheduleDto> getMaintenanceScheduleById(@PathVariable Long id) {
        return ApiResponse.ok(maintenanceService.getMaintenanceScheduleById(id));
    }

    @GetMapping("/schedules")
    public ApiResponse<PagedResponse<MaintenanceScheduleDto>> searchMaintenanceSchedules(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) ScheduleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nextDueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(maintenanceService.searchMaintenanceSchedules(keyword, assetId, departmentId, status, pageable));
    }

    @PostMapping("/schedules/{id}/pause")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR')")
    public ApiResponse<MaintenanceScheduleDto> pauseMaintenanceSchedule(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(maintenanceService.pauseMaintenanceSchedule(id, username));
    }

    @PostMapping("/schedules/{id}/resume")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR')")
    public ApiResponse<MaintenanceScheduleDto> resumeMaintenanceSchedule(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(maintenanceService.resumeMaintenanceSchedule(id, username));
    }

    @PostMapping("/schedules/trigger-due-jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<Map<String, Object>> triggerDueMaintenanceJobs() {
        int generatedCount = maintenanceService.generateDueMaintenanceWorkOrders();
        return ApiResponse.ok(Map.of(
                "message", "Preventive maintenance job triggered successfully",
                "workOrdersGenerated", generatedCount
        ));
    }

    // =========================================================================
    // 3. CONDITION ASSESSMENTS
    // =========================================================================

    @PostMapping("/condition-assessments")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR', 'FIELD_WORKER')")
    public ApiResponse<ConditionAssessmentDto> recordConditionAssessment(@Valid @RequestBody CreateConditionAssessmentRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Condition assessment recorded successfully",
                maintenanceService.recordConditionAssessment(request, username));
    }

    @GetMapping("/condition-assessments/{id}")
    public ApiResponse<ConditionAssessmentDto> getConditionAssessmentById(@PathVariable Long id) {
        return ApiResponse.ok(maintenanceService.getConditionAssessmentById(id));
    }

    @GetMapping("/condition-assessments/asset/{assetId}")
    public ApiResponse<List<ConditionAssessmentDto>> getConditionAssessmentsForAsset(@PathVariable Long assetId) {
        return ApiResponse.ok(maintenanceService.getConditionAssessmentsForAsset(assetId));
    }

    // =========================================================================
    // 4. REPAIR RECOMMENDATIONS
    // =========================================================================

    @PostMapping("/recommendations")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR', 'FIELD_WORKER')")
    public ApiResponse<RepairRecommendationDto> createRepairRecommendation(@Valid @RequestBody CreateRepairRecommendationRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Repair recommendation submitted successfully",
                maintenanceService.createRepairRecommendation(request, username));
    }

    @GetMapping("/recommendations/{id}")
    public ApiResponse<RepairRecommendationDto> getRepairRecommendationById(@PathVariable Long id) {
        return ApiResponse.ok(maintenanceService.getRepairRecommendationById(id));
    }

    @GetMapping("/recommendations/asset/{assetId}")
    public ApiResponse<List<RepairRecommendationDto>> getRecommendationsForAsset(@PathVariable Long assetId) {
        return ApiResponse.ok(maintenanceService.getRecommendationsForAsset(assetId));
    }

    @PostMapping("/recommendations/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<RepairRecommendationDto> approveRepairRecommendation(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(maintenanceService.approveRepairRecommendation(id, username));
    }

    @PostMapping("/recommendations/{id}/convert-to-work-order")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR')")
    public ApiResponse<RepairRecommendationDto> convertRecommendationToWorkOrder(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(maintenanceService.convertRecommendationToWorkOrder(id, username));
    }

    // =========================================================================
    // 5. ASSET FAILURE LOGS
    // =========================================================================

    @PostMapping("/failure-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR', 'FIELD_WORKER')")
    public ApiResponse<AssetFailureLogDto> logAssetFailure(@Valid @RequestBody CreateAssetFailureLogRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Asset failure logged successfully",
                maintenanceService.logAssetFailure(request, username));
    }

    @GetMapping("/failure-logs/asset/{assetId}")
    public ApiResponse<List<AssetFailureLogDto>> getAssetFailureLogsForAsset(@PathVariable Long assetId) {
        return ApiResponse.ok(maintenanceService.getAssetFailureLogsForAsset(assetId));
    }

    // =========================================================================
    // 6. MAINTENANCE REMINDERS
    // =========================================================================

    @GetMapping("/reminders/my")
    public ApiResponse<PagedResponse<MaintenanceReminderDto>> getMyReminders(
            @RequestParam Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(maintenanceService.getRemindersForUser(employeeId, pageable));
    }

    @PostMapping("/reminders/{id}/acknowledge")
    public ApiResponse<MaintenanceReminderDto> acknowledgeReminder(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(maintenanceService.acknowledgeReminder(id, username));
    }
}
