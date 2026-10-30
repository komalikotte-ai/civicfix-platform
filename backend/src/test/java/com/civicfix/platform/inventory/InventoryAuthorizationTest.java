package com.civicfix.platform.inventory;

import com.civicfix.platform.assets.AssetTestMocks;
import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.inventory.controller.InventoryController;
import com.civicfix.platform.inventory.dto.*;
import com.civicfix.platform.inventory.enums.*;
import com.civicfix.platform.inventory.service.InventoryService;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.security.UserPrincipal;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
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

class InventoryAuthorizationTest {

    private InventoryController controller;
    private InventoryService service;

    private InventoryTestMocks.MockWarehouseRepository warehouseRepo;
    private InventoryTestMocks.MockInventoryItemRepository itemRepo;
    private InventoryTestMocks.MockWarehouseStockRepository stockRepo;
    private InventoryTestMocks.MockStockTransactionRepository transactionRepo;
    private InventoryTestMocks.MockWorkOrderMaterialUsageRepository materialUsageRepo;
    private InventoryTestMocks.MockSupplierRepository supplierRepo;
    private InventoryTestMocks.MockSupplierItemRepository supplierItemRepo;
    private InventoryTestMocks.MockPurchaseRequestRepository purchaseRequestRepo;
    private InventoryTestMocks.MockPurchaseRequestItemRepository purchaseRequestItemRepo;

    private ComplaintTestMocks.MockDepartmentRepository departmentRepo;
    private AssetTestMocks.MockOrganizationRepository organizationRepo;
    private ComplaintTestMocks.MockEmployeeRepository employeeRepo;
    private WorkOrderTestMocks.MockWorkOrderRepository workOrderRepo;
    private InventoryTestMocks.MockAuditService auditService;

    private Organization organization;
    private Department department;
    private User adminUser;
    private User supervisorUser;
    private User fieldWorkerUser;

