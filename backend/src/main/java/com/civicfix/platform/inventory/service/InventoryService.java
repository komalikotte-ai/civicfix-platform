package com.civicfix.platform.inventory.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.inventory.dto.*;
import com.civicfix.platform.inventory.entity.*;
import com.civicfix.platform.inventory.enums.PurchaseRequestStatus;
import com.civicfix.platform.inventory.enums.TransactionType;
import com.civicfix.platform.inventory.enums.WarehouseStatus;
import com.civicfix.platform.inventory.repository.*;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.organizations.repository.OrganizationRepository;
import com.civicfix.platform.inventory.enums.ItemCategory;
import com.civicfix.platform.inventory.enums.WarehouseType;
import com.civicfix.platform.inventory.enums.PurchasePriority;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.repository.EmployeeRepository;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository itemRepository;
    private final WarehouseStockRepository stockRepository;
    private final StockTransactionRepository transactionRepository;
    private final WorkOrderMaterialUsageRepository materialUsageRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierItemRepository supplierItemRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseRequestItemRepository purchaseRequestItemRepository;
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AuditService auditService;

    public InventoryService(WarehouseRepository warehouseRepository,
                            InventoryItemRepository itemRepository,
                            WarehouseStockRepository stockRepository,
                            StockTransactionRepository transactionRepository,
                            WorkOrderMaterialUsageRepository materialUsageRepository,
                            SupplierRepository supplierRepository,
                            SupplierItemRepository supplierItemRepository,
                            PurchaseRequestRepository purchaseRequestRepository,
                            PurchaseRequestItemRepository purchaseRequestItemRepository,
                            DepartmentRepository departmentRepository,
                            OrganizationRepository organizationRepository,
                            EmployeeRepository employeeRepository,
                            WorkOrderRepository workOrderRepository,
                            AuditService auditService) {
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
        this.stockRepository = stockRepository;
        this.transactionRepository = transactionRepository;
        this.materialUsageRepository = materialUsageRepository;
        this.supplierRepository = supplierRepository;
        this.supplierItemRepository = supplierItemRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.purchaseRequestItemRepository = purchaseRequestItemRepository;
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
        this.employeeRepository = employeeRepository;
        this.workOrderRepository = workOrderRepository;
        this.auditService = auditService;
    }

    // =========================================================================
    // WAREHOUSES
    // =========================================================================

    @Transactional
    public WarehouseDto createWarehouse(CreateWarehouseRequest request, String username) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + request.getOrganizationId()));

        String code = (request.getWarehouseCode() != null && !request.getWarehouseCode().isBlank())
                ? request.getWarehouseCode().toUpperCase().trim()
                : generateWarehouseCode(department);

        if (warehouseRepository.existsByWarehouseCode(code)) {
            throw new ConflictException("Warehouse code already exists: " + code);
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getManagerId()));
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseCode(code);
        warehouse.setName(request.getName());
        warehouse.setDescription(request.getDescription());
        warehouse.setAddress(request.getAddress());
        warehouse.setLatitude(request.getLatitude());
        warehouse.setLongitude(request.getLongitude());
        warehouse.setWarehouseType(request.getWarehouseType() != null ? request.getWarehouseType() : com.civicfix.platform.inventory.enums.WarehouseType.GENERAL);
        warehouse.setStatus(WarehouseStatus.ACTIVE);
        warehouse.setDepartment(department);
        warehouse.setOrganization(organization);
        warehouse.setManager(manager);
        warehouse.setCapacityNotes(request.getCapacityNotes());

        Warehouse saved = warehouseRepository.save(warehouse);
        auditService.record("Warehouse", saved.getId().toString(), AuditAction.CREATE, username,
                "Warehouse created: " + saved.getWarehouseCode());
        return WarehouseDto.fromEntity(saved);
    }

    @Transactional
    public WarehouseDto updateWarehouse(Long id, UpdateWarehouseRequest request, String username) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) warehouse.setName(request.getName());
        if (request.getDescription() != null) warehouse.setDescription(request.getDescription());
        if (request.getAddress() != null) warehouse.setAddress(request.getAddress());
        if (request.getLatitude() != null) warehouse.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) warehouse.setLongitude(request.getLongitude());
        if (request.getWarehouseType() != null) warehouse.setWarehouseType(request.getWarehouseType());
        if (request.getStatus() != null) warehouse.setStatus(request.getStatus());
        if (request.getCapacityNotes() != null) warehouse.setCapacityNotes(request.getCapacityNotes());
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getManagerId()));
            warehouse.setManager(manager);
        }

        Warehouse saved = warehouseRepository.save(warehouse);
        auditService.record("Warehouse", saved.getId().toString(), AuditAction.UPDATE, username,
                "Warehouse updated: " + saved.getWarehouseCode());
        return WarehouseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public WarehouseDto getWarehouseById(Long id) {
        return WarehouseDto.fromEntity(warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id)));
    }

    @Transactional(readOnly = true)
    public PagedResponse<WarehouseDto> searchWarehouses(String keyword, Long departmentId, Long organizationId,
                                                         WarehouseStatus status, WarehouseType warehouseType,
                                                         Pageable pageable) {
        Page<WarehouseDto> page = warehouseRepository
                .searchWarehouses(keyword, departmentId, organizationId, status, warehouseType, pageable)
                .map(WarehouseDto::fromEntity);
        return PagedResponse.from(page);
    }

    // =========================================================================
    // INVENTORY ITEMS
    // =========================================================================

    @Transactional
    public InventoryItemDto createItem(CreateItemRequest request, String username) {
        String code = (request.getItemCode() != null && !request.getItemCode().isBlank())
                ? request.getItemCode().toUpperCase().trim()
                : generateItemCode(request.getItemCategory());

        if (itemRepository.existsByItemCode(code)) {
            throw new ConflictException("Item code already exists: " + code);
        }

        InventoryItem item = new InventoryItem();
        item.setItemCode(code);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setItemCategory(request.getItemCategory());
        item.setUnitOfMeasure(request.getUnitOfMeasure());
        item.setUnitPrice(request.getUnitPrice() != null ? request.getUnitPrice() : BigDecimal.ZERO);
        item.setMinimumStock(request.getMinimumStock());
        item.setReorderLevel(request.getReorderLevel());
        item.setMaximumStock(request.getMaximumStock());
        item.setHsnSacCode(request.getHsnSacCode());
        item.setSpecifications(request.getSpecifications());

        InventoryItem saved = itemRepository.save(item);
        auditService.record("InventoryItem", saved.getId().toString(), AuditAction.CREATE, username,
                "Item created: " + saved.getItemCode());
        return InventoryItemDto.fromEntity(saved);
    }

    @Transactional
    public InventoryItemDto updateItem(Long id, UpdateItemRequest request, String username) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getItemCategory() != null) item.setItemCategory(request.getItemCategory());
        if (request.getUnitOfMeasure() != null) item.setUnitOfMeasure(request.getUnitOfMeasure());
        if (request.getUnitPrice() != null) item.setUnitPrice(request.getUnitPrice());
        if (request.getMinimumStock() != null) item.setMinimumStock(request.getMinimumStock());
        if (request.getReorderLevel() != null) item.setReorderLevel(request.getReorderLevel());
        if (request.getMaximumStock() != null) item.setMaximumStock(request.getMaximumStock());
        if (request.getHsnSacCode() != null) item.setHsnSacCode(request.getHsnSacCode());
        if (request.getSpecifications() != null) item.setSpecifications(request.getSpecifications());
        if (request.getActive() != null) item.setActive(request.getActive());

        InventoryItem saved = itemRepository.save(item);
        auditService.record("InventoryItem", saved.getId().toString(), AuditAction.UPDATE, username,
                "Item updated: " + saved.getItemCode());
        return InventoryItemDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public InventoryItemDto getItemById(Long id) {
        return InventoryItemDto.fromEntity(itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with ID: " + id)));
    }

    @Transactional(readOnly = true)
    public PagedResponse<InventoryItemDto> searchItems(String keyword, ItemCategory itemCategory,
                                                        Boolean active, Pageable pageable) {
        Page<InventoryItemDto> page = itemRepository
                .searchItems(keyword, itemCategory, active, pageable)
                .map(InventoryItemDto::fromEntity);
        return PagedResponse.from(page);
    }

    // =========================================================================
    // STOCK
    // =========================================================================

    @Transactional(readOnly = true)
    public PagedResponse<WarehouseStockDto> getStock(Long warehouseId, Pageable pageable) {
        warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + warehouseId));
        Page<WarehouseStockDto> page = stockRepository.findByWarehouseId(warehouseId, pageable)
                .map(WarehouseStockDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WarehouseStockDto> getLowStockItems(Long warehouseId, Pageable pageable) {
        warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + warehouseId));
        Page<WarehouseStockDto> page = stockRepository.findLowStockByWarehouseId(warehouseId, pageable)
                .map(WarehouseStockDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WarehouseStockDto> getBelowMinimumStockItems(Long warehouseId, Pageable pageable) {
        warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + warehouseId));
        Page<WarehouseStockDto> page = stockRepository.findBelowMinimumStockByWarehouseId(warehouseId, pageable)
                .map(WarehouseStockDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional
    public WarehouseStockDto reserveStock(ReserveStockRequest request, String username) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));
        InventoryItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + request.getItemId()));

        WarehouseStock stock = stockRepository.findByWarehouseIdAndItemId(warehouse.getId(), item.getId())
                .orElseThrow(() -> new BadRequestException("No stock record exists for item " + item.getItemCode() + " in warehouse " + warehouse.getWarehouseCode()));

        int available = stock.getQuantity() - stock.getReservedQty();
        if (available < request.getQuantity()) {
            throw new BadRequestException(String.format(
                    "Cannot reserve %d units of %s. Total: %d, Already reserved: %d, Available: %d",
                    request.getQuantity(), item.getItemCode(), stock.getQuantity(), stock.getReservedQty(), available));
        }

        stock.setReservedQty(stock.getReservedQty() + request.getQuantity());
        stock.setUpdatedAt(Instant.now());
        WarehouseStock saved = stockRepository.save(stock);

        auditService.record("WarehouseStock", saved.getId().toString(), AuditAction.UPDATE, username,
                String.format("Reserved %d units of %s in warehouse %s", request.getQuantity(), item.getItemCode(), warehouse.getWarehouseCode()));
        return WarehouseStockDto.fromEntity(saved);
    }

    @Transactional
    public WarehouseStockDto releaseReservedStock(ReserveStockRequest request, String username) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));
        InventoryItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + request.getItemId()));

        WarehouseStock stock = stockRepository.findByWarehouseIdAndItemId(warehouse.getId(), item.getId())
                .orElseThrow(() -> new BadRequestException("No stock record exists for item " + item.getItemCode() + " in warehouse " + warehouse.getWarehouseCode()));

        if (stock.getReservedQty() < request.getQuantity()) {
            throw new BadRequestException(String.format(
                    "Cannot release %d reserved units of %s. Currently reserved: %d",
                    request.getQuantity(), item.getItemCode(), stock.getReservedQty()));
        }

        stock.setReservedQty(stock.getReservedQty() - request.getQuantity());
        stock.setUpdatedAt(Instant.now());
        WarehouseStock saved = stockRepository.save(stock);

        auditService.record("WarehouseStock", saved.getId().toString(), AuditAction.UPDATE, username,
                String.format("Released %d reserved units of %s in warehouse %s", request.getQuantity(), item.getItemCode(), warehouse.getWarehouseCode()));
        return WarehouseStockDto.fromEntity(saved);
    }

    @Transactional
    public WarehouseStockDto reconcileStock(ReconcileStockRequest request, String username) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));
        InventoryItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + request.getItemId()));

        WarehouseStock stock = stockRepository.findByWarehouseIdAndItemId(warehouse.getId(), item.getId())
                .orElseGet(() -> {
                    WarehouseStock s = new WarehouseStock();
                    s.setWarehouse(warehouse);
                    s.setItem(item);
                    s.setQuantity(0);
                    s.setReservedQty(0);
                    s.setCreatedAt(Instant.now());
                    return s;
                });

        int systemQty = stock.getQuantity();
        int physicalQty = request.getPhysicalQuantity();
        int diff = physicalQty - systemQty;

        stock.setQuantity(physicalQty);
        stock.setLastCountedAt(Instant.now());
        stock.setUpdatedAt(Instant.now());
        WarehouseStock saved = stockRepository.save(stock);

        // Record adjustment audit transaction if difference exists
        if (diff != 0) {
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            StockTransaction txn = new StockTransaction();
            txn.setTransactionCode(generateTransactionCode());
            txn.setWarehouse(warehouse);
            txn.setItem(item);
            txn.setTransactionType(TransactionType.ADJUSTMENT);
            txn.setQuantity(diff);
            txn.setUnitPrice(unitPrice);
            txn.setTotalValue(unitPrice.multiply(BigDecimal.valueOf(Math.abs(diff))));
            txn.setRemarks("Physical stock reconciliation: System=" + systemQty + ", Count=" + physicalQty + ". " + (request.getNotes() != null ? request.getNotes() : ""));
            txn.setTransactionDate(Instant.now());
            txn.setCreatedAt(Instant.now());
            txn.setCreatedBy(username);
            transactionRepository.save(txn);
        }

        auditService.record("WarehouseStock", saved.getId().toString(), AuditAction.UPDATE, username,
                String.format("Reconciled stock for item %s in warehouse %s: previous=%d, physical=%d, variance=%d",
                        item.getItemCode(), warehouse.getWarehouseCode(), systemQty, physicalQty, diff));
        return WarehouseStockDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public InventorySummaryReportDto getInventorySummaryReport() {
        InventorySummaryReportDto report = new InventorySummaryReportDto();
        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<InventoryItem> items = itemRepository.findAll();
        List<WarehouseStock> allStock = stockRepository.findAll();

        report.setTotalWarehouses(warehouses.size());
        report.setTotalItems(items.size());

        long lowStockCount = allStock.stream().filter(s -> s.getItem() != null && s.getQuantity() <= s.getItem().getReorderLevel()).count();
        long minStockCount = allStock.stream().filter(s -> s.getItem() != null && s.getQuantity() <= s.getItem().getMinimumStock()).count();
        report.setTotalLowStockItems(lowStockCount);
        report.setTotalBelowMinimumStockItems(minStockCount);

        BigDecimal totalValuation = BigDecimal.ZERO;
        for (WarehouseStock s : allStock) {
            if (s.getItem() != null && s.getItem().getUnitPrice() != null) {
                totalValuation = totalValuation.add(s.getItem().getUnitPrice().multiply(BigDecimal.valueOf(s.getQuantity())));
            }
        }
        report.setTotalInventoryValuation(totalValuation);

        List<InventorySummaryReportDto.WarehouseStockSummaryDto> summaries = warehouses.stream().map(wh -> {
            InventorySummaryReportDto.WarehouseStockSummaryDto wSummary = new InventorySummaryReportDto.WarehouseStockSummaryDto();
            wSummary.setWarehouseId(wh.getId());
            wSummary.setWarehouseCode(wh.getWarehouseCode());
            wSummary.setWarehouseName(wh.getName());

            List<WarehouseStock> whStock = allStock.stream().filter(s -> s.getWarehouse() != null && wh.getId().equals(s.getWarehouse().getId())).toList();
            wSummary.setTotalStockItems(whStock.size());
            wSummary.setTotalUnits(whStock.stream().mapToInt(WarehouseStock::getQuantity).sum());
            wSummary.setTotalReservedUnits(whStock.stream().mapToInt(WarehouseStock::getReservedQty).sum());
            wSummary.setLowStockItemsCount((int) whStock.stream().filter(s -> s.getItem() != null && s.getQuantity() <= s.getItem().getReorderLevel()).count());

            BigDecimal whValuation = BigDecimal.ZERO;
            for (WarehouseStock s : whStock) {
                if (s.getItem() != null && s.getItem().getUnitPrice() != null) {
                    whValuation = whValuation.add(s.getItem().getUnitPrice().multiply(BigDecimal.valueOf(s.getQuantity())));
                }
            }
            wSummary.setTotalValuation(whValuation);
            return wSummary;
        }).collect(Collectors.toList());

        report.setWarehouseSummaries(summaries);
        return report;
    }

    // =========================================================================
    // TRANSACTIONS
    // =========================================================================

    @Transactional
    public synchronized StockTransactionDto recordTransaction(RecordStockTransactionRequest request, String username) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));
        InventoryItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + request.getItemId()));

        TransactionType type = request.getTransactionType();
        int qty = request.getQuantity();

        if (type == TransactionType.STOCK_IN) {
            adjustStock(warehouse, item, qty);
        } else if (type == TransactionType.STOCK_OUT) {
            ensureSufficientStock(warehouse, item, qty);
            adjustStock(warehouse, item, -qty);
        } else if (type == TransactionType.TRANSFER) {
            if (request.getToWarehouseId() == null) {
                throw new BadRequestException("Destination warehouse ID is required for TRANSFER transactions");
            }
            Warehouse toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination warehouse not found with ID: " + request.getToWarehouseId()));
            ensureSufficientStock(warehouse, item, qty);
            adjustStock(warehouse, item, -qty);
            adjustStock(toWarehouse, item, qty);
        } else if (type == TransactionType.ADJUSTMENT) {
            // net adjustment — positive adds, negative removes
            if (qty < 0) {
                ensureSufficientStock(warehouse, item, -qty);
            }
            adjustStock(warehouse, item, qty);
        } else if (type == TransactionType.RETURN) {
            adjustStock(warehouse, item, qty);
        }

        BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal totalValue = unitPrice.multiply(BigDecimal.valueOf(Math.abs(qty)));

        WorkOrder workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + request.getWorkOrderId()));
        }
        Warehouse toWarehouse = null;
        if (request.getToWarehouseId() != null && type == TransactionType.TRANSFER) {
            toWarehouse = warehouseRepository.findById(request.getToWarehouseId()).orElse(null);
        }
        Employee performedBy = null;
        if (request.getPerformedById() != null) {
            performedBy = employeeRepository.findById(request.getPerformedById()).orElse(null);
        }

        StockTransaction txn = new StockTransaction();
        txn.setTransactionCode(generateTransactionCode());
        txn.setWarehouse(warehouse);
        txn.setItem(item);
        txn.setTransactionType(type);
        txn.setQuantity(qty);
        txn.setUnitPrice(unitPrice);
        txn.setTotalValue(totalValue);
        txn.setRemarks(request.getRemarks());
        txn.setWorkOrder(workOrder);
        txn.setToWarehouse(toWarehouse);
        txn.setTransactionDate(Instant.now());
        txn.setPerformedBy(performedBy);
        txn.setCreatedAt(Instant.now());
        txn.setCreatedBy(username);

        StockTransaction saved = transactionRepository.save(txn);
        auditService.record("StockTransaction", saved.getId().toString(), AuditAction.CREATE, username,
                "Stock transaction: " + type + " qty=" + qty + " item=" + item.getItemCode() + " warehouse=" + warehouse.getWarehouseCode());
        return StockTransactionDto.fromEntity(saved);
    }

    @Transactional
    public WorkOrderMaterialUsageDto recordMaterialUsage(RecordMaterialUsageRequest request, String username) {
        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + request.getWorkOrderId()));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));
        InventoryItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + request.getItemId()));

        // Deduct from stock via STOCK_OUT transaction
        ensureSufficientStock(warehouse, item, request.getQuantityUsed());
        adjustStock(warehouse, item, -request.getQuantityUsed());

        BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal totalCost = unitPrice.multiply(BigDecimal.valueOf(request.getQuantityUsed()));

        // Record stock transaction
        StockTransaction txn = new StockTransaction();
        txn.setTransactionCode(generateTransactionCode());
        txn.setWarehouse(warehouse);
        txn.setItem(item);
        txn.setTransactionType(TransactionType.STOCK_OUT);
        txn.setQuantity(request.getQuantityUsed());
        txn.setUnitPrice(unitPrice);
        txn.setTotalValue(totalCost);
        txn.setWorkOrder(workOrder);
        txn.setRemarks("Material used for work order: " + workOrder.getWorkOrderNumber());
        txn.setTransactionDate(Instant.now());
        txn.setCreatedAt(Instant.now());
        txn.setCreatedBy(username);
        transactionRepository.save(txn);

        // Record material usage
        WorkOrderMaterialUsage usage = new WorkOrderMaterialUsage();
        usage.setWorkOrder(workOrder);
        usage.setItem(item);
        usage.setWarehouse(warehouse);
        usage.setQuantityUsed(request.getQuantityUsed());
        usage.setUnitPrice(unitPrice);
        usage.setTotalCost(totalCost);
        usage.setUsageNotes(request.getUsageNotes());
        usage.setRecordedBy(username);
        usage.setRecordedAt(Instant.now());
        usage.setCreatedAt(Instant.now());

        WorkOrderMaterialUsage saved = materialUsageRepository.save(usage);
        auditService.record("WorkOrderMaterialUsage", saved.getId().toString(), AuditAction.CREATE, username,
                "Material usage recorded for WO " + workOrder.getWorkOrderNumber() + " item=" + item.getItemCode());
        return WorkOrderMaterialUsageDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderMaterialUsageDto> getMaterialUsageForWorkOrder(Long workOrderId) {
        return materialUsageRepository.findByWorkOrderIdOrderByRecordedAtDesc(workOrderId)
                .stream().map(WorkOrderMaterialUsageDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<StockTransactionDto> getTransactionsForWarehouse(Long warehouseId, Pageable pageable) {
        warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + warehouseId));
        Page<StockTransactionDto> page = transactionRepository
                .findByWarehouseIdOrderByTransactionDateDesc(warehouseId, pageable)
                .map(StockTransactionDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StockTransactionDto> searchStockMovements(Long warehouseId, Long itemId, TransactionType type,
                                                                   Instant startDate, Instant endDate, Pageable pageable) {
        Page<StockTransactionDto> page = transactionRepository
                .searchTransactions(warehouseId, itemId, type, startDate, endDate, pageable)
                .map(StockTransactionDto::fromEntity);
        return PagedResponse.from(page);
    }

    // =========================================================================
    // SUPPLIERS
    // =========================================================================

    @Transactional
    public SupplierDto createSupplier(CreateSupplierRequest request, String username) {
        String code = (request.getSupplierCode() != null && !request.getSupplierCode().isBlank())
                ? request.getSupplierCode().toUpperCase().trim()
                : generateSupplierCode();

        if (supplierRepository.existsBySupplierCode(code)) {
            throw new ConflictException("Supplier code already exists: " + code);
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
        supplier.setRating(request.getRating() != null ? request.getRating() : BigDecimal.ZERO);

        Supplier saved = supplierRepository.save(supplier);
        auditService.record("Supplier", saved.getId().toString(), AuditAction.CREATE, username,
                "Supplier created: " + saved.getSupplierCode());
        return SupplierDto.fromEntity(saved);
    }

    @Transactional
    public SupplierDto updateSupplier(Long id, UpdateSupplierRequest request, String username) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) supplier.setName(request.getName());
        if (request.getContactPerson() != null) supplier.setContactPerson(request.getContactPerson());
        if (request.getEmail() != null) supplier.setEmail(request.getEmail());
        if (request.getPhone() != null) supplier.setPhone(request.getPhone());
        if (request.getAddress() != null) supplier.setAddress(request.getAddress());
        if (request.getGstin() != null) supplier.setGstin(request.getGstin());
        if (request.getPan() != null) supplier.setPan(request.getPan());
        if (request.getPaymentTerms() != null) supplier.setPaymentTerms(request.getPaymentTerms());
        if (request.getLeadTimeDays() != null) supplier.setLeadTimeDays(request.getLeadTimeDays());
        if (request.getRating() != null) supplier.setRating(request.getRating());
        if (request.getActive() != null) supplier.setActive(request.getActive());

        Supplier saved = supplierRepository.save(supplier);
        auditService.record("Supplier", saved.getId().toString(), AuditAction.UPDATE, username,
                "Supplier updated: " + saved.getSupplierCode());
        return SupplierDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public SupplierDto getSupplierById(Long id) {
        return SupplierDto.fromEntity(supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id)));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SupplierDto> searchSuppliers(String keyword, Boolean active, Pageable pageable) {
        Page<SupplierDto> page = supplierRepository.searchSuppliers(keyword, active, pageable)
                .map(SupplierDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional
    public SupplierItemDto addSupplierItem(Long supplierId, AddSupplierItemRequest request, String username) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + supplierId));
        InventoryItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + request.getItemId()));

        if (supplierItemRepository.findBySupplierIdAndItemId(supplierId, request.getItemId()).isPresent()) {
            throw new ConflictException("Supplier already has this item in their catalog");
        }

        SupplierItem si = new SupplierItem();
        si.setSupplier(supplier);
        si.setItem(item);
        si.setUnitPrice(request.getUnitPrice());
        si.setLeadTimeDays(request.getLeadTimeDays());
        si.setPreferred(request.isPreferred());
        si.setCreatedAt(Instant.now());

        SupplierItem saved = supplierItemRepository.save(si);
        auditService.record("SupplierItem", saved.getId().toString(), AuditAction.CREATE, username,
                "Supplier item added: supplier=" + supplier.getSupplierCode() + " item=" + item.getItemCode());
        return SupplierItemDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SupplierItemDto> getSupplierItems(Long supplierId) {
        supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + supplierId));
        return supplierItemRepository.findBySupplierId(supplierId)
                .stream().map(SupplierItemDto::fromEntity).collect(Collectors.toList());
    }

    // =========================================================================
    // PURCHASE REQUESTS
    // =========================================================================

    @Transactional
    public PurchaseRequestDto createPurchaseRequest(CreatePurchaseRequestRequest request, String username) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        Organization organization = department.getOrganization();
        if (organization == null) {
            throw new BadRequestException("Department is not linked to an organization");
        }

        Warehouse warehouse = null;
        if (request.getWarehouseId() != null) {
            warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + request.getWarehouseId()));
        }
        WorkOrder workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + request.getWorkOrderId()));
        }
        Employee requestedBy = null;
        if (request.getRequestedById() != null) {
            requestedBy = employeeRepository.findById(request.getRequestedById()).orElse(null);
        }

        PurchaseRequest pr = new PurchaseRequest();
        pr.setRequestCode(generateRequestCode());
        pr.setTitle(request.getTitle());
        pr.setJustification(request.getJustification());
        pr.setStatus(PurchaseRequestStatus.PENDING);
        pr.setPriority(request.getPriority() != null ? request.getPriority() : PurchasePriority.MEDIUM);
        pr.setDepartment(department);
        pr.setOrganization(organization);
        pr.setWarehouse(warehouse);
        pr.setRequestedBy(requestedBy);
        pr.setExpectedDelivery(request.getExpectedDelivery());
        pr.setWorkOrder(workOrder);

        // Build line items and compute total
        BigDecimal total = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (PurchaseRequestLineItemRequest lineReq : request.getItems()) {
                InventoryItem item = itemRepository.findById(lineReq.getItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + lineReq.getItemId()));
                BigDecimal unitPrice = lineReq.getUnitPriceEstimate() != null ? lineReq.getUnitPriceEstimate() : BigDecimal.ZERO;
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(lineReq.getQuantityRequested()));

                PurchaseRequestItem pri = new PurchaseRequestItem();
                pri.setPurchaseRequest(pr);
                pri.setItem(item);
                pri.setQuantityRequested(lineReq.getQuantityRequested());
                pri.setUnitPriceEstimate(unitPrice);
                pri.setTotalEstimatedCost(lineTotal);
                pri.setRemarks(lineReq.getRemarks());
                pri.setCreatedAt(Instant.now());

                if (lineReq.getSupplierId() != null) {
                    supplierRepository.findById(lineReq.getSupplierId()).ifPresent(pri::setSupplier);
                }
                pr.getItems().add(pri);
                total = total.add(lineTotal);
            }
        }
        pr.setTotalEstimatedCost(total);

        PurchaseRequest saved = purchaseRequestRepository.save(pr);
        auditService.record("PurchaseRequest", saved.getId().toString(), AuditAction.CREATE, username,
                "Purchase request created: " + saved.getRequestCode());
        return PurchaseRequestDto.fromEntity(saved);
    }

    @Transactional
    public PurchaseRequestDto approvePurchaseRequest(Long id, ApprovePurchaseRequestRequest request, String username) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with ID: " + id));

        if (pr.getStatus() != PurchaseRequestStatus.PENDING) {
            throw new BadRequestException("Only PENDING purchase requests can be approved. Current status: " + pr.getStatus());
        }

        pr.setStatus(PurchaseRequestStatus.APPROVED);
        pr.setApprovedBy(username);
        pr.setApprovedAt(Instant.now());
        if (request.getRemarks() != null) pr.setRejectionReason(null); // clear any old rejection

        // Apply approved quantities if provided
        if (request.getApprovedQuantities() != null && pr.getItems() != null) {
            for (PurchaseRequestItem item : pr.getItems()) {
                Integer approvedQty = request.getApprovedQuantities().get(item.getId());
                if (approvedQty != null) {
                    item.setQuantityApproved(approvedQty);
                } else {
                    item.setQuantityApproved(item.getQuantityRequested());
                }
            }
        }

        PurchaseRequest saved = purchaseRequestRepository.save(pr);
        auditService.record("PurchaseRequest", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Purchase request approved: " + saved.getRequestCode());
        return PurchaseRequestDto.fromEntity(saved);
    }

    @Transactional
    public PurchaseRequestDto rejectPurchaseRequest(Long id, String reason, String username) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with ID: " + id));

        if (pr.getStatus() != PurchaseRequestStatus.PENDING) {
            throw new BadRequestException("Only PENDING purchase requests can be rejected. Current status: " + pr.getStatus());
        }

        pr.setStatus(PurchaseRequestStatus.REJECTED);
        pr.setRejectionReason(reason);

        PurchaseRequest saved = purchaseRequestRepository.save(pr);
        auditService.record("PurchaseRequest", saved.getId().toString(), AuditAction.STATUS_CHANGE, username,
                "Purchase request rejected: " + saved.getRequestCode() + " reason=" + reason);
        return PurchaseRequestDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PurchaseRequestDto getPurchaseRequestById(Long id) {
        return PurchaseRequestDto.fromEntity(purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase request not found with ID: " + id)));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PurchaseRequestDto> searchPurchaseRequests(Long departmentId, Long organizationId,
                                                                      PurchaseRequestStatus status,
                                                                      PurchasePriority priority,
                                                                      Long workOrderId,
                                                                      Instant startDate, Instant endDate,
                                                                      Pageable pageable) {
        Page<PurchaseRequestDto> page = purchaseRequestRepository
                .searchRequests(departmentId, organizationId, status, priority, workOrderId, startDate, endDate, pageable)
                .map(PurchaseRequestDto::fromEntity);
        return PagedResponse.from(page);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private synchronized void adjustStock(Warehouse warehouse, InventoryItem item, int delta) {
        WarehouseStock stock = stockRepository.findByWarehouseIdAndItemId(warehouse.getId(), item.getId())
                .orElseGet(() -> {
                    WarehouseStock s = new WarehouseStock();
                    s.setWarehouse(warehouse);
                    s.setItem(item);
                    s.setQuantity(0);
                    s.setReservedQty(0);
                    s.setCreatedAt(Instant.now());
                    return s;
                });
        if (delta < 0 && (stock.getQuantity() + delta) < 0) {
            throw new BadRequestException(String.format(
                    "Insufficient stock for item %s in warehouse %s. Available: %d, Requested deduction: %d",
                    item.getItemCode(), warehouse.getWarehouseCode(), stock.getQuantity(), -delta));
        }
        stock.setQuantity(stock.getQuantity() + delta);
        stock.setUpdatedAt(Instant.now());
        stockRepository.save(stock);
    }

    private void ensureSufficientStock(Warehouse warehouse, InventoryItem item, int required) {
        WarehouseStock stock = stockRepository.findByWarehouseIdAndItemId(warehouse.getId(), item.getId())
                .orElse(null);
        int available = (stock != null) ? stock.getQuantity() : 0;
        if (available < required) {
            throw new BadRequestException(String.format(
                    "Insufficient stock for item %s in warehouse %s. Available: %d, Required: %d",
                    item.getItemCode(), warehouse.getWarehouseCode(), available, required));
        }
    }

    private synchronized String generateWarehouseCode(Department department) {
        String deptCode = (department.getCode() != null) ? department.getCode().toUpperCase() : "DEPT";
        String prefix = "WH-" + deptCode + "-";
        long count = warehouseRepository.count() + 1;
        return prefix + String.format("%04d", count);
    }

    private synchronized String generateItemCode(ItemCategory category) {
        String prefix = "ITM-" + (category != null ? category.name().substring(0, Math.min(3, category.name().length())) : "GEN") + "-";
        long rand = ThreadLocalRandom.current().nextLong(1000, 9999);
        String code = prefix + rand;
        while (itemRepository.existsByItemCode(code)) {
            rand = ThreadLocalRandom.current().nextLong(1000, 9999);
            code = prefix + rand;
        }
        return code;
    }

    private synchronized String generateSupplierCode() {
        long count = supplierRepository.count() + 1;
        return "SUP-" + String.format("%05d", count);
    }

    private synchronized String generateTransactionCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long rand = ThreadLocalRandom.current().nextLong(10000, 99999);
        return "TXN-" + date + "-" + rand;
    }

    private synchronized String generateRequestCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseRequestRepository.count() + 1;
        return "PR-" + date + "-" + String.format("%04d", count);
    }
}
