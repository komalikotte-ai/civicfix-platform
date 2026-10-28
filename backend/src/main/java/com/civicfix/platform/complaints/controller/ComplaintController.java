package com.civicfix.platform.complaints.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.complaints.dto.*;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.complaints.service.ComplaintDuplicateService;
import com.civicfix.platform.complaints.service.ComplaintService;
import com.civicfix.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ComplaintDuplicateService duplicateService;

    public ComplaintController(ComplaintService complaintService,
                               ComplaintDuplicateService duplicateService) {
        this.complaintService = complaintService;
        this.duplicateService = duplicateService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ComplaintDto> lodgeComplaint(@Valid @RequestBody CreateComplaintRequest request) {
        return ApiResponse.created("Complaint lodged successfully", complaintService.createComplaint(request));
    }

    @GetMapping("/categories")
    public ApiResponse<List<ComplaintCategoryDto>> getCategories() {
        return ApiResponse.ok(complaintService.getCategories());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PagedResponse<ComplaintDto>> searchComplaints(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintPriority priority,
            @RequestParam(required = false) ComplaintSeverity severity,
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long assignedEmployeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(complaintService.searchComplaints(
                category, status, priority, severity, wardId, departmentId,
                assignedEmployeeId, startDate, endDate, keyword, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ComplaintDto> getComplaint(@PathVariable Long id) {
        return ApiResponse.ok(complaintService.getComplaintById(id));
    }

    @GetMapping("/track/{trackingNumber}")
    public ApiResponse<ComplaintDto> trackComplaint(@PathVariable String trackingNumber) {
        return ApiResponse.ok(complaintService.getComplaintByTrackingNumber(trackingNumber));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'EMPLOYEE', 'FIELD_WORKER', 'OFFICER', 'OPERATOR')")
    public ApiResponse<ComplaintDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComplaintStatusRequest request) {
        return ApiResponse.ok("Complaint status updated", complaintService.updateStatus(id, request));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<ComplaintDto> assignComplaint(
            @PathVariable Long id,
            @Valid @RequestBody AssignComplaintRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Complaint assigned successfully", complaintService.assignComplaint(id, request, username));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ComplaintDto> reopenComplaint(
            @PathVariable Long id,
            @Valid @RequestBody ReopenComplaintRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Complaint reopened successfully", complaintService.reopenComplaint(id, request, username));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ComplaintCommentDto> addComment(
            @PathVariable Long id,
            @Valid @RequestBody AddComplaintCommentRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Comment added successfully", complaintService.addComment(id, request, username));
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ComplaintCommentDto>> getComments(@PathVariable Long id) {
        return ApiResponse.ok(complaintService.getComments(id));
    }

    @GetMapping("/{id}/assignments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ComplaintAssignmentHistoryDto>> getAssignmentHistory(@PathVariable Long id) {
        return ApiResponse.ok(complaintService.getAssignmentHistory(id));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ComplaintStatusHistoryDto>> getStatusHistory(@PathVariable Long id) {
        return ApiResponse.ok(complaintService.getStatusHistory(id));
    }

    // AI Duplicate Grouping Endpoints
    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'OPERATOR', 'OFFICER')")
    public ApiResponse<ComplaintDto> linkAsDuplicate(
            @PathVariable Long id,
            @Valid @RequestBody DuplicateLinkRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Complaint linked as duplicate", duplicateService.linkAsDuplicate(id, request, username));
    }

    @DeleteMapping("/{id}/duplicate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'OPERATOR')")
    public ApiResponse<ComplaintDto> unlinkDuplicate(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Complaint unlinked from duplicate group", duplicateService.unlinkDuplicate(id, username));
    }

    @GetMapping("/{id}/duplicates")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ComplaintDto>> getDuplicatesOf(@PathVariable Long id) {
        return ApiResponse.ok(duplicateService.getDuplicatesOf(id));
    }

    @GetMapping("/duplicate-group/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ComplaintDto>> getDuplicateGroup(@PathVariable String groupId) {
        return ApiResponse.ok(duplicateService.getDuplicateGroup(groupId));
    }
}
