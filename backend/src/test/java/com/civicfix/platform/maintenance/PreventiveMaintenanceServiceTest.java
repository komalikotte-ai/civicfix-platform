package com.civicfix.platform.maintenance;

import com.civicfix.platform.assets.AssetTestMocks;
import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.inventory.InventoryTestMocks;
import com.civicfix.platform.workorders.WorkOrderTestMocks;
import com.civicfix.platform.maintenance.dto.*;
import com.civicfix.platform.maintenance.entity.*;
import com.civicfix.platform.maintenance.enums.*;
import com.civicfix.platform.maintenance.service.PreventiveMaintenanceService;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PreventiveMaintenanceService Unit Tests")
class PreventiveMaintenanceServiceTest {

    private PreventiveMaintenanceService service;

    private MaintenanceTestMocks.MockInspectionPlanRepository inspectionPlanRepo;
    private MaintenanceTestMocks.MockMaintenanceScheduleRepository scheduleRepo;
    private MaintenanceTestMocks.MockConditionAssessmentRepository assessmentRepo;
    private MaintenanceTestMocks.MockMaintenanceReminderRepository reminderRepo;
    private MaintenanceTestMocks.MockRepairRecommendationRepository recommendationRepo;
    private MaintenanceTestMocks.MockAssetFailureLogRepository failureLogRepo;

    private AssetTestMocks.MockAssetRepository assetRepo;
    private ComplaintTestMocks.MockDepartmentRepository departmentRepo;
    private AssetTestMocks.MockOrganizationRepository organizationRepo;
    private ComplaintTestMocks.MockEmployeeRepository employeeRepo;
    private WorkOrderTestMocks.MockWorkOrderRepository workOrderRepo;
    private AssetTestMocks.MockAssetInspectionRepository inspectionRepo;

    private Organization organization;
    private Department department;
    private Asset asset;
    private Employee worker;
    private Employee supervisor;

    @BeforeEach
    void setUp() {
        inspectionPlanRepo = new MaintenanceTestMocks.MockInspectionPlanRepository();
        scheduleRepo = new MaintenanceTestMocks.MockMaintenanceScheduleRepository();
        assessmentRepo = new MaintenanceTestMocks.MockConditionAssessmentRepository();
        reminderRepo = new MaintenanceTestMocks.MockMaintenanceReminderRepository();
        recommendationRepo = new MaintenanceTestMocks.MockRepairRecommendationRepository();
        failureLogRepo = new MaintenanceTestMocks.MockAssetFailureLogRepository();

        assetRepo = new AssetTestMocks.MockAssetRepository();
        departmentRepo = new ComplaintTestMocks.MockDepartmentRepository();
        organizationRepo = new AssetTestMocks.MockOrganizationRepository();
        employeeRepo = new ComplaintTestMocks.MockEmployeeRepository();
        workOrderRepo = new WorkOrderTestMocks.MockWorkOrderRepository();
        inspectionRepo = new AssetTestMocks.MockAssetInspectionRepository();

        InventoryTestMocks.MockAuditService auditService = new InventoryTestMocks.MockAuditService();

        service = new PreventiveMaintenanceService(
                inspectionPlanRepo, scheduleRepo, assessmentRepo, reminderRepo,
                recommendationRepo, failureLogRepo, assetRepo, departmentRepo,
                organizationRepo, employeeRepo, workOrderRepo, inspectionRepo,
                auditService);

        organization = new Organization("City Corp", "CORP", "Region");
        organization.setId(1L);
        organizationRepo.save(organization);

        department = new Department();
        department.setId(10L);
        department.setName("Public Works");
        department.setCode("PW");
        departmentRepo.save(department);

        asset = new Asset();
        asset.setAssetCode("AST-SL-0001");
        asset.setName("Main Street Light 42");
        asset.setAssetType(AssetType.STREETLIGHTS);
        asset.setDepartment(department);
        asset.setOrganization(organization);
        asset.setStatus(AssetStatus.OPERATIONAL);
        asset.setConditionRating(ConditionRating.GOOD);
        assetRepo.save(asset);

        worker = new Employee();
        worker.setId(201L);
        worker.setFullName("Ramesh Kumar");
        employeeRepo.save(worker);

        supervisor = new Employee();
        supervisor.setId(202L);
        supervisor.setFullName("Priya Sharma");
        employeeRepo.save(supervisor);
    }

