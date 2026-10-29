package com.civicfix.platform.sla;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.sla.dto.ComplaintEscalationHistoryDto;
import com.civicfix.platform.sla.dto.ManualEscalationRequest;
import com.civicfix.platform.sla.dto.SlaEvaluationResultDto;
import com.civicfix.platform.sla.entity.BusinessCalendar;
import com.civicfix.platform.sla.entity.BusinessHoliday;
import com.civicfix.platform.sla.entity.ComplaintEscalationHistory;
import com.civicfix.platform.sla.entity.SlaPolicy;
import com.civicfix.platform.sla.enums.EscalationLevel;
import com.civicfix.platform.sla.enums.SlaState;
import com.civicfix.platform.sla.event.SlaBreachedEvent;
import com.civicfix.platform.sla.event.SlaEscalatedEvent;
import com.civicfix.platform.sla.event.SlaWarningEvent;
import com.civicfix.platform.sla.service.BusinessHoursCalculator;
import com.civicfix.platform.sla.service.EscalationService;
import com.civicfix.platform.sla.service.SlaEvaluationService;
import com.civicfix.platform.sla.service.SlaService;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlaAndEscalationTest {

    private BusinessHoursCalculator businessHoursCalculator;
    private SlaService slaService;
    private EscalationService escalationService;
    private SlaEvaluationService evaluationService;

    private SlaTestMocks.MockSlaPolicyRepository slaPolicyRepo;
    private SlaTestMocks.MockBusinessCalendarRepository calendarRepo;
    private SlaTestMocks.MockBusinessHolidayRepository holidayRepo;
    private SlaTestMocks.MockComplaintEscalationHistoryRepository escalationHistoryRepo;
    private ComplaintTestMocks.MockComplaintRepository complaintRepo;
    private ComplaintTestMocks.MockEmployeeRepository employeeRepo;
    private ComplaintTestMocks.MockDepartmentRepository departmentRepo;
    private SlaTestMocks.CapturingEventPublisher eventPublisher;

    private Organization organization;
    private Department sanitationDept;
    private Department waterDept;
    private Employee workerEmployee;
    private Employee supervisorEmployee;
    private Employee managerEmployee;
    private BusinessCalendar standardCalendar;

    private final ZoneId zone = ZoneId.of("Asia/Kolkata");

    @BeforeEach
    void setUp() {
        businessHoursCalculator = new BusinessHoursCalculator();
        slaPolicyRepo = new SlaTestMocks.MockSlaPolicyRepository();
        calendarRepo = new SlaTestMocks.MockBusinessCalendarRepository();
        holidayRepo = new SlaTestMocks.MockBusinessHolidayRepository();
        escalationHistoryRepo = new SlaTestMocks.MockComplaintEscalationHistoryRepository();
        complaintRepo = new ComplaintTestMocks.MockComplaintRepository();
        employeeRepo = new ComplaintTestMocks.MockEmployeeRepository();
        departmentRepo = new ComplaintTestMocks.MockDepartmentRepository();
        eventPublisher = new SlaTestMocks.CapturingEventPublisher();

        slaService = new SlaService(
                slaPolicyRepo,
                calendarRepo,
                holidayRepo,
                businessHoursCalculator,
                departmentRepo,
                null
        );

        escalationService = new EscalationService(
                complaintRepo,
                escalationHistoryRepo,
                employeeRepo,
                slaService,
                new AuditService(null) {
                    @Override
                    public void record(String entityType, String entityId, AuditAction action, String performedBy, String details) {}
                },
                eventPublisher
        );

        evaluationService = new SlaEvaluationService(complaintRepo, escalationService);

        organization = new Organization();
        organization.setId(1L);
        organization.setName("Metropolis Municipal Corporation");

        sanitationDept = new Department();
        sanitationDept.setId(10L);
        sanitationDept.setName("Sanitation & Waste");
        sanitationDept.setCode("SAN");
        sanitationDept.setOrganization(organization);
        departmentRepo.save(sanitationDept);

        waterDept = new Department();
        waterDept.setId(20L);
        waterDept.setName("Water Supply");
        waterDept.setCode("WAT");
        waterDept.setOrganization(organization);
        departmentRepo.save(waterDept);

        // Standard municipal calendar (09:00 - 18:00, Mon-Fri)
        standardCalendar = new BusinessCalendar(
                organization, null, "Standard Municipal Calendar",
                "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY",
                LocalTime.of(9, 0), LocalTime.of(18, 0), "Asia/Kolkata", false
        );
        standardCalendar.setId(1L);
        calendarRepo.save(standardCalendar);

        // Employees
        workerEmployee = new Employee();
        workerEmployee.setId(101L);
        workerEmployee.setEmployeeCode("EMP-101");
        workerEmployee.setRole(RoleType.ROLE_EMPLOYEE);
        workerEmployee.setDepartment(sanitationDept);
        workerEmployee.setActive(true);
        employeeRepo.save(workerEmployee);

        supervisorEmployee = new Employee();
        supervisorEmployee.setId(102L);
        supervisorEmployee.setEmployeeCode("EMP-102");
        supervisorEmployee.setRole(RoleType.ROLE_SUPERVISOR);
        supervisorEmployee.setDepartment(sanitationDept);
        supervisorEmployee.setActive(true);
        employeeRepo.save(supervisorEmployee);

        managerEmployee = new Employee();
        managerEmployee.setId(103L);
        managerEmployee.setEmployeeCode("EMP-103");
        managerEmployee.setRole(RoleType.ROLE_DEPARTMENT_MANAGER);
        managerEmployee.setDepartment(sanitationDept);
        managerEmployee.setActive(true);
        employeeRepo.save(managerEmployee);
    }

    @Test
    @DisplayName("24x7 Calendar and Standard Priority Fallback Deadline Calculation")
    void testDeadlineCalculationStandard() {
        BusinessCalendar cal24x7 = new BusinessCalendar(
                organization, null, "Emergency 24x7", "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY",
                LocalTime.MIN, LocalTime.MAX, "Asia/Kolkata", true
        );
        cal24x7.setId(2L);

        // 2026-11-06 (Friday) 14:00
        Instant start = ZonedDateTime.of(LocalDate.of(2026, 11, 6), LocalTime.of(14, 0), zone).toInstant();

        // 12 hours duration on 24x7 calendar
        Instant deadline = businessHoursCalculator.calculateDeadline(start, 12 * 60, cal24x7, List.of());
        ZonedDateTime deadlineZdt = deadline.atZone(zone);

        // Should land on Saturday 02:00
        assertEquals(LocalDate.of(2026, 11, 7), deadlineZdt.toLocalDate());
        assertEquals(LocalTime.of(2, 0), deadlineZdt.toLocalTime());

        // Default Priority Hours Fallbacks
        assertEquals(6, slaService.getDefaultHours(ComplaintPriority.CRITICAL));
        assertEquals(12, slaService.getDefaultHours(ComplaintPriority.URGENT));
        assertEquals(24, slaService.getDefaultHours(ComplaintPriority.HIGH));
        assertEquals(48, slaService.getDefaultHours(ComplaintPriority.MEDIUM));
        assertEquals(72, slaService.getDefaultHours(ComplaintPriority.LOW));
    }

    @Test
    @DisplayName("Business Hours Calculation: Overnights and Weekends are Skipped")
    void testBusinessHoursCalculation() {
        // Friday Nov 6, 2026 at 16:00 IST
        // Working hours: 09:00 - 18:00 (9 hours/day).
        // 4 business hours requested (240 minutes).
        // Friday has 2 hours remaining (16:00 to 18:00).
        // Skips Saturday Nov 7 and Sunday Nov 8.
        // Resumes Monday Nov 9 at 09:00 and completes remaining 2 hours -> Monday 11:00.
        Instant startFriday = ZonedDateTime.of(LocalDate.of(2026, 11, 6), LocalTime.of(16, 0), zone).toInstant();

        Instant deadline = businessHoursCalculator.calculateDeadline(startFriday, 4 * 60, standardCalendar, List.of());
        ZonedDateTime deadlineZdt = deadline.atZone(zone);

        assertEquals(LocalDate.of(2026, 11, 9), deadlineZdt.toLocalDate(), "Should land on Monday");
        assertEquals(DayOfWeek.MONDAY, deadlineZdt.getDayOfWeek());
        assertEquals(LocalTime.of(11, 0), deadlineZdt.toLocalTime(), "Should finish at 11:00");

        // Start before working hours: Friday 07:00 + 2 hours -> Friday 11:00
        Instant earlyStart = ZonedDateTime.of(LocalDate.of(2026, 11, 6), LocalTime.of(7, 0), zone).toInstant();
        Instant earlyDeadline = businessHoursCalculator.calculateDeadline(earlyStart, 2 * 60, standardCalendar, List.of());
        assertEquals(LocalTime.of(11, 0), earlyDeadline.atZone(zone).toLocalTime());
    }

    @Test
    @DisplayName("Holiday Skipping: Business Calendar Skips Registered Municipal Holidays")
    void testHolidaySkipping() {
        // Monday Nov 9, 2026 is registered as a Municipal Holiday
        BusinessHoliday holiday = new BusinessHoliday(
                organization, null, LocalDate.of(2026, 11, 9), "City Foundation Day", false
        );
        holiday.setId(10L);
        holidayRepo.save(holiday);

        // Friday Nov 6, 2026 at 16:00 + 4 hours
        // 2 hours consumed on Friday (ends at 18:00)
        // Saturday & Sunday skipped
        // Monday Nov 9 skipped because it's a holiday
        // Resumes Tuesday Nov 10 at 09:00 + 2 hours -> Tuesday 11:00
        Instant startFriday = ZonedDateTime.of(LocalDate.of(2026, 11, 6), LocalTime.of(16, 0), zone).toInstant();

        List<BusinessHoliday> holidays = slaService.resolveHolidays(1L, null);
        Instant deadline = businessHoursCalculator.calculateDeadline(startFriday, 4 * 60, standardCalendar, holidays);
        ZonedDateTime deadlineZdt = deadline.atZone(zone);

        assertEquals(LocalDate.of(2026, 11, 10), deadlineZdt.toLocalDate(), "Must skip Monday holiday and land on Tuesday");
        assertEquals(DayOfWeek.TUESDAY, deadlineZdt.getDayOfWeek());
        assertEquals(LocalTime.of(11, 0), deadlineZdt.toLocalTime());
    }

    @Test
    @DisplayName("Department-Specific SLA Policy Overrides Organization Default")
    void testDepartmentSpecificPolicies() {
        // Org-wide default SLA Policy for HIGH priority: 24 business hours
        SlaPolicy orgPolicy = new SlaPolicy(
                organization, null, null, ComplaintPriority.HIGH, null,
                2, 24, 24, 75, true, standardCalendar
        );
        orgPolicy.setId(1L);
        slaPolicyRepo.save(orgPolicy);

        // Sanitation Department specific SLA Policy for HIGH priority: 12 business hours
        SlaPolicy sanitationPolicy = new SlaPolicy(
                organization, sanitationDept, null, ComplaintPriority.HIGH, null,
                1, 12, 12, 50, true, standardCalendar
        );
        sanitationPolicy.setId(2L);
        slaPolicyRepo.save(sanitationPolicy);

        Instant start = ZonedDateTime.of(LocalDate.of(2026, 11, 4), LocalTime.of(10, 0), zone).toInstant(); // Wednesday

        // Calculate for Sanitation Department (Department 10) -> Should use 12h policy
        Instant sanitationDue = slaService.calculateDueDate(1L, 10L, "Garbage", ComplaintPriority.HIGH, ComplaintSeverity.MEDIUM, start);

        // Calculate for Water Department (Department 20) -> Should fall back to 24h org policy
        Instant waterDue = slaService.calculateDueDate(1L, 20L, "Leakage", ComplaintPriority.HIGH, ComplaintSeverity.MEDIUM, start);

        // Sanitation due is earlier because of the 12-hour specific department SLA
        assertTrue(sanitationDue.isBefore(waterDue), "Department-specific SLA (12h) must produce earlier deadline than org default (24h)");
    }

    @Test
    @DisplayName("Warning State: Detects 75% Elapsed Time and Triggers SlaWarningEvent")
    void testWarningState() {
        // Policy: 20 hours resolution, 75% warning threshold (= 15 business hours)
        SlaPolicy policy = new SlaPolicy(
                organization, sanitationDept, null, ComplaintPriority.HIGH, null,
                2, 20, 20, 75, false, null // 24x7 for straightforward math
        );
        policy.setId(3L);
        slaPolicyRepo.save(policy);

        Instant created = Instant.parse("2026-11-01T10:00:00Z");
        Instant warningDate = slaService.calculateWarningDate(1L, 10L, null, ComplaintPriority.HIGH, null, created);
        Instant dueDate = slaService.calculateDueDate(1L, 10L, null, ComplaintPriority.HIGH, null, created);

        // 75% of 20h = 15h
        assertEquals(created.plus(Duration.ofHours(15)), warningDate);
        assertEquals(created.plus(Duration.ofHours(20)), dueDate);

        Complaint complaint = new Complaint();
        complaint.setId(501L);
        complaint.setTrackingNumber("CMP-20261101-001");
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setSlaWarningAt(warningDate);
        complaint.setSlaDueAt(dueDate);
        complaint.setSlaState(SlaState.ACTIVE);
        complaintRepo.save(complaint);

        // Case 1: Evaluated at 10 hours after creation (before warning) -> ACTIVE
        Instant at10Hours = created.plus(Duration.ofHours(10));
        assertEquals(SlaState.ACTIVE, slaService.calculateSlaState(complaint, at10Hours));

        // Case 2: Evaluated at 16 hours after creation (after warning, before due) -> WARNING
        Instant at16Hours = created.plus(Duration.ofHours(16));
        assertEquals(SlaState.WARNING, slaService.calculateSlaState(complaint, at16Hours));

        // Evaluate via EscalationService and assert SlaWarningEvent is published
        escalationService.evaluateAndEscalate(complaint, at16Hours);
        assertEquals(SlaState.WARNING, complaint.getSlaState());

        List<SlaWarningEvent> warningEvents = eventPublisher.getEventsOfType(SlaWarningEvent.class);
        assertEquals(1, warningEvents.size());
        assertEquals("CMP-20261101-001", warningEvents.get(0).getComplaint().getTrackingNumber());
    }

    @Test
    @DisplayName("Overdue and Breached SLA State Determination")
    void testOverdueAndBreachedState() {
        Instant created = Instant.parse("2026-11-01T10:00:00Z");
        Instant dueDate = created.plus(Duration.ofHours(24));

        Complaint complaint = new Complaint();
        complaint.setId(502L);
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setSlaDueAt(dueDate);

        // Before due date -> not breached
        Instant beforeDue = dueDate.minus(Duration.ofMinutes(10));
        assertNotEquals(SlaState.BREACHED, slaService.calculateSlaState(complaint, beforeDue));

        // After due date -> BREACHED
        Instant afterDue = dueDate.plus(Duration.ofMinutes(10));
        assertEquals(SlaState.BREACHED, slaService.calculateSlaState(complaint, afterDue));

        // When resolved before due date -> RESOLVED
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolvedAt(dueDate.minus(Duration.ofHours(2)));
        assertEquals(SlaState.RESOLVED, slaService.calculateSlaState(complaint, afterDue));

        // When resolved AFTER due date -> BREACHED
        complaint.setResolvedAt(dueDate.plus(Duration.ofHours(2)));
        assertEquals(SlaState.BREACHED, slaService.calculateSlaState(complaint, afterDue));
    }

    @Test
    @DisplayName("Automatic Escalation: Auto-assigns Supervisor and Logs Escalation History")
    void testAutomaticEscalation() {
        Instant created = Instant.parse("2026-11-01T10:00:00Z");
        Instant dueDate = created.plus(Duration.ofHours(24));
        Instant evaluationTime = dueDate.plus(Duration.ofHours(1));

        Complaint complaint = new Complaint();
        complaint.setId(503L);
        complaint.setTrackingNumber("CMP-20261101-503");
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        complaint.setDepartment(sanitationDept);
        complaint.setAssignedEmployee(workerEmployee);
        complaint.setEscalationLevel(EscalationLevel.LEVEL_0);
        complaint.setSlaDueAt(dueDate);
        complaint.setSlaState(SlaState.ACTIVE);
        complaintRepo.save(complaint);

        // Run automatic evaluation
        boolean escalated = escalationService.evaluateAndEscalate(complaint, evaluationTime);

        assertTrue(escalated, "Evaluation must escalate breached complaint");
        assertEquals(EscalationLevel.LEVEL_1, complaint.getEscalationLevel(), "Must escalate to LEVEL_1");
        assertNotNull(complaint.getAssignedSupervisor(), "Supervisor must be automatically assigned");
        assertEquals(102L, complaint.getAssignedSupervisor().getId());
        assertEquals(SlaState.BREACHED, complaint.getSlaState());

        // Check history
        List<ComplaintEscalationHistory> historyList = escalationHistoryRepo.findByComplaintIdOrderByEscalatedAtDesc(503L);
        assertEquals(1, historyList.size());
        ComplaintEscalationHistory history = historyList.get(0);
        assertEquals(EscalationLevel.LEVEL_0, history.getFromLevel());
        assertEquals(EscalationLevel.LEVEL_1, history.getToLevel());
        assertEquals(supervisorEmployee.getId(), history.getEscalatedToEmployee().getId());
        assertEquals("ROLE_SUPERVISOR", history.getTargetRole());

        // Check event published
        List<SlaEscalatedEvent> events = eventPublisher.getEventsOfType(SlaEscalatedEvent.class);
        assertEquals(1, events.size());
        assertEquals(EscalationLevel.LEVEL_1, events.get(0).getToLevel());

        // Second evaluation breach -> Escalates to LEVEL_2 (Department Manager)
        boolean secondEscalation = escalationService.evaluateAndEscalate(complaint, evaluationTime.plus(Duration.ofHours(2)));
        assertTrue(secondEscalation);
        assertEquals(EscalationLevel.LEVEL_2, complaint.getEscalationLevel());

        List<ComplaintEscalationHistory> updatedHistory = escalationHistoryRepo.findByComplaintIdOrderByEscalatedAtDesc(503L);
        assertEquals(2, updatedHistory.size());
        assertEquals(EscalationLevel.LEVEL_2, updatedHistory.get(0).getToLevel());
    }

    @Test
    @DisplayName("Manual Escalation: Authorized User Manually Escalates Critical Complaint")
    void testManualEscalation() {
        Complaint complaint = new Complaint();
        complaint.setId(504L);
        complaint.setTrackingNumber("CMP-20261101-504");
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setDepartment(sanitationDept);
        complaint.setEscalationLevel(EscalationLevel.LEVEL_0);
        complaintRepo.save(complaint);

        ManualEscalationRequest request = new ManualEscalationRequest();
        request.setTargetLevel(EscalationLevel.LEVEL_2);
        request.setReason("Citizen VIP complaint requiring immediate senior management attention");
        request.setTargetEmployeeId(managerEmployee.getId());

        ComplaintEscalationHistoryDto result = escalationService.manualEscalate(504L, request, "supervisor_rao");

        assertNotNull(result);
        assertEquals(EscalationLevel.LEVEL_0, result.getFromLevel());
        assertEquals(EscalationLevel.LEVEL_2, result.getToLevel());
        assertEquals(managerEmployee.getId(), result.getEscalatedToEmployeeId());

        Complaint updated = complaintRepo.findById(504L).orElseThrow();
        assertEquals(EscalationLevel.LEVEL_2, updated.getEscalationLevel());

        // Disallow escalation to same or lower level
        ManualEscalationRequest invalidRequest = new ManualEscalationRequest();
        invalidRequest.setTargetLevel(EscalationLevel.LEVEL_1);
        invalidRequest.setReason("Invalid downgrade");

        assertThrows(BadRequestException.class, () ->
                escalationService.manualEscalate(504L, invalidRequest, "admin")
        );
    }

    @Test
    @DisplayName("SLA Evaluation Service: Batch Evaluates All Active Complaints")
    void testSlaEvaluationServiceBatchRun() {
        Instant now = Instant.now();

        // Complaint 1: Active and well within SLA
        Complaint c1 = new Complaint();
        c1.setId(601L);
        c1.setStatus(ComplaintStatus.IN_PROGRESS);
        c1.setSlaWarningAt(now.plus(Duration.ofHours(10)));
        c1.setSlaDueAt(now.plus(Duration.ofHours(20)));
        c1.setSlaState(SlaState.ACTIVE);
        c1.setEscalationLevel(EscalationLevel.LEVEL_0);
        complaintRepo.save(c1);

        // Complaint 2: In warning window
        Complaint c2 = new Complaint();
        c2.setId(602L);
        c2.setStatus(ComplaintStatus.IN_PROGRESS);
        c2.setSlaWarningAt(now.minus(Duration.ofHours(1)));
        c2.setSlaDueAt(now.plus(Duration.ofHours(5)));
        c2.setSlaState(SlaState.ACTIVE);
        c2.setEscalationLevel(EscalationLevel.LEVEL_0);
        complaintRepo.save(c2);

        // Complaint 3: Breached deadline
        Complaint c3 = new Complaint();
        c3.setId(603L);
        c3.setStatus(ComplaintStatus.ASSIGNED);
        c3.setDepartment(sanitationDept);
        c3.setSlaWarningAt(now.minus(Duration.ofHours(10)));
        c3.setSlaDueAt(now.minus(Duration.ofHours(2)));
        c3.setSlaState(SlaState.ACTIVE);
        c3.setEscalationLevel(EscalationLevel.LEVEL_0);
        complaintRepo.save(c3);

        SlaEvaluationResultDto result = evaluationService.evaluateAllActiveComplaints();

        assertEquals(3, result.getEvaluatedCount(), "Should evaluate all 3 active complaints");
        assertEquals(1, result.getActiveCount(), "1 active complaint");
        assertEquals(1, result.getWarningCount(), "1 complaint in warning");
        assertEquals(1, result.getBreachedCount(), "1 breached complaint");
        assertEquals(1, result.getEscalatedCount(), "1 complaint auto-escalated");
    }
}
