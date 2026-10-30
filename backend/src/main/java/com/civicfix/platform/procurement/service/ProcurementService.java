package com.civicfix.platform.procurement.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.inventory.dto.RecordStockTransactionRequest;
import com.civicfix.platform.inventory.entity.*;
import com.civicfix.platform.inventory.enums.TransactionType;
import com.civicfix.platform.inventory.repository.*;
import com.civicfix.platform.inventory.service.InventoryService;
import com.civicfix.platform.procurement.dto.*;
import com.civicfix.platform.procurement.entity.*;
import com.civicfix.platform.procurement.enums.*;
import com.civicfix.platform.procurement.repository.*;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ProcurementService {

    private static final Logger log = LoggerFactory.getLogger(ProcurementService.class);

    private final SupplierRepository supplierRepository;
    private final VendorContractRepository contractRepository;
    private final QuotationRepository quotationRepository;
    private final QuotationItemRepository quotationItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final GoodsReceiptDeliveryRepository deliveryRepository;
    private final GoodsReceiptDeliveryItemRepository deliveryItemRepository;
    private final VendorEvaluationRepository evaluationRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final DepartmentRepository departmentRepository;
    private final InventoryService inventoryService;
    private final AuditService auditService;

    public ProcurementService(SupplierRepository supplierRepository,
                              VendorContractRepository contractRepository,
                              QuotationRepository quotationRepository,
                              QuotationItemRepository quotationItemRepository,
                              PurchaseOrderRepository purchaseOrderRepository,
                              PurchaseOrderItemRepository purchaseOrderItemRepository,
                              GoodsReceiptDeliveryRepository deliveryRepository,
                              GoodsReceiptDeliveryItemRepository deliveryItemRepository,
                              VendorEvaluationRepository evaluationRepository,
                              InventoryItemRepository inventoryItemRepository,
                              WarehouseRepository warehouseRepository,
                              WarehouseStockRepository warehouseStockRepository,
                              PurchaseRequestRepository purchaseRequestRepository,
                              DepartmentRepository departmentRepository,
                              InventoryService inventoryService,
                              AuditService auditService) {
        this.supplierRepository = supplierRepository;
        this.contractRepository = contractRepository;
        this.quotationRepository = quotationRepository;
        this.quotationItemRepository = quotationItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.deliveryRepository = deliveryRepository;
        this.deliveryItemRepository = deliveryItemRepository;
        this.evaluationRepository = evaluationRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.departmentRepository = departmentRepository;
        this.inventoryService = inventoryService;
        this.auditService = auditService;
    }

    // =========================================================================
    // VENDOR REGISTRATION & VERIFICATION
    // =========================================================================

    @Transactional
    public VendorDto registerVendor(RegisterVendorRequest request, String username) {
        String code = request.getSupplierCode() != null
                ? request.getSupplierCode()
                : generateSupplierCode();
        if (supplierRepository.existsBySupplierCode(code)) {
            throw new ConflictException("Supplier/Vendor with code " + code + " already exists");
        }
        Supplier supplier = new Supplier();
        supplier.setSupplierCode(code);
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setGstin(request.getGstin());
        supplier.setPan(request.getPan());
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setLeadTimeDays(request.getLeadTimeDays() != null ? request.getLeadTimeDays() : 7);
        supplier.setBankAccountNo(request.getBankAccountNo());
        supplier.setBankIfsc(request.getBankIfsc());
        supplier.setVendorType(request.getVendorType() != null ? request.getVendorType() : "SUPPLIER");
        supplier.setVerificationStatus(VendorVerificationStatus.PENDING);

        Supplier saved = supplierRepository.save(supplier);
        auditService.record("Supplier", saved.getId().toString(), AuditAction.CREATE, username,
                "Registered vendor: " + saved.getName() + " [" + saved.getSupplierCode() + "]");
        return VendorDto.fromEntity(saved);
    }

    @Transactional
    public VendorDto verifyVendor(Long supplierId, VerifyVendorRequest request, String username) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + supplierId));

        supplier.setVerificationStatus(request.getStatus());
        supplier.setVerifiedBy(username);
        supplier.setVerifiedAt(Instant.now());
        if (request.getVerificationNotes() != null) supplier.setVerificationNotes(request.getVerificationNotes());
        if (request.getBankAccountNo() != null) supplier.setBankAccountNo(request.getBankAccountNo());
        if (request.getBankIfsc() != null) supplier.setBankIfsc(request.getBankIfsc());
        if (request.getVendorType() != null) supplier.setVendorType(request.getVendorType());

        Supplier saved = supplierRepository.save(supplier);
        auditService.record("Supplier", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Vendor verification status changed to: " + request.getStatus());
        return VendorDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public VendorDto getVendorById(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + supplierId));
        return VendorDto.fromEntity(supplier);
    }

    @Transactional(readOnly = true)
    public PagedResponse<VendorDto> searchVendors(String keyword, Boolean active, Pageable pageable) {
        Page<VendorDto> page = supplierRepository.searchSuppliers(keyword, active, pageable)
                .map(VendorDto::fromEntity);
        return PagedResponse.from(page);
    }

    // =========================================================================
    // VENDOR CONTRACTS
    // =========================================================================

    @Transactional
    public VendorContractDto createContract(CreateContractRequest request, String username) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.getSupplierId()));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        String contractNumber = request.getContractNumber() != null
                ? request.getContractNumber()
                : generateContractNumber();
        if (contractRepository.existsByContractNumber(contractNumber)) {
            throw new ConflictException("Contract with number " + contractNumber + " already exists");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Contract end date must be after start date");
        }

        VendorContract contract = new VendorContract();
        contract.setContractNumber(contractNumber);
        contract.setSupplier(supplier);
        contract.setDepartment(department);
        contract.setTitle(request.getTitle());
        contract.setDescription(request.getDescription());
        contract.setContractValue(request.getContractValue());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setStatus(request.getStatus() != null ? request.getStatus() : ContractStatus.ACTIVE);
        contract.setTermsAndConditions(request.getTermsAndConditions());

        VendorContract saved = contractRepository.save(contract);
        auditService.record("VendorContract", saved.getId().toString(), AuditAction.CREATE, username,
                "Contract created: " + saved.getContractNumber() + " with vendor " + supplier.getName());
        return VendorContractDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public VendorContractDto getContractById(Long id) {
        VendorContract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + id));
        return VendorContractDto.fromEntity(contract);
    }

    @Transactional(readOnly = true)
    public PagedResponse<VendorContractDto> searchContracts(Long supplierId, Long departmentId,
                                                             ContractStatus status, Pageable pageable) {
        Page<VendorContractDto> page = contractRepository
                .searchContracts(supplierId, departmentId, status, pageable)
                .map(VendorContractDto::fromEntity);
        return PagedResponse.from(page);
    }

    // =========================================================================
    // QUOTATIONS
    // =========================================================================

    @Transactional
    public QuotationDto submitQuotation(CreateQuotationRequest request, String username) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.getSupplierId()));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        String quotationNumber = request.getQuotationNumber() != null
                ? request.getQuotationNumber()
                : generateQuotationNumber();
        if (quotationRepository.existsByQuotationNumber(quotationNumber)) {
            throw new ConflictException("Quotation with number " + quotationNumber + " already exists");
        }

        Quotation quotation = new Quotation();
        quotation.setQuotationNumber(quotationNumber);
        quotation.setSupplier(supplier);
        quotation.setDepartment(department);
        quotation.setValidUntil(request.getValidUntil());
        quotation.setDeliveryTerms(request.getDeliveryTerms());
        quotation.setPaymentTerms(request.getPaymentTerms());
        quotation.setRemarks(request.getRemarks());
        quotation.setStatus(QuotationStatus.SUBMITTED);

        if (request.getPurchaseRequestId() != null) {
            PurchaseRequest pr = purchaseRequestRepository.findById(request.getPurchaseRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found: " + request.getPurchaseRequestId()));
            quotation.setPurchaseRequest(pr);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CreateQuotationRequest.QuotationItemRequest itemReq : request.getItems()) {
            InventoryItem item = inventoryItemRepository.findById(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + itemReq.getItemId()));
            QuotationItem qi = new QuotationItem();
            qi.setQuotation(quotation);
            qi.setItem(item);
            qi.setQuantity(itemReq.getQuantity());
            qi.setUnitPrice(itemReq.getUnitPrice());
            BigDecimal lineTotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            qi.setTotalPrice(lineTotal);
            qi.setSpecifications(itemReq.getSpecifications());
            quotation.getItems().add(qi);
            total = total.add(lineTotal);
        }
        quotation.setTotalAmount(total);

        Quotation saved = quotationRepository.save(quotation);
        for (QuotationItem qi : saved.getItems()) {
            quotationItemRepository.save(qi);
        }
        auditService.record("Quotation", saved.getId().toString(), AuditAction.CREATE, username,
                "Quotation submitted: " + saved.getQuotationNumber() + " by vendor " + supplier.getName());
        return QuotationDto.fromEntity(saved);
    }

    @Transactional
    public QuotationDto acceptQuotation(Long quotationId, String username) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with ID: " + quotationId));
        if (quotation.getStatus() != QuotationStatus.SUBMITTED) {
            throw new BadRequestException("Only SUBMITTED quotations can be accepted. Current status: " + quotation.getStatus());
        }

        // Accept this quotation
        quotation.setStatus(QuotationStatus.ACCEPTED);
        Quotation saved = quotationRepository.save(quotation);

        // Reject sibling quotations for the same purchase request
        if (quotation.getPurchaseRequest() != null) {
            List<Quotation> siblings = quotationRepository
                    .findByPurchaseRequestId(quotation.getPurchaseRequest().getId());
            for (Quotation sibling : siblings) {
                if (!sibling.getId().equals(quotationId) && sibling.getStatus() == QuotationStatus.SUBMITTED) {
                    sibling.setStatus(QuotationStatus.REJECTED);
                    quotationRepository.save(sibling);
                }
            }
        }

        auditService.record("Quotation", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Quotation accepted: " + saved.getQuotationNumber());
        return QuotationDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<QuotationDto> searchQuotations(Long supplierId, Long departmentId,
                                                         Long purchaseRequestId, QuotationStatus status,
                                                         Pageable pageable) {
        Page<QuotationDto> page = quotationRepository
                .searchQuotations(supplierId, departmentId, purchaseRequestId, status, pageable)
                .map(QuotationDto::fromEntity);
        return PagedResponse.from(page);
    }

    // =========================================================================
    // PURCHASE ORDERS
    // =========================================================================

    @Transactional
    public PurchaseOrderDto createPurchaseOrder(CreatePurchaseOrderRequest request, String username) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.getSupplierId()));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));

        String orderNumber = request.getOrderNumber() != null
                ? request.getOrderNumber()
                : generateOrderNumber();
        if (purchaseOrderRepository.existsByOrderNumber(orderNumber)) {
            throw new ConflictException("Purchase order with number " + orderNumber + " already exists");
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setOrderNumber(orderNumber);
        po.setSupplier(supplier);
        po.setDepartment(department);
        po.setWarehouse(warehouse);
        po.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now());
        po.setExpectedDelivery(request.getExpectedDelivery());
        po.setTermsAndConditions(request.getTermsAndConditions());
        po.setStatus(PurchaseOrderStatus.DRAFT);

        if (request.getPurchaseRequestId() != null) {
            PurchaseRequest pr = purchaseRequestRepository.findById(request.getPurchaseRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found: " + request.getPurchaseRequestId()));
            po.setPurchaseRequest(pr);
        }
        if (request.getQuotationId() != null) {
            Quotation q = quotationRepository.findById(request.getQuotationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quotation not found: " + request.getQuotationId()));
            po.setQuotation(q);
        }
        if (request.getContractId() != null) {
            VendorContract contract = contractRepository.findById(request.getContractId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + request.getContractId()));
            po.setContract(contract);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CreatePurchaseOrderRequest.PurchaseOrderItemRequest itemReq : request.getItems()) {
            InventoryItem item = inventoryItemRepository.findById(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + itemReq.getItemId()));
            PurchaseOrderItem poi = new PurchaseOrderItem();
            poi.setPurchaseOrder(po);
            poi.setItem(item);
            poi.setQuantityOrdered(itemReq.getQuantityOrdered());
            poi.setQuantityReceived(0);
            poi.setUnitPrice(itemReq.getUnitPrice());
            BigDecimal lineTotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantityOrdered()));
            poi.setTotalPrice(lineTotal);
            po.getItems().add(poi);
            total = total.add(lineTotal);
        }
        po.setTotalAmount(total);

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        for (PurchaseOrderItem poi : saved.getItems()) {
            purchaseOrderItemRepository.save(poi);
        }
        auditService.record("PurchaseOrder", saved.getId().toString(), AuditAction.CREATE, username,
                "Purchase order created: " + saved.getOrderNumber() + " for vendor " + supplier.getName());
        return PurchaseOrderDto.fromEntity(saved);
    }

    @Transactional
    public PurchaseOrderDto submitForApproval(Long poId, String username) {
        PurchaseOrder po = getPurchaseOrderEntity(poId);
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT purchase orders can be submitted. Current: " + po.getStatus());
        }
        po.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditService.record("PurchaseOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Purchase order submitted for approval: " + saved.getOrderNumber());
        return PurchaseOrderDto.fromEntity(saved);
    }

    @Transactional
    public PurchaseOrderDto approvePurchaseOrder(Long poId, String username) {
        PurchaseOrder po = getPurchaseOrderEntity(poId);
        if (po.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only PENDING_APPROVAL purchase orders can be approved. Current: " + po.getStatus());
        }
        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.setApprovedBy(username);
        po.setApprovedAt(Instant.now());
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditService.record("PurchaseOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Purchase order approved: " + saved.getOrderNumber());
        return PurchaseOrderDto.fromEntity(saved);
    }

    @Transactional
    public PurchaseOrderDto rejectPurchaseOrder(Long poId, String reason, String username) {
        PurchaseOrder po = getPurchaseOrderEntity(poId);
        if (po.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only PENDING_APPROVAL purchase orders can be rejected. Current: " + po.getStatus());
        }
        po.setStatus(PurchaseOrderStatus.REJECTED);
        po.setRejectionReason(reason);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditService.record("PurchaseOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Purchase order rejected: " + saved.getOrderNumber() + " Reason: " + reason);
        return PurchaseOrderDto.fromEntity(saved);
    }

    @Transactional
    public PurchaseOrderDto issuePurchaseOrder(Long poId, String username) {
        PurchaseOrder po = getPurchaseOrderEntity(poId);
        if (po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED purchase orders can be issued. Current: " + po.getStatus());
        }
        po.setStatus(PurchaseOrderStatus.ISSUED);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditService.record("PurchaseOrder", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Purchase order issued: " + saved.getOrderNumber());
        return PurchaseOrderDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDto getPurchaseOrderById(Long id) {
        return PurchaseOrderDto.fromEntity(getPurchaseOrderEntity(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PurchaseOrderDto> searchPurchaseOrders(Long supplierId, Long departmentId,
                                                                  Long warehouseId, PurchaseOrderStatus status,
                                                                  Pageable pageable) {
        Page<PurchaseOrderDto> page = purchaseOrderRepository
                .searchPurchaseOrders(supplierId, departmentId, warehouseId, status, pageable)
                .map(PurchaseOrderDto::fromEntity);
        return PagedResponse.from(page);
    }

    // =========================================================================
    // DELIVERIES (GOODS RECEIPT)
    // =========================================================================

    @Transactional
    public GoodsReceiptDeliveryDto receiveDelivery(ReceiveDeliveryRequest request, String username) {
        PurchaseOrder po = getPurchaseOrderEntity(request.getPurchaseOrderId());
        if (po.getStatus() != PurchaseOrderStatus.ISSUED
                && po.getStatus() != PurchaseOrderStatus.PARTIALLY_DELIVERED) {
            throw new BadRequestException(
                    "Deliveries can only be received for ISSUED or PARTIALLY_DELIVERED purchase orders. Current: " + po.getStatus());
        }
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));

        String deliveryNumber = request.getDeliveryNumber() != null
                ? request.getDeliveryNumber()
                : generateDeliveryNumber();
        if (deliveryRepository.existsByDeliveryNumber(deliveryNumber)) {
            throw new ConflictException("Delivery with number " + deliveryNumber + " already exists");
        }

        GoodsReceiptDelivery delivery = new GoodsReceiptDelivery();
        delivery.setDeliveryNumber(deliveryNumber);
        delivery.setPurchaseOrder(po);
        delivery.setWarehouse(warehouse);
        delivery.setReceivedBy(username);
        delivery.setReceivedAt(Instant.now());
        delivery.setDeliveryChallanNo(request.getDeliveryChallanNo());
        delivery.setInspectionNotes(request.getInspectionNotes());

        boolean anyRejected = false;
        boolean allAccepted = true;

        for (ReceiveDeliveryRequest.DeliveryItemRequest itemReq : request.getItems()) {
            PurchaseOrderItem poItem = purchaseOrderItemRepository.findById(itemReq.getPoItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("PO item not found: " + itemReq.getPoItemId()));
            InventoryItem inventoryItem = inventoryItemRepository.findById(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + itemReq.getItemId()));

            int accepted = itemReq.getQuantityAccepted() != null ? itemReq.getQuantityAccepted() : 0;
            int rejected = itemReq.getQuantityRejected() != null ? itemReq.getQuantityRejected() : 0;

            GoodsReceiptDeliveryItem deliveryItem = new GoodsReceiptDeliveryItem();
            deliveryItem.setDelivery(delivery);
            deliveryItem.setPoItemId(itemReq.getPoItemId());
            deliveryItem.setItem(inventoryItem);
            deliveryItem.setQuantityDelivered(itemReq.getQuantityDelivered());
            deliveryItem.setQuantityAccepted(accepted);
            deliveryItem.setQuantityRejected(rejected);
            deliveryItem.setRejectionReason(itemReq.getRejectionReason());
            deliveryItem.setBatchNumber(itemReq.getBatchNumber());
            delivery.getItems().add(deliveryItem);

            // Update PO item received qty
            poItem.setQuantityReceived(poItem.getQuantityReceived() + accepted);
            purchaseOrderItemRepository.save(poItem);

            if (rejected > 0) anyRejected = true;
            if (poItem.getQuantityReceived() < poItem.getQuantityOrdered()) allAccepted = false;

            // Add accepted stock to warehouse
            if (accepted > 0) {
                RecordStockTransactionRequest txnReq = new RecordStockTransactionRequest();
                txnReq.setWarehouseId(warehouse.getId());
                txnReq.setItemId(inventoryItem.getId());
                txnReq.setTransactionType(TransactionType.STOCK_IN);
                txnReq.setQuantity(accepted);
                txnReq.setUnitPrice(poItem.getUnitPrice());
                txnReq.setRemarks("Goods receipt: " + deliveryNumber + " from PO " + po.getOrderNumber());
                inventoryService.recordTransaction(txnReq, username);
            }
        }

        // Determine delivery status
        DeliveryStatus deliveryStatus = anyRejected ? DeliveryStatus.PARTIALLY_ACCEPTED : DeliveryStatus.ACCEPTED;
        if (request.getItems().stream().allMatch(i -> i.getQuantityAccepted() != null && i.getQuantityAccepted() == 0)) {
            deliveryStatus = DeliveryStatus.REJECTED;
        }
        delivery.setStatus(deliveryStatus);

        GoodsReceiptDelivery saved = deliveryRepository.save(delivery);
        for (GoodsReceiptDeliveryItem di : saved.getItems()) {
            deliveryItemRepository.save(di);
        }

        // Update PO status
        List<PurchaseOrderItem> poItems = purchaseOrderItemRepository.findByPurchaseOrderId(po.getId());
        boolean allFulfilled = poItems.stream()
                .allMatch(i -> i.getQuantityReceived() >= i.getQuantityOrdered());
        if (allFulfilled) {
            po.setStatus(PurchaseOrderStatus.FULFILLED);
        } else {
            po.setStatus(PurchaseOrderStatus.PARTIALLY_DELIVERED);
        }
        purchaseOrderRepository.save(po);

        auditService.record("GoodsReceiptDelivery", saved.getId().toString(), AuditAction.CREATE, username,
                "Delivery received: " + saved.getDeliveryNumber() + " for PO " + po.getOrderNumber()
                        + " status=" + deliveryStatus);
        return GoodsReceiptDeliveryDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public GoodsReceiptDeliveryDto getDeliveryById(Long id) {
        GoodsReceiptDelivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with ID: " + id));
        return GoodsReceiptDeliveryDto.fromEntity(delivery);
    }

    @Transactional(readOnly = true)
    public List<GoodsReceiptDeliveryDto> getDeliveriesForPurchaseOrder(Long poId) {
        return deliveryRepository.findByPurchaseOrderId(poId).stream()
                .map(GoodsReceiptDeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // VENDOR EVALUATIONS
    // =========================================================================

    @Transactional
    public VendorEvaluationDto evaluateVendor(CreateVendorEvaluationRequest request, String username) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.getSupplierId()));

        BigDecimal overall = request.getDeliveryRating()
                .add(request.getQualityRating())
                .add(request.getPricingRating())
                .divide(BigDecimal.valueOf(3), 1, RoundingMode.HALF_UP);

        VendorEvaluation evaluation = new VendorEvaluation();
        evaluation.setSupplier(supplier);
        evaluation.setDeliveryRating(request.getDeliveryRating());
        evaluation.setQualityRating(request.getQualityRating());
        evaluation.setPricingRating(request.getPricingRating());
        evaluation.setOverallRating(overall);
        evaluation.setEvaluatorComments(request.getEvaluatorComments());
        evaluation.setEvaluatedBy(username);
        evaluation.setEvaluatedAt(Instant.now());

        if (request.getPurchaseOrderId() != null) {
            PurchaseOrder po = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + request.getPurchaseOrderId()));
            evaluation.setPurchaseOrder(po);
        }

        VendorEvaluation saved = evaluationRepository.save(evaluation);

        // Update supplier average rating
        List<VendorEvaluation> allEvals = evaluationRepository.findBySupplierIdOrderByEvaluatedAtDesc(supplier.getId());
        BigDecimal avgRating = allEvals.stream()
                .map(VendorEvaluation::getOverallRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allEvals.size()), 1, RoundingMode.HALF_UP);
        supplier.setRating(avgRating);
        supplierRepository.save(supplier);

        auditService.record("VendorEvaluation", saved.getId().toString(), AuditAction.CREATE, username,
                "Vendor evaluated: " + supplier.getName() + " overall=" + overall);
        return VendorEvaluationDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<VendorEvaluationDto> getVendorEvaluations(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Vendor not found with ID: " + supplierId);
        }
        return evaluationRepository.findBySupplierIdOrderByEvaluatedAtDesc(supplierId).stream()
                .map(VendorEvaluationDto::fromEntity)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // PROCUREMENT HISTORY
    // =========================================================================

    @Transactional(readOnly = true)
    public VendorProcurementHistoryDto getProcurementHistoryForVendor(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + supplierId));

        List<VendorContractDto> contracts = contractRepository.findBySupplierId(supplierId).stream()
                .map(VendorContractDto::fromEntity)
                .collect(Collectors.toList());

        List<PurchaseOrder> poEntities = purchaseOrderRepository.findBySupplierId(supplierId);
        List<PurchaseOrderDto> purchaseOrders = poEntities.stream()
                .map(PurchaseOrderDto::fromEntity)
                .collect(Collectors.toList());

        List<GoodsReceiptDeliveryDto> deliveries = poEntities.stream()
                .flatMap(po -> deliveryRepository.findByPurchaseOrderId(po.getId()).stream())
                .map(GoodsReceiptDeliveryDto::fromEntity)
                .collect(Collectors.toList());

        List<VendorEvaluationDto> evaluations = evaluationRepository
                .findBySupplierIdOrderByEvaluatedAtDesc(supplierId).stream()
                .map(VendorEvaluationDto::fromEntity)
                .collect(Collectors.toList());

        BigDecimal totalSpend = poEntities.stream()
                .filter(po -> po.getStatus() == PurchaseOrderStatus.FULFILLED
                        || po.getStatus() == PurchaseOrderStatus.PARTIALLY_DELIVERED)
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRating = supplier.getRating() != null ? supplier.getRating() : BigDecimal.ZERO;

        VendorProcurementHistoryDto history = new VendorProcurementHistoryDto();
        history.setVendor(VendorDto.fromEntity(supplier));
        history.setContracts(contracts);
        history.setPurchaseOrders(purchaseOrders);
        history.setDeliveries(deliveries);
        history.setEvaluations(evaluations);
        history.setTotalOrders(poEntities.size());
        history.setTotalSpend(totalSpend);
        history.setAverageRating(avgRating);
        return history;
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private PurchaseOrder getPurchaseOrderEntity(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));
    }

    private synchronized String generateSupplierCode() {
        long count = supplierRepository.count() + 1;
        return "SUP-" + String.format("%05d", count);
    }

    private synchronized String generateContractNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = contractRepository.count() + 1;
        return "CNT-" + date + "-" + String.format("%04d", count);
    }

    private synchronized String generateQuotationNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = quotationRepository.count() + 1;
        return "QT-" + date + "-" + String.format("%04d", count);
    }

    private synchronized String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseOrderRepository.count() + 1;
        return "PO-" + date + "-" + String.format("%04d", count);
    }

    private synchronized String generateDeliveryNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long rand = ThreadLocalRandom.current().nextLong(10000, 99999);
        return "GRD-" + date + "-" + rand;
    }
}
