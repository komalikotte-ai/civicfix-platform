package com.civicfix.platform.wards.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.organizations.entity.Municipality;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.organizations.repository.MunicipalityRepository;
import com.civicfix.platform.organizations.repository.OrganizationRepository;
import com.civicfix.platform.security.service.SecurityAuditService;
import com.civicfix.platform.wards.dto.CreateWardRequest;
import com.civicfix.platform.wards.dto.UpdateWardRequest;
import com.civicfix.platform.wards.dto.WardDto;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.wards.repository.WardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WardService {

    private final WardRepository wardRepository;
    private final MunicipalityRepository municipalityRepository;
    private final OrganizationRepository organizationRepository;
    private final SecurityAuditService auditService;

    public WardService(WardRepository wardRepository,
                       MunicipalityRepository municipalityRepository,
                       OrganizationRepository organizationRepository,
                       SecurityAuditService auditService) {
        this.wardRepository = wardRepository;
        this.municipalityRepository = municipalityRepository;
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<WardDto> getWardsByOrganization(Long organizationId) {
        return wardRepository.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(WardDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WardDto> getWardsByMunicipality(Long municipalityId) {
        return wardRepository.findByMunicipalityIdAndActiveTrue(municipalityId).stream()
                .map(WardDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<WardDto> getWards(Long organizationId, Pageable pageable) {
        Page<WardDto> page = wardRepository.findByOrganizationId(organizationId, pageable)
                .map(WardDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WardDto> searchWards(Long municipalityId, Long organizationId, String zoneName,
                                             Boolean active, String query, Pageable pageable) {
        Page<WardDto> page = wardRepository.searchWards(municipalityId, organizationId, zoneName, active, query, pageable)
                .map(WardDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public WardDto getWardById(Long id) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward", "id", id));
        return WardDto.fromEntity(ward);
    }

    @Transactional
    public WardDto createWard(CreateWardRequest request, String performedBy) {
        Ward ward = new Ward();

        if (request.getMunicipalityId() != null) {
            Municipality m = municipalityRepository.findById(request.getMunicipalityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Municipality", "id", request.getMunicipalityId()));
            if (wardRepository.existsByMunicipalityIdAndWardNumber(m.getId(), request.getWardNumber())) {
                throw new ConflictException("Ward number '" + request.getWardNumber() + "' already exists in municipality");
            }
            ward.setMunicipality(m);
        }

        if (request.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
            if (wardRepository.existsByOrganizationIdAndWardNumber(org.getId(), request.getWardNumber())) {
                throw new ConflictException("Ward number '" + request.getWardNumber() + "' already exists in organization");
            }
            ward.setOrganization(org);
        }

        ward.setWardNumber(request.getWardNumber());
        ward.setName(request.getName());
        ward.setZoneName(request.getZoneName());
        ward.setBoundaryGeoJson(request.getBoundaryGeoJson());
        ward.setCenterLatitude(request.getCenterLatitude());
        ward.setCenterLongitude(request.getCenterLongitude());
        ward.setAreaSqKm(request.getAreaSqKm());
        ward.setPopulation(request.getPopulation());

        Ward saved = wardRepository.save(ward);
        auditService.record("Ward", saved.getId().toString(), AuditAction.CREATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Created ward " + saved.getName() + " (" + saved.getWardNumber() + ")");

        return WardDto.fromEntity(saved);
    }

    @Transactional
    public WardDto createWard(WardDto dto) {
        CreateWardRequest req = new CreateWardRequest();
        req.setMunicipalityId(dto.getMunicipalityId());
        req.setOrganizationId(dto.getOrganizationId());
        req.setWardNumber(dto.getWardNumber());
        req.setName(dto.getName());
        req.setZoneName(dto.getZoneName());
        req.setBoundaryGeoJson(dto.getBoundaryGeoJson());
        req.setCenterLatitude(dto.getCenterLatitude());
        req.setCenterLongitude(dto.getCenterLongitude());
        req.setAreaSqKm(dto.getAreaSqKm());
        req.setPopulation(dto.getPopulation());
        return createWard(req, "LEGACY_API");
    }

    @Transactional
    public WardDto updateWard(Long id, UpdateWardRequest request, String performedBy) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward", "id", id));

        if (request.getName() != null) ward.setName(request.getName());
        if (request.getZoneName() != null) ward.setZoneName(request.getZoneName());
        if (request.getBoundaryGeoJson() != null) ward.setBoundaryGeoJson(request.getBoundaryGeoJson());
        if (request.getCenterLatitude() != null) ward.setCenterLatitude(request.getCenterLatitude());
        if (request.getCenterLongitude() != null) ward.setCenterLongitude(request.getCenterLongitude());
        if (request.getAreaSqKm() != null) ward.setAreaSqKm(request.getAreaSqKm());
        if (request.getPopulation() != null) ward.setPopulation(request.getPopulation());
        if (request.getActive() != null) ward.setActive(request.getActive());

        Ward saved = wardRepository.save(ward);
        auditService.record("Ward", saved.getId().toString(), AuditAction.UPDATE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Updated ward " + saved.getName());

        return WardDto.fromEntity(saved);
    }

    @Transactional
    public void deleteWard(Long id, String performedBy) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward", "id", id));
        ward.setActive(false);
        wardRepository.save(ward);
        auditService.record("Ward", id.toString(), AuditAction.DELETE,
                performedBy != null ? performedBy : "SYSTEM", null,
                "Deactivated ward " + ward.getName());
    }
}
