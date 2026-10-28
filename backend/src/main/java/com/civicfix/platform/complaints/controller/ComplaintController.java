package com.civicfix.platform.complaints.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.complaints.dto.ComplaintDto;
import com.civicfix.platform.complaints.dto.CreateComplaintRequest;
import com.civicfix.platform.complaints.dto.UpdateComplaintStatusRequest;
import com.civicfix.platform.complaints.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    public ApiResponse<ComplaintDto> lodgeComplaint(@Valid @RequestBody CreateComplaintRequest request) {
        return ApiResponse.created("Complaint lodged successfully", complaintService.createComplaint(request));
    }

    @GetMapping
    public ApiResponse<PagedResponse<ComplaintDto>> listComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(complaintService.getComplaints(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ComplaintDto> getComplaint(@PathVariable Long id) {
        return ApiResponse.ok(complaintService.getComplaintById(id));
    }

    @GetMapping("/track/{trackingNumber}")
    public ApiResponse<ComplaintDto> trackComplaint(@PathVariable String trackingNumber) {
        return ApiResponse.ok(complaintService.getComplaintByTrackingNumber(trackingNumber));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN', 'OFFICER', 'OPERATOR')")
    public ApiResponse<ComplaintDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComplaintStatusRequest request) {
        return ApiResponse.ok("Complaint status updated", complaintService.updateStatus(id, request));
    }
}
