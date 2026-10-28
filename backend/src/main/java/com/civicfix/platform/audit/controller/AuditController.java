package com.civicfix.platform.audit.controller;

import com.civicfix.platform.audit.dto.AuditLogDto;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public ApiResponse<PagedResponse<AuditLogDto>> getEntityAuditLogs(
            @PathVariable String entityName,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestBuilder.build(page, size, "timestamp", "desc");
        PagedResponse<AuditLogDto> logs = auditService.getEntityAuditLogs(entityName, entityId, pageable);
        return ApiResponse.ok(logs);
    }
}
