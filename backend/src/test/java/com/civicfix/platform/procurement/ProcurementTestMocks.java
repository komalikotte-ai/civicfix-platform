package com.civicfix.platform.procurement;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.inventory.entity.*;
import com.civicfix.platform.inventory.enums.*;
import com.civicfix.platform.inventory.repository.*;
import com.civicfix.platform.procurement.entity.*;
import com.civicfix.platform.procurement.enums.*;
import com.civicfix.platform.procurement.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * In-memory mock repositories for procurement service unit tests.
 */
public class ProcurementTestMocks {

    // =========================================================================
    // Mock AuditService
    // =========================================================================
    public static class MockAuditService extends AuditService {
        public MockAuditService() { super(null); }
        @Override public void record(String e, String id, AuditAction a, String by, String d) {}
    }

    // =========================================================================
    // Shared helpers
    // =========================================================================
    private static <E> List<E> toList(Map<Long, E> map) { return new ArrayList<>(map.values()); }

    // =========================================================================
    // MockVendorContractRepository
    // =========================================================================
    public static class MockVendorContractRepository implements VendorContractRepository {
        public final Map<Long, VendorContract> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1000);

        @Override public <S extends VendorContract> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<VendorContract> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<VendorContract> findByContractNumber(String n) {
            return map.values().stream().filter(c -> n.equals(c.getContractNumber())).findFirst();
        }
        @Override public boolean existsByContractNumber(String n) {
            return map.values().stream().anyMatch(c -> n.equals(c.getContractNumber()));
        }
        @Override public List<VendorContract> findBySupplierId(Long sId) {
            return map.values().stream().filter(c -> c.getSupplier() != null && sId.equals(c.getSupplier().getId())).toList();
        }
        @Override public List<VendorContract> findByDepartmentId(Long dId) {
            return map.values().stream().filter(c -> c.getDepartment() != null && dId.equals(c.getDepartment().getId())).toList();
        }
        @Override public Page<VendorContract> searchContracts(Long sId, Long dId, ContractStatus status, Pageable p) {
            List<VendorContract> filtered = map.values().stream()
                    .filter(c -> sId == null || (c.getSupplier() != null && sId.equals(c.getSupplier().getId())))
                    .filter(c -> dId == null || (c.getDepartment() != null && dId.equals(c.getDepartment().getId())))
                    .filter(c -> status == null || c.getStatus() == status).toList();
            return new PageImpl<>(filtered, p, filtered.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<VendorContract> findAll() { return toList(map); }
        @Override public List<VendorContract> findAll(Sort s) { return toList(map); }
        @Override public Page<VendorContract> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends VendorContract> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<VendorContract> findAllById(Iterable<Long> ids) {
            List<VendorContract> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(VendorContract e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends VendorContract> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends VendorContract> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends VendorContract> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<VendorContract> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public VendorContract getOne(Long id) { return map.get(id); }
        @Override public VendorContract getById(Long id) { return map.get(id); }
        @Override public VendorContract getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends VendorContract> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends VendorContract> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends VendorContract> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends VendorContract> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends VendorContract> long count(Example<S> ex) { return 0; }
        @Override public <S extends VendorContract> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends VendorContract, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockQuotationRepository
    // =========================================================================
    public static class MockQuotationRepository implements QuotationRepository {
        public final Map<Long, Quotation> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(2000);

        @Override public <S extends Quotation> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<Quotation> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<Quotation> findByQuotationNumber(String n) {
            return map.values().stream().filter(q -> n.equals(q.getQuotationNumber())).findFirst();
        }
        @Override public boolean existsByQuotationNumber(String n) {
            return map.values().stream().anyMatch(q -> n.equals(q.getQuotationNumber()));
        }
        @Override public List<Quotation> findByPurchaseRequestId(Long prId) {
            return map.values().stream().filter(q -> q.getPurchaseRequest() != null && prId.equals(q.getPurchaseRequest().getId())).toList();
        }
        @Override public List<Quotation> findBySupplierId(Long sId) {
            return map.values().stream().filter(q -> q.getSupplier() != null && sId.equals(q.getSupplier().getId())).toList();
        }
        @Override public Page<Quotation> searchQuotations(Long sId, Long dId, Long prId, QuotationStatus status, Pageable p) {
            List<Quotation> filtered = map.values().stream()
                    .filter(q -> sId == null || (q.getSupplier() != null && sId.equals(q.getSupplier().getId())))
                    .filter(q -> dId == null || (q.getDepartment() != null && dId.equals(q.getDepartment().getId())))
                    .filter(q -> prId == null || (q.getPurchaseRequest() != null && prId.equals(q.getPurchaseRequest().getId())))
                    .filter(q -> status == null || q.getStatus() == status).toList();
            return new PageImpl<>(filtered, p, filtered.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<Quotation> findAll() { return toList(map); }
        @Override public List<Quotation> findAll(Sort s) { return toList(map); }
        @Override public Page<Quotation> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends Quotation> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<Quotation> findAllById(Iterable<Long> ids) {
            List<Quotation> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(Quotation e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends Quotation> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends Quotation> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends Quotation> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<Quotation> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public Quotation getOne(Long id) { return map.get(id); }
        @Override public Quotation getById(Long id) { return map.get(id); }
        @Override public Quotation getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends Quotation> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends Quotation> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends Quotation> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends Quotation> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends Quotation> long count(Example<S> ex) { return 0; }
        @Override public <S extends Quotation> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends Quotation, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockQuotationItemRepository
    // =========================================================================
    public static class MockQuotationItemRepository implements QuotationItemRepository {
        public final Map<Long, QuotationItem> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(3000);

        private void setId(QuotationItem e) {
            try { var f = QuotationItem.class.getDeclaredField("id"); f.setAccessible(true); f.set(e, seq.incrementAndGet()); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }

        @Override public <S extends QuotationItem> S save(S e) {
            if (e.getId() == null) setId(e);
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<QuotationItem> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<QuotationItem> findByQuotationId(Long qId) {
            return map.values().stream().filter(i -> i.getQuotation() != null && qId.equals(i.getQuotation().getId())).toList();
        }
        @Override public long count() { return map.size(); }
        @Override public List<QuotationItem> findAll() { return toList(map); }
        @Override public List<QuotationItem> findAll(Sort s) { return toList(map); }
        @Override public Page<QuotationItem> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends QuotationItem> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<QuotationItem> findAllById(Iterable<Long> ids) {
            List<QuotationItem> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(QuotationItem e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends QuotationItem> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends QuotationItem> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends QuotationItem> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<QuotationItem> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public QuotationItem getOne(Long id) { return map.get(id); }
        @Override public QuotationItem getById(Long id) { return map.get(id); }
        @Override public QuotationItem getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends QuotationItem> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends QuotationItem> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends QuotationItem> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends QuotationItem> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends QuotationItem> long count(Example<S> ex) { return 0; }
        @Override public <S extends QuotationItem> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends QuotationItem, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockPurchaseOrderRepository
    // =========================================================================
    public static class MockPurchaseOrderRepository implements PurchaseOrderRepository {
        public final Map<Long, PurchaseOrder> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(4000);

        @Override public <S extends PurchaseOrder> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<PurchaseOrder> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<PurchaseOrder> findByOrderNumber(String n) {
            return map.values().stream().filter(p -> n.equals(p.getOrderNumber())).findFirst();
        }
        @Override public boolean existsByOrderNumber(String n) {
            return map.values().stream().anyMatch(p -> n.equals(p.getOrderNumber()));
        }
        @Override public List<PurchaseOrder> findBySupplierId(Long sId) {
            return map.values().stream().filter(p -> p.getSupplier() != null && sId.equals(p.getSupplier().getId())).toList();
        }
        @Override public List<PurchaseOrder> findByWarehouseId(Long wId) {
            return map.values().stream().filter(p -> p.getWarehouse() != null && wId.equals(p.getWarehouse().getId())).toList();
        }
        @Override public Page<PurchaseOrder> searchPurchaseOrders(Long sId, Long dId, Long wId, PurchaseOrderStatus status, Pageable p) {
            List<PurchaseOrder> filtered = map.values().stream()
                    .filter(po -> sId == null || (po.getSupplier() != null && sId.equals(po.getSupplier().getId())))
                    .filter(po -> dId == null || (po.getDepartment() != null && dId.equals(po.getDepartment().getId())))
                    .filter(po -> wId == null || (po.getWarehouse() != null && wId.equals(po.getWarehouse().getId())))
                    .filter(po -> status == null || po.getStatus() == status).toList();
            return new PageImpl<>(filtered, p, filtered.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<PurchaseOrder> findAll() { return toList(map); }
        @Override public List<PurchaseOrder> findAll(Sort s) { return toList(map); }
        @Override public Page<PurchaseOrder> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends PurchaseOrder> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<PurchaseOrder> findAllById(Iterable<Long> ids) {
            List<PurchaseOrder> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(PurchaseOrder e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends PurchaseOrder> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends PurchaseOrder> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends PurchaseOrder> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<PurchaseOrder> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public PurchaseOrder getOne(Long id) { return map.get(id); }
        @Override public PurchaseOrder getById(Long id) { return map.get(id); }
        @Override public PurchaseOrder getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends PurchaseOrder> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends PurchaseOrder> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends PurchaseOrder> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends PurchaseOrder> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends PurchaseOrder> long count(Example<S> ex) { return 0; }
        @Override public <S extends PurchaseOrder> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends PurchaseOrder, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockPurchaseOrderItemRepository
    // =========================================================================
    public static class MockPurchaseOrderItemRepository implements PurchaseOrderItemRepository {
        public final Map<Long, PurchaseOrderItem> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(5000);

        private void setId(PurchaseOrderItem e) {
            try { var f = PurchaseOrderItem.class.getDeclaredField("id"); f.setAccessible(true); f.set(e, seq.incrementAndGet()); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }

        @Override public <S extends PurchaseOrderItem> S save(S e) {
            if (e.getId() == null) setId(e);
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<PurchaseOrderItem> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<PurchaseOrderItem> findByPurchaseOrderId(Long poId) {
            return map.values().stream().filter(i -> i.getPurchaseOrder() != null && poId.equals(i.getPurchaseOrder().getId())).toList();
        }
        @Override public long count() { return map.size(); }
        @Override public List<PurchaseOrderItem> findAll() { return toList(map); }
        @Override public List<PurchaseOrderItem> findAll(Sort s) { return toList(map); }
        @Override public Page<PurchaseOrderItem> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends PurchaseOrderItem> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<PurchaseOrderItem> findAllById(Iterable<Long> ids) {
            List<PurchaseOrderItem> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(PurchaseOrderItem e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends PurchaseOrderItem> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends PurchaseOrderItem> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends PurchaseOrderItem> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<PurchaseOrderItem> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public PurchaseOrderItem getOne(Long id) { return map.get(id); }
        @Override public PurchaseOrderItem getById(Long id) { return map.get(id); }
        @Override public PurchaseOrderItem getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends PurchaseOrderItem> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends PurchaseOrderItem> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends PurchaseOrderItem> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends PurchaseOrderItem> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends PurchaseOrderItem> long count(Example<S> ex) { return 0; }
        @Override public <S extends PurchaseOrderItem> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends PurchaseOrderItem, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockGoodsReceiptDeliveryRepository
    // =========================================================================
    public static class MockGoodsReceiptDeliveryRepository implements GoodsReceiptDeliveryRepository {
        public final Map<Long, GoodsReceiptDelivery> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(6000);

        @Override public <S extends GoodsReceiptDelivery> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<GoodsReceiptDelivery> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<GoodsReceiptDelivery> findByDeliveryNumber(String n) {
            return map.values().stream().filter(d -> n.equals(d.getDeliveryNumber())).findFirst();
        }
        @Override public boolean existsByDeliveryNumber(String n) {
            return map.values().stream().anyMatch(d -> n.equals(d.getDeliveryNumber()));
        }
        @Override public List<GoodsReceiptDelivery> findByPurchaseOrderId(Long poId) {
            return map.values().stream().filter(d -> d.getPurchaseOrder() != null && poId.equals(d.getPurchaseOrder().getId())).toList();
        }
        @Override public Page<GoodsReceiptDelivery> findByWarehouseIdOrderByReceivedAtDesc(Long wId, Pageable p) {
            List<GoodsReceiptDelivery> list = map.values().stream()
                    .filter(d -> d.getWarehouse() != null && wId.equals(d.getWarehouse().getId())).toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<GoodsReceiptDelivery> findAll() { return toList(map); }
        @Override public List<GoodsReceiptDelivery> findAll(Sort s) { return toList(map); }
        @Override public Page<GoodsReceiptDelivery> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends GoodsReceiptDelivery> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<GoodsReceiptDelivery> findAllById(Iterable<Long> ids) {
            List<GoodsReceiptDelivery> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(GoodsReceiptDelivery e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends GoodsReceiptDelivery> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends GoodsReceiptDelivery> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends GoodsReceiptDelivery> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<GoodsReceiptDelivery> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public GoodsReceiptDelivery getOne(Long id) { return map.get(id); }
        @Override public GoodsReceiptDelivery getById(Long id) { return map.get(id); }
        @Override public GoodsReceiptDelivery getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends GoodsReceiptDelivery> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends GoodsReceiptDelivery> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends GoodsReceiptDelivery> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends GoodsReceiptDelivery> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends GoodsReceiptDelivery> long count(Example<S> ex) { return 0; }
        @Override public <S extends GoodsReceiptDelivery> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends GoodsReceiptDelivery, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockGoodsReceiptDeliveryItemRepository
    // =========================================================================
    public static class MockGoodsReceiptDeliveryItemRepository implements GoodsReceiptDeliveryItemRepository {
        public final Map<Long, GoodsReceiptDeliveryItem> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(7000);

        private void setId(GoodsReceiptDeliveryItem e) {
            try { var f = GoodsReceiptDeliveryItem.class.getDeclaredField("id"); f.setAccessible(true); f.set(e, seq.incrementAndGet()); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }

        @Override public <S extends GoodsReceiptDeliveryItem> S save(S e) {
            if (e.getId() == null) setId(e);
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<GoodsReceiptDeliveryItem> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<GoodsReceiptDeliveryItem> findByDeliveryId(Long dId) {
            return map.values().stream().filter(i -> i.getDelivery() != null && dId.equals(i.getDelivery().getId())).toList();
        }
        @Override public long count() { return map.size(); }
        @Override public List<GoodsReceiptDeliveryItem> findAll() { return toList(map); }
        @Override public List<GoodsReceiptDeliveryItem> findAll(Sort s) { return toList(map); }
        @Override public Page<GoodsReceiptDeliveryItem> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends GoodsReceiptDeliveryItem> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<GoodsReceiptDeliveryItem> findAllById(Iterable<Long> ids) {
            List<GoodsReceiptDeliveryItem> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(GoodsReceiptDeliveryItem e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends GoodsReceiptDeliveryItem> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends GoodsReceiptDeliveryItem> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends GoodsReceiptDeliveryItem> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<GoodsReceiptDeliveryItem> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public GoodsReceiptDeliveryItem getOne(Long id) { return map.get(id); }
        @Override public GoodsReceiptDeliveryItem getById(Long id) { return map.get(id); }
        @Override public GoodsReceiptDeliveryItem getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends GoodsReceiptDeliveryItem> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends GoodsReceiptDeliveryItem> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends GoodsReceiptDeliveryItem> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends GoodsReceiptDeliveryItem> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends GoodsReceiptDeliveryItem> long count(Example<S> ex) { return 0; }
        @Override public <S extends GoodsReceiptDeliveryItem> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends GoodsReceiptDeliveryItem, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockVendorEvaluationRepository
    // =========================================================================
    public static class MockVendorEvaluationRepository implements VendorEvaluationRepository {
        public final Map<Long, VendorEvaluation> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(8000);

        private void setId(VendorEvaluation e) {
            try { var f = VendorEvaluation.class.getDeclaredField("id"); f.setAccessible(true); f.set(e, seq.incrementAndGet()); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }

        @Override public <S extends VendorEvaluation> S save(S e) {
            if (e.getId() == null) setId(e);
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<VendorEvaluation> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<VendorEvaluation> findBySupplierIdOrderByEvaluatedAtDesc(Long sId) {
            return map.values().stream()
                    .filter(e -> e.getSupplier() != null && sId.equals(e.getSupplier().getId()))
                    .sorted((a, b) -> b.getEvaluatedAt() != null && a.getEvaluatedAt() != null ?
                            b.getEvaluatedAt().compareTo(a.getEvaluatedAt()) : 0)
                    .toList();
        }
        @Override public Page<VendorEvaluation> findBySupplierId(Long sId, Pageable p) {
            List<VendorEvaluation> list = map.values().stream()
                    .filter(e -> e.getSupplier() != null && sId.equals(e.getSupplier().getId())).toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<VendorEvaluation> findAll() { return toList(map); }
        @Override public List<VendorEvaluation> findAll(Sort s) { return toList(map); }
        @Override public Page<VendorEvaluation> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends VendorEvaluation> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<VendorEvaluation> findAllById(Iterable<Long> ids) {
            List<VendorEvaluation> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(VendorEvaluation e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends VendorEvaluation> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends VendorEvaluation> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends VendorEvaluation> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<VendorEvaluation> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public VendorEvaluation getOne(Long id) { return map.get(id); }
        @Override public VendorEvaluation getById(Long id) { return map.get(id); }
        @Override public VendorEvaluation getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends VendorEvaluation> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends VendorEvaluation> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends VendorEvaluation> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends VendorEvaluation> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends VendorEvaluation> long count(Example<S> ex) { return 0; }
        @Override public <S extends VendorEvaluation> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends VendorEvaluation, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }
}
