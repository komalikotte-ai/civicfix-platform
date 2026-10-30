package com.civicfix.platform.assets;

import com.civicfix.platform.assets.dto.*;
import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.assets.service.AssetService;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.complaints.dto.ComplaintDto;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.workorders.WorkOrderTestMocks;
import com.civicfix.platform.workorders.dto.WorkOrderDto;
import com.civicfix.platform.workorders.entity.WorkOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssetRegistryTest {

    private AssetService assetService;

    private AssetTestMocks.MockAssetRepository assetRepo;
    private AssetTestMocks.MockAssetInspectionRepository inspectionRepo;
    private AssetTestMocks.MockAssetRepairRepository repairRepo;
    private AssetTestMocks.MockAssetReplacementRepository replacementRepo;
    private AssetTestMocks.MockOrganizationRepository orgRepo;
    private ComplaintTestMocks.MockDepartmentRepository deptRepo;
    private ComplaintTestMocks.MockWardRepository wardRepo;
    private ComplaintTestMocks.MockEmployeeRepository empRepo;
    private ComplaintTestMocks.MockComplaintRepository complaintRepo;
    private WorkOrderTestMocks.MockWorkOrderRepository workOrderRepo;
    private WorkOrderTestMocks.MockAuditService auditService;

    private Organization organization;
    private Department department;
    private Ward ward;
    private Employee custodian;
    private Employee inspector;

    @BeforeEach
    void setUp() {
        assetRepo = new AssetTestMocks.MockAssetRepository();
        inspectionRepo = new AssetTestMocks.MockAssetInspectionRepository();
        repairRepo = new AssetTestMocks.MockAssetRepairRepository();
        replacementRepo = new AssetTestMocks.MockAssetReplacementRepository();
        orgRepo = new AssetTestMocks.MockOrganizationRepository();
        deptRepo = new ComplaintTestMocks.MockDepartmentRepository();
        wardRepo = new ComplaintTestMocks.MockWardRepository();
        empRepo = new ComplaintTestMocks.MockEmployeeRepository();
        complaintRepo = new ComplaintTestMocks.MockComplaintRepository();
        workOrderRepo = new WorkOrderTestMocks.MockWorkOrderRepository();
        auditService = new WorkOrderTestMocks.MockAuditService();

        organization = new Organization();
        organization.setId(1L);
        organization.setName("Metropolis Municipal Corp");
        organization.setCode("MMC");
        orgRepo.save(organization);

        department = new Department();
        department.setId(10L);
        department.setName("Public Works Department");
        department.setCode("PWD");
        department.setOrganization(organization);
        deptRepo.save(department);

        ward = new Ward();
        ward.setId(100L);
        ward.setName("Ward 12 - Downtown Central");
        ward.setWardNumber("W-12");
        ward.setOrganization(organization);
        wardRepo.save(ward);

        custodian = new Employee();
        custodian.setId(50L);
        custodian.setFullName("Rajesh Kumar");
        custodian.setEmployeeCode("EMP-50");
        custodian.setActive(true);
        empRepo.save(custodian);

        inspector = new Employee();
        inspector.setId(51L);
        inspector.setFullName("Vikram Singh");
        inspector.setEmployeeCode("EMP-51");
        inspector.setActive(true);
        empRepo.save(inspector);

        assetService = new AssetService(
                assetRepo,
                inspectionRepo,
                repairRepo,
                replacementRepo,
                deptRepo,
                wardRepo,
                orgRepo,
                empRepo,
                complaintRepo,
                workOrderRepo,
                auditService
        );
    }

    @Test
    @DisplayName("Should register assets across all eight required municipal asset types")
    void testCreateAllAssetTypes() {
        AssetType[] allTypes = {
                AssetType.ROADS,
                AssetType.STREETLIGHTS,
                AssetType.WATER_PIPELINES,
                AssetType.DRAINAGE,
                AssetType.PUBLIC_BUILDINGS,
                AssetType.PARKS,
                AssetType.BRIDGES,
                AssetType.TRAFFIC_SIGNALS
        };

        for (AssetType type : allTypes) {
            CreateAssetRequest req = new CreateAssetRequest();
            req.setName("Municipal " + type.name());
            req.setAssetType(type);
            req.setOrganizationId(organization.getId());
            req.setDepartmentId(department.getId());
            req.setWardId(ward.getId());
            req.setDescription("Primary municipal asset for " + type);
            req.setAddressLine("Central Plaza, Sector 4");
            req.setLatitude(new BigDecimal("28.6139"));
            req.setLongitude(new BigDecimal("77.2090"));
            req.setEstimatedReplacementCost(new BigDecimal("500000.00"));
            req.setExpectedLifecycleYears(15);
            req.setCustodianEmployeeId(custodian.getId());

            AssetDto created = assetService.createAsset(req, "test_admin");

            assertNotNull(created.getId());
            assertNotNull(created.getAssetCode());
            assertTrue(created.getAssetCode().startsWith("AST-"));
            assertEquals(type, created.getAssetType());
            assertEquals(organization.getId(), created.getOrganizationId());
            assertEquals(department.getId(), created.getDepartmentId());
            assertEquals(ward.getId(), created.getWardId());
            assertEquals(AssetStatus.OPERATIONAL, created.getStatus());
            assertEquals(ConditionRating.GOOD, created.getConditionRating());
            assertEquals(custodian.getId(), created.getCustodianEmployeeId());
            assertTrue(created.isActive());
        }

        assertEquals(8, assetRepo.map.size());
    }

    @Test
    @DisplayName("Should generate unique formatted asset codes with type abbreviations")
    void testUniqueAssetCodeGeneration() {
        String codeRd = assetService.generateUniqueAssetCode(AssetType.ROADS);
        String codeSl = assetService.generateUniqueAssetCode(AssetType.STREETLIGHTS);
        String codeWp = assetService.generateUniqueAssetCode(AssetType.WATER_PIPELINES);
        String codeDr = assetService.generateUniqueAssetCode(AssetType.DRAINAGE);
        String codePb = assetService.generateUniqueAssetCode(AssetType.PUBLIC_BUILDINGS);
        String codePk = assetService.generateUniqueAssetCode(AssetType.PARKS);
        String codeBr = assetService.generateUniqueAssetCode(AssetType.BRIDGES);
        String codeTs = assetService.generateUniqueAssetCode(AssetType.TRAFFIC_SIGNALS);

        assertTrue(codeRd.contains("AST-RD-"));
        assertTrue(codeSl.contains("AST-SL-"));
        assertTrue(codeWp.contains("AST-WP-"));
        assertTrue(codeDr.contains("AST-DR-"));
        assertTrue(codePb.contains("AST-PB-"));
        assertTrue(codePk.contains("AST-PK-"));
        assertTrue(codeBr.contains("AST-BR-"));
        assertTrue(codeTs.contains("AST-TS-"));
    }

    @Test
    @DisplayName("Should retrieve asset by ID and by unique code")
    void testGetAssetByIdAndCode() {
        CreateAssetRequest req = new CreateAssetRequest();
        req.setName("Central Flyover Bridge");
        req.setAssetType(AssetType.BRIDGES);
        req.setOrganizationId(organization.getId());
        req.setDepartmentId(department.getId());
        req.setConditionRating(ConditionRating.EXCELLENT);

        AssetDto created = assetService.createAsset(req, "admin");

        AssetDto fetchedById = assetService.getAssetById(created.getId());
        assertEquals(created.getId(), fetchedById.getId());
        assertEquals("Central Flyover Bridge", fetchedById.getName());

        AssetDto fetchedByCode = assetService.getAssetByCode(created.getAssetCode());
        assertEquals(created.getId(), fetchedByCode.getId());
        assertEquals(created.getAssetCode(), fetchedByCode.getAssetCode());

        assertThrows(ResourceNotFoundException.class, () -> assetService.getAssetById(99999L));
        assertThrows(ResourceNotFoundException.class, () -> assetService.getAssetByCode("NON-EXISTENT"));
    }

    @Test
    @DisplayName("Should update asset technical specifications, condition rating and location")
    void testUpdateAsset() {
        CreateAssetRequest req = new CreateAssetRequest();
        req.setName("Old Drainage Line");
        req.setAssetType(AssetType.DRAINAGE);
        req.setOrganizationId(organization.getId());
        req.setDepartmentId(department.getId());
        req.setConditionRating(ConditionRating.FAIR);

        AssetDto created = assetService.createAsset(req, "admin");

        UpdateAssetRequest updateReq = new UpdateAssetRequest();
        updateReq.setName("Upgraded Storm Drainage System");
        updateReq.setConditionRating(ConditionRating.GOOD);
        updateReq.setStatus(AssetStatus.OPERATIONAL);
        updateReq.setGisGeometry("{\"type\":\"LineString\",\"coordinates\":[[77.20,28.61],[77.21,28.62]]}");
        updateReq.setSpecificationsJson("{\"pipe_diameter_mm\": 1200, \"material\": \"RCC\"}");
        updateReq.setExpectedLifecycleYears(25);
        updateReq.setInstallationDate(LocalDate.of(2025, 1, 15));

        AssetDto updated = assetService.updateAsset(created.getId(), updateReq, "admin");

        assertEquals("Upgraded Storm Drainage System", updated.getName());
        assertEquals(ConditionRating.GOOD, updated.getConditionRating());
        assertEquals(AssetStatus.OPERATIONAL, updated.getStatus());
        assertEquals("{\"pipe_diameter_mm\": 1200, \"material\": \"RCC\"}", updated.getSpecificationsJson());
        assertEquals(25, updated.getExpectedLifecycleYears());
        assertEquals(LocalDate.of(2025, 1, 15), updated.getInstallationDate());
    }

    @Test
    @DisplayName("Should search and filter assets by keyword, asset type, status, and condition")
    void testSearchAndFilterAssets() {
        createTestAsset("Main Avenue Road", AssetType.ROADS, AssetStatus.OPERATIONAL, ConditionRating.GOOD);
        createTestAsset("Ring Road North", AssetType.ROADS, AssetStatus.UNDER_MAINTENANCE, ConditionRating.POOR);
        createTestAsset("Downtown Streetlight Cluster", AssetType.STREETLIGHTS, AssetStatus.OPERATIONAL, ConditionRating.EXCELLENT);
        createTestAsset("City Hall Public Building", AssetType.PUBLIC_BUILDINGS, AssetStatus.OPERATIONAL, ConditionRating.GOOD);

        // Filter by type ROADS
        PagedResponse<AssetDto> roads = assetService.searchAssets(
                null, AssetType.ROADS, null, null, null, null, null, PageRequest.of(0, 10));
        assertEquals(2, roads.getContent().size());

        // Filter by keyword "Road"
        PagedResponse<AssetDto> keywordSearch = assetService.searchAssets(
                "Road", null, null, null, null, null, null, PageRequest.of(0, 10));
        assertEquals(2, keywordSearch.getContent().size());

        // Filter by status UNDER_MAINTENANCE
        PagedResponse<AssetDto> underMaint = assetService.searchAssets(
                null, null, AssetStatus.UNDER_MAINTENANCE, null, null, null, null, PageRequest.of(0, 10));
        assertEquals(1, underMaint.getContent().size());
        assertEquals("Ring Road North", underMaint.getContent().get(0).getName());

        // Filter by condition POOR
        PagedResponse<AssetDto> poorCondition = assetService.searchAssets(
                null, null, null, ConditionRating.POOR, null, null, null, PageRequest.of(0, 10));
        assertEquals(1, poorCondition.getContent().size());
    }

    @Test
    @DisplayName("Should record inspection, update asset health rating, and persist inspection history")
    void testRecordInspectionUpdatesAssetConditionAndStatus() {
        AssetDto asset = createTestAsset("North Water Pipeline", AssetType.WATER_PIPELINES, AssetStatus.OPERATIONAL, ConditionRating.GOOD);

        RecordInspectionRequest inspReq = new RecordInspectionRequest();
        inspReq.setInspectorId(inspector.getId());
        inspReq.setConditionRating(ConditionRating.POOR);
        inspReq.setOperationalStatus(AssetStatus.DEGRADED);
        inspReq.setFindings("Severe corrosion detected near joint 4B; minor leakage observed");
        inspReq.setRecommendations("Schedule corrective pipe welding immediately");
        inspReq.setDefectsObserved("Joint rust, pressure drop to 2.1 bar");
        inspReq.setScore(45);

        AssetInspectionDto recorded = assetService.recordInspection(asset.getId(), inspReq, "inspector_user");

        assertNotNull(recorded.getId());
        assertEquals(asset.getId(), recorded.getAssetId());
        assertEquals(inspector.getId(), recorded.getInspectorId());
        assertEquals(ConditionRating.POOR, recorded.getConditionRating());
        assertEquals(AssetStatus.DEGRADED, recorded.getOperationalStatus());
        assertEquals(45, recorded.getScore());

        // Verify the asset itself was updated
        AssetDto updatedAsset = assetService.getAssetById(asset.getId());
        assertEquals(ConditionRating.POOR, updatedAsset.getConditionRating());
        assertEquals(AssetStatus.DEGRADED, updatedAsset.getStatus());
        assertNotNull(updatedAsset.getLastInspectedAt());

        // Verify inspection history retrieval
        List<AssetInspectionDto> history = assetService.getInspections(asset.getId());
        assertEquals(1, history.size());
        assertEquals("Severe corrosion detected near joint 4B; minor leakage observed", history.get(0).getFindings());
    }

    @Test
    @DisplayName("Should record repair, update asset condition, and persist repair history")
    void testRecordRepairUpdatesAssetConditionAndLastRepairedAt() {
        AssetDto asset = createTestAsset("High St Traffic Signal", AssetType.TRAFFIC_SIGNALS, AssetStatus.OUT_OF_SERVICE, ConditionRating.CRITICAL);

        RecordRepairRequest repairReq = new RecordRepairRequest();
        repairReq.setRepairType("EMERGENCY_REPLACEMENT");
        repairReq.setRepairedById(custodian.getId());
        repairReq.setCost(new BigDecimal("15500.00"));
        repairReq.setDescription("Replaced damaged microcontroller motherboard and burnt LED arrays");
        repairReq.setPartsReplaced("Microcontroller MCU-88, 3x LED Cluster Lens");
        repairReq.setBeforeCondition(ConditionRating.CRITICAL);
        repairReq.setAfterCondition(ConditionRating.GOOD);
        repairReq.setDowntimeHours(new BigDecimal("4.5"));

        AssetRepairDto recorded = assetService.recordRepair(asset.getId(), repairReq, "repair_crew");

        assertNotNull(recorded.getId());
        assertEquals(asset.getId(), recorded.getAssetId());
        assertEquals(new BigDecimal("15500.00"), recorded.getCost());
        assertEquals(ConditionRating.GOOD, recorded.getAfterCondition());

        // Verify the asset state was restored to OPERATIONAL and condition rating improved to GOOD
        AssetDto updatedAsset = assetService.getAssetById(asset.getId());
        assertEquals(ConditionRating.GOOD, updatedAsset.getConditionRating());
        assertEquals(AssetStatus.OPERATIONAL, updatedAsset.getStatus());
        assertNotNull(updatedAsset.getLastRepairedAt());

        // Verify repair history retrieval
        List<AssetRepairDto> repairs = assetService.getRepairs(asset.getId());
        assertEquals(1, repairs.size());
        assertEquals("Microcontroller MCU-88, 3x LED Cluster Lens", repairs.get(0).getPartsReplaced());
    }

    @Test
    @DisplayName("Should record asset replacement and decommission old asset")
    void testRecordReplacementDecommissionsOldAsset() {
        AssetDto oldAsset = createTestAsset("Old Water Pump Station", AssetType.WATER_PIPELINES, AssetStatus.OUT_OF_SERVICE, ConditionRating.CRITICAL);
        AssetDto newAsset = createTestAsset("New VFD Pump Station Unit 1", AssetType.WATER_PIPELINES, AssetStatus.PLANNING, ConditionRating.EXCELLENT);

        RecordReplacementRequest replReq = new RecordReplacementRequest();
        replReq.setReplacedAssetId(oldAsset.getId());
        replReq.setReplacementReason("Old pump reached end of 30-year lifecycle; continuous motor failures");
        replReq.setReplacementCost(new BigDecimal("1200000.00"));
        replReq.setApprovedBy("Chief Municipal Engineer");
        replReq.setDisposalMethod("AUCTION");
        replReq.setSalvageValue(new BigDecimal("75000.00"));

        AssetReplacementDto recorded = assetService.recordReplacement(newAsset.getId(), replReq, "commissioner");

        assertNotNull(recorded.getId());
        assertEquals(newAsset.getId(), recorded.getAssetId());
        assertEquals(oldAsset.getId(), recorded.getReplacedAssetId());
        assertEquals(new BigDecimal("1200000.00"), recorded.getReplacementCost());
        assertEquals(new BigDecimal("75000.00"), recorded.getSalvageValue());

        // Verify old asset was decommissioned and marked inactive
        AssetDto decommissioned = assetService.getAssetById(oldAsset.getId());
        assertEquals(AssetStatus.DECOMMISSIONED, decommissioned.getStatus());
        assertFalse(decommissioned.isActive());

        // Verify new asset is now OPERATIONAL
        AssetDto activated = assetService.getAssetById(newAsset.getId());
        assertEquals(AssetStatus.OPERATIONAL, activated.getStatus());
        assertTrue(activated.isActive());

        // Verify replacement history
        List<AssetReplacementDto> history = assetService.getReplacements(newAsset.getId());
        assertEquals(1, history.size());
        assertEquals("AUCTION", history.get(0).getDisposalMethod());
    }

    @Test
    @DisplayName("Should link complaints and work orders to infrastructure assets")
    void testAssetRelationshipsWithComplaintsAndWorkOrders() {
        AssetDto asset = createTestAsset("Grand Central Public Park", AssetType.PARKS, AssetStatus.OPERATIONAL, ConditionRating.GOOD);

        // 1. Create a complaint
        Complaint complaint = new Complaint();
        complaint.setId(1001L);
        complaint.setTrackingNumber("CMP-2026-1001");
        complaint.setTitle("Broken swings and damaged fence in Grand Central Park");
        complaint.setDescription("Two swing chains snapped and fence is broken");
        complaint.setOrganization(organization);
        complaint.setDepartment(department);
        complaint.setWard(ward);
        complaintRepo.save(complaint);

        // Link complaint to asset
        ComplaintDto linkedComplaint = assetService.linkComplaintToAsset(asset.getId(), complaint.getId(), "operator");
        assertNotNull(linkedComplaint);

        PagedResponse<ComplaintDto> assetComplaints = assetService.getComplaintsForAsset(asset.getId(), PageRequest.of(0, 10));
        assertEquals(1, assetComplaints.getContent().size());
        assertEquals("CMP-2026-1001", assetComplaints.getContent().get(0).getTrackingNumber());

        // 2. Create a work order
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(2001L);
        workOrder.setWorkOrderNumber("WO-2026-2001");
        workOrder.setTitle("Repair park fencing and swings");
        workOrder.setDescription("Fix swing sets and weld damaged perimeter fence");
        workOrder.setDepartment(department);
        workOrder.setOrganization(organization);
        workOrder.setWard(ward);
        workOrderRepo.save(workOrder);

        // Link work order to asset
        WorkOrderDto linkedWo = assetService.linkWorkOrderToAsset(asset.getId(), workOrder.getId(), "operator");
        assertNotNull(linkedWo);
        assertEquals(asset.getId(), linkedWo.getAssetId());

        PagedResponse<WorkOrderDto> assetWorkOrders = assetService.getWorkOrdersForAsset(asset.getId(), PageRequest.of(0, 10));
        assertEquals(1, assetWorkOrders.getContent().size());
        assertEquals("WO-2026-2001", assetWorkOrders.getContent().get(0).getWorkOrderNumber());
    }

    private AssetDto createTestAsset(String name, AssetType type, AssetStatus status, ConditionRating condition) {
        CreateAssetRequest req = new CreateAssetRequest();
        req.setName(name);
        req.setAssetType(type);
        req.setOrganizationId(organization.getId());
        req.setDepartmentId(department.getId());
        req.setWardId(ward.getId());
        req.setStatus(status);
        req.setConditionRating(condition);
        return assetService.createAsset(req, "test_admin");
    }
}
