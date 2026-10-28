package com.civicfix.platform.sla.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.security.SecurityUtils;
import com.civicfix.platform.sla.dto.*;
import com.civicfix.platform.sla.service.EscalationService;
import com.civicfix.platform.sla.service.SlaEvaluationService;
import com.civicfix.platform.sla.service.SlaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sla")
public class SlaController {

    private final SlaService slaService;
    private final EscalationService escalationService;
    private final SlaEvaluationService evaluationService;

    public SlaController(SlaService slaService,
                         EscalationService escalationService,
                         SlaEvaluationService evaluationService) {
        this.slaService = slaService;
        this.escalationService = escalationService;
        this.evaluationService = evaluationService;
    }

    // --- SLA Policies ---

    @GetMapping("/policies/organization/{organizationId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<SlaPolicyDto>> getPolicies(@PathVariable Long organizationId) {
        return ApiResponse.ok(slaService.getPoliciesByOrganization(organizationId));
    }

    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<SlaPolicyDto>> getPoliciesLegacy(@PathVariable Long organizationId) {
        return ApiResponse.ok(slaService.getPoliciesByOrganization(organizationId));
    }

    @GetMapping("/policies/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SlaPolicyDto> getPolicyById(@PathVariable Long id) {
        return ApiResponse.ok(slaService.getPolicyById(id));
    }

    @PostMapping("/policies")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<SlaPolicyDto> createPolicy(@Valid @RequestBody SlaPolicyDto dto) {
        return ApiResponse.created("SLA Policy created successfully", slaService.createPolicy(dto));
    }

    // --- Business Calendars ---

    @GetMapping("/calendars/organization/{organizationId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<BusinessCalendarDto>> getCalendars(@PathVariable Long organizationId) {
        return ApiResponse.ok(slaService.getCalendarsByOrganization(organizationId));
    }

    @PostMapping("/calendars")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<BusinessCalendarDto> createCalendar(@Valid @RequestBody CreateBusinessCalendarRequest request) {
        return ApiResponse.created("Business calendar created successfully", slaService.createCalendar(request));
    }

    // --- Business Holidays ---

    @GetMapping("/holidays/organization/{organizationId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<BusinessHolidayDto>> getHolidays(@PathVariable Long organizationId) {
        return ApiResponse.ok(slaService.getHolidaysByOrganization(organizationId));
    }

    @PostMapping("/holidays")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<BusinessHolidayDto> addHoliday(@Valid @RequestBody CreateBusinessHolidayRequest request) {
        return ApiResponse.created("Business holiday registered successfully", slaService.addHoliday(request));
    }

    // --- SLA Evaluation & Escalation ---

    @PostMapping("/evaluate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")
    public ApiResponse<SlaEvaluationResultDto> triggerEvaluation() {
        return ApiResponse.ok("SLA evaluation completed", evaluationService.evaluateAllActiveComplaints());
    }

    @PostMapping("/complaints/{complaintId}/escalate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<ComplaintEscalationHistoryDto> manualEscalate(
            @PathVariable Long complaintId,
            @Valid @RequestBody ManualEscalationRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Complaint escalated successfully",
                escalationService.manualEscalate(complaintId, request, username));
    }

    @GetMapping("/complaints/{complaintId}/escalation-history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ComplaintEscalationHistoryDto>> getEscalationHistory(@PathVariable Long complaintId) {
        return ApiResponse.ok(escalationService.getEscalationHistory(complaintId));
    }
}
