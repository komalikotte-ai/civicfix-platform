package com.civicfix.platform.organizations.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.organizations.dto.CreateMunicipalityRequest;
import com.civicfix.platform.organizations.dto.MunicipalityDto;
import com.civicfix.platform.organizations.dto.UpdateMunicipalityRequest;
import com.civicfix.platform.organizations.enums.MunicipalityType;
import com.civicfix.platform.organizations.service.MunicipalityService;
import com.civicfix.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/municipalities")
public class MunicipalityController {

    private final MunicipalityService municipalityService;

    public MunicipalityController(MunicipalityService municipalityService) {
        this.municipalityService = municipalityService;
    }

    @GetMapping
    public ApiResponse<PagedResponse<MunicipalityDto>> listMunicipalities(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) MunicipalityType type,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(municipalityService.getMunicipalities(cityId, type, active, query, pageable));
    }

    @GetMapping("/city/{cityId}")
    public ApiResponse<List<MunicipalityDto>> listByCity(@PathVariable Long cityId) {
        return ApiResponse.ok(municipalityService.getActiveMunicipalitiesByCity(cityId));
    }

    @GetMapping("/{id}")
    public ApiResponse<MunicipalityDto> getMunicipality(@PathVariable Long id) {
        return ApiResponse.ok(municipalityService.getMunicipalityById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<MunicipalityDto> createMunicipality(@Valid @RequestBody CreateMunicipalityRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Municipality created successfully", municipalityService.createMunicipality(request, actor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<MunicipalityDto> updateMunicipality(@PathVariable Long id, @Valid @RequestBody UpdateMunicipalityRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Municipality updated successfully", municipalityService.updateMunicipality(id, request, actor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> deleteMunicipality(@PathVariable Long id) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        municipalityService.deleteMunicipality(id, actor);
        return ApiResponse.ok("Municipality deactivated successfully", null);
    }
}
