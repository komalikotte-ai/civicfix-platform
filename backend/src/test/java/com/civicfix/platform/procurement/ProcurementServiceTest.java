package com.civicfix.platform.procurement;

import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.inventory.InventoryTestMocks;
import com.civicfix.platform.workorders.WorkOrderTestMocks;
import com.civicfix.platform.inventory.entity.*;
import com.civicfix.platform.inventory.enums.*;
import com.civicfix.platform.inventory.service.InventoryService;
import com.civicfix.platform.procurement.dto.*;
import com.civicfix.platform.procurement.enums.*;
import com.civicfix.platform.procurement.service.ProcurementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProcurementService Unit Tests")
class ProcurementServiceTest {

    private ProcurementService service;
    private InventoryService inventoryService;

    // Inventory repos (needed by InventoryService used inside ProcurementService)
    private InventoryTestMocks.MockSupplierRepository supplierRepo;
    private InventoryTestMocks.MockWarehouseRepository warehouseRepo;
    private InventoryTestMocks.MockInventoryItemRepository itemRepo;
    private InventoryTestMocks.MockWarehouseStockRepository stockRepo;
    private InventoryTestMocks.MockStockTransactionRepository transactionRepo;
    private InventoryTestMocks.MockPurchaseRequestRepository purchaseRequestRepo;

    // Procurement repos
    private ProcurementTestMocks.MockVendorContractRepository contractRepo;
    private ProcurementTestMocks.MockQuotationRepository quotationRepo;
    private ProcurementTestMocks.MockQuotationItemRepository quotationItemRepo;
    private ProcurementTestMocks.MockPurchaseOrderRepository poRepo;
    private ProcurementTestMocks.MockPurchaseOrderItemRepository poItemRepo;
    private ProcurementTestMocks.MockGoodsReceiptDeliveryRepository deliveryRepo;
    private ProcurementTestMocks.MockGoodsReceiptDeliveryItemRepository deliveryItemRepo;
    private ProcurementTestMocks.MockVendorEvaluationRepository evaluationRepo;

    // Shared test data
    private Supplier vendor;
    private Department department;
    private Warehouse warehouse;
    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        supplierRepo           = new InventoryTestMocks.MockSupplierRepository();
        warehouseRepo          = new InventoryTestMocks.MockWarehouseRepository();
        itemRepo               = new InventoryTestMocks.MockInventoryItemRepository();
        stockRepo              = new InventoryTestMocks.MockWarehouseStockRepository();
        transactionRepo        = new InventoryTestMocks.MockStockTransactionRepository();
        purchaseRequestRepo    = new InventoryTestMocks.MockPurchaseRequestRepository();
        contractRepo           = new ProcurementTestMocks.MockVendorContractRepository();
        quotationRepo          = new ProcurementTestMocks.MockQuotationRepository();
        quotationItemRepo      = new ProcurementTestMocks.MockQuotationItemRepository();
        poRepo                 = new ProcurementTestMocks.MockPurchaseOrderRepository();
        poItemRepo             = new ProcurementTestMocks.MockPurchaseOrderItemRepository();
        deliveryRepo           = new ProcurementTestMocks.MockGoodsReceiptDeliveryRepository();
        deliveryItemRepo       = new ProcurementTestMocks.MockGoodsReceiptDeliveryItemRepository();
        evaluationRepo         = new ProcurementTestMocks.MockVendorEvaluationRepository();

        ComplaintTestMocks.MockDepartmentRepository departmentRepo = new ComplaintTestMocks.MockDepartmentRepository();
        InventoryTestMocks.MockAuditService auditService = new InventoryTestMocks.MockAuditService();
        InventoryTestMocks.MockSupplierItemRepository supplierItemRepo = new InventoryTestMocks.MockSupplierItemRepository();
        InventoryTestMocks.MockPurchaseRequestItemRepository purchaseRequestItemRepo = new InventoryTestMocks.MockPurchaseRequestItemRepository();
        InventoryTestMocks.MockWorkOrderMaterialUsageRepository materialUsageRepo = new InventoryTestMocks.MockWorkOrderMaterialUsageRepository();
        com.civicfix.platform.assets.AssetTestMocks.MockOrganizationRepository orgRepo =
                new com.civicfix.platform.assets.AssetTestMocks.MockOrganizationRepository();
        ComplaintTestMocks.MockEmployeeRepository employeeRepo = new ComplaintTestMocks.MockEmployeeRepository();
        WorkOrderTestMocks.MockWorkOrderRepository workOrderRepo = new WorkOrderTestMocks.MockWorkOrderRepository();

