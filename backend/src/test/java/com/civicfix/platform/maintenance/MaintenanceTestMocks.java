package com.civicfix.platform.maintenance;

import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.maintenance.entity.*;
import com.civicfix.platform.maintenance.enums.*;
import com.civicfix.platform.maintenance.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class MaintenanceTestMocks {

    private static <E> List<E> toList(Map<Long, E> map) { return new ArrayList<>(map.values()); }

    // =========================================================================
    // MockInspectionPlanRepository
    // =========================================================================
    public static class MockInspectionPlanRepository implements InspectionPlanRepository {
        public final Map<Long, InspectionPlan> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(100);

        @Override public <S extends InspectionPlan> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<InspectionPlan> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<InspectionPlan> findByPlanCode(String code) {
            return map.values().stream().filter(p -> code.equalsIgnoreCase(p.getPlanCode())).findFirst();
        }
        @Override public boolean existsByPlanCode(String code) {
            return map.values().stream().anyMatch(p -> code.equalsIgnoreCase(p.getPlanCode()));
        }
        @Override public List<InspectionPlan> findByDepartmentId(Long dId) {
            return map.values().stream().filter(p -> p.getDepartment() != null && dId.equals(p.getDepartment().getId())).toList();
        }
        @Override public List<InspectionPlan> findByTargetAssetTypeAndActiveTrue(AssetType type) {
            return map.values().stream().filter(p -> p.isActive() && type == p.getTargetAssetType()).toList();
        }
        @Override public Page<InspectionPlan> searchPlans(String keyword, Long deptId, PlanType planType,
                                                          AssetType assetType, Boolean active, Pageable p) {
            List<InspectionPlan> list = map.values().stream()
                    .filter(ip -> keyword == null || ip.getName().toLowerCase().contains(keyword.toLowerCase())
                            || ip.getPlanCode().toLowerCase().contains(keyword.toLowerCase()))
                    .filter(ip -> deptId == null || (ip.getDepartment() != null && deptId.equals(ip.getDepartment().getId())))
                    .filter(ip -> planType == null || ip.getPlanType() == planType)
                    .filter(ip -> assetType == null || ip.getTargetAssetType() == assetType)
                    .filter(ip -> active == null || ip.isActive() == active)
                    .toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<InspectionPlan> findAll() { return toList(map); }
        @Override public List<InspectionPlan> findAll(Sort s) { return toList(map); }
        @Override public Page<InspectionPlan> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends InspectionPlan> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<InspectionPlan> findAllById(Iterable<Long> ids) {
            List<InspectionPlan> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(InspectionPlan e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends InspectionPlan> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends InspectionPlan> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends InspectionPlan> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<InspectionPlan> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public InspectionPlan getOne(Long id) { return map.get(id); }
        @Override public InspectionPlan getById(Long id) { return map.get(id); }
        @Override public InspectionPlan getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends InspectionPlan> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends InspectionPlan> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends InspectionPlan> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends InspectionPlan> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends InspectionPlan> long count(Example<S> ex) { return 0; }
        @Override public <S extends InspectionPlan> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends InspectionPlan, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockMaintenanceScheduleRepository
    // =========================================================================
    public static class MockMaintenanceScheduleRepository implements MaintenanceScheduleRepository {
        public final Map<Long, MaintenanceSchedule> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(200);

        @Override public <S extends MaintenanceSchedule> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<MaintenanceSchedule> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<MaintenanceSchedule> findByScheduleCode(String code) {
            return map.values().stream().filter(s -> code.equalsIgnoreCase(s.getScheduleCode())).findFirst();
        }
        @Override public boolean existsByScheduleCode(String code) {
            return map.values().stream().anyMatch(s -> code.equalsIgnoreCase(s.getScheduleCode()));
        }
        @Override public List<MaintenanceSchedule> findByAssetId(Long aId) {
            return map.values().stream().filter(s -> s.getAsset() != null && aId.equals(s.getAsset().getId())).toList();
        }
        @Override public List<MaintenanceSchedule> findByDepartmentId(Long dId) {
            return map.values().stream().filter(s -> s.getDepartment() != null && dId.equals(s.getDepartment().getId())).toList();
        }
        @Override public List<MaintenanceSchedule> findByStatusAndNextDueDateLessThanEqual(ScheduleStatus status, Instant dueThreshold) {
            return map.values().stream()
                    .filter(s -> s.getStatus() == status && s.getNextDueDate() != null && !s.getNextDueDate().isAfter(dueThreshold))
                    .toList();
        }
        @Override public Page<MaintenanceSchedule> searchSchedules(String keyword, Long assetId, Long deptId,
                                                                   ScheduleStatus status, Pageable p) {
            List<MaintenanceSchedule> list = map.values().stream()
                    .filter(s -> keyword == null || s.getTitle().toLowerCase().contains(keyword.toLowerCase())
                            || s.getScheduleCode().toLowerCase().contains(keyword.toLowerCase()))
                    .filter(s -> assetId == null || (s.getAsset() != null && assetId.equals(s.getAsset().getId())))
                    .filter(s -> deptId == null || (s.getDepartment() != null && deptId.equals(s.getDepartment().getId())))
                    .filter(s -> status == null || s.getStatus() == status)
                    .toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<MaintenanceSchedule> findAll() { return toList(map); }
        @Override public List<MaintenanceSchedule> findAll(Sort s) { return toList(map); }
        @Override public Page<MaintenanceSchedule> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends MaintenanceSchedule> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<MaintenanceSchedule> findAllById(Iterable<Long> ids) {
            List<MaintenanceSchedule> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(MaintenanceSchedule e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends MaintenanceSchedule> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends MaintenanceSchedule> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends MaintenanceSchedule> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<MaintenanceSchedule> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public MaintenanceSchedule getOne(Long id) { return map.get(id); }
        @Override public MaintenanceSchedule getById(Long id) { return map.get(id); }
        @Override public MaintenanceSchedule getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends MaintenanceSchedule> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends MaintenanceSchedule> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends MaintenanceSchedule> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends MaintenanceSchedule> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends MaintenanceSchedule> long count(Example<S> ex) { return 0; }
        @Override public <S extends MaintenanceSchedule> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends MaintenanceSchedule, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockConditionAssessmentRepository
    // =========================================================================
    public static class MockConditionAssessmentRepository implements ConditionAssessmentRepository {
        public final Map<Long, ConditionAssessment> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(300);

        @Override public <S extends ConditionAssessment> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<ConditionAssessment> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<ConditionAssessment> findByAssessmentCode(String code) {
            return map.values().stream().filter(c -> code.equalsIgnoreCase(c.getAssessmentCode())).findFirst();
        }
        @Override public boolean existsByAssessmentCode(String code) {
            return map.values().stream().anyMatch(c -> code.equalsIgnoreCase(c.getAssessmentCode()));
        }
        @Override public List<ConditionAssessment> findByAssetIdOrderByAssessmentDateDesc(Long aId) {
            return map.values().stream()
                    .filter(c -> c.getAsset() != null && aId.equals(c.getAsset().getId()))
                    .sorted((a, b) -> b.getAssessmentDate().compareTo(a.getAssessmentDate()))
                    .toList();
        }
        @Override public Optional<ConditionAssessment> findFirstByAssetIdOrderByAssessmentDateDesc(Long aId) {
            return findByAssetIdOrderByAssessmentDateDesc(aId).stream().findFirst();
        }
        @Override public Page<ConditionAssessment> searchAssessments(Long assetId, ConditionRating condition, Pageable p) {
            List<ConditionAssessment> list = map.values().stream()
                    .filter(c -> assetId == null || (c.getAsset() != null && assetId.equals(c.getAsset().getId())))
                    .filter(c -> condition == null || c.getOverallCondition() == condition)
                    .toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<ConditionAssessment> findAll() { return toList(map); }
        @Override public List<ConditionAssessment> findAll(Sort s) { return toList(map); }
        @Override public Page<ConditionAssessment> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends ConditionAssessment> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<ConditionAssessment> findAllById(Iterable<Long> ids) {
            List<ConditionAssessment> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(ConditionAssessment e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends ConditionAssessment> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends ConditionAssessment> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends ConditionAssessment> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<ConditionAssessment> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public ConditionAssessment getOne(Long id) { return map.get(id); }
        @Override public ConditionAssessment getById(Long id) { return map.get(id); }
        @Override public ConditionAssessment getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends ConditionAssessment> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends ConditionAssessment> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends ConditionAssessment> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends ConditionAssessment> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends ConditionAssessment> long count(Example<S> ex) { return 0; }
        @Override public <S extends ConditionAssessment> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends ConditionAssessment, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockMaintenanceReminderRepository
    // =========================================================================
    public static class MockMaintenanceReminderRepository implements MaintenanceReminderRepository {
        public final Map<Long, MaintenanceReminder> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(400);

        @Override public <S extends MaintenanceReminder> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<MaintenanceReminder> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<MaintenanceReminder> findByRecipientIdAndStatus(Long rId, ReminderStatus status) {
            return map.values().stream()
                    .filter(r -> r.getRecipient() != null && rId.equals(r.getRecipient().getId()) && r.getStatus() == status)
                    .toList();
        }
        @Override public List<MaintenanceReminder> findByStatusAndDueDateLessThanEqual(ReminderStatus status, Instant dueThreshold) {
            return map.values().stream()
                    .filter(r -> r.getStatus() == status && r.getDueDate() != null && !r.getDueDate().isAfter(dueThreshold))
                    .toList();
        }
        @Override public Page<MaintenanceReminder> findByRecipientIdOrderByDueDateAsc(Long rId, Pageable p) {
            List<MaintenanceReminder> list = map.values().stream()
                    .filter(r -> r.getRecipient() != null && rId.equals(r.getRecipient().getId()))
                    .sorted(Comparator.comparing(MaintenanceReminder::getDueDate))
                    .toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public List<MaintenanceReminder> findByScheduleId(Long sId) {
            return map.values().stream()
                    .filter(r -> r.getSchedule() != null && sId.equals(r.getSchedule().getId()))
                    .toList();
        }
        @Override public long count() { return map.size(); }
        @Override public List<MaintenanceReminder> findAll() { return toList(map); }
        @Override public List<MaintenanceReminder> findAll(Sort s) { return toList(map); }
        @Override public Page<MaintenanceReminder> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends MaintenanceReminder> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<MaintenanceReminder> findAllById(Iterable<Long> ids) {
            List<MaintenanceReminder> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(MaintenanceReminder e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends MaintenanceReminder> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends MaintenanceReminder> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends MaintenanceReminder> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<MaintenanceReminder> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public MaintenanceReminder getOne(Long id) { return map.get(id); }
        @Override public MaintenanceReminder getById(Long id) { return map.get(id); }
        @Override public MaintenanceReminder getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends MaintenanceReminder> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends MaintenanceReminder> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends MaintenanceReminder> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends MaintenanceReminder> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends MaintenanceReminder> long count(Example<S> ex) { return 0; }
        @Override public <S extends MaintenanceReminder> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends MaintenanceReminder, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockRepairRecommendationRepository
    // =========================================================================
    public static class MockRepairRecommendationRepository implements RepairRecommendationRepository {
        public final Map<Long, RepairRecommendation> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(500);

        @Override public <S extends RepairRecommendation> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<RepairRecommendation> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<RepairRecommendation> findByRecommendationCode(String code) {
            return map.values().stream().filter(r -> code.equalsIgnoreCase(r.getRecommendationCode())).findFirst();
        }
        @Override public boolean existsByRecommendationCode(String code) {
            return map.values().stream().anyMatch(r -> code.equalsIgnoreCase(r.getRecommendationCode()));
        }
        @Override public List<RepairRecommendation> findByAssetId(Long aId) {
            return map.values().stream().filter(r -> r.getAsset() != null && aId.equals(r.getAsset().getId())).toList();
        }
        @Override public Page<RepairRecommendation> searchRecommendations(Long assetId, RecommendationStatus status, Pageable p) {
            List<RepairRecommendation> list = map.values().stream()
                    .filter(r -> assetId == null || (r.getAsset() != null && assetId.equals(r.getAsset().getId())))
                    .filter(r -> status == null || r.getStatus() == status)
                    .toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<RepairRecommendation> findAll() { return toList(map); }
        @Override public List<RepairRecommendation> findAll(Sort s) { return toList(map); }
        @Override public Page<RepairRecommendation> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends RepairRecommendation> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<RepairRecommendation> findAllById(Iterable<Long> ids) {
            List<RepairRecommendation> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(RepairRecommendation e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends RepairRecommendation> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends RepairRecommendation> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends RepairRecommendation> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<RepairRecommendation> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public RepairRecommendation getOne(Long id) { return map.get(id); }
        @Override public RepairRecommendation getById(Long id) { return map.get(id); }
        @Override public RepairRecommendation getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends RepairRecommendation> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends RepairRecommendation> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends RepairRecommendation> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends RepairRecommendation> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends RepairRecommendation> long count(Example<S> ex) { return 0; }
        @Override public <S extends RepairRecommendation> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends RepairRecommendation, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }

    // =========================================================================
    // MockAssetFailureLogRepository
    // =========================================================================
    public static class MockAssetFailureLogRepository implements AssetFailureLogRepository {
        public final Map<Long, AssetFailureLog> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(600);

        @Override public <S extends AssetFailureLog> S save(S e) {
            if (e.getId() == null) e.setId(seq.incrementAndGet());
            map.put(e.getId(), e); return e;
        }
        @Override public Optional<AssetFailureLog> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<AssetFailureLog> findByFailureCode(String code) {
            return map.values().stream().filter(f -> code.equalsIgnoreCase(f.getFailureCode())).findFirst();
        }
        @Override public boolean existsByFailureCode(String code) {
            return map.values().stream().anyMatch(f -> code.equalsIgnoreCase(f.getFailureCode()));
        }
        @Override public List<AssetFailureLog> findByAssetIdOrderByFailureDateDesc(Long aId) {
            return map.values().stream()
                    .filter(f -> f.getAsset() != null && aId.equals(f.getAsset().getId()))
                    .sorted((a, b) -> b.getFailureDate().compareTo(a.getFailureDate()))
                    .toList();
        }
        @Override public Page<AssetFailureLog> searchFailureLogs(Long assetId, FailureSeverity severity, Pageable p) {
            List<AssetFailureLog> list = map.values().stream()
                    .filter(f -> assetId == null || (f.getAsset() != null && assetId.equals(f.getAsset().getId())))
                    .filter(f -> severity == null || f.getSeverity() == severity)
                    .toList();
            return new PageImpl<>(list, p, list.size());
        }
        @Override public long count() { return map.size(); }
        @Override public List<AssetFailureLog> findAll() { return toList(map); }
        @Override public List<AssetFailureLog> findAll(Sort s) { return toList(map); }
        @Override public Page<AssetFailureLog> findAll(Pageable p) { return new PageImpl<>(toList(map), p, map.size()); }
        @Override public <S extends AssetFailureLog> List<S> saveAll(Iterable<S> es) {
            List<S> r = new ArrayList<>(); es.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<AssetFailureLog> findAllById(Iterable<Long> ids) {
            List<AssetFailureLog> r = new ArrayList<>(); ids.forEach(id -> { if (map.containsKey(id)) r.add(map.get(id)); }); return r;
        }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(AssetFailureLog e) { map.remove(e.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends AssetFailureLog> es) { es.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends AssetFailureLog> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends AssetFailureLog> List<S> saveAllAndFlush(Iterable<S> es) { return saveAll(es); }
        @Override public void deleteAllInBatch(Iterable<AssetFailureLog> es) { deleteAll(es); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public AssetFailureLog getOne(Long id) { return map.get(id); }
        @Override public AssetFailureLog getById(Long id) { return map.get(id); }
        @Override public AssetFailureLog getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends AssetFailureLog> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
        @Override public <S extends AssetFailureLog> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
        @Override public <S extends AssetFailureLog> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
        @Override public <S extends AssetFailureLog> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
        @Override public <S extends AssetFailureLog> long count(Example<S> ex) { return 0; }
        @Override public <S extends AssetFailureLog> boolean exists(Example<S> ex) { return false; }
        @Override public <S extends AssetFailureLog, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
    }
}
