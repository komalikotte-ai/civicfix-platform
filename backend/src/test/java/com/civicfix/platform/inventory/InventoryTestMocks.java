package com.civicfix.platform.inventory;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.repository.AuditLogRepository;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.inventory.entity.*;
import com.civicfix.platform.inventory.enums.*;
import com.civicfix.platform.inventory.repository.*;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.organizations.repository.OrganizationRepository;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.repository.EmployeeRepository;
import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.repository.WorkOrderRepository;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public class InventoryTestMocks {

    // =========================================================================
    // Mock AuditService (no-op, using null repo)
    // =========================================================================
    public static class MockAuditService extends AuditService {
        public MockAuditService() { super(null); }
        @Override public void record(String entityName, String entityId, AuditAction action,
                                     String performedBy, String details) { /* no-op */ }
    }

    // =========================================================================
    // Shared JPA boilerplate helper
    // =========================================================================
    private static <E> List<E> toList(Map<Long, E> map) {
        return new ArrayList<>(map.values());
    }

    private static <E, S extends E> List<S> saveAll(Map<Long, E> map, Iterable<S> entities,
                                                      java.util.function.Consumer<S> saver) {
        List<S> res = new ArrayList<>();
        entities.forEach(e -> { saver.accept(e); res.add(e); });
        return res;
    }

    // =========================================================================
    // MockWarehouseRepository
    // =========================================================================
    public static class MockWarehouseRepository implements WarehouseRepository {
        public final Map<Long, Warehouse> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(100);

        @Override public <S extends Warehouse> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<Warehouse> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<Warehouse> findByWarehouseCode(String code) {
            return map.values().stream().filter(w -> code.equalsIgnoreCase(w.getWarehouseCode())).findFirst();
        }
        @Override public boolean existsByWarehouseCode(String code) {
            return map.values().stream().anyMatch(w -> code.equalsIgnoreCase(w.getWarehouseCode()));
        }
        @Override public List<Warehouse> findByDepartmentId(Long deptId) {
            return map.values().stream().filter(w -> w.getDepartment() != null && deptId.equals(w.getDepartment().getId())).toList();
        }
        @Override public List<Warehouse> findByOrganizationId(Long orgId) {
            return map.values().stream().filter(w -> w.getOrganization() != null && orgId.equals(w.getOrganization().getId())).toList();
        }
        @Override public List<Warehouse> findByStatus(WarehouseStatus status) {
            return map.values().stream().filter(w -> w.getStatus() == status).toList();
        }
        @Override public Page<Warehouse> searchWarehouses(String keyword, Long departmentId, Long organizationId,
                                                           WarehouseStatus status, WarehouseType warehouseType, Pageable pageable) {
            List<Warehouse> filtered = map.values().stream()
                    .filter(w -> keyword == null || w.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                            w.getWarehouseCode().toLowerCase().contains(keyword.toLowerCase()))
                    .filter(w -> departmentId == null || (w.getDepartment() != null && departmentId.equals(w.getDepartment().getId())))
                    .filter(w -> organizationId == null || (w.getOrganization() != null && organizationId.equals(w.getOrganization().getId())))
                    .filter(w -> status == null || w.getStatus() == status)
                    .filter(w -> warehouseType == null || w.getWarehouseType() == warehouseType)
                    .toList();
            return new PageImpl<>(filtered, pageable, filtered.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<Warehouse> findAll() { return toList(map); }
        @Override public List<Warehouse> findAll(Sort sort) { return toList(map); }
        @Override public Page<Warehouse> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends Warehouse> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<Warehouse> findAllById(Iterable<Long> ids) {
            List<Warehouse> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(Warehouse e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends Warehouse> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends Warehouse> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends Warehouse> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<Warehouse> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public Warehouse getOne(Long id) { return map.get(id); }
        @Override public Warehouse getById(Long id) { return map.get(id); }
        @Override public Warehouse getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends Warehouse> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends Warehouse> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends Warehouse> List<S> findAll(Example<S> ex, Sort sort) { return Collections.emptyList(); }
        @Override public <S extends Warehouse> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends Warehouse> long count(Example<S> ex) { return 0; }
        @Override public <S extends Warehouse> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends Warehouse, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockInventoryItemRepository
    // =========================================================================
    public static class MockInventoryItemRepository implements InventoryItemRepository {
        public final Map<Long, InventoryItem> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(200);

        @Override public <S extends InventoryItem> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<InventoryItem> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<InventoryItem> findByItemCode(String code) {
            return map.values().stream().filter(i -> code.equalsIgnoreCase(i.getItemCode())).findFirst();
        }
        @Override public boolean existsByItemCode(String code) {
            return map.values().stream().anyMatch(i -> code.equalsIgnoreCase(i.getItemCode()));
        }
        @Override public Page<InventoryItem> searchItems(String keyword, ItemCategory category, Boolean active, Pageable p) {
            List<InventoryItem> filtered = map.values().stream()
                    .filter(i -> keyword == null || i.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                            i.getItemCode().toLowerCase().contains(keyword.toLowerCase()))
                    .filter(i -> category == null || i.getItemCategory() == category)
                    .filter(i -> active == null || i.isActive() == active)
                    .toList();
            return new PageImpl<>(filtered, p, filtered.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<InventoryItem> findAll() { return toList(map); }
        @Override public List<InventoryItem> findAll(Sort sort) { return toList(map); }
        @Override public Page<InventoryItem> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends InventoryItem> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<InventoryItem> findAllById(Iterable<Long> ids) {
            List<InventoryItem> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(InventoryItem e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends InventoryItem> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends InventoryItem> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends InventoryItem> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<InventoryItem> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public InventoryItem getOne(Long id) { return map.get(id); }
        @Override public InventoryItem getById(Long id) { return map.get(id); }
        @Override public InventoryItem getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends InventoryItem> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends InventoryItem> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends InventoryItem> List<S> findAll(Example<S> ex, Sort sort) { return Collections.emptyList(); }
        @Override public <S extends InventoryItem> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends InventoryItem> long count(Example<S> ex) { return 0; }
        @Override public <S extends InventoryItem> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends InventoryItem, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockWarehouseStockRepository
    // =========================================================================
    public static class MockWarehouseStockRepository implements WarehouseStockRepository {
        public final Map<Long, WarehouseStock> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(300);

        private Long setId(WarehouseStock e) {
            try {
                var f = WarehouseStock.class.getDeclaredField("id");
                f.setAccessible(true);
                Long id = seq.incrementAndGet();
                f.set(e, id);
                return id;
            } catch (Exception ex) { throw new RuntimeException(ex); }
        }

        @Override public <S extends WarehouseStock> S save(S e) {
            if (e.getId() == null) setId(e);
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<WarehouseStock> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<WarehouseStock> findByWarehouseIdAndItemId(Long wId, Long iId) {
            return map.values().stream()
                    .filter(s -> s.getWarehouse() != null && wId.equals(s.getWarehouse().getId()) &&
                            s.getItem() != null && iId.equals(s.getItem().getId()))
                    .findFirst();
        }
        @Override public Page<WarehouseStock> findByWarehouseId(Long wId, Pageable p) {
            List<WarehouseStock> list = map.values().stream()
                    .filter(s -> s.getWarehouse() != null && wId.equals(s.getWarehouse().getId())).toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public Page<WarehouseStock> findByItemId(Long iId, Pageable p) {
            List<WarehouseStock> list = map.values().stream()
                    .filter(s -> s.getItem() != null && iId.equals(s.getItem().getId())).toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public Page<WarehouseStock> findLowStockByWarehouseId(Long wId, Pageable p) {
            List<WarehouseStock> list = map.values().stream()
                    .filter(s -> s.getWarehouse() != null && wId.equals(s.getWarehouse().getId()) &&
                            s.getItem() != null && s.getQuantity() <= s.getItem().getReorderLevel())
                    .toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public Page<WarehouseStock> findBelowMinimumStockByWarehouseId(Long wId, Pageable p) {
            List<WarehouseStock> list = map.values().stream()
                    .filter(s -> s.getWarehouse() != null && wId.equals(s.getWarehouse().getId()) &&
                            s.getItem() != null && s.getQuantity() <= s.getItem().getMinimumStock())
                    .toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public List<WarehouseStock> findAllLowStock() {
            return map.values().stream()
                    .filter(s -> s.getItem() != null && s.getQuantity() <= s.getItem().getReorderLevel()).toList();
        }
        @Override public List<WarehouseStock> findAllBelowMinimumStock() {
            return map.values().stream()
                    .filter(s -> s.getItem() != null && s.getQuantity() <= s.getItem().getMinimumStock()).toList();
        }
        @Override public long count() { return map.size(); }
        @Override public List<WarehouseStock> findAll() { return toList(map); }
        @Override public List<WarehouseStock> findAll(Sort sort) { return toList(map); }
        @Override public Page<WarehouseStock> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends WarehouseStock> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<WarehouseStock> findAllById(Iterable<Long> ids) {
            List<WarehouseStock> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(WarehouseStock e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends WarehouseStock> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends WarehouseStock> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends WarehouseStock> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<WarehouseStock> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public WarehouseStock getOne(Long id) { return map.get(id); }
        @Override public WarehouseStock getById(Long id) { return map.get(id); }
        @Override public WarehouseStock getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends WarehouseStock> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends WarehouseStock> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends WarehouseStock> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends WarehouseStock> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends WarehouseStock> long count(Example<S> ex) { return 0; }
        @Override public <S extends WarehouseStock> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends WarehouseStock, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockStockTransactionRepository
    // =========================================================================
    public static class MockStockTransactionRepository implements StockTransactionRepository {
        public final Map<Long, StockTransaction> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(400);

        private void setId(StockTransaction e) {
            try { var f = StockTransaction.class.getDeclaredField("id"); f.setAccessible(true); f.set(e, seq.incrementAndGet()); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }

        @Override public <S extends StockTransaction> S save(S e) {
            if (e.getId() == null) setId(e);
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<StockTransaction> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Page<StockTransaction> findByWarehouseIdOrderByTransactionDateDesc(Long wId, Pageable p) {
            List<StockTransaction> list = map.values().stream()
                    .filter(t -> t.getWarehouse() != null && wId.equals(t.getWarehouse().getId()))
                    .sorted((a, b) -> b.getTransactionDate() != null && a.getTransactionDate() != null ?
                            b.getTransactionDate().compareTo(a.getTransactionDate()) : 0)
                    .toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public Page<StockTransaction> findByItemIdOrderByTransactionDateDesc(Long iId, Pageable p) {
            List<StockTransaction> list = map.values().stream()
                    .filter(t -> t.getItem() != null && iId.equals(t.getItem().getId())).toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public List<StockTransaction> findByWorkOrderId(Long woId) {
            return map.values().stream().filter(t -> t.getWorkOrder() != null && woId.equals(t.getWorkOrder().getId())).toList();
        }
        @Override public Page<StockTransaction> searchTransactions(Long wId, Long iId, TransactionType type,
                                                                    Instant start, Instant end, Pageable p) {
            List<StockTransaction> filtered = map.values().stream()
                    .filter(t -> wId == null || (t.getWarehouse() != null && wId.equals(t.getWarehouse().getId())))
                    .filter(t -> iId == null || (t.getItem() != null && iId.equals(t.getItem().getId())))
                    .filter(t -> type == null || t.getTransactionType() == type)
                    .toList();
            return new PageImpl<>(filtered, p, filtered.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<StockTransaction> findAll() { return toList(map); }
        @Override public List<StockTransaction> findAll(Sort sort) { return toList(map); }
        @Override public Page<StockTransaction> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends StockTransaction> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<StockTransaction> findAllById(Iterable<Long> ids) {
            List<StockTransaction> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(StockTransaction e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends StockTransaction> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends StockTransaction> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends StockTransaction> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<StockTransaction> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public StockTransaction getOne(Long id) { return map.get(id); }
        @Override public StockTransaction getById(Long id) { return map.get(id); }
        @Override public StockTransaction getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends StockTransaction> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends StockTransaction> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends StockTransaction> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends StockTransaction> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends StockTransaction> long count(Example<S> ex) { return 0; }
        @Override public <S extends StockTransaction> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends StockTransaction, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockWorkOrderMaterialUsageRepository
    // =========================================================================
    public static class MockWorkOrderMaterialUsageRepository implements WorkOrderMaterialUsageRepository {
        public final Map<Long, WorkOrderMaterialUsage> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(500);

        private void setId(WorkOrderMaterialUsage e) {
            try { var f = WorkOrderMaterialUsage.class.getDeclaredField("id"); f.setAccessible(true); f.set(e, seq.incrementAndGet()); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }

        @Override public <S extends WorkOrderMaterialUsage> S save(S e) {
            if (e.getId() == null) setId(e);
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<WorkOrderMaterialUsage> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<WorkOrderMaterialUsage> findByWorkOrderIdOrderByRecordedAtDesc(Long woId) {
            return map.values().stream().filter(u -> u.getWorkOrder() != null && woId.equals(u.getWorkOrder().getId())).toList();
        }
        @Override public List<WorkOrderMaterialUsage> findByItemId(Long iId) {
            return map.values().stream().filter(u -> u.getItem() != null && iId.equals(u.getItem().getId())).toList();
        }
        @Override public List<WorkOrderMaterialUsage> findByWarehouseId(Long wId) {
            return map.values().stream().filter(u -> u.getWarehouse() != null && wId.equals(u.getWarehouse().getId())).toList();
        }
        @Override public long count() { return map.size(); }
        @Override public List<WorkOrderMaterialUsage> findAll() { return toList(map); }
        @Override public List<WorkOrderMaterialUsage> findAll(Sort sort) { return toList(map); }
        @Override public Page<WorkOrderMaterialUsage> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends WorkOrderMaterialUsage> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<WorkOrderMaterialUsage> findAllById(Iterable<Long> ids) {
            List<WorkOrderMaterialUsage> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(WorkOrderMaterialUsage e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends WorkOrderMaterialUsage> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends WorkOrderMaterialUsage> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends WorkOrderMaterialUsage> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<WorkOrderMaterialUsage> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public WorkOrderMaterialUsage getOne(Long id) { return map.get(id); }
        @Override public WorkOrderMaterialUsage getById(Long id) { return map.get(id); }
        @Override public WorkOrderMaterialUsage getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends WorkOrderMaterialUsage> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends WorkOrderMaterialUsage> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends WorkOrderMaterialUsage> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends WorkOrderMaterialUsage> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends WorkOrderMaterialUsage> long count(Example<S> ex) { return 0; }
        @Override public <S extends WorkOrderMaterialUsage> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends WorkOrderMaterialUsage, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockSupplierRepository
    // =========================================================================
    public static class MockSupplierRepository implements SupplierRepository {
        public final Map<Long, Supplier> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(600);

        @Override public <S extends Supplier> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<Supplier> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<Supplier> findBySupplierCode(String code) {
            return map.values().stream().filter(s -> code.equalsIgnoreCase(s.getSupplierCode())).findFirst();
        }
        @Override public boolean existsBySupplierCode(String code) {
            return map.values().stream().anyMatch(s -> code.equalsIgnoreCase(s.getSupplierCode()));
        }
        @Override public Page<Supplier> searchSuppliers(String keyword, Boolean active, Pageable p) {
            List<Supplier> filtered = map.values().stream()
                    .filter(s -> keyword == null || s.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                            s.getSupplierCode().toLowerCase().contains(keyword.toLowerCase()))
                    .filter(s -> active == null || s.isActive() == active)
                    .toList();
            return new PageImpl<>(filtered, p, filtered.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<Supplier> findAll() { return toList(map); }
        @Override public List<Supplier> findAll(Sort sort) { return toList(map); }
        @Override public Page<Supplier> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends Supplier> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<Supplier> findAllById(Iterable<Long> ids) {
            List<Supplier> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(Supplier e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends Supplier> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends Supplier> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends Supplier> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<Supplier> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public Supplier getOne(Long id) { return map.get(id); }
        @Override public Supplier getById(Long id) { return map.get(id); }
        @Override public Supplier getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends Supplier> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends Supplier> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends Supplier> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends Supplier> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends Supplier> long count(Example<S> ex) { return 0; }
        @Override public <S extends Supplier> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends Supplier, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockSupplierItemRepository
    // =========================================================================
    public static class MockSupplierItemRepository implements SupplierItemRepository {
        public final Map<Long, SupplierItem> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(700);

        private void setId(SupplierItem e) {
            try { var f = SupplierItem.class.getDeclaredField("id"); f.setAccessible(true); f.set(e, seq.incrementAndGet()); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }

        @Override public <S extends SupplierItem> S save(S e) {
            if (e.getId() == null) setId(e);
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<SupplierItem> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<SupplierItem> findBySupplierIdAndItemId(Long sId, Long iId) {
            return map.values().stream()
                    .filter(si -> si.getSupplier() != null && sId.equals(si.getSupplier().getId()) &&
                            si.getItem() != null && iId.equals(si.getItem().getId()))
                    .findFirst();
        }
        @Override public List<SupplierItem> findBySupplierId(Long sId) {
            return map.values().stream().filter(si -> si.getSupplier() != null && sId.equals(si.getSupplier().getId())).toList();
        }
        @Override public List<SupplierItem> findByItemId(Long iId) {
            return map.values().stream().filter(si -> si.getItem() != null && iId.equals(si.getItem().getId())).toList();
        }
        @Override public List<SupplierItem> findByItemIdAndPreferredTrue(Long iId) {
            return map.values().stream()
                    .filter(si -> si.getItem() != null && iId.equals(si.getItem().getId()) && si.isPreferred()).toList();
        }
        @Override public long count() { return map.size(); }
        @Override public List<SupplierItem> findAll() { return toList(map); }
        @Override public List<SupplierItem> findAll(Sort sort) { return toList(map); }
        @Override public Page<SupplierItem> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends SupplierItem> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<SupplierItem> findAllById(Iterable<Long> ids) {
            List<SupplierItem> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(SupplierItem e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends SupplierItem> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends SupplierItem> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends SupplierItem> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<SupplierItem> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public SupplierItem getOne(Long id) { return map.get(id); }
        @Override public SupplierItem getById(Long id) { return map.get(id); }
        @Override public SupplierItem getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends SupplierItem> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends SupplierItem> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends SupplierItem> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends SupplierItem> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends SupplierItem> long count(Example<S> ex) { return 0; }
        @Override public <S extends SupplierItem> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends SupplierItem, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockPurchaseRequestRepository
    // =========================================================================
    public static class MockPurchaseRequestRepository implements PurchaseRequestRepository {
        public final Map<Long, PurchaseRequest> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(800);

        @Override public <S extends PurchaseRequest> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<PurchaseRequest> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<PurchaseRequest> findByRequestCode(String code) {
            return map.values().stream().filter(pr -> code.equalsIgnoreCase(pr.getRequestCode())).findFirst();
        }
        @Override public Page<PurchaseRequest> searchRequests(Long deptId, Long orgId, PurchaseRequestStatus status,
                                                               PurchasePriority priority, Long woId,
                                                               Instant start, Instant end, Pageable p) {
            List<PurchaseRequest> filtered = map.values().stream()
                    .filter(pr -> deptId == null || (pr.getDepartment() != null && deptId.equals(pr.getDepartment().getId())))
                    .filter(pr -> status == null || pr.getStatus() == status)
                    .filter(pr -> priority == null || pr.getPriority() == priority)
                    .toList();
            return new PageImpl<>(filtered, p, filtered.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<PurchaseRequest> findAll() { return toList(map); }
        @Override public List<PurchaseRequest> findAll(Sort sort) { return toList(map); }
        @Override public Page<PurchaseRequest> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends PurchaseRequest> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<PurchaseRequest> findAllById(Iterable<Long> ids) {
            List<PurchaseRequest> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(PurchaseRequest e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends PurchaseRequest> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends PurchaseRequest> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends PurchaseRequest> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<PurchaseRequest> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public PurchaseRequest getOne(Long id) { return map.get(id); }
        @Override public PurchaseRequest getById(Long id) { return map.get(id); }
        @Override public PurchaseRequest getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends PurchaseRequest> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends PurchaseRequest> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends PurchaseRequest> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends PurchaseRequest> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends PurchaseRequest> long count(Example<S> ex) { return 0; }
        @Override public <S extends PurchaseRequest> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends PurchaseRequest, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockPurchaseRequestItemRepository (simple)
    // =========================================================================
    public static class MockPurchaseRequestItemRepository implements PurchaseRequestItemRepository {
        public final Map<Long, PurchaseRequestItem> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(900);

        private void setId(PurchaseRequestItem e) {
            try { var f = PurchaseRequestItem.class.getDeclaredField("id"); f.setAccessible(true); f.set(e, seq.incrementAndGet()); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }

        @Override public <S extends PurchaseRequestItem> S save(S e) {
            if (e.getId() == null) setId(e);
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<PurchaseRequestItem> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<PurchaseRequestItem> findByPurchaseRequestId(Long prId) {
            return map.values().stream()
                    .filter(pri -> pri.getPurchaseRequest() != null && prId.equals(pri.getPurchaseRequest().getId())).toList();
        }
        @Override public List<PurchaseRequestItem> findByItemId(Long iId) {
            return map.values().stream().filter(pri -> pri.getItem() != null && iId.equals(pri.getItem().getId())).toList();
        }
        @Override public long count() { return map.size(); }
        @Override public List<PurchaseRequestItem> findAll() { return toList(map); }
        @Override public List<PurchaseRequestItem> findAll(Sort sort) { return toList(map); }
        @Override public Page<PurchaseRequestItem> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends PurchaseRequestItem> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<PurchaseRequestItem> findAllById(Iterable<Long> ids) {
            List<PurchaseRequestItem> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(PurchaseRequestItem e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends PurchaseRequestItem> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends PurchaseRequestItem> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends PurchaseRequestItem> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<PurchaseRequestItem> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public PurchaseRequestItem getOne(Long id) { return map.get(id); }
        @Override public PurchaseRequestItem getById(Long id) { return map.get(id); }
        @Override public PurchaseRequestItem getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends PurchaseRequestItem> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends PurchaseRequestItem> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends PurchaseRequestItem> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends PurchaseRequestItem> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends PurchaseRequestItem> long count(Example<S> ex) { return 0; }
        @Override public <S extends PurchaseRequestItem> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends PurchaseRequestItem, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }
}
