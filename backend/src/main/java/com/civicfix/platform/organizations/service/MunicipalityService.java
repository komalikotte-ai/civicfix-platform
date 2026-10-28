package com.civicfix.platform.organizations.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.organizations.dto.CreateMunicipalityRequest;
import com.civicfix.platform.organizations.dto.MunicipalityDto;
import com.civicfix.platform.organizations.dto.UpdateMunicipalityRequest;
import com.civicfix.platform.organizations.entity.City;
import com.civicfix.platform.organizations.entity.Municipality;
import com.civicfix.platform.organizations.enums.MunicipalityType;
import com.civicfix.platform.organizations.repository.CityRepository;
import com.civicfix.platform.organizations.repository.MunicipalityRepository;
import com.civicfix.platform.security.service.SecurityAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MunicipalityService {

    private final MunicipalityRepository municipalityRepository;
    private final CityRepository cityRepository;
    private final SecurityAuditService auditService;

    public MunicipalityService(MunicipalityRepository municipalityRepository,
                               CityRepository cityRepository,
                               SecurityAuditService auditService) {
        this.municipalityRepository = municipalityRepository;
        this.cityRepository = cityRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<MunicipalityDto> getMunicipalities(Long cityId, MunicipalityType type, Boolean active, String query, Pageable pageable) {
        Page<MunicipalityDto> page = municipalityRepository.searchMunicipalities(cityId, type, active, query, pageable)
                .map(MunicipalityDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public List<MunicipalityDto> getActiveMunicipalitiesByCity(Long cityId) {
        return municipalityRepository.findByCityIdAndActiveTrue(cityId).stream()
                .map(MunicipalityDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MunicipalityDto getMunicipalityById(Long id) {
        Municipality m = municipalityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Municipality", "id", id));
        return MunicipalityDto.fromEntity(m);
    }

    @Transactional
    public MunicipalityDto createMunicipality(CreateMunicipalityRequest request, String performedBy) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));

        if (municipalityRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Municipality code '" + request.getCode() + "' already exists");
        }
        if (municipalityRepository.existsByName(request.getName())) {
            throw new ConflictException("Municipality name '" + request.getName() + "' already exists");
        }

        Municipality m = new Municipality();
        m.setCity(city);
        m.setName(request.getName());
        m.setCode(request.getCode().toUpperCase());
        if (request.getType() != null) m.setType(request.getType());
        m.setContactEmail(request.getContactEmail());
        m.setContactPhone(request.getContactPhone());
        m.setAddress(request.getAddress());
        m.setWebsite(request.getWebsite());

        Municipality saved = municipalityRepository.save(m);
        auditService.record("Municipality", saved.getId().toString(), AuditAction.CREATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Created municipality " + saved.getName() + " (" + saved.getCode() + ")");

        return MunicipalityDto.fromEntity(saved);
    }

    @Transactional
    public MunicipalityDto updateMunicipality(Long id, UpdateMunicipalityRequest request, String performedBy) {
        Municipality m = municipalityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Municipality", "id", id));

        if (request.getName() != null && !request.getName().equals(m.getName())) {
            if (municipalityRepository.existsByName(request.getName())) {
                throw new ConflictException("Municipality name '" + request.getName() + "' already exists");
            }
            m.setName(request.getName());
        }
        if (request.getType() != null) m.setType(request.getType());
        if (request.getContactEmail() != null) m.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) m.setContactPhone(request.getContactPhone());
        if (request.getAddress() != null) m.setAddress(request.getAddress());
        if (request.getWebsite() != null) m.setWebsite(request.getWebsite());
        if (request.getActive() != null) m.setActive(request.getActive());

        Municipality saved = municipalityRepository.save(m);
        auditService.record("Municipality", saved.getId().toString(), AuditAction.UPDATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Updated municipality " + saved.getName());

        return MunicipalityDto.fromEntity(saved);
    }

    @Transactional
    public void deleteMunicipality(Long id, String performedBy) {
        Municipality m = municipalityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Municipality", "id", id));
        m.setActive(false);
        municipalityRepository.save(m);
        auditService.record("Municipality", id.toString(), AuditAction.DELETE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Deactivated municipality " + m.getName());
    }
}