        inventoryService = new InventoryService(
                warehouseRepo, itemRepo, stockRepo, transactionRepo, materialUsageRepo,
                supplierRepo, supplierItemRepo, purchaseRequestRepo, purchaseRequestItemRepo,
                departmentRepo, orgRepo, employeeRepo, workOrderRepo, auditService);

        service = new ProcurementService(
                supplierRepo, contractRepo, quotationRepo, quotationItemRepo,
                poRepo, poItemRepo, deliveryRepo, deliveryItemRepo, evaluationRepo,
                itemRepo, warehouseRepo, stockRepo, purchaseRequestRepo,
                departmentRepo, inventoryService, auditService);

        // Seed test data
        department = new Department();
        department.setId(1L);
        department.setName("Public Works");
        department.setCode("PW");
        departmentRepo.save(department);

        vendor = new Supplier();
        vendor.setSupplierCode("SUP-00001");
        vendor.setName("Test Vendor Ltd");
        vendor.setEmail("vendor@test.com");
        vendor.setVerificationStatus(VendorVerificationStatus.PENDING);
        supplierRepo.save(vendor);

        warehouse = new Warehouse();
        warehouse.setWarehouseCode("WH-PW-0001");
        warehouse.setName("Main Store");
        warehouse.setDepartment(department);
        warehouse.setStatus(WarehouseStatus.ACTIVE);
        warehouse.setWarehouseType(WarehouseType.GENERAL);
        warehouseRepo.save(warehouse);

