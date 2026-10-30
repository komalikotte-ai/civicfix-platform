package com.civicfix.platform.assets.service;

import com.civicfix.platform.assets.dto.*;
import com.civicfix.platform.assets.entity.*;
import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.assets.repository.AssetInspectionRepository;
import com.civicfix.platform.assets.repository.AssetRepairRepository;
import com.civicfix.platform.assets.repository.AssetReplacementRepository;
import com.civicfix.platform.assets.repository.AssetRepository;
import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.complaints.dto.ComplaintDto;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.repository.ComplaintRepository;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.organizations.repository.OrganizationRepository;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.repository.EmployeeRepository;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.wards.repository.WardRepository;
import com.civicfix.platform.workorders.dto.WorkOrderDto;
import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetService.class);

    private final AssetRepository assetRepository;
    private final AssetInspectionRepository inspectionRepository;
    private final AssetRepairRepository repairRepository;
    private final AssetReplacementRepository replacementRepository;
    private final DepartmentRepository departmentRepository;
    private final WardRepository wardRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final ComplaintRepository complaintRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AuditService auditService;

    public AssetService(AssetRepository assetRepository,
                        AssetInspectionRepository inspectionRepository,
                        AssetRepairRepository repairRepository,
                        AssetReplacementRepository replacementRepository,
                        DepartmentRepository departmentRepository,
                        WardRepository wardRepository,
                        OrganizationRepository organizationRepository,
                        EmployeeRepository employeeRepository,
                        ComplaintRepository complaintRepository,
                        WorkOrderRepository workOrderRepository,
                        AuditService auditService) {
        this.assetRepository = assetRepository;
        this.inspectionRepository = inspectionRepository;
        this.repairRepository = repairRepository;
        this.replacementRepository = replacementRepository;
        this.departmentRepository = departmentRepository;
        this.wardRepository = wardRepository;
        this.organizationRepository = organizationRepository;
        this.employeeRepository = employeeRepository;
        this.complaintRepository = complaintRepository;
        this.workOrderRepository = workOrderRepository;
        this.auditService = auditService;
    }

    /**
     * Registers a new infrastructure asset.
     */
    @Transactional
    public AssetDto createAsset(CreateAssetRequest request, String username) {
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + request.getOrganizationId()));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        Ward ward = null;
        if (request.getWardId() != null) {
            ward = wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + request.getWardId()));
        }

        Employee custodian = null;
        if (request.getCustodianEmployeeId() != null) {
            custodian = employeeRepository.findById(request.getCustodianEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Custodian employee not found with ID: " + request.getCustodianEmployeeId()));
        }

        String assetCode = generateUniqueAssetCode(request.getAssetType());

        Asset asset = new Asset();
        asset.setAssetCode(assetCode);
        asset.setName(request.getName());
        asset.setAssetType(request.getAssetType());
        asset.setOrganization(organization);
        asset.setDepartment(department);
        asset.setWard(ward);
        asset.setDescription(request.getDescription());
        asset.setStatus(request.getStatus() != null ? request.getStatus() : AssetStatus.OPERATIONAL);
        asset.setConditionRating(request.getConditionRating() != null ? request.getConditionRating() : ConditionRating.GOOD);
        asset.setAddressLine(request.getAddressLine());
        asset.setLatitude(request.getLatitude());
        asset.setLongitude(request.getLongitude());
        asset.setGisGeometry(request.getGisGeometry());
        asset.setInstallationDate(request.getInstallationDate());
        asset.setCommissionedDate(request.getCommissionedDate());
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        asset.setExpectedLifecycleYears(request.getExpectedLifecycleYears());
        asset.setEstimatedReplacementCost(request.getEstimatedReplacementCost() != null ? request.getEstimatedReplacementCost() : BigDecimal.ZERO);
        asset.setManufacturer(request.getManufacturer());
        asset.setModelNumber(request.getModelNumber());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setSpecificationsJson(request.getSpecificationsJson());
        asset.setCustodian(custodian);
        asset.setActive(true);
        asset.setCreatedBy(username);

        Asset saved = assetRepository.save(asset);

        auditService.record("Asset", saved.getId().toString(), AuditAction.CREATE, username,
                "Registered infrastructure asset " + saved.getAssetCode() + " (" + saved.getName() + ")");

        log.info("Asset created successfully: code={}, id={}, type={}", saved.getAssetCode(), saved.getId(), saved.getAssetType());
        return AssetDto.fromEntity(saved);
    }

    /**
     * Updates an existing infrastructure asset.
     */
    @Transactional
    public AssetDto updateAsset(Long id, UpdateAssetRequest request, String username) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            asset.setName(request.getName());
        }

        if (request.getAssetType() != null) {
            asset.setAssetType(request.getAssetType());
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));
            asset.setDepartment(department);
        }

        if (request.getWardId() != null) {
            Ward ward = wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + request.getWardId()));
            asset.setWard(ward);
        }

        if (request.getCustodianEmployeeId() != null) {
            Employee custodian = employeeRepository.findById(request.getCustodianEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Custodian employee not found with ID: " + request.getCustodianEmployeeId()));
            asset.setCustodian(custodian);
        }

        if (request.getDescription() != null) {
            asset.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            asset.setStatus(request.getStatus());
        }

        if (request.getConditionRating() != null) {
            asset.setConditionRating(request.getConditionRating());
        }

        if (request.getAddressLine() != null) {
            asset.setAddressLine(request.getAddressLine());
        }

        if (request.getLatitude() != null) {
            asset.setLatitude(request.getLatitude());
        }

        if (request.getLongitude() != null) {
            asset.setLongitude(request.getLongitude());
        }

        if (request.getGisGeometry() != null) {
            asset.setGisGeometry(request.getGisGeometry());
        }

        if (request.getInstallationDate() != null) {
            asset.setInstallationDate(request.getInstallationDate());
        }

        if (request.getCommissionedDate() != null) {
            asset.setCommissionedDate(request.getCommissionedDate());
        }

        if (request.getWarrantyExpiryDate() != null) {
            asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        }

        if (request.getExpectedLifecycleYears() != null) {
            asset.setExpectedLifecycleYears(request.getExpectedLifecycleYears());
        }

        if (request.getEstimatedReplacementCost() != null) {
            asset.setEstimatedReplacementCost(request.getEstimatedReplacementCost());
        }

        if (request.getManufacturer() != null) {
            asset.setManufacturer(request.getManufacturer());
        }

        if (request.getModelNumber() != null) {
            asset.setModelNumber(request.getModelNumber());
        }

        if (request.getSerialNumber() != null) {
            asset.setSerialNumber(request.getSerialNumber());
        }

        if (request.getSpecificationsJson() != null) {
            asset.setSpecificationsJson(request.getSpecificationsJson());
        }

        if (request.getActive() != null) {
            asset.setActive(request.getActive());
        }

        asset.setModifiedBy(username);
        Asset saved = assetRepository.save(asset);

        auditService.record("Asset", saved.getId().toString(), AuditAction.UPDATE, username,
                "Updated infrastructure asset " + saved.getAssetCode());

        return AssetDto.fromEntity(saved);
    }

    /**
     * Retrieves an asset by ID.
     */
    @Transactional(readOnly = true)
    public AssetDto getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + id));
        return AssetDto.fromEntity(asset);
    }

    /**
     * Retrieves an asset by unique asset code.
     */
    @Transactional(readOnly = true)
    public AssetDto getAssetByCode(String assetCode) {
        Asset asset = assetRepository.findByAssetCode(assetCode)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with code: " + assetCode));
        return AssetDto.fromEntity(asset);
    }

    /**
     * Searches and filters assets with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<AssetDto> searchAssets(String keyword,
                                                AssetType assetType,
                                                AssetStatus status,
                                                ConditionRating conditionRating,
                                                Long departmentId,
                                                Long wardId,
                                                Boolean active,
                                                Pageable pageable) {
        Page<Asset> page = assetRepository.searchAssets(
                (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
                assetType,
                status,
                conditionRating,
                departmentId,
                wardId,
                active,
                pageable
        );
        return PagedResponse.from(page.map(AssetDto::fromEntity));
    }

    /**
     * Records a physical inspection for an asset and updates its condition/operational status.
     */
    @Transactional
    public AssetInspectionDto recordInspection(Long assetId, RecordInspectionRequest request, String username) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));

        WorkOrder workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + request.getWorkOrderId()));
        }

        Employee inspector = null;
        if (request.getInspectorId() != null) {
            inspector = employeeRepository.findById(request.getInspectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inspector not found with ID: " + request.getInspectorId()));
        }

        AssetInspection inspection = new AssetInspection();
        inspection.setAsset(asset);
        inspection.setWorkOrder(workOrder);
        inspection.setInspector(inspector);
        inspection.setInspectionDate(Instant.now());
        inspection.setConditionRating(request.getConditionRating());
        inspection.setOperationalStatus(request.getOperationalStatus());
        inspection.setFindings(request.getFindings());
        inspection.setRecommendations(request.getRecommendations());
        inspection.setDefectsObserved(request.getDefectsObserved());
        inspection.setScore(request.getScore());
        inspection.setPhotosJson(request.getPhotosJson());
        inspection.setCreatedBy(username);

        AssetInspection saved = inspectionRepository.save(inspection);

        // Update the asset's condition rating, status, and last inspection timestamp
        asset.setConditionRating(request.getConditionRating());
        asset.setStatus(request.getOperationalStatus());
        asset.setLastInspectedAt(saved.getInspectionDate());
        asset.setModifiedBy(username);
        assetRepository.save(asset);

        auditService.record("Asset", asset.getId().toString(), AuditAction.UPDATE, username,
                "Recorded inspection for asset " + asset.getAssetCode() + ", condition=" + request.getConditionRating());

        log.info("Recorded inspection for asset: code={}, condition={}, status={}",
                asset.getAssetCode(), request.getConditionRating(), request.getOperationalStatus());
        return AssetInspectionDto.fromEntity(saved);
    }

    /**
     * Retrieves all inspection histories for an asset.
     */
    @Transactional(readOnly = true)
    public List<AssetInspectionDto> getInspections(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with ID: " + assetId);
        }
        return inspectionRepository.findByAssetIdOrderByInspectionDateDesc(assetId)
                .stream()
                .map(AssetInspectionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Records a repair activity for an asset and updates its condition/repair timestamp.
     */
    @Transactional
    public AssetRepairDto recordRepair(Long assetId, RecordRepairRequest request, String username) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));

        WorkOrder workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + request.getWorkOrderId()));
        }

        Complaint complaint = null;
        if (request.getComplaintId() != null) {
            complaint = complaintRepository.findById(request.getComplaintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + request.getComplaintId()));
        }

        Employee repairedBy = null;
        if (request.getRepairedById() != null) {
            repairedBy = employeeRepository.findById(request.getRepairedById())
                    .orElseThrow(() -> new ResourceNotFoundException("Repairer not found with ID: " + request.getRepairedById()));
        }

        AssetRepair repair = new AssetRepair();
        repair.setAsset(asset);
        repair.setWorkOrder(workOrder);
        repair.setComplaint(complaint);
        repair.setRepairType(request.getRepairType() != null ? request.getRepairType() : "CORRECTIVE");
        repair.setRepairDate(Instant.now());
        repair.setRepairedBy(repairedBy);
        repair.setCost(request.getCost() != null ? request.getCost() : BigDecimal.ZERO);
        repair.setDescription(request.getDescription());
        repair.setPartsReplaced(request.getPartsReplaced());
        repair.setBeforeCondition(request.getBeforeCondition() != null ? request.getBeforeCondition() : asset.getConditionRating());
        repair.setAfterCondition(request.getAfterCondition());
        repair.setDowntimeHours(request.getDowntimeHours() != null ? request.getDowntimeHours() : BigDecimal.ZERO);
        repair.setCreatedBy(username);

        AssetRepair saved = repairRepository.save(repair);

        // Update asset's last repaired timestamp and condition if afterCondition is provided
        asset.setLastRepairedAt(saved.getRepairDate());
        if (request.getAfterCondition() != null) {
            asset.setConditionRating(request.getAfterCondition());
        }
        // If status was OUT_OF_SERVICE or UNDER_MAINTENANCE and repair is completed, return to OPERATIONAL
        if (asset.getStatus() == AssetStatus.UNDER_MAINTENANCE || asset.getStatus() == AssetStatus.OUT_OF_SERVICE) {
            asset.setStatus(AssetStatus.OPERATIONAL);
        }
        asset.setModifiedBy(username);
        assetRepository.save(asset);

        auditService.record("Asset", asset.getId().toString(), AuditAction.UPDATE, username,
                "Recorded repair for asset " + asset.getAssetCode() + ", cost=" + saved.getCost());

        log.info("Recorded repair for asset: code={}, cost={}", asset.getAssetCode(), saved.getCost());
        return AssetRepairDto.fromEntity(saved);
    }

    /**
     * Retrieves all repair histories for an asset.
     */
    @Transactional(readOnly = true)
    public List<AssetRepairDto> getRepairs(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with ID: " + assetId);
        }
        return repairRepository.findByAssetIdOrderByRepairDateDesc(assetId)
                .stream()
                .map(AssetRepairDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Records an asset replacement event.
     */
    @Transactional
    public AssetReplacementDto recordReplacement(Long assetId, RecordReplacementRequest request, String username) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));

        Asset replacedAsset = null;
        if (request.getReplacedAssetId() != null) {
            replacedAsset = assetRepository.findById(request.getReplacedAssetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Replaced asset not found with ID: " + request.getReplacedAssetId()));

            // Decommission the replaced asset
            replacedAsset.setStatus(AssetStatus.DECOMMISSIONED);
            replacedAsset.setActive(false);
            replacedAsset.setModifiedBy(username);
            assetRepository.save(replacedAsset);
        }

        AssetReplacement replacement = new AssetReplacement();
        replacement.setAsset(asset);
        replacement.setReplacedAsset(replacedAsset);
        replacement.setReplacementDate(Instant.now());
        replacement.setReplacementReason(request.getReplacementReason());
        replacement.setReplacementCost(request.getReplacementCost() != null ? request.getReplacementCost() : BigDecimal.ZERO);
        replacement.setApprovedBy(request.getApprovedBy());
        replacement.setDisposalMethod(request.getDisposalMethod());
        replacement.setSalvageValue(request.getSalvageValue() != null ? request.getSalvageValue() : BigDecimal.ZERO);
        replacement.setCreatedBy(username);

        AssetReplacement saved = replacementRepository.save(replacement);

        // Ensure target asset is marked OPERATIONAL
        asset.setStatus(AssetStatus.OPERATIONAL);
        asset.setActive(true);
        asset.setModifiedBy(username);
        assetRepository.save(asset);

        auditService.record("Asset", asset.getId().toString(), AuditAction.UPDATE, username,
                "Recorded replacement event for asset " + asset.getAssetCode());

        return AssetReplacementDto.fromEntity(saved);
    }

    /**
     * Retrieves all replacement histories for an asset.
     */
    @Transactional(readOnly = true)
    public List<AssetReplacementDto> getReplacements(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with ID: " + assetId);
        }
        return replacementRepository.findByAssetIdOrderByReplacementDateDesc(assetId)
                .stream()
                .map(AssetReplacementDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Links an existing complaint to an infrastructure asset.
     */
    @Transactional
    public ComplaintDto linkComplaintToAsset(Long assetId, Long complaintId, String username) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        complaint.setAsset(asset);
        complaint.setModifiedBy(username);
        Complaint saved = complaintRepository.save(complaint);

        auditService.record("Complaint", complaint.getId().toString(), AuditAction.UPDATE, username,
                "Linked complaint " + complaint.getTrackingNumber() + " to asset " + asset.getAssetCode());

        return ComplaintDto.fromEntity(saved);
    }

    /**
     * Links an existing work order to an infrastructure asset.
     */
    @Transactional
    public WorkOrderDto linkWorkOrderToAsset(Long assetId, Long workOrderId, String username) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + workOrderId));

        workOrder.setAsset(asset);
        workOrder.setModifiedBy(username);
        WorkOrder saved = workOrderRepository.save(workOrder);

        auditService.record("WorkOrder", workOrder.getId().toString(), AuditAction.UPDATE, username,
                "Linked work order " + workOrder.getWorkOrderNumber() + " to asset " + asset.getAssetCode());

        return WorkOrderDto.fromEntity(saved);
    }

    /**
     * Retrieves all complaints associated with an asset.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ComplaintDto> getComplaintsForAsset(Long assetId, Pageable pageable) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with ID: " + assetId);
        }
        Page<Complaint> page = complaintRepository.findByAssetId(assetId, pageable);
        return PagedResponse.from(page.map(ComplaintDto::fromEntity));
    }

    /**
     * Retrieves all work orders associated with an asset.
     */
    @Transactional(readOnly = true)
    public PagedResponse<WorkOrderDto> getWorkOrdersForAsset(Long assetId, Pageable pageable) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with ID: " + assetId);
        }
        Page<WorkOrder> page = workOrderRepository.findByAssetId(assetId, pageable);
        return PagedResponse.from(page.map(WorkOrderDto::fromEntity));
    }

    /**
     * Generates a unique, formatted asset code.
     */
    public synchronized String generateUniqueAssetCode(AssetType type) {
        String typePrefix = switch (type) {
            case ROADS -> "RD";
            case STREETLIGHTS -> "SL";
            case WATER_PIPELINES -> "WP";
            case DRAINAGE -> "DR";
            case PUBLIC_BUILDINGS -> "PB";
            case PARKS -> "PK";
            case BRIDGES -> "BR";
            case TRAFFIC_SIGNALS -> "TS";
        };

        String datePrefix = "AST-" + typePrefix + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        for (int i = 0; i < 10; i++) {
            int randomSeq = ThreadLocalRandom.current().nextInt(1000, 9999);
            String candidate = datePrefix + randomSeq;
            if (!assetRepository.existsByAssetCode(candidate)) {
                return candidate;
            }
        }
        return datePrefix + (System.currentTimeMillis() % 10000);
    }
}
