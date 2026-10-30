package com.civicfix.platform.assets.controller;

import com.civicfix.platform.assets.dto.*;
import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.assets.service.AssetService;
import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.complaints.dto.ComplaintDto;
import com.civicfix.platform.security.SecurityUtils;
import com.civicfix.platform.workorders.dto.WorkOrderDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'FIELD_WORKER', 'EMPLOYEE', 'OFFICER')")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<AssetDto> createAsset(@Valid @RequestBody CreateAssetRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Asset registered successfully", assetService.createAsset(request, username));
    }

    @GetMapping
    public ApiResponse<PagedResponse<AssetDto>> searchAssets(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AssetType assetType,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) ConditionRating conditionRating,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(assetService.searchAssets(
                keyword, assetType, status, conditionRating, departmentId, wardId, active, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<AssetDto> getAssetById(@PathVariable Long id) {
        return ApiResponse.ok(assetService.getAssetById(id));
    }

    @GetMapping("/code/{assetCode}")
    public ApiResponse<AssetDto> getAssetByCode(@PathVariable String assetCode) {
        return ApiResponse.ok(assetService.getAssetByCode(assetCode));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'SUPERVISOR')")
    public ApiResponse<AssetDto> updateAsset(@PathVariable Long id, @Valid @RequestBody UpdateAssetRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Asset updated successfully", assetService.updateAsset(id, request, username));
    }

    @PostMapping("/{id}/inspections")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'SUPERVISOR', 'FIELD_WORKER', 'OFFICER')")
    public ApiResponse<AssetInspectionDto> recordInspection(
            @PathVariable Long id,
            @Valid @RequestBody RecordInspectionRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Inspection recorded successfully", assetService.recordInspection(id, request, username));
    }

    @GetMapping("/{id}/inspections")
    public ApiResponse<List<AssetInspectionDto>> getInspections(@PathVariable Long id) {
        return ApiResponse.ok(assetService.getInspections(id));
    }

    @PostMapping("/{id}/repairs")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'SUPERVISOR', 'FIELD_WORKER')")
    public ApiResponse<AssetRepairDto> recordRepair(
            @PathVariable Long id,
            @Valid @RequestBody RecordRepairRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Repair recorded successfully", assetService.recordRepair(id, request, username));
    }

    @GetMapping("/{id}/repairs")
    public ApiResponse<List<AssetRepairDto>> getRepairs(@PathVariable Long id) {
        return ApiResponse.ok(assetService.getRepairs(id));
    }

    @PostMapping("/{id}/replacements")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<AssetReplacementDto> recordReplacement(
            @PathVariable Long id,
            @Valid @RequestBody RecordReplacementRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Replacement recorded successfully", assetService.recordReplacement(id, request, username));
    }

    @GetMapping("/{id}/replacements")
    public ApiResponse<List<AssetReplacementDto>> getReplacements(@PathVariable Long id) {
        return ApiResponse.ok(assetService.getReplacements(id));
    }

    @PostMapping("/{id}/link-complaint/{complaintId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'SUPERVISOR')")
    public ApiResponse<ComplaintDto> linkComplaint(
            @PathVariable Long id,
            @PathVariable Long complaintId) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Complaint linked to asset successfully", assetService.linkComplaintToAsset(id, complaintId, username));
    }

    @GetMapping("/{id}/complaints")
    public ApiResponse<PagedResponse<ComplaintDto>> getComplaintsForAsset(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(assetService.getComplaintsForAsset(id, pageable));
    }

    @PostMapping("/{id}/link-work-order/{workOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'SUPERVISOR')")
    public ApiResponse<WorkOrderDto> linkWorkOrder(
            @PathVariable Long id,
            @PathVariable Long workOrderId) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Work order linked to asset successfully", assetService.linkWorkOrderToAsset(id, workOrderId, username));
    }

    @GetMapping("/{id}/work-orders")
    public ApiResponse<PagedResponse<WorkOrderDto>> getWorkOrdersForAsset(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(assetService.getWorkOrdersForAsset(id, pageable));
    }
}