    // =========================================================================
    // 1. INSPECTION PLANS
    // =========================================================================

    @Test
    @DisplayName("Create inspection plan - success")
    void testCreateInspectionPlan_success() {
        CreateInspectionPlanRequest req = new CreateInspectionPlanRequest();
        req.setName("Monthly Streetlight Inspection");
        req.setTargetAssetType(AssetType.STREETLIGHTS);
        req.setPlanType(PlanType.ROUTINE);
        req.setFrequency(RecurrenceFrequency.MONTHLY);
        req.setChecklistJson("{\"items\": [\"Check bulb\", \"Check wire connection\"]}");
        req.setDepartmentId(department.getId());
        req.setOrganizationId(organization.getId());

        InspectionPlanDto dto = service.createInspectionPlan(req, "admin");

        assertNotNull(dto.getId());
        assertEquals("Monthly Streetlight Inspection", dto.getName());
        assertTrue(dto.getPlanCode().startsWith("PLAN-"));
        assertEquals(AssetType.STREETLIGHTS, dto.getTargetAssetType());
    }

    @Test
    @DisplayName("Create inspection plan - duplicate code throws ConflictException")
    void testCreateInspectionPlan_duplicateCode() {
        CreateInspectionPlanRequest req = new CreateInspectionPlanRequest();
        req.setPlanCode("PLAN-FIXED-01");
        req.setName("Plan A");
        req.setChecklistJson("{}");
        req.setDepartmentId(department.getId());
        req.setOrganizationId(organization.getId());
        service.createInspectionPlan(req, "admin");

        CreateInspectionPlanRequest dup = new CreateInspectionPlanRequest();
        dup.setPlanCode("PLAN-FIXED-01");
        dup.setName("Plan B");
        dup.setChecklistJson("{}");
        dup.setDepartmentId(department.getId());
        dup.setOrganizationId(organization.getId());

        assertThrows(ConflictException.class, () -> service.createInspectionPlan(dup, "admin"));
    }

    // =========================================================================
    // 2. MAINTENANCE SCHEDULES
    // =========================================================================

    @Test
    @DisplayName("Create maintenance schedule - success")
    void testCreateMaintenanceSchedule_success() {
        CreateMaintenanceScheduleRequest req = new CreateMaintenanceScheduleRequest();
        req.setTitle("Biweekly Streetlight Servicing");
        req.setAssetId(asset.getId());
        req.setDepartmentId(department.getId());
        req.setAssignedWorkerId(worker.getId());
        req.setAssignedSupervisorId(supervisor.getId());
        req.setFrequency(RecurrenceFrequency.BIWEEKLY);
        req.setStartDate(LocalDate.now());

        MaintenanceScheduleDto dto = service.createMaintenanceSchedule(req, "admin");

        assertNotNull(dto.getId());
        assertEquals(ScheduleStatus.ACTIVE, dto.getStatus());
        assertTrue(dto.getScheduleCode().startsWith("SCH-"));
        assertNotNull(dto.getNextDueDate());
    }

    @Test
    @DisplayName("Pause and resume maintenance schedule")
    void testPauseAndResumeSchedule() {
        CreateMaintenanceScheduleRequest req = new CreateMaintenanceScheduleRequest();
        req.setTitle("Quarterly Check");
        req.setAssetId(asset.getId());
        req.setDepartmentId(department.getId());
        req.setStartDate(LocalDate.now());
        MaintenanceScheduleDto dto = service.createMaintenanceSchedule(req, "admin");

        MaintenanceScheduleDto paused = service.pauseMaintenanceSchedule(dto.getId(), "admin");
        assertEquals(ScheduleStatus.PAUSED, paused.getStatus());

        MaintenanceScheduleDto resumed = service.resumeMaintenanceSchedule(dto.getId(), "admin");
        assertEquals(ScheduleStatus.ACTIVE, resumed.getStatus());
    }

    // =========================================================================
    // 3. RECURRING JOB: WORK ORDER GENERATION & REMINDERS
    // =========================================================================

