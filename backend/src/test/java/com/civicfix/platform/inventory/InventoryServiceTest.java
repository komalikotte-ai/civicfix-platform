package com.civicfix.platform.inventory;

import com.civicfix.platform.assets.AssetTestMocks;
import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.inventory.dto.*;
import com.civicfix.platform.inventory.enums.*;
import com.civicfix.platform.inventory.service.InventoryService;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.workorders.WorkOrderTestMocks;
import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {

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

    private Organization organization;
    private Department department;
    private Employee manager;

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

        InventoryTestMocks.MockAuditService auditService = new InventoryTestMocks.MockAuditService();

        service = new InventoryService(
                warehouseRepo, itemRepo, stockRepo, transactionRepo, materialUsageRepo,
                supplierRepo, supplierItemRepo, purchaseRequestRepo, purchaseRequestItemRepo,
                departmentRepo, organizationRepo, employeeRepo, workOrderRepo, auditService);

        // Seed data
        organization = new Organization("City Corp", "CITY", "Karnataka");
        organization.setId(1L);
        organizationRepo.save(organization);

        department = new Department();
        department.setId(1L);
        department.setName("Roads Dept");
        department.setCode("ROADS");
        department.setOrganization(organization);
        departmentRepo.save(department);

        manager = new Employee("EMP-001", "Alice Manager", "alice@city.gov", department, RoleType.ROLE_SUPERVISOR);
        manager.setId(1L);
        employeeRepo.save(manager);
    }

    // =========================================================================
    // WAREHOUSE TESTS
    // =========================================================================

    @Test
    @DisplayName("createWarehouse: auto-generates warehouse code when not provided")
    void createWarehouse_autoGeneratesCode() {
        CreateWarehouseRequest req = new CreateWarehouseRequest();
        req.setName("Roads Warehouse");
        req.setDepartmentId(1L);
        req.setOrganizationId(1L);
        req.setWarehouseType(WarehouseType.GENERAL);

        WarehouseDto dto = service.createWarehouse(req, "admin");

        assertNotNull(dto.getId());
        assertNotNull(dto.getWarehouseCode());
        assertTrue(dto.getWarehouseCode().startsWith("WH-"));
        assertEquals("Roads Warehouse", dto.getName());
        assertEquals(WarehouseStatus.ACTIVE, dto.getStatus());
    }

    @Test
    @DisplayName("createWarehouse: uses provided code and validates uniqueness")
    void createWarehouse_usesProvidedCode() {
        CreateWarehouseRequest req = new CreateWarehouseRequest();
        req.setName("Warehouse A");
        req.setWarehouseCode("WH-TEST-001");
        req.setDepartmentId(1L);
        req.setOrganizationId(1L);
        req.setWarehouseType(WarehouseType.SPARE_PARTS);

        WarehouseDto dto = service.createWarehouse(req, "admin");
        assertEquals("WH-TEST-001", dto.getWarehouseCode());

        // Duplicate code should throw ConflictException
        CreateWarehouseRequest dup = new CreateWarehouseRequest();
        dup.setName("Warehouse B");
        dup.setWarehouseCode("WH-TEST-001");
        dup.setDepartmentId(1L);
        dup.setOrganizationId(1L);
        dup.setWarehouseType(WarehouseType.GENERAL);

        assertThrows(ConflictException.class, () -> service.createWarehouse(dup, "admin"));
    }

    @Test
    @DisplayName("searchWarehouses: filters by keyword and status")
    void searchWarehouses_filteredResults() {
        CreateWarehouseRequest req1 = new CreateWarehouseRequest();
        req1.setName("Alpha Store"); req1.setDepartmentId(1L); req1.setOrganizationId(1L);
        req1.setWarehouseType(WarehouseType.GENERAL);
        service.createWarehouse(req1, "admin");

        CreateWarehouseRequest req2 = new CreateWarehouseRequest();
        req2.setName("Beta Store"); req2.setDepartmentId(1L); req2.setOrganizationId(1L);
        req2.setWarehouseType(WarehouseType.TOOLS);
        service.createWarehouse(req2, "admin");

        Pageable pageable = PageRequest.of(0, 10);
        var result = service.searchWarehouses("Alpha", null, null, null, null, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("Alpha Store", result.getContent().get(0).getName());
    }

    // =========================================================================
    // INVENTORY ITEM TESTS
    // =========================================================================

    @Test
    @DisplayName("createItem: auto-generates item code from category")
    void createItem_autoGeneratesCode() {
        CreateItemRequest req = new CreateItemRequest();
        req.setName("Road Patch Material");
        req.setItemCategory(ItemCategory.CIVIL);
        req.setUnitOfMeasure(UnitOfMeasure.KG);
        req.setUnitPrice(new BigDecimal("25.50"));
        req.setReorderLevel(50);

        InventoryItemDto dto = service.createItem(req, "admin");

        assertNotNull(dto.getId());
        assertNotNull(dto.getItemCode());
        assertTrue(dto.getItemCode().startsWith("ITM-CIV"));
        assertEquals("Road Patch Material", dto.getName());
        assertEquals(ItemCategory.CIVIL, dto.getItemCategory());
    }

    @Test
    @DisplayName("createItem: duplicate code throws ConflictException")
    void createItem_duplicateCodeThrows() {
        CreateItemRequest req = new CreateItemRequest();
        req.setName("Widget"); req.setItemCode("ITM-TEST-001");
        req.setItemCategory(ItemCategory.GENERAL); req.setUnitOfMeasure(UnitOfMeasure.UNIT);
        service.createItem(req, "admin");

        CreateItemRequest dup = new CreateItemRequest();
        dup.setName("Widget2"); dup.setItemCode("ITM-TEST-001");
        dup.setItemCategory(ItemCategory.GENERAL); dup.setUnitOfMeasure(UnitOfMeasure.UNIT);
        assertThrows(ConflictException.class, () -> service.createItem(dup, "admin"));
    }

    // =========================================================================
    // STOCK TRANSACTION TESTS
    // =========================================================================

    @Test
    @DisplayName("recordTransaction STOCK_IN: increases warehouse stock")
    void stockIn_increasesStock() {
        // Setup warehouse and item
        WarehouseDto wh = createTestWarehouse("WH-STOCK-001");
        InventoryItemDto item = createTestItem("ITM-STOCK-001");

        RecordStockTransactionRequest req = new RecordStockTransactionRequest();
        req.setWarehouseId(wh.getId());
        req.setItemId(item.getId());
        req.setTransactionType(TransactionType.STOCK_IN);
        req.setQuantity(100);
        req.setUnitPrice(new BigDecimal("10.00"));

        StockTransactionDto txn = service.recordTransaction(req, "admin");

        assertNotNull(txn.getId());
        assertEquals(TransactionType.STOCK_IN, txn.getTransactionType());
        assertEquals(100, txn.getQuantity());

        // Verify stock level
        Pageable p = PageRequest.of(0, 10);
        var stock = service.getStock(wh.getId(), p);
        assertEquals(1, stock.getTotalElements());
        assertEquals(100, stock.getContent().get(0).getQuantity());
    }

    @Test
    @DisplayName("recordTransaction STOCK_OUT: decreases warehouse stock")
    void stockOut_decreasesStock() {
        WarehouseDto wh = createTestWarehouse("WH-OUT-001");
        InventoryItemDto item = createTestItem("ITM-OUT-001");

        // First stock-in 50 units
        RecordStockTransactionRequest stockIn = new RecordStockTransactionRequest();
        stockIn.setWarehouseId(wh.getId()); stockIn.setItemId(item.getId());
        stockIn.setTransactionType(TransactionType.STOCK_IN); stockIn.setQuantity(50);
        service.recordTransaction(stockIn, "admin");

        // Then stock-out 20 units
        RecordStockTransactionRequest stockOut = new RecordStockTransactionRequest();
        stockOut.setWarehouseId(wh.getId()); stockOut.setItemId(item.getId());
        stockOut.setTransactionType(TransactionType.STOCK_OUT); stockOut.setQuantity(20);
        service.recordTransaction(stockOut, "admin");

        Pageable p = PageRequest.of(0, 10);
        var stock = service.getStock(wh.getId(), p);
        assertEquals(30, stock.getContent().get(0).getQuantity());
    }

    @Test
    @DisplayName("recordTransaction STOCK_OUT: insufficient stock throws BadRequestException")
    void stockOut_insufficientStock_throws() {
        WarehouseDto wh = createTestWarehouse("WH-INS-001");
        InventoryItemDto item = createTestItem("ITM-INS-001");

        // Stock-in 10 units
        RecordStockTransactionRequest stockIn = new RecordStockTransactionRequest();
        stockIn.setWarehouseId(wh.getId()); stockIn.setItemId(item.getId());
        stockIn.setTransactionType(TransactionType.STOCK_IN); stockIn.setQuantity(10);
        service.recordTransaction(stockIn, "admin");

        // Attempt to stock-out 20 units
        RecordStockTransactionRequest stockOut = new RecordStockTransactionRequest();
        stockOut.setWarehouseId(wh.getId()); stockOut.setItemId(item.getId());
        stockOut.setTransactionType(TransactionType.STOCK_OUT); stockOut.setQuantity(20);

        assertThrows(BadRequestException.class, () -> service.recordTransaction(stockOut, "admin"));
    }

    @Test
    @DisplayName("recordTransaction TRANSFER: decrements source, increments destination")
    void transfer_movesStockBetweenWarehouses() {
        WarehouseDto src = createTestWarehouse("WH-SRC-001");
        WarehouseDto dst = createTestWarehouse("WH-DST-001");
        InventoryItemDto item = createTestItem("ITM-TRF-001");

        // Stock source with 100
        RecordStockTransactionRequest stockIn = new RecordStockTransactionRequest();
        stockIn.setWarehouseId(src.getId()); stockIn.setItemId(item.getId());
        stockIn.setTransactionType(TransactionType.STOCK_IN); stockIn.setQuantity(100);
        service.recordTransaction(stockIn, "admin");

        // Transfer 30 to destination
        RecordStockTransactionRequest transfer = new RecordStockTransactionRequest();
        transfer.setWarehouseId(src.getId()); transfer.setItemId(item.getId());
        transfer.setTransactionType(TransactionType.TRANSFER); transfer.setQuantity(30);
        transfer.setToWarehouseId(dst.getId());
        service.recordTransaction(transfer, "admin");

        Pageable p = PageRequest.of(0, 10);
        var srcStock = service.getStock(src.getId(), p);
        var dstStock = service.getStock(dst.getId(), p);

        assertEquals(70, srcStock.getContent().get(0).getQuantity());
        assertEquals(30, dstStock.getContent().get(0).getQuantity());
    }

    @Test
    @DisplayName("getLowStockItems: returns items at or below reorder level")
    void getLowStockItems_detectedCorrectly() {
        WarehouseDto wh = createTestWarehouse("WH-LOW-001");

        // Item with reorderLevel=50
        CreateItemRequest itemReq = new CreateItemRequest();
        itemReq.setName("Low Stock Item"); itemReq.setItemCode("ITM-LOW-001");
        itemReq.setItemCategory(ItemCategory.SPARE_PARTS); itemReq.setUnitOfMeasure(UnitOfMeasure.UNIT);
        itemReq.setReorderLevel(50);
        InventoryItemDto item = service.createItem(itemReq, "admin");

        // Stock in 30 (below reorder level of 50)
        RecordStockTransactionRequest stockIn = new RecordStockTransactionRequest();
        stockIn.setWarehouseId(wh.getId()); stockIn.setItemId(item.getId());
        stockIn.setTransactionType(TransactionType.STOCK_IN); stockIn.setQuantity(30);
        service.recordTransaction(stockIn, "admin");

        Pageable p = PageRequest.of(0, 10);
        var lowStock = service.getLowStockItems(wh.getId(), p);
        assertEquals(1, lowStock.getTotalElements());
        assertTrue(lowStock.getContent().get(0).isBelowReorder());
    }

    // =========================================================================
    // MATERIAL USAGE TESTS
    // =========================================================================

    @Test
    @DisplayName("recordMaterialUsage: deducts stock and creates usage record")
    void recordMaterialUsage_deductsStockAndCreatesRecord() {
        WarehouseDto wh = createTestWarehouse("WH-MAT-001");
        InventoryItemDto item = createTestItem("ITM-MAT-001");

        // Stock in 100
        RecordStockTransactionRequest stockIn = new RecordStockTransactionRequest();
        stockIn.setWarehouseId(wh.getId()); stockIn.setItemId(item.getId());
        stockIn.setTransactionType(TransactionType.STOCK_IN); stockIn.setQuantity(100);
        service.recordTransaction(stockIn, "admin");

        // Create a work order in mock
        WorkOrder wo = new WorkOrder();
        wo.setId(1L); wo.setWorkOrderNumber("WO-0001"); wo.setTitle("Test WO");
        wo.setDescription("desc"); wo.setPriority(WorkOrderPriority.MEDIUM);
        wo.setStatus(WorkOrderStatus.IN_PROGRESS); wo.setDepartment(department);
        workOrderRepo.map.put(1L, wo);

        // Record usage
        RecordMaterialUsageRequest req = new RecordMaterialUsageRequest();
        req.setWorkOrderId(1L); req.setWarehouseId(wh.getId()); req.setItemId(item.getId());
        req.setQuantityUsed(15); req.setUnitPrice(new BigDecimal("10.00"));

        WorkOrderMaterialUsageDto usage = service.recordMaterialUsage(req, "admin");

        assertNotNull(usage.getId());
        assertEquals(15, usage.getQuantityUsed());
        assertEquals(new BigDecimal("150.00"), usage.getTotalCost());

        // Stock should be reduced to 85
        Pageable p = PageRequest.of(0, 10);
        var stock = service.getStock(wh.getId(), p);
        assertEquals(85, stock.getContent().get(0).getQuantity());

        // Usage record visible
        List<WorkOrderMaterialUsageDto> usages = service.getMaterialUsageForWorkOrder(1L);
        assertEquals(1, usages.size());
        assertEquals("ITM-MAT-001", usages.get(0).getItemCode());
    }

    @Test
    @DisplayName("recordMaterialUsage: insufficient stock throws BadRequestException")
    void recordMaterialUsage_insufficientStock_throws() {
        WarehouseDto wh = createTestWarehouse("WH-MATINS-001");
        InventoryItemDto item = createTestItem("ITM-MATINS-001");

        // Only 5 in stock
        RecordStockTransactionRequest stockIn = new RecordStockTransactionRequest();
        stockIn.setWarehouseId(wh.getId()); stockIn.setItemId(item.getId());
        stockIn.setTransactionType(TransactionType.STOCK_IN); stockIn.setQuantity(5);
        service.recordTransaction(stockIn, "admin");

        WorkOrder wo = new WorkOrder();
        wo.setId(2L); wo.setWorkOrderNumber("WO-0002"); wo.setTitle("T"); wo.setDescription("d");
        wo.setPriority(WorkOrderPriority.MEDIUM); wo.setStatus(WorkOrderStatus.IN_PROGRESS);
        wo.setDepartment(department);
        workOrderRepo.map.put(2L, wo);

        RecordMaterialUsageRequest req = new RecordMaterialUsageRequest();
        req.setWorkOrderId(2L); req.setWarehouseId(wh.getId()); req.setItemId(item.getId());
        req.setQuantityUsed(20);

        assertThrows(BadRequestException.class, () -> service.recordMaterialUsage(req, "admin"));
    }

    // =========================================================================
    // SUPPLIER TESTS
    // =========================================================================

    @Test
    @DisplayName("createSupplier: auto-generates supplier code")
    void createSupplier_autoGeneratesCode() {
        CreateSupplierRequest req = new CreateSupplierRequest();
        req.setName("ABC Supplies Ltd");
        req.setEmail("abc@supplies.com");
        req.setLeadTimeDays(10);

        SupplierDto dto = service.createSupplier(req, "admin");

        assertNotNull(dto.getId());
        assertNotNull(dto.getSupplierCode());
        assertTrue(dto.getSupplierCode().startsWith("SUP-"));
        assertEquals("ABC Supplies Ltd", dto.getName());
    }

    @Test
    @DisplayName("addSupplierItem: links item to supplier with preferred pricing")
    void addSupplierItem_linksCorrectly() {
        SupplierDto supplier = service.createSupplier(buildSupplierReq("XYZ Ltd"), "admin");
        InventoryItemDto item = createTestItem("ITM-SUP-001");

        AddSupplierItemRequest req = new AddSupplierItemRequest();
        req.setItemId(item.getId());
        req.setUnitPrice(new BigDecimal("99.99"));
        req.setLeadTimeDays(5);
        req.setPreferred(true);

        SupplierItemDto si = service.addSupplierItem(supplier.getId(), req, "admin");

        assertNotNull(si.getId());
        assertEquals(new BigDecimal("99.99"), si.getUnitPrice());
        assertTrue(si.isPreferred());

        List<SupplierItemDto> items = service.getSupplierItems(supplier.getId());
        assertEquals(1, items.size());
    }

    @Test
    @DisplayName("addSupplierItem: duplicate supplier+item throws ConflictException")
    void addSupplierItem_duplicate_throws() {
        SupplierDto supplier = service.createSupplier(buildSupplierReq("DupSupplier"), "admin");
        InventoryItemDto item = createTestItem("ITM-DUP-001");

        AddSupplierItemRequest req = new AddSupplierItemRequest();
        req.setItemId(item.getId());
        req.setUnitPrice(new BigDecimal("10.00"));

        service.addSupplierItem(supplier.getId(), req, "admin");
        assertThrows(ConflictException.class, () -> service.addSupplierItem(supplier.getId(), req, "admin"));
    }

    // =========================================================================
    // PURCHASE REQUEST TESTS
    // =========================================================================

    @Test
    @DisplayName("createPurchaseRequest: computes total from line items")
    void createPurchaseRequest_computesTotal() {
        InventoryItemDto item1 = createTestItem("ITM-PR-001");
        InventoryItemDto item2 = createTestItem("ITM-PR-002");

        CreatePurchaseRequestRequest req = new CreatePurchaseRequestRequest();
        req.setTitle("Q3 Supplies");
        req.setDepartmentId(1L);
        req.setPriority(PurchasePriority.HIGH);

        PurchaseRequestLineItemRequest line1 = new PurchaseRequestLineItemRequest();
        line1.setItemId(item1.getId()); line1.setQuantityRequested(10);
        line1.setUnitPriceEstimate(new BigDecimal("100.00"));

        PurchaseRequestLineItemRequest line2 = new PurchaseRequestLineItemRequest();
        line2.setItemId(item2.getId()); line2.setQuantityRequested(5);
        line2.setUnitPriceEstimate(new BigDecimal("200.00"));

        req.setItems(List.of(line1, line2));

        PurchaseRequestDto dto = service.createPurchaseRequest(req, "admin");

        assertNotNull(dto.getId());
        assertNotNull(dto.getRequestCode());
        assertTrue(dto.getRequestCode().startsWith("PR-"));
        assertEquals(PurchaseRequestStatus.PENDING, dto.getStatus());
        // 10*100 + 5*200 = 1000 + 1000 = 2000
        assertEquals(new BigDecimal("2000.00"), dto.getTotalEstimatedCost());
        assertEquals(2, dto.getItems().size());
    }

    @Test
    @DisplayName("approvePurchaseRequest: status changes to APPROVED")
    void approvePurchaseRequest_statusChanges() {
        InventoryItemDto item = createTestItem("ITM-APP-001");
        PurchaseRequestDto pr = createTestPurchaseRequest(item.getId());

        ApprovePurchaseRequestRequest approveReq = new ApprovePurchaseRequestRequest();
        PurchaseRequestDto approved = service.approvePurchaseRequest(pr.getId(), approveReq, "supervisor");

        assertEquals(PurchaseRequestStatus.APPROVED, approved.getStatus());
        assertEquals("supervisor", approved.getApprovedBy());
        assertNotNull(approved.getApprovedAt());
    }

    @Test
    @DisplayName("rejectPurchaseRequest: status changes to REJECTED with reason")
    void rejectPurchaseRequest_statusChanges() {
        InventoryItemDto item = createTestItem("ITM-REJ-001");
        PurchaseRequestDto pr = createTestPurchaseRequest(item.getId());

        PurchaseRequestDto rejected = service.rejectPurchaseRequest(pr.getId(), "Budget exceeded", "manager");

        assertEquals(PurchaseRequestStatus.REJECTED, rejected.getStatus());
        assertEquals("Budget exceeded", rejected.getRejectionReason());
    }

    @Test
    @DisplayName("approvePurchaseRequest: only PENDING can be approved")
    void approveNonPending_throws() {
        InventoryItemDto item = createTestItem("ITM-NONPEND-001");
        PurchaseRequestDto pr = createTestPurchaseRequest(item.getId());

        // Reject first
        service.rejectPurchaseRequest(pr.getId(), "Rejected", "admin");

        // Try to approve already-rejected PR
        assertThrows(BadRequestException.class,
                () -> service.approvePurchaseRequest(pr.getId(), new ApprovePurchaseRequestRequest(), "admin"));
    }

    // =========================================================================
    // RESERVATIONS & RECONCILIATION & CONCURRENCY TESTS
    // =========================================================================

    @Test
    @DisplayName("reserveStock and releaseReservedStock: manages reserved quantity safely")
    void reserveAndReleaseStock_success() {
        WarehouseDto wh = createTestWarehouse("WH-RES-001");
        InventoryItemDto item = createTestItem("ITM-RES-001");

        // Stock in 50
        RecordStockTransactionRequest inReq = new RecordStockTransactionRequest();
        inReq.setWarehouseId(wh.getId());
        inReq.setItemId(item.getId());
        inReq.setTransactionType(TransactionType.STOCK_IN);
        inReq.setQuantity(50);
        service.recordTransaction(inReq, "admin");

        // Reserve 20
        ReserveStockRequest resReq = new ReserveStockRequest();
        resReq.setWarehouseId(wh.getId());
        resReq.setItemId(item.getId());
        resReq.setQuantity(20);
        resReq.setReason("Job #101 allocation");

        WarehouseStockDto afterReserve = service.reserveStock(resReq, "admin");
        assertEquals(50, afterReserve.getQuantity());
        assertEquals(20, afterReserve.getReservedQty());
        assertEquals(30, afterReserve.getAvailableQty());

        // Reserve another 35 should fail since available is only 30
        ReserveStockRequest overReq = new ReserveStockRequest();
        overReq.setWarehouseId(wh.getId());
        overReq.setItemId(item.getId());
        overReq.setQuantity(35);
        assertThrows(BadRequestException.class, () -> service.reserveStock(overReq, "admin"));

        // Release 10
        ReserveStockRequest relReq = new ReserveStockRequest();
        relReq.setWarehouseId(wh.getId());
        relReq.setItemId(item.getId());
        relReq.setQuantity(10);
        WarehouseStockDto afterRelease = service.releaseReservedStock(relReq, "admin");
        assertEquals(50, afterRelease.getQuantity());
        assertEquals(10, afterRelease.getReservedQty());
        assertEquals(40, afterRelease.getAvailableQty());

        // Release more than reserved should fail
        ReserveStockRequest overRelease = new ReserveStockRequest();
        overRelease.setWarehouseId(wh.getId());
        overRelease.setItemId(item.getId());
        overRelease.setQuantity(15);
        assertThrows(BadRequestException.class, () -> service.releaseReservedStock(overRelease, "admin"));
    }

    @Test
    @DisplayName("reconcileStock: adjusts stock to physical count and logs variance transaction")
    void reconcileStock_adjustsQuantityAndLogsTransaction() {
        WarehouseDto wh = createTestWarehouse("WH-REC-001");
        InventoryItemDto item = createTestItem("ITM-REC-001");

        // Stock in 100
        RecordStockTransactionRequest inReq = new RecordStockTransactionRequest();
        inReq.setWarehouseId(wh.getId());
        inReq.setItemId(item.getId());
        inReq.setTransactionType(TransactionType.STOCK_IN);
        inReq.setQuantity(100);
        inReq.setUnitPrice(new BigDecimal("15.00"));
        service.recordTransaction(inReq, "admin");

        // Physical count reports 92 (variance -8)
        ReconcileStockRequest recReq = new ReconcileStockRequest();
        recReq.setWarehouseId(wh.getId());
        recReq.setItemId(item.getId());
        recReq.setPhysicalQuantity(92);
        recReq.setNotes("Annual audit count");

        WarehouseStockDto reconciled = service.reconcileStock(recReq, "auditor");
        assertEquals(92, reconciled.getQuantity());
        assertNotNull(reconciled.getLastCountedAt());

        // Verify audit transactions contain the adjustment
        var txns = service.getTransactionsForWarehouse(wh.getId(), PageRequest.of(0, 10));
        assertEquals(2, txns.getTotalElements());
        assertEquals(TransactionType.ADJUSTMENT, txns.getContent().get(0).getTransactionType());
        assertEquals(-8, txns.getContent().get(0).getQuantity());
    }

    @Test
    @DisplayName("getInventorySummaryReport: calculates accurate inventory valuation and totals")
    void getInventorySummaryReport_calculatesTotals() {
        WarehouseDto wh = createTestWarehouse("WH-REP-001");
        InventoryItemDto item = createTestItem("ITM-REP-001");

        RecordStockTransactionRequest inReq = new RecordStockTransactionRequest();
        inReq.setWarehouseId(wh.getId());
        inReq.setItemId(item.getId());
        inReq.setTransactionType(TransactionType.STOCK_IN);
        inReq.setQuantity(40);
        service.recordTransaction(inReq, "admin");

        InventorySummaryReportDto report = service.getInventorySummaryReport();
        assertNotNull(report);
        assertTrue(report.getTotalWarehouses() >= 1);
        assertTrue(report.getTotalItems() >= 1);
        assertNotNull(report.getTotalInventoryValuation());
        assertFalse(report.getWarehouseSummaries().isEmpty());
    }

    @Test
    @DisplayName("Transactional concurrency simulation: multi-threaded stock deduction prevents negative inventory")
    void concurrentStockDeduction_preventsNegativeInventory() throws InterruptedException {
        WarehouseDto wh = createTestWarehouse("WH-CONC-001");
        InventoryItemDto item = createTestItem("ITM-CONC-001");

        // Stock in 50 units
        RecordStockTransactionRequest inReq = new RecordStockTransactionRequest();
        inReq.setWarehouseId(wh.getId());
        inReq.setItemId(item.getId());
        inReq.setTransactionType(TransactionType.STOCK_IN);
        inReq.setQuantity(50);
        service.recordTransaction(inReq, "admin");

        int threadCount = 10;
        int deductionPerThread = 10; // Total requested = 100, only 50 available
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    RecordStockTransactionRequest outReq = new RecordStockTransactionRequest();
                    outReq.setWarehouseId(wh.getId());
                    outReq.setItemId(item.getId());
                    outReq.setTransactionType(TransactionType.STOCK_OUT);
                    outReq.setQuantity(deductionPerThread);
                    service.recordTransaction(outReq, "worker_thread");
                    successCount.incrementAndGet();
                } catch (BadRequestException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Exactly 5 deductions must succeed (5 x 10 = 50), and 5 must fail with BadRequestException
        assertEquals(5, successCount.get());
        assertEquals(5, failCount.get());

        // Final inventory must be exactly 0, never negative
        var stock = service.getStock(wh.getId(), PageRequest.of(0, 10));
        assertEquals(0, stock.getContent().get(0).getQuantity());
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private WarehouseDto createTestWarehouse(String code) {
        CreateWarehouseRequest req = new CreateWarehouseRequest();
        req.setName("Test Warehouse " + code);
        req.setWarehouseCode(code);
        req.setDepartmentId(1L);
        req.setOrganizationId(1L);
        req.setWarehouseType(WarehouseType.GENERAL);
        return service.createWarehouse(req, "admin");
    }

    private InventoryItemDto createTestItem(String code) {
        CreateItemRequest req = new CreateItemRequest();
        req.setName("Test Item " + code);
        req.setItemCode(code);
        req.setItemCategory(ItemCategory.GENERAL);
        req.setUnitOfMeasure(UnitOfMeasure.UNIT);
        req.setReorderLevel(10);
        return service.createItem(req, "admin");
    }

    private CreateSupplierRequest buildSupplierReq(String name) {
        CreateSupplierRequest req = new CreateSupplierRequest();
        req.setName(name);
        req.setLeadTimeDays(7);
        return req;
    }

    private PurchaseRequestDto createTestPurchaseRequest(Long itemId) {
        CreatePurchaseRequestRequest req = new CreatePurchaseRequestRequest();
        req.setTitle("Test PR");
        req.setDepartmentId(1L);
        req.setPriority(PurchasePriority.MEDIUM);
        PurchaseRequestLineItemRequest line = new PurchaseRequestLineItemRequest();
        line.setItemId(itemId); line.setQuantityRequested(5);
        line.setUnitPriceEstimate(new BigDecimal("50.00"));
        req.setItems(List.of(line));
        return service.createPurchaseRequest(req, "admin");
    }
}
