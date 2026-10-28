package com.civicfix.platform.complaints;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.complaints.entity.*;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.complaints.repository.*;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.enums.DepartmentStatus;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.sla.service.SlaService;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.users.repository.EmployeeRepository;
import com.civicfix.platform.users.repository.UserRepository;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.wards.repository.WardRepository;
import com.civicfix.platform.workflow.entity.WorkflowTransition;
import com.civicfix.platform.workflow.repository.WorkflowTransitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class ComplaintTestMocks {

    public static class MockComplaintRepository implements ComplaintRepository {
        private final Map<Long, Complaint> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1000);

        @Override public <S extends Complaint> S save(S entity) {
            if (entity.getId() == null) entity.setId(seq.incrementAndGet());
            map.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Complaint> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public Optional<Complaint> findByTrackingNumber(String trackingNumber) {
            return map.values().stream().filter(c -> trackingNumber.equalsIgnoreCase(c.getTrackingNumber())).findFirst();
        }
        @Override public Page<Complaint> searchComplaints(Long citizenId, Long departmentId, Long wardId, Long assignedEmployeeId,
                                                          String category, ComplaintStatus status, ComplaintPriority priority,
                                                          ComplaintSeverity severity, Instant startDate, Instant endDate,
                                                          String keyword, Pageable pageable) {
            List<Complaint> filtered = map.values().stream()
                    .filter(c -> citizenId == null || (c.getCitizen() != null && citizenId.equals(c.getCitizen().getId())))
                    .filter(c -> departmentId == null || (c.getDepartment() != null && departmentId.equals(c.getDepartment().getId())))
                    .filter(c -> wardId == null || (c.getWard() != null && wardId.equals(c.getWard().getId())))
                    .filter(c -> assignedEmployeeId == null || (c.getAssignedEmployee() != null && assignedEmployeeId.equals(c.getAssignedEmployee().getId())))
                    .filter(c -> category == null || category.equalsIgnoreCase(c.getCategory()))
                    .filter(c -> status == null || status == c.getStatus())
                    .filter(c -> priority == null || priority == c.getPriority())
                    .filter(c -> severity == null || severity == c.getSeverity())
                    .filter(c -> keyword == null || c.getTitle().toLowerCase().contains(keyword.toLowerCase()) || c.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
            return new PageImpl<>(filtered, pageable, filtered.size());
        }
        @Override public List<Complaint> findByDuplicateOfId(Long duplicateOfId) {
            return map.values().stream().filter(c -> c.getDuplicateOf() != null && duplicateOfId.equals(c.getDuplicateOf().getId())).toList();
        }
        @Override public List<Complaint> findByDuplicateGroupId(String duplicateGroupId) {
            return map.values().stream().filter(c -> duplicateGroupId != null && duplicateGroupId.equals(c.getDuplicateGroupId())).toList();
        }
        @Override public Page<Complaint> findByOrganizationId(Long id, Pageable p) { return Page.empty(); }
        @Override public Page<Complaint> findByCitizenId(Long id, Pageable p) { return Page.empty(); }
        @Override public Page<Complaint> findByDepartmentId(Long id, Pageable p) { return Page.empty(); }
        @Override public Page<Complaint> findByWardId(Long id, Pageable p) { return Page.empty(); }
        @Override public Page<Complaint> findByStatus(ComplaintStatus s, Pageable p) { return Page.empty(); }
        @Override public Page<Complaint> findByAssignedOfficerId(Long id, Pageable p) { return Page.empty(); }
        @Override public Page<Complaint> findByAssignedEmployeeId(Long id, Pageable p) { return Page.empty(); }
        @Override public List<Complaint> findByStatusIn(List<ComplaintStatus> statuses) {
            return map.values().stream().filter(c -> statuses.contains(c.getStatus())).toList();
        }
        @Override public List<Complaint> findByStatusNotIn(List<ComplaintStatus> statuses) {
            return map.values().stream().filter(c -> !statuses.contains(c.getStatus())).toList();
        }
        @Override public void flush() {}
        @Override public <S extends Complaint> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Complaint> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<Complaint> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Complaint getOne(Long aLong) { return null; }
        @Override public Complaint getById(Long aLong) { return null; }
        @Override public Complaint getReferenceById(Long aLong) { return null; }
        @Override public <S extends Complaint> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Complaint> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends Complaint> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends Complaint> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends Complaint> long count(Example<S> example) { return 0; }
        @Override public <S extends Complaint> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Complaint, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Complaint> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<Complaint> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<Complaint> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(Complaint entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Complaint> entities) {}
        @Override public void deleteAll() {}
        @Override public List<Complaint> findAll(Sort sort) { return findAll(); }
        @Override public Page<Complaint> findAll(Pageable pageable) { return new PageImpl<>(findAll()); }
    }

    public static class MockStatusHistoryRepository implements ComplaintStatusHistoryRepository {
        private final List<ComplaintStatusHistory> list = new ArrayList<>();
        @Override public <S extends ComplaintStatusHistory> S save(S entity) { list.add(entity); return entity; }
        @Override public List<ComplaintStatusHistory> findByComplaintIdOrderByTransitionTimeAsc(Long id) {
            return list.stream().filter(h -> h.getComplaint().getId().equals(id)).toList();
        }
        @Override public void flush() {}
        @Override public <S extends ComplaintStatusHistory> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ComplaintStatusHistory> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<ComplaintStatusHistory> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public ComplaintStatusHistory getOne(Long aLong) { return null; }
        @Override public ComplaintStatusHistory getById(Long aLong) { return null; }
        @Override public ComplaintStatusHistory getReferenceById(Long aLong) { return null; }
        @Override public <S extends ComplaintStatusHistory> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ComplaintStatusHistory> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends ComplaintStatusHistory> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends ComplaintStatusHistory> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends ComplaintStatusHistory> long count(Example<S> example) { return 0; }
        @Override public <S extends ComplaintStatusHistory> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ComplaintStatusHistory, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends ComplaintStatusHistory> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<ComplaintStatusHistory> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<ComplaintStatusHistory> findAll() { return list; }
        @Override public List<ComplaintStatusHistory> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return list.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(ComplaintStatusHistory entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends ComplaintStatusHistory> entities) {}
        @Override public void deleteAll() {}
        @Override public List<ComplaintStatusHistory> findAll(Sort sort) { return list; }
        @Override public Page<ComplaintStatusHistory> findAll(Pageable pageable) { return new PageImpl<>(list); }
    }

    public static class MockAssignmentHistoryRepository implements ComplaintAssignmentHistoryRepository {
        private final List<ComplaintAssignmentHistory> list = new ArrayList<>();
        @Override public <S extends ComplaintAssignmentHistory> S save(S entity) { list.add(entity); return entity; }
        @Override public List<ComplaintAssignmentHistory> findByComplaintIdOrderByAssignedAtDesc(Long id) {
            return list.stream().filter(h -> h.getComplaint().getId().equals(id)).toList();
        }
        @Override public void flush() {}
        @Override public <S extends ComplaintAssignmentHistory> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ComplaintAssignmentHistory> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<ComplaintAssignmentHistory> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public ComplaintAssignmentHistory getOne(Long aLong) { return null; }
        @Override public ComplaintAssignmentHistory getById(Long aLong) { return null; }
        @Override public ComplaintAssignmentHistory getReferenceById(Long aLong) { return null; }
        @Override public <S extends ComplaintAssignmentHistory> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ComplaintAssignmentHistory> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends ComplaintAssignmentHistory> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends ComplaintAssignmentHistory> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends ComplaintAssignmentHistory> long count(Example<S> example) { return 0; }
        @Override public <S extends ComplaintAssignmentHistory> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ComplaintAssignmentHistory, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends ComplaintAssignmentHistory> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<ComplaintAssignmentHistory> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<ComplaintAssignmentHistory> findAll() { return list; }
        @Override public List<ComplaintAssignmentHistory> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return list.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(ComplaintAssignmentHistory entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends ComplaintAssignmentHistory> entities) {}
        @Override public void deleteAll() {}
        @Override public List<ComplaintAssignmentHistory> findAll(Sort sort) { return list; }
        @Override public Page<ComplaintAssignmentHistory> findAll(Pageable pageable) { return new PageImpl<>(list); }
    }

    public static class MockCommentRepository implements ComplaintCommentRepository {
        private final List<ComplaintComment> list = new ArrayList<>();
        private final AtomicLong seq = new AtomicLong(100);

        @Override public <S extends ComplaintComment> S save(S entity) {
            if (entity.getId() == null) entity.setId(seq.incrementAndGet());
            list.add(entity);
            return entity;
        }
        @Override public List<ComplaintComment> findByComplaintIdOrderByCreatedAtAsc(Long id) {
            return list.stream().filter(c -> c.getComplaint().getId().equals(id)).toList();
        }
        @Override public List<ComplaintComment> findByComplaintIdAndInternalOnlyFalseOrderByCreatedAtAsc(Long id) {
            return list.stream().filter(c -> c.getComplaint().getId().equals(id) && !c.isInternalOnly()).toList();
        }
        @Override public void flush() {}
        @Override public <S extends ComplaintComment> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ComplaintComment> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<ComplaintComment> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public ComplaintComment getOne(Long aLong) { return null; }
        @Override public ComplaintComment getById(Long aLong) { return null; }
        @Override public ComplaintComment getReferenceById(Long aLong) { return null; }
        @Override public <S extends ComplaintComment> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ComplaintComment> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends ComplaintComment> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends ComplaintComment> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends ComplaintComment> long count(Example<S> example) { return 0; }
        @Override public <S extends ComplaintComment> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ComplaintComment, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends ComplaintComment> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<ComplaintComment> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<ComplaintComment> findAll() { return list; }
        @Override public List<ComplaintComment> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return list.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(ComplaintComment entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends ComplaintComment> entities) {}
        @Override public void deleteAll() {}
        @Override public List<ComplaintComment> findAll(Sort sort) { return list; }
        @Override public Page<ComplaintComment> findAll(Pageable pageable) { return new PageImpl<>(list); }
    }

    public static class MockWorkflowTransitionRepository implements WorkflowTransitionRepository {
        private final List<WorkflowTransition> list = new ArrayList<>();
        @Override public <S extends WorkflowTransition> S save(S entity) { list.add(entity); return entity; }
        @Override public List<WorkflowTransition> findByComplaintIdOrderByCreatedAtAsc(Long id) {
            return list.stream().filter(t -> t.getComplaint().getId().equals(id)).toList();
        }
        @Override public void flush() {}
        @Override public <S extends WorkflowTransition> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends WorkflowTransition> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<WorkflowTransition> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public WorkflowTransition getOne(Long aLong) { return null; }
        @Override public WorkflowTransition getById(Long aLong) { return null; }
        @Override public WorkflowTransition getReferenceById(Long aLong) { return null; }
        @Override public <S extends WorkflowTransition> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends WorkflowTransition> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends WorkflowTransition> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends WorkflowTransition> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends WorkflowTransition> long count(Example<S> example) { return 0; }
        @Override public <S extends WorkflowTransition> boolean exists(Example<S> example) { return false; }
        @Override public <S extends WorkflowTransition, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends WorkflowTransition> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<WorkflowTransition> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<WorkflowTransition> findAll() { return list; }
        @Override public List<WorkflowTransition> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return list.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(WorkflowTransition entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends WorkflowTransition> entities) {}
        @Override public void deleteAll() {}
        @Override public List<WorkflowTransition> findAll(Sort sort) { return list; }
        @Override public Page<WorkflowTransition> findAll(Pageable pageable) { return new PageImpl<>(list); }
    }

    public static class MockUserRepository implements UserRepository {
        private final Map<Long, User> map = new HashMap<>();
        @Override public <S extends User> S save(S entity) { map.put(entity.getId(), entity); return entity; }
        @Override public Optional<User> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public Optional<User> findByUsername(String u) { return map.values().stream().filter(user -> u.equalsIgnoreCase(user.getUsername())).findFirst(); }
        @Override public Optional<User> findByEmail(String email) { return map.values().stream().filter(u -> email.equalsIgnoreCase(u.getEmail())).findFirst(); }
        @Override public Optional<User> findByUsernameOrEmail(String username, String email) {
            return map.values().stream().filter(u -> username.equalsIgnoreCase(u.getUsername()) || email.equalsIgnoreCase(u.getEmail())).findFirst();
        }
        @Override public boolean existsByUsername(String username) { return map.values().stream().anyMatch(u -> username.equalsIgnoreCase(u.getUsername())); }
        @Override public boolean existsByEmail(String email) { return map.values().stream().anyMatch(u -> email.equalsIgnoreCase(u.getEmail())); }
        @Override public Page<User> findAllByActiveTrue(Pageable pageable) { return new PageImpl<>(new ArrayList<>(map.values())); }
        @Override public Page<User> findByOrganizationIdAndActiveTrue(Long organizationId, Pageable pageable) { return Page.empty(); }
        @Override public Page<User> findByDepartmentIdAndActiveTrue(Long departmentId, Pageable pageable) { return Page.empty(); }
        @Override public void flush() {}
        @Override public <S extends User> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<User> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public User getOne(Long aLong) { return null; }
        @Override public User getById(Long aLong) { return null; }
        @Override public User getReferenceById(Long aLong) { return null; }
        @Override public <S extends User> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends User> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends User> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends User> long count(Example<S> example) { return 0; }
        @Override public <S extends User> boolean exists(Example<S> example) { return false; }
        @Override public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends User> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<User> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<User> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(User entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends User> entities) {}
        @Override public void deleteAll() {}
        @Override public List<User> findAll(Sort sort) { return findAll(); }
        @Override public Page<User> findAll(Pageable pageable) { return new PageImpl<>(findAll()); }
    }

    public static class MockWardRepository implements WardRepository {
        private final Map<Long, Ward> map = new HashMap<>();
        @Override public <S extends Ward> S save(S entity) { map.put(entity.getId(), entity); return entity; }
        @Override public Optional<Ward> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public List<Ward> findByOrganizationIdAndActiveTrue(Long id) { return List.of(); }
        @Override public Page<Ward> findByOrganizationId(Long id, Pageable p) { return Page.empty(); }
        @Override public Optional<Ward> findByOrganizationIdAndWardNumber(Long id, String n) { return Optional.empty(); }
        @Override public boolean existsByOrganizationIdAndWardNumber(Long id, String n) { return false; }
        @Override public List<Ward> findByMunicipalityIdAndActiveTrue(Long id) { return List.of(); }
        @Override public Page<Ward> findByMunicipalityId(Long id, Pageable p) { return Page.empty(); }
        @Override public Optional<Ward> findByMunicipalityIdAndWardNumber(Long id, String n) { return Optional.empty(); }
        @Override public boolean existsByMunicipalityIdAndWardNumber(Long id, String n) { return false; }
        @Override public Page<Ward> searchWards(Long m, Long org, String z, Boolean a, String q, Pageable p) { return Page.empty(); }
        @Override public void flush() {}
        @Override public <S extends Ward> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Ward> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<Ward> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Ward getOne(Long aLong) { return null; }
        @Override public Ward getById(Long aLong) { return null; }
        @Override public Ward getReferenceById(Long aLong) { return null; }
        @Override public <S extends Ward> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Ward> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends Ward> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends Ward> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends Ward> long count(Example<S> example) { return 0; }
        @Override public <S extends Ward> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Ward, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Ward> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<Ward> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<Ward> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(Ward entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Ward> entities) {}
        @Override public void deleteAll() {}
        @Override public List<Ward> findAll(Sort sort) { return findAll(); }
        @Override public Page<Ward> findAll(Pageable pageable) { return new PageImpl<>(findAll()); }
    }

    public static class MockDepartmentRepository implements DepartmentRepository {
        private final Map<Long, Department> map = new HashMap<>();
        @Override public <S extends Department> S save(S entity) { map.put(entity.getId(), entity); return entity; }
        @Override public Optional<Department> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public Optional<Department> findByCode(String code) {
            return map.values().stream().filter(d -> code.equalsIgnoreCase(d.getCode())).findFirst();
        }
        @Override public List<Department> findByOrganizationIdAndActiveTrue(Long id) { return List.of(); }
        @Override public Page<Department> findByOrganizationId(Long id, Pageable p) { return Page.empty(); }
        @Override public Optional<Department> findByOrganizationIdAndCode(Long id, String c) { return Optional.empty(); }
        @Override public boolean existsByOrganizationIdAndCode(Long id, String c) { return false; }
        @Override public List<Department> findByMunicipalityIdAndActiveTrue(Long id) { return List.of(); }
        @Override public Page<Department> findByMunicipalityId(Long id, Pageable p) { return Page.empty(); }
        @Override public Optional<Department> findByMunicipalityIdAndCode(Long id, String c) { return Optional.empty(); }
        @Override public boolean existsByMunicipalityIdAndCode(Long id, String c) { return false; }
        @Override public boolean existsByCode(String code) { return false; }
        @Override public Page<Department> searchDepartments(Long m, DepartmentStatus s, Boolean a, String q, Pageable p) { return Page.empty(); }
        @Override public void flush() {}
        @Override public <S extends Department> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Department> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<Department> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Department getOne(Long aLong) { return null; }
        @Override public Department getById(Long aLong) { return null; }
        @Override public Department getReferenceById(Long aLong) { return null; }
        @Override public <S extends Department> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Department> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends Department> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends Department> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends Department> long count(Example<S> example) { return 0; }
        @Override public <S extends Department> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Department, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Department> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<Department> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<Department> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(Department entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Department> entities) {}
        @Override public void deleteAll() {}
        @Override public List<Department> findAll(Sort sort) { return findAll(); }
        @Override public Page<Department> findAll(Pageable pageable) { return new PageImpl<>(findAll()); }
    }

    public static class MockEmployeeRepository implements EmployeeRepository {
        private final Map<Long, Employee> map = new HashMap<>();
        @Override public <S extends Employee> S save(S entity) { map.put(entity.getId(), entity); return entity; }
        @Override public Optional<Employee> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public Optional<Employee> findByEmployeeCode(String c) { return map.values().stream().filter(e -> c.equalsIgnoreCase(e.getEmployeeCode())).findFirst(); }
        @Override public Optional<Employee> findByEmail(String e) { return map.values().stream().filter(emp -> e.equalsIgnoreCase(emp.getEmail())).findFirst(); }
        @Override public Optional<Employee> findByUserId(Long userId) {
            return map.values().stream().filter(e -> e.getUser() != null && userId.equals(e.getUser().getId())).findFirst();
        }
        @Override public boolean existsByEmployeeCode(String c) { return false; }
        @Override public boolean existsByEmail(String e) { return false; }
        @Override public List<Employee> findByDepartmentIdAndActiveTrue(Long id) {
            return map.values().stream()
                    .filter(e -> e.getDepartment() != null && id.equals(e.getDepartment().getId()) && e.isActive())
                    .toList();
        }
        @Override public List<Employee> findByTeamIdAndActiveTrue(Long id) { return List.of(); }
        @Override public List<Employee> findBySupervisorIdAndActiveTrue(Long id) { return List.of(); }
        @Override public Page<Employee> findAllByActiveTrue(Pageable p) { return Page.empty(); }
        @Override public Page<Employee> searchEmployees(Long d, Long t, Long s, RoleType r, Boolean a, String q, Pageable p) { return Page.empty(); }
        @Override public void flush() {}
        @Override public <S extends Employee> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Employee> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<Employee> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Employee getOne(Long aLong) { return null; }
        @Override public Employee getById(Long aLong) { return null; }
        @Override public Employee getReferenceById(Long aLong) { return null; }
        @Override public <S extends Employee> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Employee> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends Employee> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends Employee> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends Employee> long count(Example<S> example) { return 0; }
        @Override public <S extends Employee> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Employee, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Employee> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<Employee> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<Employee> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(Employee entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Employee> entities) {}
        @Override public void deleteAll() {}
        @Override public List<Employee> findAll(Sort sort) { return findAll(); }
        @Override public Page<Employee> findAll(Pageable pageable) { return new PageImpl<>(findAll()); }
    }

    public static class MockAttachmentRepository implements ComplaintAttachmentRepository {
        @Override public List<ComplaintAttachment> findByComplaintId(Long id) { return List.of(); }
        @Override public void flush() {}
        @Override public <S extends ComplaintAttachment> S saveAndFlush(S entity) { return null; }
        @Override public <S extends ComplaintAttachment> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<ComplaintAttachment> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public ComplaintAttachment getOne(Long aLong) { return null; }
        @Override public ComplaintAttachment getById(Long aLong) { return null; }
        @Override public ComplaintAttachment getReferenceById(Long aLong) { return null; }
        @Override public <S extends ComplaintAttachment> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ComplaintAttachment> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends ComplaintAttachment> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends ComplaintAttachment> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends ComplaintAttachment> long count(Example<S> example) { return 0; }
        @Override public <S extends ComplaintAttachment> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ComplaintAttachment, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends ComplaintAttachment> S save(S entity) { return null; }
        @Override public <S extends ComplaintAttachment> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<ComplaintAttachment> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<ComplaintAttachment> findAll() { return List.of(); }
        @Override public List<ComplaintAttachment> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(ComplaintAttachment entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends ComplaintAttachment> entities) {}
        @Override public void deleteAll() {}
        @Override public List<ComplaintAttachment> findAll(Sort sort) { return List.of(); }
        @Override public Page<ComplaintAttachment> findAll(Pageable pageable) { return Page.empty(); }
    }

    public static class MockAuditService extends AuditService {
        public MockAuditService() { super(null); }
        @Override public void record(String e, String i, AuditAction a, String u, String d) {}
    }

    public static class MockSlaService extends SlaService {
        public MockSlaService() { super(null, null, null, null, null, null); }
        @Override public Instant calculateDueDate(Long o, Long d, ComplaintPriority p) { return Instant.now().plusSeconds(86400); }
        @Override public Instant calculateDueDate(Long o, Long d, String cat, ComplaintPriority p, ComplaintSeverity s, Instant c) { return Instant.now().plusSeconds(86400); }
        @Override public Instant calculateWarningDate(Long o, Long d, String cat, ComplaintPriority p, ComplaintSeverity s, Instant c) { return Instant.now().plusSeconds(64800); }
    }

    public static class MockEventPublisher implements ApplicationEventPublisher {
        @Override public void publishEvent(Object event) {}
    }
}
