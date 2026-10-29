package com.civicfix.platform.wards.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.security.SecurityUtils;
import com.civicfix.platform.wards.dto.CreateWardRequest;
import com.civicfix.platform.wards.dto.UpdateWardRequest;
import com.civicfix.platform.wards.dto.WardDto;
import com.civicfix.platform.wards.service.WardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wards")
public class WardController {

    private final WardService wardService;

    public WardController(WardService wardService) {
        this.wardService = wardService;
    }

    @GetMapping
    public ApiResponse<PagedResponse<WardDto>> listWards(
            @RequestParam(required = false) Long municipalityId,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) String zoneName,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "wardNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(wardService.searchWards(municipalityId, organizationId, zoneName, active, query, pageable));
    }

    @GetMapping("/municipality/{municipalityId}")
    public ApiResponse<List<WardDto>> listByMunicipality(@PathVariable Long municipalityId) {
        return ApiResponse.ok(wardService.getWardsByMunicipality(municipalityId));
    }

    @GetMapping("/organization/{organizationId}")
    public ApiResponse<List<WardDto>> listByOrganization(@PathVariable Long organizationId) {
        return ApiResponse.ok(wardService.getWardsByOrganization(organizationId));
    }

    @GetMapping("/{id}")
    public ApiResponse<WardDto> getWard(@PathVariable Long id) {
        return ApiResponse.ok(wardService.getWardById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPT_ADMIN')")
    public ApiResponse<WardDto> createWard(@Valid @RequestBody CreateWardRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Ward created successfully", wardService.createWard(request, actor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPT_ADMIN')")
    public ApiResponse<WardDto> updateWard(@PathVariable Long id, @Valid @RequestBody UpdateWardRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Ward updated successfully", wardService.updateWard(id, request, actor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> deleteWard(@PathVariable Long id) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        wardService.deleteWard(id, actor);
        return ApiResponse.ok("Ward deactivated successfully", null);
    }
}