    @Test
    @DisplayName("generateDueMaintenanceWorkOrders: creates work order and advances nextDueDate")
    void testGenerateDueMaintenanceWorkOrders() {
        CreateMaintenanceScheduleRequest req = new CreateMaintenanceScheduleRequest();
        req.setTitle("Daily Light Inspection");
        req.setAssetId(asset.getId());
        req.setDepartmentId(department.getId());
        req.setAssignedWorkerId(worker.getId());
        req.setFrequency(RecurrenceFrequency.DAILY);
        req.setStartDate(LocalDate.now().minusDays(2));
        req.setNextDueDate(Instant.now().minus(1, ChronoUnit.HOURS)); // past due
        req.setAutoGenerateWorkOrder(true);
        MaintenanceScheduleDto sched = service.createMaintenanceSchedule(req, "admin");

        int generated = service.generateDueMaintenanceWorkOrders();

        assertEquals(1, generated);

        // Verify WorkOrder was created in repository
        List<WorkOrder> workOrders = workOrderRepo.findByAssetId(asset.getId());
        assertFalse(workOrders.isEmpty());
        WorkOrder wo = workOrders.get(0);
        assertTrue(wo.getTitle().startsWith("PM:"));
        assertEquals(WorkOrderStatus.ASSIGNED, wo.getStatus());
        assertEquals(worker.getId(), wo.getAssignedWorker().getId());

        // Verify Reminder was created
        List<MaintenanceReminder> reminders = reminderRepo.findByScheduleId(sched.getId());
        assertFalse(reminders.isEmpty());
        assertEquals(ReminderStatus.PENDING, reminders.get(0).getStatus());

        // Verify nextDueDate was advanced to the future
        MaintenanceSchedule updatedSched = scheduleRepo.findById(sched.getId()).get();
        assertTrue(updatedSched.getNextDueDate().isAfter(Instant.now()));
    }

    // =========================================================================
    // 4. CONDITION ASSESSMENTS
    // =========================================================================

    @Test
    @DisplayName("Record condition assessment: updates asset condition and calculates health degradation")
    void testRecordConditionAssessment_withDegradation() {
        // Initial baseline assessment
        CreateConditionAssessmentRequest req1 = new CreateConditionAssessmentRequest();
        req1.setAssetId(asset.getId());
        req1.setInspectorId(worker.getId());
        req1.setOverallCondition(ConditionRating.EXCELLENT);
        req1.setHealthIndexScore(90);
        req1.setAssessmentNotes("Baseline inspection: excellent condition");
        service.recordConditionAssessment(req1, "inspector_1");

        // Subsequent assessment showing degradation
        CreateConditionAssessmentRequest req2 = new CreateConditionAssessmentRequest();
        req2.setAssetId(asset.getId());
        req2.setInspectorId(worker.getId());
        req2.setOverallCondition(ConditionRating.FAIR);
        req2.setHealthIndexScore(72); // dropped from 90 -> 20% degradation
        req2.setAssessmentNotes("Degradation observed in structural mounts");
        ConditionAssessmentDto dto2 = service.recordConditionAssessment(req2, "inspector_1");

        assertNotNull(dto2.getId());
        assertEquals(ConditionRating.FAIR, dto2.getOverallCondition());
        assertEquals(72, dto2.getHealthIndexScore());
        assertEquals(90, dto2.getPreviousHealthIndexScore());
        assertEquals(new BigDecimal("20.00"), dto2.getDegradationRate());

        // Verify asset's current condition rating updated
        Asset updatedAsset = assetRepo.findById(asset.getId()).get();
        assertEquals(ConditionRating.FAIR, updatedAsset.getConditionRating());
    }

