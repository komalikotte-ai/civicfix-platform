package com.civicfix.platform.organizations.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.organizations.dto.OrganizationDto;
import com.civicfix.platform.organizations.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public ApiResponse<PagedResponse<OrganizationDto>> listOrganizations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(organizationService.getOrganizations(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrganizationDto> getOrganization(@PathVariable Long id) {
        return ApiResponse.ok(organizationService.getOrganizationById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<OrganizationDto> createOrganization(@Valid @RequestBody OrganizationDto dto) {
        return ApiResponse.created("Organization created successfully", organizationService.createOrganization(dto));
    }
}
