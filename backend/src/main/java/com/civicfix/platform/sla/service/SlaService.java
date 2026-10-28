package com.civicfix.platform.sla.service;

import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.sla.dto.SlaPolicyDto;
import com.civicfix.platform.sla.entity.SlaPolicy;
import com.civicfix.platform.sla.repository.SlaPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SlaService {

    private final SlaPolicyRepository slaPolicyRepository;

    public SlaService(SlaPolicyRepository slaPolicyRepository) {
        this.slaPolicyRepository = slaPolicyRepository;
    }

    @Transactional(readOnly = true)
    public Instant calculateDueDate(Long organizationId, Long departmentId, ComplaintPriority priority) {
        Optional<SlaPolicy> policyOpt = Optional.empty();

        if (departmentId != null) {
            policyOpt = slaPolicyRepository.findByOrganizationIdAndDepartmentIdAndPriorityAndActiveTrue(
                    organizationId, departmentId, priority
            );
        }

        if (policyOpt.isEmpty()) {
            policyOpt = slaPolicyRepository.findByOrganizationIdAndDepartmentIsNullAndPriorityAndActiveTrue(
                    organizationId, priority
            );
        }

        int hours = policyOpt.map(SlaPolicy::getResolutionTimeHours).orElseGet(() -> getDefaultHours(priority));
        return Instant.now().plus(hours, ChronoUnit.HOURS);
    }

    private int getDefaultHours(ComplaintPriority priority) {
        return switch (priority) {
            case CRITICAL -> 6;
            case HIGH -> 24;
            case MEDIUM -> 48;
            case LOW -> 72;
        };
    }

    @Transactional(readOnly = true)
    public List<SlaPolicyDto> getPoliciesByOrganization(Long organizationId) {
        return slaPolicyRepository.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(SlaPolicyDto::fromEntity)
                .collect(Collectors.toList());
    }
}