    @BeforeEach
    void setUp() {
        warehouseRepo = new InventoryTestMocks.MockWarehouseRepository();
        itemRepo = new InventoryTestMocks.MockInventoryItemRepository();
        stockRepo = new InventoryTestMocks.MockWarehouseStockRepository();
        transactionRepo = new InventoryTestMocks.MockStockTransactionRepository();
        materialUsageRepo = new InventoryTestMocks.MockWorkOrderMaterialUsageRepository();
        supplierRepo = new InventoryTestMocks.MockSupplierRepository();
        supplierItemRepo = new InventoryTestMocks.MockSupplierItemRepository();
        purchaseRequestRepo = new InventoryTestMocks.MockPurchaseRequestRepository();
        purchaseRequestItemRepo = new InventoryTestMocks.MockPurchaseRequestItemRepository();
        departmentRepo = new ComplaintTestMocks.MockDepartmentRepository();
        organizationRepo = new AssetTestMocks.MockOrganizationRepository();
        employeeRepo = new ComplaintTestMocks.MockEmployeeRepository();
        workOrderRepo = new WorkOrderTestMocks.MockWorkOrderRepository();
        auditService = new InventoryTestMocks.MockAuditService();

        organization = new Organization("Metropolis Operations", "METRO", "State");
        organization.setId(1L);
        organizationRepo.save(organization);

        department = new Department();
        department.setId(10L);
        department.setName("Operations & Logistics");
        department.setCode("LOGISTICS");
        department.setOrganization(organization);
        departmentRepo.save(department);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("inventory_admin");
        adminUser.setEmail("admin@civicfix.gov");
        adminUser.setRoles(Set.of(new Role(RoleType.ROLE_ADMIN, "Admin")));

        supervisorUser = new User();
        supervisorUser.setId(2L);
        supervisorUser.setUsername("inventory_supervisor");
        supervisorUser.setEmail("supervisor@civicfix.gov");
        supervisorUser.setRoles(Set.of(new Role(RoleType.ROLE_SUPERVISOR, "Supervisor")));

        fieldWorkerUser = new User();
        fieldWorkerUser.setId(3L);
        fieldWorkerUser.setUsername("inventory_worker");
        fieldWorkerUser.setEmail("worker@civicfix.gov");
        fieldWorkerUser.setRoles(Set.of(new Role(RoleType.ROLE_FIELD_WORKER, "Field Worker")));

        service = new InventoryService(
                warehouseRepo, itemRepo, stockRepo, transactionRepo, materialUsageRepo,
                supplierRepo, supplierItemRepo, purchaseRequestRepo, purchaseRequestItemRepo,
                departmentRepo, organizationRepo, employeeRepo, workOrderRepo, auditService
        );

        controller = new InventoryController(service);
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
    @DisplayName("Verify class-level PreAuthorize annotation on InventoryController")
    void testClassLevelAuthorization() {
        PreAuthorize classAuth = InventoryController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(classAuth);
        assertTrue(classAuth.value().contains("ADMIN"));
        assertTrue(classAuth.value().contains("SUPERVISOR"));
        assertTrue(classAuth.value().contains("DEPARTMENT_MANAGER"));
        assertTrue(classAuth.value().contains("FIELD_WORKER"));
    }

    @Test
    @DisplayName("Verify method-level PreAuthorize annotations for warehouse and inventory endpoints")
    void testMethodLevelAuthorizations() throws NoSuchMethodException {
        Method createWh = InventoryController.class.getMethod("createWarehouse", CreateWarehouseRequest.class);
        PreAuthorize createWhAuth = createWh.getAnnotation(PreAuthorize.class);
        assertNotNull(createWhAuth);
        assertTrue(createWhAuth.value().contains("ADMIN"));
        assertTrue(createWhAuth.value().contains("DEPARTMENT_MANAGER"));

        Method updateWh = InventoryController.class.getMethod("updateWarehouse", Long.class, UpdateWarehouseRequest.class);
        PreAuthorize updateWhAuth = updateWh.getAnnotation(PreAuthorize.class);
        assertNotNull(updateWhAuth);
        assertTrue(updateWhAuth.value().contains("SUPERVISOR"));

        Method createItem = InventoryController.class.getMethod("createItem", CreateItemRequest.class);
        PreAuthorize createItemAuth = createItem.getAnnotation(PreAuthorize.class);
        assertNotNull(createItemAuth);
        assertTrue(createItemAuth.value().contains("ADMIN"));

        Method recordTxn = InventoryController.class.getMethod("recordTransaction", RecordStockTransactionRequest.class);
        PreAuthorize recordTxnAuth = recordTxn.getAnnotation(PreAuthorize.class);
        assertNotNull(recordTxnAuth);
        assertTrue(recordTxnAuth.value().contains("SUPERVISOR"));

        Method matUsage = InventoryController.class.getMethod("recordMaterialUsage", RecordMaterialUsageRequest.class);
        PreAuthorize matUsageAuth = matUsage.getAnnotation(PreAuthorize.class);
        assertNotNull(matUsageAuth);
        assertTrue(matUsageAuth.value().contains("FIELD_WORKER"));

        Method approvePr = InventoryController.class.getMethod("approvePurchaseRequest", Long.class, ApprovePurchaseRequestRequest.class);
        PreAuthorize approvePrAuth = approvePr.getAnnotation(PreAuthorize.class);
        assertNotNull(approvePrAuth);
        assertTrue(approvePrAuth.value().contains("ADMIN"));
    }

    @Test
    @DisplayName("Controller endpoint execution: warehouse creation and retrieval by authenticated admin")
    void testControllerCreateAndGetWarehouse() {
        authenticateAs(adminUser, "ROLE_ADMIN");

        CreateWarehouseRequest request = new CreateWarehouseRequest();
        request.setName("Central Store 1");
        request.setWarehouseCode("WH-CEN-01");
        request.setDepartmentId(department.getId());
        request.setOrganizationId(organization.getId());
        request.setWarehouseType(WarehouseType.GENERAL);

        ApiResponse<WarehouseDto> createRes = controller.createWarehouse(request);
        assertNotNull(createRes);
        assertTrue(createRes.isSuccess());
        assertEquals("Central Store 1", createRes.getData().getName());

        ApiResponse<WarehouseDto> getRes = controller.getWarehouseById(createRes.getData().getId());
        assertTrue(getRes.isSuccess());
        assertEquals(createRes.getData().getId(), getRes.getData().getId());
    }

    @Test
    @DisplayName("Controller endpoint execution: item creation and retrieval")
    void testControllerCreateAndGetItem() {
        authenticateAs(adminUser, "ROLE_ADMIN");

        CreateItemRequest request = new CreateItemRequest();
        request.setName("High Strength Asphalt Sealant");
        request.setItemCategory(ItemCategory.CIVIL);
        request.setUnitOfMeasure(UnitOfMeasure.LITRE);
        request.setUnitPrice(new BigDecimal("120.50"));

        ApiResponse<InventoryItemDto> createRes = controller.createItem(request);
        assertNotNull(createRes);
        assertTrue(createRes.isSuccess());
        assertNotNull(createRes.getData().getId());

        ApiResponse<InventoryItemDto> getRes = controller.getItemById(createRes.getData().getId());
        assertTrue(getRes.isSuccess());
        assertEquals("High Strength Asphalt Sealant", getRes.getData().getName());
    }
}
