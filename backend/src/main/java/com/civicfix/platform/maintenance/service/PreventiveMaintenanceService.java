package com.civicfix.platform.maintenance.service;

import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.assets.repository.AssetInspectionRepository;
import com.civicfix.platform.assets.repository.AssetRepository;
import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.maintenance.dto.*;
import com.civicfix.platform.maintenance.entity.*;
import com.civicfix.platform.maintenance.enums.*;
import com.civicfix.platform.maintenance.repository.*;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.organizations.repository.OrganizationRepository;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.repository.EmployeeRepository;
import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import com.civicfix.platform.workorders.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class PreventiveMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(PreventiveMaintenanceService.class);

    private final InspectionPlanRepository inspectionPlanRepository;
    private final MaintenanceScheduleRepository scheduleRepository;
    private final ConditionAssessmentRepository assessmentRepository;
    private final MaintenanceReminderRepository reminderRepository;
    private final RepairRecommendationRepository recommendationRepository;
    private final AssetFailureLogRepository failureLogRepository;

    private final AssetRepository assetRepository;
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AssetInspectionRepository inspectionRepository;
    private final AuditService auditService;

    public PreventiveMaintenanceService(InspectionPlanRepository inspectionPlanRepository,
                                        MaintenanceScheduleRepository scheduleRepository,
                                        ConditionAssessmentRepository assessmentRepository,
                                        MaintenanceReminderRepository reminderRepository,
                                        RepairRecommendationRepository recommendationRepository,
                                        AssetFailureLogRepository failureLogRepository,
                                        AssetRepository assetRepository,
                                        DepartmentRepository departmentRepository,
                                        OrganizationRepository organizationRepository,
                                        EmployeeRepository employeeRepository,
                                        WorkOrderRepository workOrderRepository,
                                        AssetInspectionRepository inspectionRepository,
                                        AuditService auditService) {
        this.inspectionPlanRepository = inspectionPlanRepository;
        this.scheduleRepository = scheduleRepository;
        this.assessmentRepository = assessmentRepository;
        this.reminderRepository = reminderRepository;
        this.recommendationRepository = recommendationRepository;
        this.failureLogRepository = failureLogRepository;
        this.assetRepository = assetRepository;
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
        this.employeeRepository = employeeRepository;
        this.workOrderRepository = workOrderRepository;
        this.inspectionRepository = inspectionRepository;
        this.auditService = auditService;
    }

    // =========================================================================
    // 1. INSPECTION PLANS
    // =========================================================================

    @Transactional
    public InspectionPlanDto createInspectionPlan(CreateInspectionPlanRequest request, String username) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + request.getOrganizationId()));

        String planCode = request.getPlanCode() != null ? request.getPlanCode() : generatePlanCode();
        if (inspectionPlanRepository.existsByPlanCode(planCode)) {
            throw new ConflictException("Inspection plan with code " + planCode + " already exists");
        }

        InspectionPlan plan = new InspectionPlan();
        plan.setPlanCode(planCode);
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPlanType(request.getPlanType() != null ? request.getPlanType() : PlanType.ROUTINE);
        plan.setTargetAssetType(request.getTargetAssetType());
        plan.setFrequency(request.getFrequency() != null ? request.getFrequency() : RecurrenceFrequency.MONTHLY);
        plan.setChecklistJson(request.getChecklistJson());
        plan.setSafetyGuidelines(request.getSafetyGuidelines());
        plan.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        plan.setDepartment(department);
        plan.setOrganization(organization);
        plan.setActive(true);

        InspectionPlan saved = inspectionPlanRepository.save(plan);
        auditService.record("InspectionPlan", saved.getId().toString(), AuditAction.CREATE, username,
                "Created inspection plan: " + saved.getName() + " [" + saved.getPlanCode() + "]");
        return InspectionPlanDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public InspectionPlanDto getInspectionPlanById(Long id) {
        InspectionPlan plan = inspectionPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection plan not found with ID: " + id));
        return InspectionPlanDto.fromEntity(plan);
    }

    @Transactional(readOnly = true)
    public PagedResponse<InspectionPlanDto> searchInspectionPlans(String keyword, Long departmentId,
                                                                  PlanType planType,
                                                                  com.civicfix.platform.assets.enums.AssetType targetAssetType,
                                                                  Boolean active, Pageable pageable) {
        Page<InspectionPlanDto> page = inspectionPlanRepository
                .searchPlans(keyword, departmentId, planType, targetAssetType, active, pageable)
                .map(InspectionPlanDto::fromEntity);
        return PagedResponse.from(page);
    }

    // =========================================================================
    // 2. MAINTENANCE SCHEDULES & RECURRING MAINTENANCE
    // =========================================================================

    @Transactional
    public MaintenanceScheduleDto createMaintenanceSchedule(CreateMaintenanceScheduleRequest request, String username) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + request.getAssetId()));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        String scheduleCode = request.getScheduleCode() != null ? request.getScheduleCode() : generateScheduleCode();
        if (scheduleRepository.existsByScheduleCode(scheduleCode)) {
            throw new ConflictException("Maintenance schedule with code " + scheduleCode + " already exists");
        }

        InspectionPlan plan = null;
        if (request.getInspectionPlanId() != null) {
            plan = inspectionPlanRepository.findById(request.getInspectionPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inspection plan not found with ID: " + request.getInspectionPlanId()));
        }

        Employee worker = null;
        if (request.getAssignedWorkerId() != null) {
            worker = employeeRepository.findById(request.getAssignedWorkerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + request.getAssignedWorkerId()));
        }

        Employee supervisor = null;
        if (request.getAssignedSupervisorId() != null) {
            supervisor = employeeRepository.findById(request.getAssignedSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with ID: " + request.getAssignedSupervisorId()));
        }

        RecurrenceFrequency frequency = request.getFrequency() != null ? request.getFrequency() : RecurrenceFrequency.MONTHLY;
        Integer intervalDays = request.getIntervalDays() != null ? request.getIntervalDays() : calculateDefaultIntervalDays(frequency);

        Instant nextDueDate = request.getNextDueDate() != null
                ? request.getNextDueDate()
                : calculateNextDueDate(frequency, intervalDays, request.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC));

        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setScheduleCode(scheduleCode);
        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setAsset(asset);
        schedule.setDepartment(department);
        schedule.setInspectionPlan(plan);
        schedule.setAssignedWorker(worker);
        schedule.setAssignedSupervisor(supervisor);
        schedule.setFrequency(frequency);
        schedule.setIntervalDays(intervalDays);
        schedule.setStartDate(request.getStartDate());
        schedule.setEndDate(request.getEndDate());
        schedule.setNextDueDate(nextDueDate);
        schedule.setEstimatedDurationHours(request.getEstimatedDurationHours());
        schedule.setEstimatedCost(request.getEstimatedCost());
        schedule.setPriority(request.getPriority() != null ? request.getPriority() : WorkOrderPriority.MEDIUM);
        schedule.setStatus(ScheduleStatus.ACTIVE);
        schedule.setAutoGenerateWorkOrder(request.isAutoGenerateWorkOrder());
        schedule.setChecklistJson(request.getChecklistJson() != null ? request.getChecklistJson() : (plan != null ? plan.getChecklistJson() : null));
        schedule.setActive(true);

        MaintenanceSchedule saved = scheduleRepository.save(schedule);
        auditService.record("MaintenanceSchedule", saved.getId().toString(), AuditAction.CREATE, username,
                "Created maintenance schedule: " + saved.getTitle() + " for asset " + asset.getAssetCode());
        return MaintenanceScheduleDto.fromEntity(saved);
    }

    @Transactional
    public MaintenanceScheduleDto pauseMaintenanceSchedule(Long id, String username) {
        MaintenanceSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance schedule not found with ID: " + id));
        schedule.setStatus(ScheduleStatus.PAUSED);
        MaintenanceSchedule saved = scheduleRepository.save(schedule);
        auditService.record("MaintenanceSchedule", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Paused maintenance schedule: " + saved.getScheduleCode());
        return MaintenanceScheduleDto.fromEntity(saved);
    }

    @Transactional
    public MaintenanceScheduleDto resumeMaintenanceSchedule(Long id, String username) {
        MaintenanceSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance schedule not found with ID: " + id));
        schedule.setStatus(ScheduleStatus.ACTIVE);
        // if next due date was in the past, update it to now + interval
        if (schedule.getNextDueDate().isBefore(Instant.now())) {
            schedule.setNextDueDate(calculateNextDueDate(schedule.getFrequency(), schedule.getIntervalDays(), Instant.now()));
        }
        MaintenanceSchedule saved = scheduleRepository.save(schedule);
        auditService.record("MaintenanceSchedule", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Resumed maintenance schedule: " + saved.getScheduleCode());
        return MaintenanceScheduleDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public MaintenanceScheduleDto getMaintenanceScheduleById(Long id) {
        MaintenanceSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance schedule not found with ID: " + id));
        return MaintenanceScheduleDto.fromEntity(schedule);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MaintenanceScheduleDto> searchMaintenanceSchedules(String keyword, Long assetId,
                                                                            Long departmentId, ScheduleStatus status,
                                                                            Pageable pageable) {
        Page<MaintenanceScheduleDto> page = scheduleRepository
                .searchSchedules(keyword, assetId, departmentId, status, pageable)
                .map(MaintenanceScheduleDto::fromEntity);
        return PagedResponse.from(page);
    }

    // =========================================================================
    // SCHEDULED JOB ARCHITECTURE: WORK ORDER GENERATION & REMINDERS
    // =========================================================================

    @Transactional
    public int generateDueMaintenanceWorkOrders() {
        Instant now = Instant.now();
        List<MaintenanceSchedule> dueSchedules = scheduleRepository
                .findByStatusAndNextDueDateLessThanEqual(ScheduleStatus.ACTIVE, now);

        int count = 0;
        for (MaintenanceSchedule schedule : dueSchedules) {
            try {
                if (schedule.isAutoGenerateWorkOrder()) {
                    // Create Work Order
                    WorkOrder wo = new WorkOrder();
                    wo.setWorkOrderNumber(generateWorkOrderNumber());
                    wo.setTitle("PM: " + schedule.getTitle());
                    wo.setDescription("Scheduled Preventive Maintenance for " + schedule.getAsset().getName()
                            + " (" + schedule.getAsset().getAssetCode() + "). "
                            + (schedule.getDescription() != null ? schedule.getDescription() : ""));
                    wo.setDepartment(schedule.getDepartment());
                    wo.setOrganization(schedule.getAsset().getOrganization());
                    wo.setWard(schedule.getAsset().getWard());
                    wo.setAsset(schedule.getAsset());
                    wo.setPriority(schedule.getPriority());
                    wo.setStatus(schedule.getAssignedWorker() != null ? WorkOrderStatus.ASSIGNED : WorkOrderStatus.CREATED);
                    wo.setAssignedWorker(schedule.getAssignedWorker());
                    wo.setAssignedSupervisor(schedule.getAssignedSupervisor());
                    wo.setEstimatedCost(schedule.getEstimatedCost());
                    workOrderRepository.save(wo);

                    // Create Maintenance Reminder
                    Employee recipient = schedule.getAssignedWorker() != null ? schedule.getAssignedWorker() : schedule.getAssignedSupervisor();
                    MaintenanceReminder reminder = new MaintenanceReminder();
                    reminder.setTitle("Maintenance Due: " + schedule.getTitle());
                    reminder.setMessage("Work order " + wo.getWorkOrderNumber() + " has been auto-generated for asset "
                            + schedule.getAsset().getAssetCode() + ".");
                    reminder.setDueDate(schedule.getNextDueDate());
                    reminder.setStatus(ReminderStatus.PENDING);
                    reminder.setReminderType("SCHEDULED_PM");
                    reminder.setSchedule(schedule);
                    reminder.setAsset(schedule.getAsset());
                    reminder.setRecipient(recipient);
                    reminderRepository.save(reminder);

                    count++;
                }

                // Advance schedule nextDueDate
                schedule.setLastRunDate(now);
                schedule.setNextDueDate(calculateNextDueDate(schedule.getFrequency(), schedule.getIntervalDays(), now));
                scheduleRepository.save(schedule);

            } catch (Exception e) {
                log.error("Failed to process maintenance schedule ID {}: {}", schedule.getId(), e.getMessage(), e);
            }
        }
        return count;
    }

    // =========================================================================
    // 3. CONDITION ASSESSMENTS
    // =========================================================================

    @Transactional
    public ConditionAssessmentDto recordConditionAssessment(CreateConditionAssessmentRequest request, String username) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + request.getAssetId()));

        Employee inspector = null;
        if (request.getInspectorId() != null) {
            inspector = employeeRepository.findById(request.getInspectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inspector not found with ID: " + request.getInspectorId()));
        }

        String assessmentCode = request.getAssessmentCode() != null ? request.getAssessmentCode() : generateAssessmentCode();
        if (assessmentRepository.existsByAssessmentCode(assessmentCode)) {
            throw new ConflictException("Assessment with code " + assessmentCode + " already exists");
        }

        // Calculate degradation rate if prior assessment exists
        Optional<ConditionAssessment> prevAssessment = assessmentRepository
                .findFirstByAssetIdOrderByAssessmentDateDesc(asset.getId());

        Integer prevScore = prevAssessment.map(ConditionAssessment::getHealthIndexScore).orElse(null);
        BigDecimal degradationRate = BigDecimal.ZERO;
        if (prevScore != null && prevScore > 0 && prevScore > request.getHealthIndexScore()) {
            double rate = ((prevScore - request.getHealthIndexScore()) / (double) prevScore) * 100.0;
            degradationRate = BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP);
        }

        ConditionAssessment ca = new ConditionAssessment();
        ca.setAssessmentCode(assessmentCode);
        ca.setAsset(asset);
        ca.setInspector(inspector);
        ca.setAssessmentDate(Instant.now());
        ca.setOverallCondition(request.getOverallCondition());
        ca.setHealthIndexScore(request.getHealthIndexScore());
        ca.setPreviousHealthIndexScore(prevScore);
        ca.setDegradationRate(degradationRate);
        ca.setStructuralHealth(request.getStructuralHealth());
        ca.setFunctionalPerformance(request.getFunctionalPerformance());
        ca.setSafetyRiskAssessment(request.getSafetyRiskAssessment());
        ca.setEstimatedRemainingUsefulLifeYears(request.getEstimatedRemainingUsefulLifeYears());
        ca.setAssessmentNotes(request.getAssessmentNotes());
        ca.setPhotosJson(request.getPhotosJson());

        ConditionAssessment saved = assessmentRepository.save(ca);

        // Update asset condition rating
        asset.setConditionRating(request.getOverallCondition());
        assetRepository.save(asset);

        // Proactively generate repair recommendation if condition is POOR or CRITICAL
        if (request.getOverallCondition() == ConditionRating.POOR || request.getOverallCondition() == ConditionRating.CRITICAL) {
            RepairRecommendation rec = new RepairRecommendation();
            rec.setRecommendationCode(generateRecommendationCode());
            rec.setAsset(asset);
            rec.setConditionAssessment(saved);
            rec.setRecommendedAction(request.getOverallCondition() == ConditionRating.CRITICAL ? RecommendationAction.REPLACE : RecommendationAction.REPAIR);
            rec.setPriority(request.getOverallCondition() == ConditionRating.CRITICAL ? WorkOrderPriority.URGENT : WorkOrderPriority.HIGH);
            rec.setTitle("Condition Assessment Action: " + asset.getName() + " rated " + request.getOverallCondition());
            rec.setJustification("Auto-generated from condition assessment " + saved.getAssessmentCode() + " with health score " + request.getHealthIndexScore() + "/100.");
            rec.setStatus(RecommendationStatus.PENDING);
            recommendationRepository.save(rec);
        }

        auditService.record("ConditionAssessment", saved.getId().toString(), AuditAction.CREATE, username,
                "Condition assessment recorded: " + saved.getAssessmentCode() + " for asset " + asset.getAssetCode()
                        + " condition=" + request.getOverallCondition() + " score=" + request.getHealthIndexScore());
        return ConditionAssessmentDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ConditionAssessmentDto getConditionAssessmentById(Long id) {
        ConditionAssessment ca = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Condition assessment not found with ID: " + id));
        return ConditionAssessmentDto.fromEntity(ca);
    }

    @Transactional(readOnly = true)
    public List<ConditionAssessmentDto> getConditionAssessmentsForAsset(Long assetId) {
        return assessmentRepository.findByAssetIdOrderByAssessmentDateDesc(assetId).stream()
                .map(ConditionAssessmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // 4. REPAIR RECOMMENDATIONS
    // =========================================================================

    @Transactional
    public RepairRecommendationDto createRepairRecommendation(CreateRepairRecommendationRequest request, String username) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + request.getAssetId()));

        String code = request.getRecommendationCode() != null ? request.getRecommendationCode() : generateRecommendationCode();
        if (recommendationRepository.existsByRecommendationCode(code)) {
            throw new ConflictException("Recommendation with code " + code + " already exists");
        }

        RepairRecommendation rec = new RepairRecommendation();
        rec.setRecommendationCode(code);
        rec.setAsset(asset);
        if (request.getInspectionId() != null) {
            rec.setInspection(inspectionRepository.findById(request.getInspectionId()).orElse(null));
        }
        if (request.getConditionAssessmentId() != null) {
            rec.setConditionAssessment(assessmentRepository.findById(request.getConditionAssessmentId()).orElse(null));
        }
        rec.setRecommendedAction(request.getRecommendedAction());
        rec.setPriority(request.getPriority() != null ? request.getPriority() : WorkOrderPriority.MEDIUM);
        rec.setTitle(request.getTitle());
        rec.setJustification(request.getJustification());
        rec.setSuggestedScopeOfWork(request.getSuggestedScopeOfWork());
        rec.setEstimatedCost(request.getEstimatedCost() != null ? request.getEstimatedCost() : BigDecimal.ZERO);
        rec.setEstimatedDowntimeHours(request.getEstimatedDowntimeHours() != null ? request.getEstimatedDowntimeHours() : BigDecimal.ZERO);
        rec.setStatus(RecommendationStatus.PENDING);

        RepairRecommendation saved = recommendationRepository.save(rec);
        auditService.record("RepairRecommendation", saved.getId().toString(), AuditAction.CREATE, username,
                "Created repair recommendation: " + saved.getTitle() + " for asset " + asset.getAssetCode());
        return RepairRecommendationDto.fromEntity(saved);
    }

    @Transactional
    public RepairRecommendationDto approveRepairRecommendation(Long id, String username) {
        RepairRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));
        rec.setStatus(RecommendationStatus.APPROVED);
        rec.setReviewedBy(username);
        rec.setReviewedAt(Instant.now());
        RepairRecommendation saved = recommendationRepository.save(rec);
        auditService.record("RepairRecommendation", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Approved repair recommendation: " + saved.getRecommendationCode());
        return RepairRecommendationDto.fromEntity(saved);
    }

    @Transactional
    public RepairRecommendationDto convertRecommendationToWorkOrder(Long id, String username) {
        RepairRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));
        if (rec.getStatus() == RecommendationStatus.CONVERTED_TO_WORK_ORDER) {
            throw new BadRequestException("Recommendation is already converted to a work order");
        }

        Asset asset = rec.getAsset();
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderNumber(generateWorkOrderNumber());
        wo.setTitle("Repair: " + rec.getTitle());
        wo.setDescription("Work order generated from Repair Recommendation " + rec.getRecommendationCode()
                + ". Justification: " + rec.getJustification()
                + (rec.getSuggestedScopeOfWork() != null ? "\nScope: " + rec.getSuggestedScopeOfWork() : ""));
        wo.setDepartment(asset.getDepartment());
        wo.setOrganization(asset.getOrganization());
        wo.setWard(asset.getWard());
        wo.setAsset(asset);
        wo.setPriority(rec.getPriority());
        wo.setStatus(WorkOrderStatus.CREATED);
        wo.setEstimatedCost(rec.getEstimatedCost());
        WorkOrder savedWo = workOrderRepository.save(wo);

        rec.setWorkOrder(savedWo);
        rec.setStatus(RecommendationStatus.CONVERTED_TO_WORK_ORDER);
        rec.setReviewedBy(username);
        rec.setReviewedAt(Instant.now());
        RepairRecommendation savedRec = recommendationRepository.save(rec);

        auditService.record("RepairRecommendation", savedRec.getId().toString(), AuditAction.UPDATE, username,
                "Converted recommendation " + savedRec.getRecommendationCode() + " to Work Order " + savedWo.getWorkOrderNumber());
        return RepairRecommendationDto.fromEntity(savedRec);
    }

    @Transactional(readOnly = true)
    public RepairRecommendationDto getRepairRecommendationById(Long id) {
        RepairRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));
        return RepairRecommendationDto.fromEntity(rec);
    }

    @Transactional(readOnly = true)
    public List<RepairRecommendationDto> getRecommendationsForAsset(Long assetId) {
        return recommendationRepository.findByAssetId(assetId).stream()
                .map(RepairRecommendationDto::fromEntity)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // 5. ASSET FAILURE LOGS
    // =========================================================================

    @Transactional
    public AssetFailureLogDto logAssetFailure(CreateAssetFailureLogRequest request, String username) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + request.getAssetId()));

        Employee reporter = null;
        if (request.getReportedById() != null) {
            reporter = employeeRepository.findById(request.getReportedById())
                    .orElseThrow(() -> new ResourceNotFoundException("Reporter not found with ID: " + request.getReportedById()));
        }

        String code = request.getFailureCode() != null ? request.getFailureCode() : generateFailureCode();
        if (failureLogRepository.existsByFailureCode(code)) {
            throw new ConflictException("Failure log with code " + code + " already exists");
        }

        AssetFailureLog failureLog = new AssetFailureLog();
        failureLog.setFailureCode(code);
        failureLog.setAsset(asset);
        failureLog.setFailureDate(Instant.now());
        failureLog.setReportedBy(reporter);
        failureLog.setFailureCategory(request.getFailureCategory());
        failureLog.setFailureMode(request.getFailureMode());
        failureLog.setRootCause(request.getRootCause());
        failureLog.setSeverity(request.getSeverity() != null ? request.getSeverity() : FailureSeverity.MODERATE);
        failureLog.setDowntimeHours(request.getDowntimeHours() != null ? request.getDowntimeHours() : BigDecimal.ZERO);
        failureLog.setImmediateActionTaken(request.getImmediateActionTaken());
        failureLog.setPreventiveActionRecommended(request.getPreventiveActionRecommended());

        // Auto-create emergency Work Order if requested or if failure is CRITICAL/CATASTROPHIC
        if (request.isCreateWorkOrder() || failureLog.getSeverity() == FailureSeverity.CRITICAL
                || failureLog.getSeverity() == FailureSeverity.CATASTROPHIC) {
            WorkOrder wo = new WorkOrder();
            wo.setWorkOrderNumber(generateWorkOrderNumber());
            wo.setTitle("Breakdown: " + asset.getName() + " - " + failureLog.getFailureCategory());
            wo.setDescription("Emergency breakdown work order from failure log " + code + ". Root cause: " + failureLog.getRootCause());
            wo.setDepartment(asset.getDepartment());
            wo.setOrganization(asset.getOrganization());
            wo.setWard(asset.getWard());
            wo.setAsset(asset);
            wo.setPriority(failureLog.getSeverity() == FailureSeverity.CATASTROPHIC ? WorkOrderPriority.EMERGENCY : WorkOrderPriority.URGENT);
            wo.setStatus(WorkOrderStatus.CREATED);
            WorkOrder savedWo = workOrderRepository.save(wo);
            failureLog.setWorkOrder(savedWo);

            // Update asset status to DEGRADED or OUT_OF_SERVICE
            asset.setStatus(failureLog.getSeverity() == FailureSeverity.CATASTROPHIC ? AssetStatus.OUT_OF_SERVICE : AssetStatus.DEGRADED);
            assetRepository.save(asset);
        }

        AssetFailureLog saved = failureLogRepository.save(failureLog);
        auditService.record("AssetFailureLog", saved.getId().toString(), AuditAction.CREATE, username,
                "Logged asset failure: " + saved.getFailureCode() + " for asset " + asset.getAssetCode() + " severity=" + saved.getSeverity());
        return AssetFailureLogDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<AssetFailureLogDto> getAssetFailureLogsForAsset(Long assetId) {
        return failureLogRepository.findByAssetIdOrderByFailureDateDesc(assetId).stream()
                .map(AssetFailureLogDto::fromEntity)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // 6. MAINTENANCE REMINDERS
    // =========================================================================

    @Transactional(readOnly = true)
    public PagedResponse<MaintenanceReminderDto> getRemindersForUser(Long employeeId, Pageable pageable) {
        Page<MaintenanceReminderDto> page = reminderRepository
                .findByRecipientIdOrderByDueDateAsc(employeeId, pageable)
                .map(MaintenanceReminderDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional
    public MaintenanceReminderDto acknowledgeReminder(Long id, String username) {
        MaintenanceReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + id));
        reminder.setStatus(ReminderStatus.ACKNOWLEDGED);
        reminder.setAcknowledgedAt(Instant.now());
        MaintenanceReminder saved = reminderRepository.save(reminder);
        return MaintenanceReminderDto.fromEntity(saved);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    public Instant calculateNextDueDate(RecurrenceFrequency frequency, Integer intervalDays, Instant baseDate) {
        LocalDate base = baseDate.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate next;
        switch (frequency) {
            case DAILY -> next = base.plusDays(1);
            case WEEKLY -> next = base.plusWeeks(1);
            case BIWEEKLY -> next = base.plusWeeks(2);
            case MONTHLY -> next = base.plusMonths(1);
            case QUARTERLY -> next = base.plusMonths(3);
            case SEMI_ANNUALLY -> next = base.plusMonths(6);
            case ANNUALLY -> next = base.plusYears(1);
            case CUSTOM -> next = base.plusDays(intervalDays != null ? intervalDays : 30);
            default -> next = base.plusMonths(1);
        }
        return next.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private int calculateDefaultIntervalDays(RecurrenceFrequency frequency) {
        return switch (frequency) {
            case DAILY -> 1;
            case WEEKLY -> 7;
            case BIWEEKLY -> 14;
            case MONTHLY -> 30;
            case QUARTERLY -> 90;
            case SEMI_ANNUALLY -> 180;
            case ANNUALLY -> 365;
            case CUSTOM -> 30;
        };
    }

    private synchronized String generatePlanCode() {
        long count = inspectionPlanRepository.count() + 1;
        return "PLAN-" + String.format("%04d", count);
    }

    private synchronized String generateScheduleCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = scheduleRepository.count() + 1;
        return "SCH-" + date + "-" + String.format("%04d", count);
    }

    private synchronized String generateAssessmentCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long rand = ThreadLocalRandom.current().nextLong(1000, 9999);
        return "CA-" + date + "-" + rand;
    }

    private synchronized String generateRecommendationCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long rand = ThreadLocalRandom.current().nextLong(1000, 9999);
        return "REC-" + date + "-" + rand;
    }

    private synchronized String generateFailureCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long rand = ThreadLocalRandom.current().nextLong(1000, 9999);
        return "FAIL-" + date + "-" + rand;
    }

    private synchronized String generateWorkOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long rand = ThreadLocalRandom.current().nextLong(10000, 99999);
        return "WO-PM-" + date + "-" + rand;
    }
}
