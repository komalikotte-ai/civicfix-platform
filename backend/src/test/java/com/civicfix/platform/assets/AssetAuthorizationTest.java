package com.civicfix.platform.assets;

import com.civicfix.platform.assets.controller.AssetController;
import com.civicfix.platform.assets.dto.*;
import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.assets.service.AssetService;
import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.security.UserPrincipal;
import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.workorders.WorkOrderTestMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssetAuthorizationTest {

    private AssetController controller;
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

    private User adminUser;
    private User supervisorUser;
    private User fieldWorkerUser;

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
        organization.setName("Metropolis Corp");
        organization.setCode("MC");
        orgRepo.save(organization);

        department = new Department();
        department.setId(10L);
        department.setName("Roads and Bridges");
        department.setCode("RAB");
        department.setOrganization(organization);
        deptRepo.save(department);

        ward = new Ward();
        ward.setId(100L);
        ward.setName("Ward 1");
        ward.setWardNumber("W-01");
        ward.setOrganization(organization);
        wardRepo.save(ward);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin_user");
        adminUser.setEmail("admin@civicfix.gov");
        adminUser.setRoles(Set.of(new Role(RoleType.ROLE_ADMIN, "Admin")));

        supervisorUser = new User();
        supervisorUser.setId(2L);
        supervisorUser.setUsername("supervisor_user");
        supervisorUser.setEmail("supervisor@civicfix.gov");
        supervisorUser.setRoles(Set.of(new Role(RoleType.ROLE_SUPERVISOR, "Supervisor")));

        fieldWorkerUser = new User();
        fieldWorkerUser.setId(3L);
        fieldWorkerUser.setUsername("worker_user");
        fieldWorkerUser.setEmail("worker@civicfix.gov");
        fieldWorkerUser.setRoles(Set.of(new Role(RoleType.ROLE_FIELD_WORKER, "Field Worker")));

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

        controller = new AssetController(assetService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user, String role) {
        UserPrincipal principal = new UserPrincipal(
                user.getId(), user.getUsername(), user.getEmail(), "secret",
                organization.getId(), department.getId(), null, true, false, true,
                Set.of(role), Set.of(), List.of(new SimpleGrantedAuthority(role))
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Verify class-level PreAuthorize annotation allows municipal operational roles")
    void testClassLevelAuthorization() {
        PreAuthorize classAuth = AssetController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(classAuth);
        assertTrue(classAuth.value().contains("ADMIN"));
        assertTrue(classAuth.value().contains("SUPERVISOR"));
        assertTrue(classAuth.value().contains("DEPARTMENT_MANAGER"));
        assertTrue(classAuth.value().contains("FIELD_WORKER"));
    }

    @Test
    @DisplayName("Verify method-level PreAuthorize annotations on sensitive asset actions")
    void testMethodLevelAuthorizations() throws NoSuchMethodException {
        // createAsset requires management privileges
        Method createMethod = AssetController.class.getMethod("createAsset", CreateAssetRequest.class);
        PreAuthorize createAuth = createMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(createAuth);
        assertTrue(createAuth.value().contains("ADMIN"));
        assertTrue(createAuth.value().contains("DEPARTMENT_MANAGER"));

        // updateAsset requires manager/supervisor privileges
        Method updateMethod = AssetController.class.getMethod("updateAsset", Long.class, UpdateAssetRequest.class);
        PreAuthorize updateAuth = updateMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(updateAuth);
        assertTrue(updateAuth.value().contains("SUPERVISOR"));

        // recordInspection allows field workers
        Method inspMethod = AssetController.class.getMethod("recordInspection", Long.class, RecordInspectionRequest.class);
        PreAuthorize inspAuth = inspMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(inspAuth);
        assertTrue(inspAuth.value().contains("FIELD_WORKER"));

        // recordRepair allows field workers and supervisors
        Method repairMethod = AssetController.class.getMethod("recordRepair", Long.class, RecordRepairRequest.class);
        PreAuthorize repairAuth = repairMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(repairAuth);
        assertTrue(repairAuth.value().contains("FIELD_WORKER"));

        // recordReplacement requires management privileges
        Method replMethod = AssetController.class.getMethod("recordReplacement", Long.class, RecordReplacementRequest.class);
        PreAuthorize replAuth = replMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(replAuth);
        assertTrue(replAuth.value().contains("ADMIN"));
    }

    @Test
    @DisplayName("Controller endpoint execution: create and retrieve asset with authenticated admin")
    void testControllerCreateAndGet() {
        authenticateAs(adminUser, "ROLE_ADMIN");

        CreateAssetRequest request = new CreateAssetRequest();
        request.setName("Sector 9 Main Pipeline");
        request.setAssetType(AssetType.WATER_PIPELINES);
        request.setOrganizationId(organization.getId());
        request.setDepartmentId(department.getId());
        request.setConditionRating(ConditionRating.EXCELLENT);

        ApiResponse<AssetDto> createRes = controller.createAsset(request);
        assertNotNull(createRes);
        assertTrue(createRes.isSuccess());
        assertEquals("Sector 9 Main Pipeline", createRes.getData().getName());

        ApiResponse<AssetDto> getRes = controller.getAssetById(createRes.getData().getId());
        assertTrue(getRes.isSuccess());
        assertEquals(createRes.getData().getId(), getRes.getData().getId());

        ApiResponse<AssetDto> getCodeRes = controller.getAssetByCode(createRes.getData().getAssetCode());
        assertTrue(getCodeRes.isSuccess());
        assertEquals(createRes.getData().getAssetCode(), getCodeRes.getData().getAssetCode());
    }

    @Test
    @DisplayName("Controller endpoint execution: record inspection and repair as authenticated user")
    void testControllerInspectionAndRepair() {
        authenticateAs(adminUser, "ROLE_ADMIN");

        CreateAssetRequest request = new CreateAssetRequest();
        request.setName("Traffic Light Signal - Junction 5");
        request.setAssetType(AssetType.TRAFFIC_SIGNALS);
        request.setOrganizationId(organization.getId());
        request.setDepartmentId(department.getId());
        ApiResponse<AssetDto> assetRes = controller.createAsset(request);
        Long assetId = assetRes.getData().getId();

        // Field worker logs in and records inspection
        authenticateAs(fieldWorkerUser, "ROLE_FIELD_WORKER");

        RecordInspectionRequest inspReq = new RecordInspectionRequest();
        inspReq.setConditionRating(ConditionRating.FAIR);
        inspReq.setOperationalStatus(AssetStatus.OPERATIONAL);
        inspReq.setFindings("Controller timer clock lagging by 10 seconds");
        ApiResponse<AssetInspectionDto> inspRes = controller.recordInspection(assetId, inspReq);
        assertTrue(inspRes.isSuccess());
        assertEquals(ConditionRating.FAIR, inspRes.getData().getConditionRating());

        ApiResponse<List<AssetInspectionDto>> inspList = controller.getInspections(assetId);
        assertTrue(inspList.isSuccess());
        assertEquals(1, inspList.getData().size());

        // Field worker records repair
        RecordRepairRequest repairReq = new RecordRepairRequest();
        repairReq.setCost(new BigDecimal("250.00"));
        repairReq.setDescription("Recalibrated RTC module");
        repairReq.setAfterCondition(ConditionRating.GOOD);
        ApiResponse<AssetRepairDto> repairRes = controller.recordRepair(assetId, repairReq);
        assertTrue(repairRes.isSuccess());
        assertEquals(ConditionRating.GOOD, repairRes.getData().getAfterCondition());

        ApiResponse<List<AssetRepairDto>> repairList = controller.getRepairs(assetId);
        assertTrue(repairList.isSuccess());
        assertEquals(1, repairList.getData().size());
    }
}