    @Test
    @DisplayName("Record poor condition assessment: auto-generates repair recommendation")
    void testRecordConditionAssessment_autoGeneratesRepairRecommendation() {
        CreateConditionAssessmentRequest req = new CreateConditionAssessmentRequest();
        req.setAssetId(asset.getId());
        req.setInspectorId(worker.getId());
        req.setOverallCondition(ConditionRating.POOR);
        req.setHealthIndexScore(40);
        req.setAssessmentNotes("Critical corrosion on electrical mountings");

        service.recordConditionAssessment(req, "inspector_1");

        // Verify repair recommendation was auto-generated
        List<RepairRecommendationDto> recs = service.getRecommendationsForAsset(asset.getId());
        assertFalse(recs.isEmpty());
        RepairRecommendationDto rec = recs.get(0);
        assertEquals(RecommendationAction.REPAIR, rec.getRecommendedAction());
        assertEquals(RecommendationStatus.PENDING, rec.getStatus());
        assertTrue(rec.getTitle().contains("POOR"));
    }

    // =========================================================================
    // 5. REPAIR RECOMMENDATIONS & WORK ORDER INTEGRATION
    // =========================================================================

    @Test
    @DisplayName("Convert repair recommendation to Work Order")
    void testConvertRecommendationToWorkOrder() {
        CreateRepairRecommendationRequest req = new CreateRepairRecommendationRequest();
        req.setAssetId(asset.getId());
        req.setRecommendedAction(RecommendationAction.REPLACE);
        req.setTitle("Replace Corroded Bracket");
        req.setJustification("Safety hazard in high wind conditions");
        req.setEstimatedCost(new BigDecimal("1500.00"));
        req.setPriority(WorkOrderPriority.HIGH);

        RepairRecommendationDto rec = service.createRepairRecommendation(req, "engineer");
        service.approveRepairRecommendation(rec.getId(), "supervisor");

        RepairRecommendationDto converted = service.convertRecommendationToWorkOrder(rec.getId(), "supervisor");

        assertEquals(RecommendationStatus.CONVERTED_TO_WORK_ORDER, converted.getStatus());
        assertNotNull(converted.getWorkOrderId());
        assertTrue(converted.getWorkOrderNumber().startsWith("WO-PM-"));

        // Verify work order is created and linked to the asset
        WorkOrder wo = workOrderRepo.findById(converted.getWorkOrderId()).get();
        assertEquals(asset.getId(), wo.getAsset().getId());
        assertEquals(WorkOrderPriority.HIGH, wo.getPriority());
        assertEquals(new BigDecimal("1500.00"), wo.getEstimatedCost());
    }

    // =========================================================================
    // 6. ASSET FAILURE LOGS (BREAKDOWN HISTORY)
    // =========================================================================

    @Test
    @DisplayName("Log asset failure: critical failure auto-creates emergency work order & updates asset status")
    void testLogAssetFailure_criticalAutoCreatesWorkOrder() {
        CreateAssetFailureLogRequest req = new CreateAssetFailureLogRequest();
        req.setAssetId(asset.getId());
        req.setReportedById(worker.getId());
        req.setFailureCategory("Electrical Short");
        req.setFailureMode("Total blackout");
        req.setRootCause("Water ingress in junction box");
        req.setSeverity(FailureSeverity.CRITICAL);
        req.setDowntimeHours(new BigDecimal("6.5"));
        req.setImmediateActionTaken("Isolated breaker");

        AssetFailureLogDto dto = service.logAssetFailure(req, "reporter");

        assertNotNull(dto.getId());
        assertNotNull(dto.getWorkOrderId());
        assertTrue(dto.getWorkOrderNumber().startsWith("WO-PM-"));

        // Verify asset status updated to DEGRADED
        Asset updatedAsset = assetRepo.findById(asset.getId()).get();
        assertEquals(AssetStatus.DEGRADED, updatedAsset.getStatus());
    }

    // =========================================================================
    // 7. MAINTENANCE REMINDERS
    // =========================================================================

    @Test
    @DisplayName("Acknowledge maintenance reminder")
    void testAcknowledgeReminder() {
        MaintenanceReminder reminder = new MaintenanceReminder();
        reminder.setTitle("Check oil level");
        reminder.setMessage("Quarterly oil check");
        reminder.setDueDate(Instant.now().plus(2, ChronoUnit.DAYS));
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setRecipient(worker);
        reminderRepo.save(reminder);

        MaintenanceReminderDto ack = service.acknowledgeReminder(reminder.getId(), "ramesh");

        assertEquals(ReminderStatus.ACKNOWLEDGED, ack.getStatus());
        assertNotNull(ack.getAcknowledgedAt());
    }
}
