package com.civicfix.platform.organizations.service;

import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.organizations.dto.OrganizationDto;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.organizations.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrganizationDto> getOrganizations(Pageable pageable) {
        Page<OrganizationDto> page = organizationRepository.findAllByActiveTrue(pageable)
                .map(OrganizationDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public OrganizationDto getOrganizationById(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return OrganizationDto.fromEntity(org);
    }

    @Transactional
    public OrganizationDto createOrganization(OrganizationDto dto) {
        if (organizationRepository.existsByCode(dto.getCode())) {
            throw new ConflictException("Organization with code '" + dto.getCode() + "' already exists");
        }

        Organization org = new Organization();
        org.setName(dto.getName());
        org.setCode(dto.getCode());
        org.setState(dto.getState());
        if (dto.getCountry() != null) org.setCountry(dto.getCountry());
        org.setContactEmail(dto.getContactEmail());
        org.setContactPhone(dto.getContactPhone());
        if (dto.getTimezone() != null) org.setTimezone(dto.getTimezone());

        Organization saved = organizationRepository.save(org);
        return OrganizationDto.fromEntity(saved);
    }
}
