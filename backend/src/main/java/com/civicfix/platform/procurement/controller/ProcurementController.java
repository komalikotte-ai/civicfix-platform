package com.civicfix.platform.procurement.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.procurement.dto.*;
import com.civicfix.platform.procurement.enums.*;
import com.civicfix.platform.procurement.service.ProcurementService;
import com.civicfix.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procurement")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'EMPLOYEE')")
public class ProcurementController {

    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    // =========================================================================
    // VENDOR REGISTRATION & VERIFICATION
    // =========================================================================

    @PostMapping("/vendors")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<VendorDto> registerVendor(@Valid @RequestBody RegisterVendorRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Vendor registered successfully",
                procurementService.registerVendor(request, username));
    }

    @GetMapping("/vendors")
    public ApiResponse<PagedResponse<VendorDto>> searchVendors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(procurementService.searchVendors(keyword, active, pageable));
    }

    @GetMapping("/vendors/{id}")
    public ApiResponse<VendorDto> getVendorById(@PathVariable Long id) {
        return ApiResponse.ok(procurementService.getVendorById(id));
    }

    @PostMapping("/vendors/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<VendorDto> verifyVendor(@PathVariable Long id,
                                               @Valid @RequestBody VerifyVendorRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(procurementService.verifyVendor(id, request, username));
    }

    // =========================================================================
    // VENDOR CONTRACTS
    // =========================================================================

    @PostMapping("/contracts")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<VendorContractDto> createContract(@Valid @RequestBody CreateContractRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Contract created successfully",
                procurementService.createContract(request, username));
    }

    @GetMapping("/contracts")
    public ApiResponse<PagedResponse<VendorContractDto>> searchContracts(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(procurementService.searchContracts(supplierId, departmentId, status, pageable));
    }

    @GetMapping("/contracts/{id}")
    public ApiResponse<VendorContractDto> getContractById(@PathVariable Long id) {
        return ApiResponse.ok(procurementService.getContractById(id));
    }

    // =========================================================================
    // QUOTATIONS
    // =========================================================================

    @PostMapping("/quotations")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'EMPLOYEE')")
    public ApiResponse<QuotationDto> submitQuotation(@Valid @RequestBody CreateQuotationRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Quotation submitted successfully",
                procurementService.submitQuotation(request, username));
    }

    @GetMapping("/quotations")
    public ApiResponse<PagedResponse<QuotationDto>> searchQuotations(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long purchaseRequestId,
            @RequestParam(required = false) QuotationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(procurementService.searchQuotations(supplierId, departmentId, purchaseRequestId, status, pageable));
    }

    @PostMapping("/quotations/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<QuotationDto> acceptQuotation(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(procurementService.acceptQuotation(id, username));
    }

    // =========================================================================
    // PURCHASE ORDERS
    // =========================================================================

    @PostMapping("/purchase-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<PurchaseOrderDto> createPurchaseOrder(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Purchase order created successfully",
                procurementService.createPurchaseOrder(request, username));
    }

    @GetMapping("/purchase-orders")
    public ApiResponse<PagedResponse<PurchaseOrderDto>> searchPurchaseOrders(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(procurementService.searchPurchaseOrders(supplierId, departmentId, warehouseId, status, pageable));
    }

    @GetMapping("/purchase-orders/{id}")
    public ApiResponse<PurchaseOrderDto> getPurchaseOrderById(@PathVariable Long id) {
        return ApiResponse.ok(procurementService.getPurchaseOrderById(id));
    }

    @PostMapping("/purchase-orders/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<PurchaseOrderDto> submitForApproval(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(procurementService.submitForApproval(id, username));
    }

    @PostMapping("/purchase-orders/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<PurchaseOrderDto> approvePurchaseOrder(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(procurementService.approvePurchaseOrder(id, username));
    }

    @PostMapping("/purchase-orders/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<PurchaseOrderDto> rejectPurchaseOrder(@PathVariable Long id,
                                                              @RequestParam(defaultValue = "") String reason) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(procurementService.rejectPurchaseOrder(id, reason, username));
    }

    @PostMapping("/purchase-orders/{id}/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<PurchaseOrderDto> issuePurchaseOrder(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok(procurementService.issuePurchaseOrder(id, username));
    }

    // =========================================================================
    // DELIVERIES (GOODS RECEIPT)
    // =========================================================================

    @PostMapping("/deliveries")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'EMPLOYEE')")
    public ApiResponse<GoodsReceiptDeliveryDto> receiveDelivery(@Valid @RequestBody ReceiveDeliveryRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Delivery received successfully",
                procurementService.receiveDelivery(request, username));
    }

    @GetMapping("/deliveries/{id}")
    public ApiResponse<GoodsReceiptDeliveryDto> getDeliveryById(@PathVariable Long id) {
        return ApiResponse.ok(procurementService.getDeliveryById(id));
    }

    @GetMapping("/deliveries/by-po/{poId}")
    public ApiResponse<List<GoodsReceiptDeliveryDto>> getDeliveriesByPurchaseOrder(@PathVariable Long poId) {
        return ApiResponse.ok(procurementService.getDeliveriesForPurchaseOrder(poId));
    }

    // =========================================================================
    // VENDOR EVALUATIONS
    // =========================================================================

    @PostMapping("/evaluations")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<VendorEvaluationDto> evaluateVendor(@Valid @RequestBody CreateVendorEvaluationRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Vendor evaluation recorded successfully",
                procurementService.evaluateVendor(request, username));
    }

    @GetMapping("/evaluations/vendor/{vendorId}")
    public ApiResponse<List<VendorEvaluationDto>> getVendorEvaluations(@PathVariable Long vendorId) {
        return ApiResponse.ok(procurementService.getVendorEvaluations(vendorId));
    }

    // =========================================================================
    // PROCUREMENT HISTORY
    // =========================================================================

    @GetMapping("/history/vendor/{vendorId}")
    public ApiResponse<VendorProcurementHistoryDto> getProcurementHistoryForVendor(@PathVariable Long vendorId) {
        return ApiResponse.ok(procurementService.getProcurementHistoryForVendor(vendorId));
    }
}
