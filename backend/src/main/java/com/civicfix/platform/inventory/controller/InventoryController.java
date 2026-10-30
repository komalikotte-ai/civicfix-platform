package com.civicfix.platform.inventory.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.inventory.dto.*;
import com.civicfix.platform.inventory.enums.*;
import com.civicfix.platform.inventory.service.InventoryService;
import com.civicfix.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'FIELD_WORKER', 'EMPLOYEE', 'OFFICER')")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // =========================================================================
    // WAREHOUSES
    // =========================================================================

    @PostMapping("/warehouses")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<WarehouseDto> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Warehouse created successfully", inventoryService.createWarehouse(request, username));
    }

    @GetMapping("/warehouses")
    public ApiResponse<PagedResponse<WarehouseDto>> searchWarehouses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) WarehouseStatus status,
            @RequestParam(required = false) WarehouseType warehouseType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(inventoryService.searchWarehouses(keyword, departmentId, organizationId, status, warehouseType, pageable));
    }

    @GetMapping("/warehouses/{id}")
    public ApiResponse<WarehouseDto> getWarehouseById(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.getWarehouseById(id));
    }

    @PutMapping("/warehouses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'SUPERVISOR')")
    public ApiResponse<WarehouseDto> updateWarehouse(@PathVariable Long id, @Valid @RequestBody UpdateWarehouseRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Warehouse updated successfully", inventoryService.updateWarehouse(id, request, username));
    }

    @GetMapping("/warehouses/{id}/stock")
    public ApiResponse<PagedResponse<WarehouseStockDto>> getStock(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestBuilder.build(page, size);
        return ApiResponse.ok(inventoryService.getStock(id, pageable));
    }

    @GetMapping("/warehouses/{id}/stock/low")
    public ApiResponse<PagedResponse<WarehouseStockDto>> getLowStockItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestBuilder.build(page, size);
        return ApiResponse.ok(inventoryService.getLowStockItems(id, pageable));
    }

    @GetMapping("/warehouses/{id}/stock/minimum")
    public ApiResponse<PagedResponse<WarehouseStockDto>> getBelowMinimumStockItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestBuilder.build(page, size);
        return ApiResponse.ok(inventoryService.getBelowMinimumStockItems(id, pageable));
    }

    @PostMapping("/stock/reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER')")
    public ApiResponse<WarehouseStockDto> reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Stock reserved successfully", inventoryService.reserveStock(request, username));
    }

    @PostMapping("/stock/release-reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'DEPARTMENT_MANAGER')")
    public ApiResponse<WarehouseStockDto> releaseReservedStock(@Valid @RequestBody ReserveStockRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Reserved stock released successfully", inventoryService.releaseReservedStock(request, username));
    }

    @PostMapping("/stock/reconcile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'SUPERVISOR')")
    public ApiResponse<WarehouseStockDto> reconcileStock(@Valid @RequestBody ReconcileStockRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Stock reconciled successfully", inventoryService.reconcileStock(request, username));
    }

    @GetMapping("/reports/summary")
    public ApiResponse<InventorySummaryReportDto> getInventorySummaryReport() {
        return ApiResponse.ok(inventoryService.getInventorySummaryReport());
    }

    @GetMapping("/transactions/movements")
    public ApiResponse<PagedResponse<StockTransactionDto>> searchStockMovements(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(inventoryService.searchStockMovements(warehouseId, itemId, transactionType, startDate, endDate, pageable));
    }

    @GetMapping("/warehouses/{id}/transactions")
    public ApiResponse<PagedResponse<StockTransactionDto>> getTransactions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequestBuilder.build(page, size, "transactionDate", "desc");
        return ApiResponse.ok(inventoryService.getTransactionsForWarehouse(id, pageable));
    }

    // =========================================================================
    // ITEMS
    // =========================================================================

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<InventoryItemDto> createItem(@Valid @RequestBody CreateItemRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Item created successfully", inventoryService.createItem(request, username));
    }

    @GetMapping("/items")
    public ApiResponse<PagedResponse<InventoryItemDto>> searchItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ItemCategory itemCategory,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(inventoryService.searchItems(keyword, itemCategory, active, pageable));
    }

    @GetMapping("/items/{id}")
    public ApiResponse<InventoryItemDto> getItemById(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.getItemById(id));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN', 'SUPERVISOR')")
    public ApiResponse<InventoryItemDto> updateItem(@PathVariable Long id, @Valid @RequestBody UpdateItemRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Item updated successfully", inventoryService.updateItem(id, request, username));
    }

    // =========================================================================
    // TRANSACTIONS
    // =========================================================================

    @PostMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'EMPLOYEE')")
    public ApiResponse<StockTransactionDto> recordTransaction(@Valid @RequestBody RecordStockTransactionRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Stock transaction recorded successfully", inventoryService.recordTransaction(request, username));
    }

    // =========================================================================
    // MATERIAL USAGE
    // =========================================================================

    @PostMapping("/material-usage")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR', 'FIELD_WORKER')")
    public ApiResponse<WorkOrderMaterialUsageDto> recordMaterialUsage(@Valid @RequestBody RecordMaterialUsageRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Material usage recorded successfully", inventoryService.recordMaterialUsage(request, username));
    }

    @GetMapping("/material-usage/work-order/{workOrderId}")
    public ApiResponse<List<WorkOrderMaterialUsageDto>> getMaterialUsageForWorkOrder(@PathVariable Long workOrderId) {
        return ApiResponse.ok(inventoryService.getMaterialUsageForWorkOrder(workOrderId));
    }

    // =========================================================================
    // SUPPLIERS
    // =========================================================================

    @PostMapping("/suppliers")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<SupplierDto> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Supplier created successfully", inventoryService.createSupplier(request, username));
    }

    @GetMapping("/suppliers")
    public ApiResponse<PagedResponse<SupplierDto>> searchSuppliers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(inventoryService.searchSuppliers(keyword, active, pageable));
    }

    @GetMapping("/suppliers/{id}")
    public ApiResponse<SupplierDto> getSupplierById(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.getSupplierById(id));
    }

    @PutMapping("/suppliers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<SupplierDto> updateSupplier(@PathVariable Long id, @Valid @RequestBody UpdateSupplierRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Supplier updated successfully", inventoryService.updateSupplier(id, request, username));
    }

    @PostMapping("/suppliers/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER', 'DEPT_ADMIN')")
    public ApiResponse<SupplierItemDto> addSupplierItem(@PathVariable Long id,
                                                         @Valid @RequestBody AddSupplierItemRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Supplier item added successfully", inventoryService.addSupplierItem(id, request, username));
    }

    @GetMapping("/suppliers/{id}/items")
    public ApiResponse<List<SupplierItemDto>> getSupplierItems(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.getSupplierItems(id));
    }

    // =========================================================================
    // PURCHASE REQUESTS
    // =========================================================================

    @PostMapping("/purchase-requests")
    public ApiResponse<PurchaseRequestDto> createPurchaseRequest(@Valid @RequestBody CreatePurchaseRequestRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.created("Purchase request created successfully", inventoryService.createPurchaseRequest(request, username));
    }

    @GetMapping("/purchase-requests")
    public ApiResponse<PagedResponse<PurchaseRequestDto>> searchPurchaseRequests(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) PurchaseRequestStatus status,
            @RequestParam(required = false) PurchasePriority priority,
            @RequestParam(required = false) Long workOrderId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(inventoryService.searchPurchaseRequests(
                departmentId, organizationId, status, priority, workOrderId, startDate, endDate, pageable));
    }

    @GetMapping("/purchase-requests/{id}")
    public ApiResponse<PurchaseRequestDto> getPurchaseRequestById(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.getPurchaseRequestById(id));
    }

    @PostMapping("/purchase-requests/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<PurchaseRequestDto> approvePurchaseRequest(@PathVariable Long id,
                                                                   @RequestBody(required = false) ApprovePurchaseRequestRequest request) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        if (request == null) request = new ApprovePurchaseRequestRequest();
        return ApiResponse.ok("Purchase request approved", inventoryService.approvePurchaseRequest(id, request, username));
    }

    @PostMapping("/purchase-requests/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DEPARTMENT_MANAGER')")
    public ApiResponse<PurchaseRequestDto> rejectPurchaseRequest(@PathVariable Long id,
                                                                  @RequestParam(required = false) String reason) {
        String username = SecurityUtils.getCurrentUsername().orElse("SYSTEM");
        return ApiResponse.ok("Purchase request rejected", inventoryService.rejectPurchaseRequest(id, reason, username));
    }
}
