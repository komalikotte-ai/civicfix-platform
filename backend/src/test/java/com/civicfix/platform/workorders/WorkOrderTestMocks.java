package com.civicfix.platform.workorders;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.workorders.entity.*;
import com.civicfix.platform.workorders.enums.DependencyType;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import com.civicfix.platform.workorders.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class WorkOrderTestMocks {

    public static class MockWorkOrderRepository implements WorkOrderRepository {
        public final Map<Long, WorkOrder> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(2000);

        @Override public <S extends WorkOrder> S save(S entity) {
            if (entity.getId() == null) entity.setId(seq.incrementAndGet());
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public Optional<WorkOrder> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public Optional<WorkOrder> findByWorkOrderNumber(String num) {
            return map.values().stream().filter(w -> num.equalsIgnoreCase(w.getWorkOrderNumber())).findFirst();
        }
        @Override public boolean existsByWorkOrderNumber(String num) {
            return map.values().stream().anyMatch(w -> num.equalsIgnoreCase(w.getWorkOrderNumber()));
        }
        @Override public List<WorkOrder> findByComplaintId(Long complaintId) {
            return map.values().stream().filter(w -> w.getComplaint() != null && complaintId.equals(w.getComplaint().getId())).toList();
        }
        @Override public Optional<WorkOrder> findFirstByComplaintIdAndStatusNot(Long complaintId, WorkOrderStatus status) {
            return map.values().stream()
                    .filter(w -> w.getComplaint() != null && complaintId.equals(w.getComplaint().getId()) && w.getStatus() != status)
                    .findFirst();
        }
        @Override public boolean existsByComplaintIdAndStatusNot(Long complaintId, WorkOrderStatus status) {
            return map.values().stream()
                    .anyMatch(w -> w.getComplaint() != null && complaintId.equals(w.getComplaint().getId()) && w.getStatus() != status);
        }
        @Override public Page<WorkOrder> findByDepartmentId(Long deptId, Pageable p) {
            List<WorkOrder> list = map.values().stream().filter(w -> w.getDepartment() != null && deptId.equals(w.getDepartment().getId())).toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public Page<WorkOrder> findByAssignedWorkerId(Long workerId, Pageable p) {
            List<WorkOrder> list = map.values().stream().filter(w -> w.getAssignedWorker() != null && workerId.equals(w.getAssignedWorker().getId())).toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public Page<WorkOrder> findByAssignedSupervisorId(Long supervisorId, Pageable p) {
            List<WorkOrder> list = map.values().stream().filter(w -> w.getAssignedSupervisor() != null && supervisorId.equals(w.getAssignedSupervisor().getId())).toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public Page<WorkOrder> findByAssetId(Long assetId, Pageable p) {
            List<WorkOrder> list = map.values().stream().filter(w -> w.getAsset() != null && assetId.equals(w.getAsset().getId())).toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public List<WorkOrder> findByAssetId(Long assetId) {
            return map.values().stream().filter(w -> w.getAsset() != null && assetId.equals(w.getAsset().getId())).toList();
        }

        @Override public Page<WorkOrder> searchWorkOrders(String workOrderNumber, Long complaintId, String complaintTrackingNumber,
                                                          Long departmentId, Long wardId, Long assignedWorkerId, Long assignedSupervisorId,
                                                          WorkOrderStatus status, WorkOrderPriority priority, Instant startDate,
                                                          Instant endDate, String keyword, Pageable pageable) {
            List<WorkOrder> filtered = map.values().stream()
                    .filter(w -> workOrderNumber == null || (w.getWorkOrderNumber() != null && w.getWorkOrderNumber().toLowerCase().contains(workOrderNumber.toLowerCase())))
                    .filter(w -> complaintId == null || (w.getComplaint() != null && complaintId.equals(w.getComplaint().getId())))
                    .filter(w -> complaintTrackingNumber == null || (w.getComplaint() != null && w.getComplaint().getTrackingNumber() != null && w.getComplaint().getTrackingNumber().toLowerCase().contains(complaintTrackingNumber.toLowerCase())))
                    .filter(w -> departmentId == null || (w.getDepartment() != null && departmentId.equals(w.getDepartment().getId())))
                    .filter(w -> wardId == null || (w.getWard() != null && wardId.equals(w.getWard().getId())))
                    .filter(w -> assignedWorkerId == null || (w.getAssignedWorker() != null && assignedWorkerId.equals(w.getAssignedWorker().getId())))
                    .filter(w -> assignedSupervisorId == null || (w.getAssignedSupervisor() != null && assignedSupervisorId.equals(w.getAssignedSupervisor().getId())))
                    .filter(w -> status == null || status == w.getStatus())
                    .filter(w -> priority == null || priority == w.getPriority())
                    .filter(w -> startDate == null || (w.getCreatedAt() != null && !w.getCreatedAt().isBefore(startDate)))
                    .filter(w -> endDate == null || (w.getCreatedAt() != null && !w.getCreatedAt().isAfter(endDate)))
                    .filter(w -> keyword == null || (w.getTitle() != null && w.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                            || (w.getDescription() != null && w.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                            || (w.getWorkOrderNumber() != null && w.getWorkOrderNumber().toLowerCase().contains(keyword.toLowerCase())))
                    .toList();
            return new PageImpl<>(filtered, pageable, filtered.size());
        }

        @Override public void flush() {}
        @Override public <S extends WorkOrder> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends WorkOrder> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<WorkOrder> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public WorkOrder getOne(Long aLong) { return null; }
        @Override public WorkOrder getById(Long aLong) { return null; }
        @Override public WorkOrder getReferenceById(Long aLong) { return null; }
        @Override public <S extends WorkOrder> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends WorkOrder> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends WorkOrder> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends WorkOrder> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends WorkOrder> long count(Example<S> example) { return 0; }
        @Override public <S extends WorkOrder> boolean exists(Example<S> example) { return false; }
        @Override public <S extends WorkOrder, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends WorkOrder> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public boolean existsById(Long aLong) { return map.containsKey(aLong); }
        @Override public List<WorkOrder> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<WorkOrder> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) { map.remove(aLong); }
        @Override public void delete(WorkOrder entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends WorkOrder> entities) {}
        @Override public void deleteAll() { map.clear(); }
        @Override public List<WorkOrder> findAll(Sort sort) { return List.of(); }
        @Override public Page<WorkOrder> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(map.values()), pageable, map.size()); }
    }

    public static class MockWorkOrderDependencyRepository implements WorkOrderDependencyRepository {
        private final Map<Long, WorkOrderDependency> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(3000);

        @Override public <S extends WorkOrderDependency> S save(S entity) {
            if (entity.getId() == null) {
                try {
                    java.lang.reflect.Field field = WorkOrderDependency.class.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(entity, seq.incrementAndGet());
                } catch (Exception ignored) {}
            }
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public List<WorkOrderDependency> findByWorkOrderId(Long workOrderId) {
            return map.values().stream().filter(d -> d.getWorkOrder() != null && workOrderId.equals(d.getWorkOrder().getId())).toList();
        }

        @Override public List<WorkOrderDependency> findByDependsOnWorkOrderId(Long dependsOnWorkOrderId) {
            return map.values().stream().filter(d -> d.getDependsOnWorkOrder() != null && dependsOnWorkOrderId.equals(d.getDependsOnWorkOrder().getId())).toList();
        }

        @Override public Optional<WorkOrderDependency> findByWorkOrderIdAndDependsOnWorkOrderId(Long workOrderId, Long dependsOnWorkOrderId) {
            return map.values().stream()
                    .filter(d -> d.getWorkOrder() != null && workOrderId.equals(d.getWorkOrder().getId())
                            && d.getDependsOnWorkOrder() != null && dependsOnWorkOrderId.equals(d.getDependsOnWorkOrder().getId()))
                    .findFirst();
        }

        @Override public boolean existsByWorkOrderIdAndDependsOnWorkOrderId(Long workOrderId, Long dependsOnWorkOrderId) {
            return findByWorkOrderIdAndDependsOnWorkOrderId(workOrderId, dependsOnWorkOrderId).isPresent();
        }

        @Override public void deleteByWorkOrderIdAndDependsOnWorkOrderId(Long workOrderId, Long dependsOnWorkOrderId) {
            findByWorkOrderIdAndDependsOnWorkOrderId(workOrderId, dependsOnWorkOrderId).ifPresent(d -> map.remove(d.getId()));
        }

        @Override public void flush() {}
        @Override public <S extends WorkOrderDependency> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends WorkOrderDependency> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<WorkOrderDependency> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public WorkOrderDependency getOne(Long aLong) { return null; }
        @Override public WorkOrderDependency getById(Long aLong) { return null; }
        @Override public WorkOrderDependency getReferenceById(Long aLong) { return null; }
        @Override public <S extends WorkOrderDependency> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends WorkOrderDependency> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends WorkOrderDependency> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends WorkOrderDependency> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends WorkOrderDependency> long count(Example<S> example) { return 0; }
        @Override public <S extends WorkOrderDependency> boolean exists(Example<S> example) { return false; }
        @Override public <S extends WorkOrderDependency, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends WorkOrderDependency> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<WorkOrderDependency> findById(Long aLong) { return Optional.ofNullable(map.get(aLong)); }
        @Override public boolean existsById(Long aLong) { return map.containsKey(aLong); }
        @Override public List<WorkOrderDependency> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<WorkOrderDependency> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) { map.remove(aLong); }
        @Override public void delete(WorkOrderDependency entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends WorkOrderDependency> entities) {}
        @Override public void deleteAll() { map.clear(); }
        @Override public List<WorkOrderDependency> findAll(Sort sort) { return List.of(); }
        @Override public Page<WorkOrderDependency> findAll(Pageable pageable) { return Page.empty(); }
    }

    public static class MockWorkOrderStatusHistoryRepository implements WorkOrderStatusHistoryRepository {
        private final Map<Long, WorkOrderStatusHistory> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(4000);

        @Override public <S extends WorkOrderStatusHistory> S save(S entity) {
            if (entity.getId() == null) {
                try {
                    java.lang.reflect.Field field = WorkOrderStatusHistory.class.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(entity, seq.incrementAndGet());
                } catch (Exception ignored) {}
            }
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public List<WorkOrderStatusHistory> findByWorkOrderIdOrderByCreatedAtAsc(Long workOrderId) {
            return map.values().stream()
                    .filter(h -> h.getWorkOrder() != null && workOrderId.equals(h.getWorkOrder().getId()))
                    .sorted(Comparator.comparing(WorkOrderStatusHistory::getCreatedAt))
                    .toList();
        }

        @Override public void flush() {}
        @Override public <S extends WorkOrderStatusHistory> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends WorkOrderStatusHistory> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<WorkOrderStatusHistory> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public WorkOrderStatusHistory getOne(Long aLong) { return null; }
        @Override public WorkOrderStatusHistory getById(Long aLong) { return null; }
        @Override public WorkOrderStatusHistory getReferenceById(Long aLong) { return null; }
        @Override public <S extends WorkOrderStatusHistory> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends WorkOrderStatusHistory> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends WorkOrderStatusHistory> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends WorkOrderStatusHistory> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends WorkOrderStatusHistory> long count(Example<S> example) { return 0; }
        @Override public <S extends WorkOrderStatusHistory> boolean exists(Example<S> example) { return false; }
        @Override public <S extends WorkOrderStatusHistory, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends WorkOrderStatusHistory> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<WorkOrderStatusHistory> findById(Long aLong) { return Optional.ofNullable(map.get(aLong)); }
        @Override public boolean existsById(Long aLong) { return map.containsKey(aLong); }
        @Override public List<WorkOrderStatusHistory> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<WorkOrderStatusHistory> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) { map.remove(aLong); }
        @Override public void delete(WorkOrderStatusHistory entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends WorkOrderStatusHistory> entities) {}
        @Override public void deleteAll() { map.clear(); }
        @Override public List<WorkOrderStatusHistory> findAll(Sort sort) { return List.of(); }
        @Override public Page<WorkOrderStatusHistory> findAll(Pageable pageable) { return Page.empty(); }
    }

    public static class MockWorkOrderAssignmentHistoryRepository implements WorkOrderAssignmentHistoryRepository {
        private final Map<Long, WorkOrderAssignmentHistory> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(5000);

        @Override public <S extends WorkOrderAssignmentHistory> S save(S entity) {
            if (entity.getId() == null) {
                try {
                    java.lang.reflect.Field field = WorkOrderAssignmentHistory.class.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(entity, seq.incrementAndGet());
                } catch (Exception ignored) {}
            }
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public List<WorkOrderAssignmentHistory> findByWorkOrderIdOrderByAssignedAtAsc(Long workOrderId) {
            return map.values().stream()
                    .filter(h -> h.getWorkOrder() != null && workOrderId.equals(h.getWorkOrder().getId()))
                    .sorted(Comparator.comparing(WorkOrderAssignmentHistory::getAssignedAt))
                    .toList();
        }

        @Override public void flush() {}
        @Override public <S extends WorkOrderAssignmentHistory> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends WorkOrderAssignmentHistory> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<WorkOrderAssignmentHistory> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public WorkOrderAssignmentHistory getOne(Long aLong) { return null; }
        @Override public WorkOrderAssignmentHistory getById(Long aLong) { return null; }
        @Override public WorkOrderAssignmentHistory getReferenceById(Long aLong) { return null; }
        @Override public <S extends WorkOrderAssignmentHistory> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends WorkOrderAssignmentHistory> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends WorkOrderAssignmentHistory> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends WorkOrderAssignmentHistory> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends WorkOrderAssignmentHistory> long count(Example<S> example) { return 0; }
        @Override public <S extends WorkOrderAssignmentHistory> boolean exists(Example<S> example) { return false; }
        @Override public <S extends WorkOrderAssignmentHistory, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends WorkOrderAssignmentHistory> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<WorkOrderAssignmentHistory> findById(Long aLong) { return Optional.ofNullable(map.get(aLong)); }
        @Override public boolean existsById(Long aLong) { return map.containsKey(aLong); }
        @Override public List<WorkOrderAssignmentHistory> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<WorkOrderAssignmentHistory> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) { map.remove(aLong); }
        @Override public void delete(WorkOrderAssignmentHistory entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends WorkOrderAssignmentHistory> entities) {}
        @Override public void deleteAll() { map.clear(); }
        @Override public List<WorkOrderAssignmentHistory> findAll(Sort sort) { return List.of(); }
        @Override public Page<WorkOrderAssignmentHistory> findAll(Pageable pageable) { return Page.empty(); }
    }

    public static class MockWorkOrderCommentRepository implements WorkOrderCommentRepository {
        private final Map<Long, WorkOrderComment> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(6000);

        @Override public <S extends WorkOrderComment> S save(S entity) {
            if (entity.getId() == null) {
                try {
                    java.lang.reflect.Field field = WorkOrderComment.class.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(entity, seq.incrementAndGet());
                } catch (Exception ignored) {}
            }
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public List<WorkOrderComment> findByWorkOrderIdOrderByCreatedAtAsc(Long workOrderId) {
            return map.values().stream()
                    .filter(c -> c.getWorkOrder() != null && workOrderId.equals(c.getWorkOrder().getId()))
                    .sorted(Comparator.comparing(WorkOrderComment::getCreatedAt))
                    .toList();
        }

        @Override public List<WorkOrderComment> findByWorkOrderIdAndInternalOnlyFalseOrderByCreatedAtAsc(Long workOrderId) {
            return map.values().stream()
                    .filter(c -> c.getWorkOrder() != null && workOrderId.equals(c.getWorkOrder().getId()) && !c.isInternalOnly())
                    .sorted(Comparator.comparing(WorkOrderComment::getCreatedAt))
                    .toList();
        }

        @Override public void flush() {}
        @Override public <S extends WorkOrderComment> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends WorkOrderComment> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<WorkOrderComment> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public WorkOrderComment getOne(Long aLong) { return null; }
        @Override public WorkOrderComment getById(Long aLong) { return null; }
        @Override public WorkOrderComment getReferenceById(Long aLong) { return null; }
        @Override public <S extends WorkOrderComment> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends WorkOrderComment> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends WorkOrderComment> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends WorkOrderComment> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends WorkOrderComment> long count(Example<S> example) { return 0; }
        @Override public <S extends WorkOrderComment> boolean exists(Example<S> example) { return false; }
        @Override public <S extends WorkOrderComment, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends WorkOrderComment> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<WorkOrderComment> findById(Long aLong) { return Optional.ofNullable(map.get(aLong)); }
        @Override public boolean existsById(Long aLong) { return map.containsKey(aLong); }
        @Override public List<WorkOrderComment> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<WorkOrderComment> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) { map.remove(aLong); }
        @Override public void delete(WorkOrderComment entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends WorkOrderComment> entities) {}
        @Override public void deleteAll() { map.clear(); }
        @Override public List<WorkOrderComment> findAll(Sort sort) { return List.of(); }
        @Override public Page<WorkOrderComment> findAll(Pageable pageable) { return Page.empty(); }
    }

    public static class MockAuditService extends AuditService {
        public MockAuditService() { super(null); }
        @Override public void record(String entityName, String entityId, AuditAction action, String performedBy, String details) {}
    }
}