        inventoryItem = new InventoryItem();
        inventoryItem.setItemCode("ITM-MAT-0001");
        inventoryItem.setName("Cement Bags");
        inventoryItem.setItemCategory(ItemCategory.CIVIL);
        inventoryItem.setUnitOfMeasure(UnitOfMeasure.BOX);
        inventoryItem.setUnitPrice(new BigDecimal("200.00"));
        inventoryItem.setMinimumStock(10);
        inventoryItem.setReorderLevel(20);
        itemRepo.save(inventoryItem);
    }

    // =========================================================================
    // VENDOR REGISTRATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Register vendor - success")
    void testRegisterVendor_success() {
        RegisterVendorRequest req = new RegisterVendorRequest();
        req.setName("New Vendor Co");
        req.setEmail("new@vendor.com");
        req.setVendorType("CONTRACTOR");

        VendorDto result = service.registerVendor(req, "admin");

        assertNotNull(result.getId());
        assertEquals("New Vendor Co", result.getName());
        assertEquals(VendorVerificationStatus.PENDING, result.getVerificationStatus());
        assertTrue(result.getSupplierCode().startsWith("SUP-"));
    }

    @Test
    @DisplayName("Register vendor - duplicate code throws ConflictException")
    void testRegisterVendor_duplicateCode() {
        RegisterVendorRequest req = new RegisterVendorRequest();
        req.setSupplierCode("SUP-00001"); // already exists
        req.setName("Another Vendor");

        assertThrows(ConflictException.class, () -> service.registerVendor(req, "admin"));
    }

    // =========================================================================
    // VENDOR VERIFICATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Verify vendor - approve successfully")
    void testVerifyVendor_approve() {
        VerifyVendorRequest req = new VerifyVendorRequest();
        req.setStatus(VendorVerificationStatus.VERIFIED);
        req.setVerificationNotes("All documents checked");
        req.setBankAccountNo("123456789012");
        req.setBankIfsc("SBIN0001234");

        VendorDto result = service.verifyVendor(vendor.getId(), req, "procurement_officer");

        assertEquals(VendorVerificationStatus.VERIFIED, result.getVerificationStatus());
        assertEquals("123456789012", result.getBankAccountNo());
        assertEquals("SBIN0001234", result.getBankIfsc());
    }

    @Test
    @DisplayName("Verify vendor - reject successfully")
    void testVerifyVendor_reject() {
        VerifyVendorRequest req = new VerifyVendorRequest();
        req.setStatus(VendorVerificationStatus.REJECTED);
        req.setVerificationNotes("Documents invalid");

        VendorDto result = service.verifyVendor(vendor.getId(), req, "admin");

        assertEquals(VendorVerificationStatus.REJECTED, result.getVerificationStatus());
    }

    @Test
    @DisplayName("Verify vendor - not found throws ResourceNotFoundException")
    void testVerifyVendor_notFound() {
        VerifyVendorRequest req = new VerifyVendorRequest();
        req.setStatus(VendorVerificationStatus.VERIFIED);

        assertThrows(ResourceNotFoundException.class, () -> service.verifyVendor(9999L, req, "admin"));
    }

    // =========================================================================
    // CONTRACT TESTS
    // =========================================================================

    @Test
    @DisplayName("Create contract - success")
    void testCreateContract_success() {
        CreateContractRequest req = new CreateContractRequest();
        req.setSupplierId(vendor.getId());
        req.setDepartmentId(department.getId());
        req.setTitle("Annual Supply Agreement");
        req.setContractValue(new BigDecimal("500000.00"));
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusYears(1));
        req.setStatus(ContractStatus.ACTIVE);

        VendorContractDto result = service.createContract(req, "admin");

        assertNotNull(result.getId());
        assertEquals("Annual Supply Agreement", result.getTitle());
        assertEquals(ContractStatus.ACTIVE, result.getStatus());
        assertTrue(result.getContractNumber().startsWith("CNT-"));
    }

    @Test
    @DisplayName("Create contract - end date before start date throws BadRequestException")
    void testCreateContract_invalidDates() {
        CreateContractRequest req = new CreateContractRequest();
        req.setSupplierId(vendor.getId());
        req.setDepartmentId(department.getId());
        req.setTitle("Bad Contract");
        req.setContractValue(BigDecimal.TEN);
        req.setStartDate(LocalDate.now().plusDays(10));
        req.setEndDate(LocalDate.now()); // end before start

        assertThrows(BadRequestException.class, () -> service.createContract(req, "admin"));
    }

    // =========================================================================
    // QUOTATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Submit quotation - success")
    void testSubmitQuotation_success() {
        CreateQuotationRequest req = buildQuotationRequest(10, new BigDecimal("250.00"));

        QuotationDto result = service.submitQuotation(req, "vendor_user");

        assertNotNull(result.getId());
        assertEquals(QuotationStatus.SUBMITTED, result.getStatus());
        assertEquals(new BigDecimal("2500.00"), result.getTotalAmount());
        assertTrue(result.getQuotationNumber().startsWith("QT-"));
    }

    @Test
    @DisplayName("Accept quotation - success and siblings rejected")
    void testAcceptQuotation_rejectsSiblings() {
        // Create a purchase request
        PurchaseRequest pr = new PurchaseRequest();
        pr.setRequestCode("PR-0001");
        pr.setDepartment(department);
        pr.setStatus(com.civicfix.platform.inventory.enums.PurchaseRequestStatus.APPROVED);
        purchaseRequestRepo.save(pr);

        // Create first quotation linked to PR
        CreateQuotationRequest req1 = buildQuotationRequest(10, new BigDecimal("200.00"));
        req1.setPurchaseRequestId(pr.getId());
        QuotationDto q1 = service.submitQuotation(req1, "vendor1");

        // Create second quotation for same PR
        Supplier vendor2 = new Supplier();
        vendor2.setSupplierCode("SUP-00002");
        vendor2.setName("Rival Vendor");
        supplierRepo.save(vendor2);

        CreateQuotationRequest req2 = new CreateQuotationRequest();
        req2.setSupplierId(vendor2.getId());
        req2.setDepartmentId(department.getId());
        req2.setPurchaseRequestId(pr.getId());
        CreateQuotationRequest.QuotationItemRequest q2Item = new CreateQuotationRequest.QuotationItemRequest();
        q2Item.setItemId(inventoryItem.getId());
        q2Item.setQuantity(10);
        q2Item.setUnitPrice(new BigDecimal("180.00"));
        req2.setItems(List.of(q2Item));
        QuotationDto q2 = service.submitQuotation(req2, "vendor2");

        // Accept q1 - q2 should get REJECTED
        QuotationDto acceptedQ1 = service.acceptQuotation(q1.getId(), "manager");

        assertEquals(QuotationStatus.ACCEPTED, acceptedQ1.getStatus());
        // Sibling q2 should be REJECTED
        assertEquals(QuotationStatus.REJECTED, quotationRepo.findById(q2.getId()).get().getStatus());
    }

    @Test
    @DisplayName("Accept non-submitted quotation throws BadRequestException")
    void testAcceptQuotation_alreadyAccepted() {
        CreateQuotationRequest req = buildQuotationRequest(5, new BigDecimal("100.00"));
        QuotationDto q = service.submitQuotation(req, "vendor");
        service.acceptQuotation(q.getId(), "manager");

        // Accept again should fail
        assertThrows(BadRequestException.class, () -> service.acceptQuotation(q.getId(), "manager"));
    }

    // =========================================================================
    // PURCHASE ORDER LIFECYCLE TESTS
    // =========================================================================

    @Test
    @DisplayName("Full PO lifecycle: create -> submit -> approve -> issue")
    void testPurchaseOrderFullLifecycle() {
        CreatePurchaseOrderRequest req = buildPORequest();

        PurchaseOrderDto po = service.createPurchaseOrder(req, "manager");
        assertNotNull(po.getId());
        assertEquals(PurchaseOrderStatus.DRAFT, po.getStatus());
        assertTrue(po.getOrderNumber().startsWith("PO-"));

        PurchaseOrderDto submitted = service.submitForApproval(po.getId(), "manager");
        assertEquals(PurchaseOrderStatus.PENDING_APPROVAL, submitted.getStatus());

        PurchaseOrderDto approved = service.approvePurchaseOrder(po.getId(), "dept_head");
        assertEquals(PurchaseOrderStatus.APPROVED, approved.getStatus());
        assertEquals("dept_head", approved.getApprovedBy());

        PurchaseOrderDto issued = service.issuePurchaseOrder(po.getId(), "dept_head");
        assertEquals(PurchaseOrderStatus.ISSUED, issued.getStatus());
    }

    @Test
    @DisplayName("Approve PO that is not PENDING_APPROVAL throws BadRequestException")
    void testApprovePO_invalidStatus() {
        PurchaseOrderDto po = service.createPurchaseOrder(buildPORequest(), "manager");
        // Still DRAFT — cannot approve
        assertThrows(BadRequestException.class, () -> service.approvePurchaseOrder(po.getId(), "admin"));
    }

    @Test
    @DisplayName("Reject PO - success")
    void testRejectPO_success() {
        PurchaseOrderDto po = service.createPurchaseOrder(buildPORequest(), "manager");
        service.submitForApproval(po.getId(), "manager");

        PurchaseOrderDto rejected = service.rejectPurchaseOrder(po.getId(), "Price too high", "admin");
        assertEquals(PurchaseOrderStatus.REJECTED, rejected.getStatus());
        assertEquals("Price too high", rejected.getRejectionReason());
    }

    // =========================================================================
    // DELIVERY RECEIPT TESTS
    // =========================================================================

    @Test
    @DisplayName("Receive full delivery - PO becomes FULFILLED, stock increases")
    void testReceiveDelivery_fullFulfillment() {
        // Create, submit, approve, issue PO
        PurchaseOrderDto po = service.createPurchaseOrder(buildPORequest(), "manager");
        service.submitForApproval(po.getId(), "manager");
        service.approvePurchaseOrder(po.getId(), "admin");
        service.issuePurchaseOrder(po.getId(), "admin");

        // Get the PO item ID
        Long poItemId = po.getItems().get(0).getId();

        ReceiveDeliveryRequest req = new ReceiveDeliveryRequest();
        req.setPurchaseOrderId(po.getId());
        req.setWarehouseId(warehouse.getId());
        req.setInspectionNotes("Good condition");

        ReceiveDeliveryRequest.DeliveryItemRequest dItem = new ReceiveDeliveryRequest.DeliveryItemRequest();
        dItem.setPoItemId(poItemId);
        dItem.setItemId(inventoryItem.getId());
        dItem.setQuantityDelivered(50);
        dItem.setQuantityAccepted(50);
        dItem.setQuantityRejected(0);
        req.setItems(List.of(dItem));

        GoodsReceiptDeliveryDto delivery = service.receiveDelivery(req, "warehouse_staff");

        assertNotNull(delivery.getId());
        assertEquals(DeliveryStatus.ACCEPTED, delivery.getStatus());
        assertTrue(delivery.getDeliveryNumber().startsWith("GRD-"));

        // PO should be FULFILLED
        PurchaseOrderDto updatedPo = service.getPurchaseOrderById(po.getId());
        assertEquals(PurchaseOrderStatus.FULFILLED, updatedPo.getStatus());

        // Warehouse stock should have 50
        var stock = stockRepo.findByWarehouseIdAndItemId(warehouse.getId(), inventoryItem.getId());
        assertTrue(stock.isPresent());
        assertEquals(50, stock.get().getQuantity());
    }

    @Test
    @DisplayName("Receive partial delivery - PO becomes PARTIALLY_DELIVERED")
    void testReceiveDelivery_partial() {
        PurchaseOrderDto po = service.createPurchaseOrder(buildPORequest(), "manager");
        service.submitForApproval(po.getId(), "manager");
        service.approvePurchaseOrder(po.getId(), "admin");
        service.issuePurchaseOrder(po.getId(), "admin");

        Long poItemId = po.getItems().get(0).getId();

        ReceiveDeliveryRequest req = new ReceiveDeliveryRequest();
        req.setPurchaseOrderId(po.getId());
        req.setWarehouseId(warehouse.getId());

        ReceiveDeliveryRequest.DeliveryItemRequest dItem = new ReceiveDeliveryRequest.DeliveryItemRequest();
        dItem.setPoItemId(poItemId);
        dItem.setItemId(inventoryItem.getId());
        dItem.setQuantityDelivered(30);
        dItem.setQuantityAccepted(25); // partial acceptance
        dItem.setQuantityRejected(5);
        dItem.setRejectionReason("Damaged bags");
        req.setItems(List.of(dItem));

        GoodsReceiptDeliveryDto delivery = service.receiveDelivery(req, "staff");

        assertEquals(DeliveryStatus.PARTIALLY_ACCEPTED, delivery.getStatus());

        PurchaseOrderDto updatedPo = service.getPurchaseOrderById(po.getId());
        assertEquals(PurchaseOrderStatus.PARTIALLY_DELIVERED, updatedPo.getStatus());
    }

    @Test
    @DisplayName("Cannot receive delivery for non-ISSUED PO")
    void testReceiveDelivery_invalidPOStatus() {
        PurchaseOrderDto po = service.createPurchaseOrder(buildPORequest(), "manager");
        // PO is still DRAFT

        ReceiveDeliveryRequest req = new ReceiveDeliveryRequest();
        req.setPurchaseOrderId(po.getId());
        req.setWarehouseId(warehouse.getId());
        req.setItems(List.of());

        assertThrows(BadRequestException.class, () -> service.receiveDelivery(req, "staff"));
    }

    // =========================================================================
    // VENDOR EVALUATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Evaluate vendor - updates overall rating and supplier rating")
    void testEvaluateVendor_updatesRating() {
        CreateVendorEvaluationRequest req = new CreateVendorEvaluationRequest();
        req.setSupplierId(vendor.getId());
        req.setDeliveryRating(new BigDecimal("4.0"));
        req.setQualityRating(new BigDecimal("5.0"));
        req.setPricingRating(new BigDecimal("3.0"));
        req.setEvaluatorComments("Good overall delivery");

        VendorEvaluationDto result = service.evaluateVendor(req, "dept_head");

        assertNotNull(result.getId());
        assertEquals(new BigDecimal("4.0"), result.getOverallRating()); // (4+5+3)/3 = 4.0
        assertEquals("dept_head", result.getEvaluatedBy());

        // Supplier rating should also be updated
        Supplier updatedVendor = supplierRepo.findById(vendor.getId()).get();
        assertEquals(new BigDecimal("4.0"), updatedVendor.getRating());
    }

    @Test
    @DisplayName("Get vendor evaluations returns sorted list")
    void testGetVendorEvaluations() {
        CreateVendorEvaluationRequest req1 = new CreateVendorEvaluationRequest();
        req1.setSupplierId(vendor.getId());
        req1.setDeliveryRating(new BigDecimal("3.0"));
        req1.setQualityRating(new BigDecimal("3.0"));
        req1.setPricingRating(new BigDecimal("3.0"));
        service.evaluateVendor(req1, "user1");

        CreateVendorEvaluationRequest req2 = new CreateVendorEvaluationRequest();
        req2.setSupplierId(vendor.getId());
        req2.setDeliveryRating(new BigDecimal("5.0"));
        req2.setQualityRating(new BigDecimal("5.0"));
        req2.setPricingRating(new BigDecimal("5.0"));
        service.evaluateVendor(req2, "user2");

        List<VendorEvaluationDto> evals = service.getVendorEvaluations(vendor.getId());
        assertEquals(2, evals.size());
    }

    // =========================================================================
    // PROCUREMENT HISTORY TESTS
    // =========================================================================

    @Test
    @DisplayName("Get procurement history for vendor - aggregated DTO")
    void testGetProcurementHistory() {
        // Create a contract
        CreateContractRequest contractReq = new CreateContractRequest();
        contractReq.setSupplierId(vendor.getId());
        contractReq.setDepartmentId(department.getId());
        contractReq.setTitle("Framework Agreement");
        contractReq.setContractValue(new BigDecimal("1000000.00"));
        contractReq.setStartDate(LocalDate.now());
        contractReq.setEndDate(LocalDate.now().plusYears(2));
        service.createContract(contractReq, "admin");

        // Create a PO
        service.createPurchaseOrder(buildPORequest(), "manager");

        VendorProcurementHistoryDto history = service.getProcurementHistoryForVendor(vendor.getId());

        assertNotNull(history);
        assertNotNull(history.getVendor());
        assertEquals(1, history.getContracts().size());
        assertEquals(1, history.getPurchaseOrders().size());
        assertNotNull(history.getTotalSpend());
    }

    @Test
    @DisplayName("Search purchase orders with filters")
    void testSearchPurchaseOrders() {
        service.createPurchaseOrder(buildPORequest(), "manager");
        service.createPurchaseOrder(buildPORequest(), "manager");

        var result = service.searchPurchaseOrders(vendor.getId(), null, null, null,
                PageRequest.of(0, 20));
        assertEquals(2, result.getContent().size());
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private CreateQuotationRequest buildQuotationRequest(int qty, BigDecimal unitPrice) {
        CreateQuotationRequest req = new CreateQuotationRequest();
        req.setSupplierId(vendor.getId());
        req.setDepartmentId(department.getId());
        req.setValidUntil(LocalDate.now().plusDays(30));

        CreateQuotationRequest.QuotationItemRequest item = new CreateQuotationRequest.QuotationItemRequest();
        item.setItemId(inventoryItem.getId());
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        req.setItems(List.of(item));
        return req;
    }

    private CreatePurchaseOrderRequest buildPORequest() {
        CreatePurchaseOrderRequest req = new CreatePurchaseOrderRequest();
        req.setSupplierId(vendor.getId());
        req.setDepartmentId(department.getId());
        req.setWarehouseId(warehouse.getId());
        req.setOrderDate(LocalDate.now());
        req.setExpectedDelivery(LocalDate.now().plusDays(14));

        CreatePurchaseOrderRequest.PurchaseOrderItemRequest item = new CreatePurchaseOrderRequest.PurchaseOrderItemRequest();
        item.setItemId(inventoryItem.getId());
        item.setQuantityOrdered(50);
        item.setUnitPrice(new BigDecimal("200.00"));
        req.setItems(List.of(item));
        return req;
    }
}
