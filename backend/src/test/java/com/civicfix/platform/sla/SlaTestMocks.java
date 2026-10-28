package com.civicfix.platform.sla;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.audit.service.AuditService;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.repository.ComplaintRepository;
import com.civicfix.platform.sla.entity.BusinessCalendar;
import com.civicfix.platform.sla.entity.BusinessHoliday;
import com.civicfix.platform.sla.entity.ComplaintEscalationHistory;
import com.civicfix.platform.sla.entity.SlaPolicy;
import com.civicfix.platform.sla.repository.BusinessCalendarRepository;
import com.civicfix.platform.sla.repository.BusinessHolidayRepository;
import com.civicfix.platform.sla.repository.ComplaintEscalationHistoryRepository;
import com.civicfix.platform.sla.repository.SlaPolicyRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class SlaTestMocks {

    public static class MockSlaPolicyRepository implements SlaPolicyRepository {
        private final Map<Long, SlaPolicy> policies = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public <S extends SlaPolicy> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(seq.getAndIncrement());
            }
            policies.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<SlaPolicy> findById(Long id) {
            return Optional.ofNullable(policies.get(id));
        }

        @Override
        public List<SlaPolicy> findByOrganizationIdAndActiveTrue(Long orgId) {
            return policies.values().stream()
                    .filter(p -> p.getOrganization() != null && orgId.equals(p.getOrganization().getId()) && p.isActive())
                    .toList();
        }

        @Override
        public List<SlaPolicy> findByDepartmentIdAndActiveTrue(Long deptId) {
            return policies.values().stream()
                    .filter(p -> p.getDepartment() != null && deptId.equals(p.getDepartment().getId()) && p.isActive())
                    .toList();
        }

        @Override
        public Optional<SlaPolicy> findByOrganizationIdAndDepartmentIdAndPriorityAndActiveTrue(
                Long organizationId, Long departmentId, ComplaintPriority priority) {
            return policies.values().stream()
                    .filter(p -> p.isActive()
                            && p.getOrganization() != null && organizationId.equals(p.getOrganization().getId())
                            && p.getDepartment() != null && departmentId.equals(p.getDepartment().getId())
                            && p.getPriority() == priority)
                    .findFirst();
        }

        @Override
        public Optional<SlaPolicy> findByOrganizationIdAndDepartmentIsNullAndPriorityAndActiveTrue(
                Long organizationId, ComplaintPriority priority) {
            return policies.values().stream()
                    .filter(p -> p.isActive()
                            && p.getOrganization() != null && organizationId.equals(p.getOrganization().getId())
                            && p.getDepartment() == null
                            && p.getPriority() == priority)
                    .findFirst();
        }

        @Override
        public List<SlaPolicy> findMatchingPolicies(Long orgId, Long deptId, String category,
                                                    ComplaintPriority priority, ComplaintSeverity severity) {
            return policies.values().stream()
                    .filter(p -> p.isActive() && p.getOrganization() != null && orgId.equals(p.getOrganization().getId()))
                    .filter(p -> p.getDepartment() == null || deptId == null || deptId.equals(p.getDepartment().getId()))
                    .filter(p -> p.getCategory() == null || category == null || p.getCategory().equalsIgnoreCase(category))
                    .filter(p -> p.getPriority() == null || priority == null || p.getPriority() == priority)
                    .filter(p -> p.getSeverity() == null || severity == null || p.getSeverity() == severity)
                    .sorted((a, b) -> {
                        int deptA = a.getDepartment() != null ? 1 : 0;
                        int deptB = b.getDepartment() != null ? 1 : 0;
                        if (deptA != deptB) return Integer.compare(deptB, deptA);

                        int catA = a.getCategory() != null ? 1 : 0;
                        int catB = b.getCategory() != null ? 1 : 0;
                        if (catA != catB) return Integer.compare(catB, catA);

                        int sevA = a.getSeverity() != null ? 1 : 0;
                        int sevB = b.getSeverity() != null ? 1 : 0;
                        if (sevA != sevB) return Integer.compare(sevB, sevA);

                        return 0;
                    })
                    .toList();
        }

        @Override public void flush() {}
        @Override public <S extends SlaPolicy> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends SlaPolicy> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<SlaPolicy> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public SlaPolicy getOne(Long aLong) { return null; }
        @Override public SlaPolicy getById(Long aLong) { return null; }
        @Override public SlaPolicy getReferenceById(Long aLong) { return null; }
        @Override public <S extends SlaPolicy> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends SlaPolicy> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends SlaPolicy> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends SlaPolicy> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends SlaPolicy> long count(Example<S> example) { return 0; }
        @Override public <S extends SlaPolicy> boolean exists(Example<S> example) { return false; }
        @Override public <S extends SlaPolicy, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends SlaPolicy> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<SlaPolicy> findAll() { return new ArrayList<>(policies.values()); }
        @Override public List<SlaPolicy> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return policies.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(SlaPolicy entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends SlaPolicy> entities) {}
        @Override public void deleteAll() {}
        @Override public List<SlaPolicy> findAll(Sort sort) { return findAll(); }
        @Override public Page<SlaPolicy> findAll(Pageable pageable) { return new PageImpl<>(findAll()); }
    }

    public static class MockBusinessCalendarRepository implements BusinessCalendarRepository {
        private final Map<Long, BusinessCalendar> calendars = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public <S extends BusinessCalendar> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(seq.getAndIncrement());
            }
            calendars.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<BusinessCalendar> findById(Long id) {
            return Optional.ofNullable(calendars.get(id));
        }

        @Override
        public List<BusinessCalendar> findByOrganizationIdAndActiveTrue(Long orgId) {
            return calendars.values().stream()
                    .filter(c -> c.getOrganization() != null && orgId.equals(c.getOrganization().getId()) && c.isActive())
                    .toList();
        }

        @Override
        public Optional<BusinessCalendar> findByDepartmentIdAndActiveTrue(Long deptId) {
            return calendars.values().stream()
                    .filter(c -> c.getDepartment() != null && deptId.equals(c.getDepartment().getId()) && c.isActive())
                    .findFirst();
        }

        @Override
        public Optional<BusinessCalendar> findByOrganizationIdAndDepartmentIdAndActiveTrue(Long orgId, Long deptId) {
            return calendars.values().stream()
                    .filter(c -> c.getOrganization() != null && orgId.equals(c.getOrganization().getId())
                            && c.getDepartment() != null && deptId.equals(c.getDepartment().getId())
                            && c.isActive())
                    .findFirst();
        }

        @Override
        public Optional<BusinessCalendar> findByOrganizationIdAndDepartmentIsNullAndActiveTrue(Long orgId) {
            return calendars.values().stream()
                    .filter(c -> c.getOrganization() != null && orgId.equals(c.getOrganization().getId())
                            && c.getDepartment() == null && c.isActive())
                    .findFirst();
        }

        @Override public void flush() {}
        @Override public <S extends BusinessCalendar> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends BusinessCalendar> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<BusinessCalendar> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public BusinessCalendar getOne(Long aLong) { return null; }
        @Override public BusinessCalendar getById(Long aLong) { return null; }
        @Override public BusinessCalendar getReferenceById(Long aLong) { return null; }
        @Override public <S extends BusinessCalendar> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends BusinessCalendar> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends BusinessCalendar> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends BusinessCalendar> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends BusinessCalendar> long count(Example<S> example) { return 0; }
        @Override public <S extends BusinessCalendar> boolean exists(Example<S> example) { return false; }
        @Override public <S extends BusinessCalendar, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends BusinessCalendar> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<BusinessCalendar> findAll() { return new ArrayList<>(calendars.values()); }
        @Override public List<BusinessCalendar> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return calendars.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(BusinessCalendar entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends BusinessCalendar> entities) {}
        @Override public void deleteAll() {}
        @Override public List<BusinessCalendar> findAll(Sort sort) { return findAll(); }
        @Override public Page<BusinessCalendar> findAll(Pageable pageable) { return new PageImpl<>(findAll()); }
    }

    public static class MockBusinessHolidayRepository implements BusinessHolidayRepository {
        private final List<BusinessHoliday> holidays = new ArrayList<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public <S extends BusinessHoliday> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(seq.getAndIncrement());
            }
            holidays.add(entity);
            return entity;
        }

        @Override
        public List<BusinessHoliday> findByOrganizationIdAndActiveTrue(Long orgId) {
            return holidays.stream()
                    .filter(h -> h.getOrganization() != null && orgId.equals(h.getOrganization().getId()) && h.isActive())
                    .toList();
        }

        @Override
        public List<BusinessHoliday> findByOrganizationIdAndDepartmentIdAndActiveTrue(Long orgId, Long deptId) {
            return holidays.stream()
                    .filter(h -> h.getOrganization() != null && orgId.equals(h.getOrganization().getId())
                            && h.getDepartment() != null && deptId.equals(h.getDepartment().getId())
                            && h.isActive())
                    .toList();
        }

        @Override
        public List<BusinessHoliday> findByOrganizationIdAndDepartmentIsNullAndActiveTrue(Long orgId) {
            return holidays.stream()
                    .filter(h -> h.getOrganization() != null && orgId.equals(h.getOrganization().getId())
                            && h.getDepartment() == null && h.isActive())
                    .toList();
        }

        @Override
        public List<BusinessHoliday> findByHolidayDate(LocalDate holidayDate) {
            return holidays.stream()
                    .filter(h -> h.getHolidayDate() != null && h.getHolidayDate().equals(holidayDate) && h.isActive())
                    .toList();
        }

        @Override public void flush() {}
        @Override public <S extends BusinessHoliday> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends BusinessHoliday> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<BusinessHoliday> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public BusinessHoliday getOne(Long aLong) { return null; }
        @Override public BusinessHoliday getById(Long aLong) { return null; }
        @Override public BusinessHoliday getReferenceById(Long aLong) { return null; }
        @Override public <S extends BusinessHoliday> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends BusinessHoliday> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends BusinessHoliday> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends BusinessHoliday> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends BusinessHoliday> long count(Example<S> example) { return 0; }
        @Override public <S extends BusinessHoliday> boolean exists(Example<S> example) { return false; }
        @Override public <S extends BusinessHoliday, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends BusinessHoliday> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<BusinessHoliday> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<BusinessHoliday> findAll() { return holidays; }
        @Override public List<BusinessHoliday> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return holidays.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(BusinessHoliday entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends BusinessHoliday> entities) {}
        @Override public void deleteAll() {}
        @Override public List<BusinessHoliday> findAll(Sort sort) { return holidays; }
        @Override public Page<BusinessHoliday> findAll(Pageable pageable) { return new PageImpl<>(holidays); }
    }

    public static class MockComplaintEscalationHistoryRepository implements ComplaintEscalationHistoryRepository {
        private final List<ComplaintEscalationHistory> histories = new ArrayList<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public <S extends ComplaintEscalationHistory> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(seq.getAndIncrement());
            }
            histories.add(entity);
            return entity;
        }

        @Override
        public List<ComplaintEscalationHistory> findByComplaintIdOrderByEscalatedAtDesc(Long complaintId) {
            return histories.stream()
                    .filter(h -> h.getComplaint() != null && complaintId.equals(h.getComplaint().getId()))
                    .sorted((a, b) -> b.getEscalatedAt().compareTo(a.getEscalatedAt()))
                    .toList();
        }

        @Override
        public List<ComplaintEscalationHistory> findByComplaintIdOrderByEscalatedAtAsc(Long complaintId) {
            return histories.stream()
                    .filter(h -> h.getComplaint() != null && complaintId.equals(h.getComplaint().getId()))
                    .sorted(Comparator.comparing(ComplaintEscalationHistory::getEscalatedAt))
                    .toList();
        }

        public List<ComplaintEscalationHistory> getAllHistories() {
            return Collections.unmodifiableList(histories);
        }

        @Override public void flush() {}
        @Override public <S extends ComplaintEscalationHistory> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends ComplaintEscalationHistory> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<ComplaintEscalationHistory> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public ComplaintEscalationHistory getOne(Long aLong) { return null; }
        @Override public ComplaintEscalationHistory getById(Long aLong) { return null; }
        @Override public ComplaintEscalationHistory getReferenceById(Long aLong) { return null; }
        @Override public <S extends ComplaintEscalationHistory> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends ComplaintEscalationHistory> List<S> findAll(Example<S> example) { return List.of(); }
        @Override public <S extends ComplaintEscalationHistory> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
        @Override public <S extends ComplaintEscalationHistory> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends ComplaintEscalationHistory> long count(Example<S> example) { return 0; }
        @Override public <S extends ComplaintEscalationHistory> boolean exists(Example<S> example) { return false; }
        @Override public <S extends ComplaintEscalationHistory, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends ComplaintEscalationHistory> List<S> saveAll(Iterable<S> entities) { return List.of(); }
        @Override public Optional<ComplaintEscalationHistory> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<ComplaintEscalationHistory> findAll() { return histories; }
        @Override public List<ComplaintEscalationHistory> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return histories.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(ComplaintEscalationHistory entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends ComplaintEscalationHistory> entities) {}
        @Override public void deleteAll() {}
        @Override public List<ComplaintEscalationHistory> findAll(Sort sort) { return histories; }
        @Override public Page<ComplaintEscalationHistory> findAll(Pageable pageable) { return new PageImpl<>(histories); }
    }

    public static class CapturingEventPublisher implements ApplicationEventPublisher {
        private final List<Object> publishedEvents = new ArrayList<>();

        @Override
        public void publishEvent(Object event) {
            publishedEvents.add(event);
        }

        public List<Object> getPublishedEvents() {
            return publishedEvents;
        }

        public <T> List<T> getEventsOfType(Class<T> type) {
            return publishedEvents.stream()
                    .filter(type::isInstance)
                    .map(type::cast)
                    .toList();
        }

        public void clear() {
            publishedEvents.clear();
        }
    }
}
