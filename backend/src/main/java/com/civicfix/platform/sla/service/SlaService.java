package com.civicfix.platform.sla.service;

import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.organizations.repository.OrganizationRepository;
import com.civicfix.platform.sla.dto.BusinessCalendarDto;
import com.civicfix.platform.sla.dto.BusinessHolidayDto;
import com.civicfix.platform.sla.dto.CreateBusinessCalendarRequest;
import com.civicfix.platform.sla.dto.CreateBusinessHolidayRequest;
import com.civicfix.platform.sla.dto.SlaPolicyDto;
import com.civicfix.platform.sla.entity.BusinessCalendar;
import com.civicfix.platform.sla.entity.BusinessHoliday;
import com.civicfix.platform.sla.entity.SlaPolicy;
import com.civicfix.platform.sla.enums.SlaState;
import com.civicfix.platform.sla.repository.BusinessCalendarRepository;
import com.civicfix.platform.sla.repository.BusinessHolidayRepository;
import com.civicfix.platform.sla.repository.SlaPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SlaService {

    private final SlaPolicyRepository slaPolicyRepository;
    private final BusinessCalendarRepository calendarRepository;
    private final BusinessHolidayRepository holidayRepository;
    private final BusinessHoursCalculator businessHoursCalculator;
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;

    public SlaService(SlaPolicyRepository slaPolicyRepository,
                      BusinessCalendarRepository calendarRepository,
                      BusinessHolidayRepository holidayRepository,
                      BusinessHoursCalculator businessHoursCalculator,
                      DepartmentRepository departmentRepository,
                      OrganizationRepository organizationRepository) {
        this.slaPolicyRepository = slaPolicyRepository;
        this.calendarRepository = calendarRepository;
        this.holidayRepository = holidayRepository;
        this.businessHoursCalculator = businessHoursCalculator;
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
    }

    /**
     * Backward-compatible overload for simple deadline calculation.
     */
    @Transactional(readOnly = true)
    public Instant calculateDueDate(Long organizationId, Long departmentId, ComplaintPriority priority) {
        return calculateDueDate(organizationId, departmentId, null, priority, null, Instant.now());
    }

    /**
     * Full deadline calculation taking into account department, category, priority, severity,
     * working days, working hours, and municipal holidays.
     */
    @Transactional(readOnly = true)
    public Instant calculateDueDate(Long organizationId, Long departmentId, String category,
                                    ComplaintPriority priority, ComplaintSeverity severity, Instant createdAt) {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        SlaPolicy policy = resolvePolicy(organizationId, departmentId, category, priority, severity);
        int resolutionHours = policy != null ? policy.getResolutionTimeHours() : getDefaultHours(priority);
        boolean businessHoursOnly = policy != null ? policy.isBusinessHoursOnly() : true;

        if (!businessHoursOnly) {
            return createdAt.plus(resolutionHours, ChronoUnit.HOURS);
        }

        BusinessCalendar calendar = resolveCalendar(organizationId, departmentId, policy);
        List<BusinessHoliday> holidays = resolveHolidays(organizationId, departmentId);

        return businessHoursCalculator.calculateDeadline(createdAt, resolutionHours * 60L, calendar, holidays);
    }

    /**
     * Calculates the timestamp when the complaint transitions to WARNING state.
     */
    @Transactional(readOnly = true)
    public Instant calculateWarningDate(Long organizationId, Long departmentId, String category,
                                        ComplaintPriority priority, ComplaintSeverity severity, Instant createdAt) {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        SlaPolicy policy = resolvePolicy(organizationId, departmentId, category, priority, severity);
        int resolutionHours = policy != null ? policy.getResolutionTimeHours() : getDefaultHours(priority);
        int warningPercent = policy != null ? policy.getWarningThresholdPercent() : 75;
        boolean businessHoursOnly = policy != null ? policy.isBusinessHoursOnly() : true;

        long warningMinutes = (resolutionHours * 60L * warningPercent) / 100L;

        if (!businessHoursOnly) {
            return createdAt.plus(Duration.ofMinutes(warningMinutes));
        }

        BusinessCalendar calendar = resolveCalendar(organizationId, departmentId, policy);
        List<BusinessHoliday> holidays = resolveHolidays(organizationId, departmentId);

        return businessHoursCalculator.calculateDeadline(createdAt, warningMinutes, calendar, holidays);
    }

    /**
     * Determines the live SLA state of a complaint as of the specified time.
     */
    public SlaState calculateSlaState(Complaint complaint, Instant asOf) {
        if (complaint == null) return SlaState.NOT_STARTED;
        if (asOf == null) asOf = Instant.now();

        ComplaintStatus status = complaint.getStatus();

        // If complaint is completed
        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.CLOSED) {
            if (complaint.getResolvedAt() != null && complaint.getSlaDueAt() != null
                    && complaint.getResolvedAt().isAfter(complaint.getSlaDueAt())) {
                return SlaState.BREACHED;
            }
            return SlaState.RESOLVED;
        }

        if (status == ComplaintStatus.REJECTED) {
            return SlaState.RESOLVED;
        }

        // Active complaint evaluations
        if (complaint.getSlaDueAt() != null && asOf.isAfter(complaint.getSlaDueAt())) {
            return SlaState.BREACHED;
        }

        if (complaint.getSlaWarningAt() != null && !asOf.isBefore(complaint.getSlaWarningAt())) {
            return SlaState.WARNING;
        }

        if (status == ComplaintStatus.NEW || status == ComplaintStatus.SUBMITTED) {
            return SlaState.NOT_STARTED;
        }

        return SlaState.ACTIVE;
    }

    private SlaPolicy resolvePolicy(Long organizationId, Long departmentId, String category,
                                   ComplaintPriority priority, ComplaintSeverity severity) {
        if (organizationId == null) {
            return null;
        }

        List<SlaPolicy> matches = slaPolicyRepository.findMatchingPolicies(
                organizationId, departmentId, category, priority, severity
        );

        if (!matches.isEmpty()) {
            return matches.get(0);
        }

        return null;
    }

    public BusinessCalendar resolveCalendar(Long organizationId, Long departmentId, SlaPolicy policy) {
        if (policy != null && policy.getCalendar() != null) {
            return policy.getCalendar();
        }

        if (departmentId != null) {
            Optional<BusinessCalendar> deptCal = calendarRepository.findByDepartmentIdAndActiveTrue(departmentId);
            if (deptCal.isPresent()) return deptCal.get();
        }

        if (organizationId != null) {
            Optional<BusinessCalendar> orgCal = calendarRepository.findByOrganizationIdAndDepartmentIsNullAndActiveTrue(organizationId);
            if (orgCal.isPresent()) return orgCal.get();
        }

        // Default standard 9:00 - 18:00 Mon-Fri calendar fallback
        BusinessCalendar defaultCal = new BusinessCalendar();
        defaultCal.setName("Standard Municipal Calendar");
        defaultCal.setWorkingDays("MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY");
        defaultCal.setStartTime(LocalTime.of(9, 0));
        defaultCal.setEndTime(LocalTime.of(18, 0));
        defaultCal.setTimeZone("Asia/Kolkata");
        defaultCal.setIs24x7(false);
        return defaultCal;
    }

    public List<BusinessHoliday> resolveHolidays(Long organizationId, Long departmentId) {
        if (organizationId == null) return List.of();
        if (departmentId != null) {
            List<BusinessHoliday> combined = new java.util.ArrayList<>(
                    holidayRepository.findByOrganizationIdAndDepartmentIdAndActiveTrue(organizationId, departmentId)
            );
            combined.addAll(holidayRepository.findByOrganizationIdAndDepartmentIsNullAndActiveTrue(organizationId));
            return combined;
        }
        return holidayRepository.findByOrganizationIdAndActiveTrue(organizationId);
    }

    public int getDefaultHours(ComplaintPriority priority) {
        if (priority == null) return 48;
        return switch (priority) {
            case CRITICAL -> 6;
            case URGENT -> 12;
            case HIGH -> 24;
            case MEDIUM -> 48;
            case LOW -> 72;
        };
    }

    @Transactional
    public SlaPolicyDto createPolicy(SlaPolicyDto dto) {
        Organization org = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", dto.getOrganizationId()));

        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
        }

        BusinessCalendar cal = null;
        if (dto.getCalendarId() != null) {
            cal = calendarRepository.findById(dto.getCalendarId())
                    .orElseThrow(() -> new ResourceNotFoundException("BusinessCalendar", "id", dto.getCalendarId()));
        }

        SlaPolicy policy = new SlaPolicy(
                org, dept, dto.getCategory(), dto.getPriority(), dto.getSeverity(),
                dto.getResponseTimeHours(), dto.getResolutionTimeHours(), dto.getEscalationTimeHours(),
                dto.getWarningThresholdPercent(), dto.isBusinessHoursOnly(), cal
        );

        SlaPolicy saved = slaPolicyRepository.save(policy);
        return SlaPolicyDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SlaPolicyDto> getPoliciesByOrganization(Long organizationId) {
        return slaPolicyRepository.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(SlaPolicyDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SlaPolicyDto getPolicyById(Long id) {
        SlaPolicy policy = slaPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SlaPolicy", "id", id));
        return SlaPolicyDto.fromEntity(policy);
    }

    @Transactional
    public BusinessCalendarDto createCalendar(CreateBusinessCalendarRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        Department dept = null;
        if (request.getDepartmentId() != null) {
            dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
        }

        BusinessCalendar calendar = new BusinessCalendar(
                org, dept, request.getName(), request.getWorkingDays(),
                request.getStartTime(), request.getEndTime(), request.getTimeZone(), request.is24x7()
        );

        BusinessCalendar saved = calendarRepository.save(calendar);
        return BusinessCalendarDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<BusinessCalendarDto> getCalendarsByOrganization(Long organizationId) {
        return calendarRepository.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(BusinessCalendarDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public BusinessHolidayDto addHoliday(CreateBusinessHolidayRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        Department dept = null;
        if (request.getDepartmentId() != null) {
            dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
        }

        BusinessHoliday holiday = new BusinessHoliday(
                org, dept, request.getHolidayDate(), request.getName(), request.isRecurring()
        );

        BusinessHoliday saved = holidayRepository.save(holiday);
        return BusinessHolidayDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<BusinessHolidayDto> getHolidaysByOrganization(Long organizationId) {
        return holidayRepository.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(BusinessHolidayDto::fromEntity)
                .collect(Collectors.toList());
    }
}
