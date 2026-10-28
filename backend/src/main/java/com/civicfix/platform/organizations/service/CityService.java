package com.civicfix.platform.organizations.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.organizations.dto.CityDto;
import com.civicfix.platform.organizations.dto.CreateCityRequest;
import com.civicfix.platform.organizations.dto.UpdateCityRequest;
import com.civicfix.platform.organizations.entity.City;
import com.civicfix.platform.organizations.repository.CityRepository;
import com.civicfix.platform.security.service.SecurityAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CityService {

    private final CityRepository cityRepository;
    private final SecurityAuditService auditService;

    public CityService(CityRepository cityRepository, SecurityAuditService auditService) {
        this.cityRepository = cityRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<CityDto> getCities(String query, Boolean active, Pageable pageable) {
        Page<CityDto> page = cityRepository.searchCities(query, active, pageable)
                .map(CityDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public List<CityDto> getAllActiveCities() {
        return cityRepository.findAllByActiveTrue().stream()
                .map(CityDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CityDto getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", id));
        return CityDto.fromEntity(city);
    }

    @Transactional
    public CityDto createCity(CreateCityRequest request, String performedBy) {
        if (cityRepository.existsByCode(request.getCode())) {
            throw new ConflictException("City code '" + request.getCode() + "' already exists");
        }
        if (cityRepository.existsByName(request.getName())) {
            throw new ConflictException("City name '" + request.getName() + "' already exists");
        }

        City city = new City();
        city.setName(request.getName());
        city.setCode(request.getCode().toUpperCase());
        city.setState(request.getState());
        if (request.getCountry() != null) city.setCountry(request.getCountry());
        if (request.getTimezone() != null) city.setTimezone(request.getTimezone());

        City saved = cityRepository.save(city);
        auditService.record("City", saved.getId().toString(), AuditAction.CREATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Created city " + saved.getName() + " (" + saved.getCode() + ")");

        return CityDto.fromEntity(saved);
    }

    @Transactional
    public CityDto updateCity(Long id, UpdateCityRequest request, String performedBy) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", id));

        if (request.getName() != null && !request.getName().equals(city.getName())) {
            if (cityRepository.existsByName(request.getName())) {
                throw new ConflictException("City name '" + request.getName() + "' already exists");
            }
            city.setName(request.getName());
        }
        if (request.getState() != null) city.setState(request.getState());
        if (request.getCountry() != null) city.setCountry(request.getCountry());
        if (request.getTimezone() != null) city.setTimezone(request.getTimezone());
        if (request.getActive() != null) city.setActive(request.getActive());

        City saved = cityRepository.save(city);
        auditService.record("City", saved.getId().toString(), AuditAction.UPDATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Updated city " + saved.getName());

        return CityDto.fromEntity(saved);
    }

    @Transactional
    public void deleteCity(Long id, String performedBy) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", id));
        city.setActive(false);
        cityRepository.save(city);
        auditService.record("City", id.toString(), AuditAction.DELETE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Deactivated city " + city.getName());
    }
}
