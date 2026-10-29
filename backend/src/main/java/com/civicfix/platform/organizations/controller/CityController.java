package com.civicfix.platform.organizations.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.organizations.dto.CityDto;
import com.civicfix.platform.organizations.dto.CreateCityRequest;
import com.civicfix.platform.organizations.dto.UpdateCityRequest;
import com.civicfix.platform.organizations.service.CityService;
import com.civicfix.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    public ApiResponse<PagedResponse<CityDto>> listCities(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(cityService.getCities(query, active, pageable));
    }

    @GetMapping("/active")
    public ApiResponse<List<CityDto>> listAllActiveCities() {
        return ApiResponse.ok(cityService.getAllActiveCities());
    }

    @GetMapping("/{id}")
    public ApiResponse<CityDto> getCity(@PathVariable Long id) {
        return ApiResponse.ok(cityService.getCityById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<CityDto> createCity(@Valid @RequestBody CreateCityRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("City created successfully", cityService.createCity(request, actor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<CityDto> updateCity(@PathVariable Long id, @Valid @RequestBody UpdateCityRequest request) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("City updated successfully", cityService.updateCity(id, request, actor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> deleteCity(@PathVariable Long id) {
        String actor = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        cityService.deleteCity(id, actor);
        return ApiResponse.ok("City deactivated successfully", null);
    }
}
