package com.civicfix.platform.sla.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.sla.dto.SlaPolicyDto;
import com.civicfix.platform.sla.service.SlaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sla")
public class SlaController {

    private final SlaService slaService;

    public SlaController(SlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping("/organization/{organizationId}")
    public ApiResponse<List<SlaPolicyDto>> getPolicies(@PathVariable Long organizationId) {
        return ApiResponse.ok(slaService.getPoliciesByOrganization(organizationId));
    }
}
