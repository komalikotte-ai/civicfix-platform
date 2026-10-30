package com.civicfix.platform.assets;

import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.assets.entity.AssetInspection;
import com.civicfix.platform.assets.entity.AssetRepair;
import com.civicfix.platform.assets.entity.AssetReplacement;
import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.assets.repository.AssetInspectionRepository;
import com.civicfix.platform.assets.repository.AssetRepairRepository;
import com.civicfix.platform.assets.repository.AssetReplacementRepository;
import com.civicfix.platform.assets.repository.AssetRepository;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.organizations.repository.OrganizationRepository;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class AssetTestMocks {

    public static class MockOrganizationRepository implements OrganizationRepository {
        public final Map<Long, Organization> map = new HashMap<>();

        @Override public <S extends Organization> S save(S entity) {
            if (entity.getId() == null) entity.setId((long) (map.size() + 1));
            map.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Organization> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<Organization> findAll() { return new ArrayList<>(map.values()); }
        @Override public Optional<Organization> findByCode(String code) {
            return map.values().stream().filter(o -> code.equalsIgnoreCase(o.getCode())).findFirst();
        }
        @Override public boolean existsByCode(String code) {
            return map.values().stream().anyMatch(o -> code.equalsIgnoreCase(o.getCode()));
        }
        @Override public Page<Organization> findAllByActiveTrue(Pageable pageable) {
            List<Organization> list = map.values().stream().filter(Organization::isActive).toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(Organization entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends Organization> entities) { entities.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public List<Organization> findAll(Sort sort) { return new ArrayList<>(map.values()); }
        @Override public Page<Organization> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(map.values()), pageable, map.size()); }
        @Override public <S extends Organization> List<S> saveAll(Iterable<S> entities) {
            List<S> res = new ArrayList<>();
            entities.forEach(e -> res.add(save(e)));
            return res;
        }
        @Override public List<Organization> findAllById(Iterable<Long> ids) {
            List<Organization> res = new ArrayList<>();
            ids.forEach(id -> { if (map.containsKey(id)) res.add(map.get(id)); });
            return res;
        }
        @Override public void flush() {}
        @Override public <S extends Organization> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Organization> List<S> saveAllAndFlush(Iterable<S> entities) { return saveAll(entities); }
        @Override public void deleteAllInBatch(Iterable<Organization> entities) { deleteAll(entities); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public Organization getOne(Long id) { return map.get(id); }
        @Override public Organization getById(Long id) { return map.get(id); }
        @Override public Organization getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends Organization> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Organization> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
        @Override public <S extends Organization> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
        @Override public <S extends Organization> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends Organization> long count(Example<S> example) { return 0; }
        @Override public <S extends Organization> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Organization, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    }

    public static class MockAssetRepository implements AssetRepository {
        public final Map<Long, Asset> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(100);

        @Override public <S extends Asset> S save(S entity) {
            if (entity.getId() == null) entity.setId(seq.incrementAndGet());
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public Optional<Asset> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public Optional<Asset> findByAssetCode(String code) {
            return map.values().stream().filter(a -> code.equalsIgnoreCase(a.getAssetCode())).findFirst();
        }
        @Override public boolean existsByAssetCode(String code) {
            return map.values().stream().anyMatch(a -> code.equalsIgnoreCase(a.getAssetCode()));
        }
        @Override public Page<Asset> findByDepartmentId(Long deptId, Pageable pageable) {
            List<Asset> list = map.values().stream().filter(a -> a.getDepartment() != null && deptId.equals(a.getDepartment().getId())).toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public Page<Asset> findByWardId(Long wardId, Pageable pageable) {
            List<Asset> list = map.values().stream().filter(a -> a.getWard() != null && wardId.equals(a.getWard().getId())).toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public Page<Asset> findByAssetType(AssetType type, Pageable pageable) {
            List<Asset> list = map.values().stream().filter(a -> type == a.getAssetType()).toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public Page<Asset> findByStatus(AssetStatus status, Pageable pageable) {
            List<Asset> list = map.values().stream().filter(a -> status == a.getStatus()).toList();
            return new PageImpl<>(list, pageable, list.size());
        }

        @Override public Page<Asset> searchAssets(String keyword, AssetType assetType, AssetStatus status,
                                                  ConditionRating conditionRating, Long departmentId, Long wardId,
                                                  Boolean active, Pageable pageable) {
            List<Asset> filtered = map.values().stream()
                    .filter(a -> keyword == null || (
                            (a.getName() != null && a.getName().toLowerCase().contains(keyword.toLowerCase())) ||
                            (a.getAssetCode() != null && a.getAssetCode().toLowerCase().contains(keyword.toLowerCase())) ||
                            (a.getDescription() != null && a.getDescription().toLowerCase().contains(keyword.toLowerCase())) ||
                            (a.getAddressLine() != null && a.getAddressLine().toLowerCase().contains(keyword.toLowerCase())) ||
                            (a.getManufacturer() != null && a.getManufacturer().toLowerCase().contains(keyword.toLowerCase())) ||
                            (a.getModelNumber() != null && a.getModelNumber().toLowerCase().contains(keyword.toLowerCase())) ||
                            (a.getSerialNumber() != null && a.getSerialNumber().toLowerCase().contains(keyword.toLowerCase()))
                    ))
                    .filter(a -> assetType == null || a.getAssetType() == assetType)
                    .filter(a -> status == null || a.getStatus() == status)
                    .filter(a -> conditionRating == null || a.getConditionRating() == conditionRating)
                    .filter(a -> departmentId == null || (a.getDepartment() != null && departmentId.equals(a.getDepartment().getId())))
                    .filter(a -> wardId == null || (a.getWard() != null && wardId.equals(a.getWard().getId())))
                    .filter(a -> active == null || a.isActive() == active)
                    .toList();
            return new PageImpl<>(filtered, pageable, filtered.size());
        }

        @Override public List<Asset> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<Asset> findAll(Sort sort) { return new ArrayList<>(map.values()); }
        @Override public Page<Asset> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(map.values()), pageable, map.size()); }
        @Override public <S extends Asset> List<S> saveAll(Iterable<S> entities) {
            List<S> res = new ArrayList<>();
            entities.forEach(e -> res.add(save(e)));
            return res;
        }
        @Override public List<Asset> findAllById(Iterable<Long> ids) {
            List<Asset> res = new ArrayList<>();
            ids.forEach(id -> { if (map.containsKey(id)) res.add(map.get(id)); });
            return res;
        }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(Asset entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends Asset> entities) { entities.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends Asset> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Asset> List<S> saveAllAndFlush(Iterable<S> entities) { return saveAll(entities); }
        @Override public void deleteAllInBatch(Iterable<Asset> entities) { deleteAll(entities); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public Asset getOne(Long id) { return map.get(id); }
        @Override public Asset getById(Long id) { return map.get(id); }
        @Override public Asset getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends Asset> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Asset> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
        @Override public <S extends Asset> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
        @Override public <S extends Asset> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends Asset> long count(Example<S> example) { return 0; }
        @Override public <S extends Asset> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Asset, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    }

    public static class MockAssetInspectionRepository implements AssetInspectionRepository {
        public final Map<Long, AssetInspection> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(300);

        @Override public <S extends AssetInspection> S save(S entity) {
            if (entity.getId() == null) {
                try {
                    var idField = AssetInspection.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(entity, seq.incrementAndGet());
                } catch (Exception ignored) {}
            }
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public Optional<AssetInspection> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<AssetInspection> findByAssetIdOrderByInspectionDateDesc(Long assetId) {
            return map.values().stream()
                    .filter(i -> i.getAsset() != null && assetId.equals(i.getAsset().getId()))
                    .sorted((a, b) -> b.getInspectionDate().compareTo(a.getInspectionDate()))
                    .toList();
        }
        @Override public Page<AssetInspection> findByAssetId(Long assetId, Pageable pageable) {
            List<AssetInspection> list = findByAssetIdOrderByInspectionDateDesc(assetId);
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public List<AssetInspection> findByWorkOrderId(Long workOrderId) {
            return map.values().stream().filter(i -> i.getWorkOrder() != null && workOrderId.equals(i.getWorkOrder().getId())).toList();
        }
        @Override public Page<AssetInspection> findByInspectorId(Long inspectorId, Pageable pageable) {
            List<AssetInspection> list = map.values().stream().filter(i -> i.getInspector() != null && inspectorId.equals(i.getInspector().getId())).toList();
            return new PageImpl<>(list, pageable, list.size());
        }

        @Override public List<AssetInspection> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<AssetInspection> findAll(Sort sort) { return new ArrayList<>(map.values()); }
        @Override public Page<AssetInspection> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(map.values()), pageable, map.size()); }
        @Override public <S extends AssetInspection> List<S> saveAll(Iterable<S> entities) {
            List<S> res = new ArrayList<>();
            entities.forEach(e -> res.add(save(e)));
            return res;
        }
        @Override public List<AssetInspection> findAllById(Iterable<Long> ids) {
            List<AssetInspection> res = new ArrayList<>();
            ids.forEach(id -> { if (map.containsKey(id)) res.add(map.get(id)); });
            return res;
        }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(AssetInspection entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends AssetInspection> entities) { entities.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends AssetInspection> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends AssetInspection> List<S> saveAllAndFlush(Iterable<S> entities) { return saveAll(entities); }
        @Override public void deleteAllInBatch(Iterable<AssetInspection> entities) { deleteAll(entities); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public AssetInspection getOne(Long id) { return map.get(id); }
        @Override public AssetInspection getById(Long id) { return map.get(id); }
        @Override public AssetInspection getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends AssetInspection> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends AssetInspection> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
        @Override public <S extends AssetInspection> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
        @Override public <S extends AssetInspection> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends AssetInspection> long count(Example<S> example) { return 0; }
        @Override public <S extends AssetInspection> boolean exists(Example<S> example) { return false; }
        @Override public <S extends AssetInspection, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    }

    public static class MockAssetRepairRepository implements AssetRepairRepository {
        public final Map<Long, AssetRepair> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(400);

        @Override public <S extends AssetRepair> S save(S entity) {
            if (entity.getId() == null) {
                try {
                    var idField = AssetRepair.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(entity, seq.incrementAndGet());
                } catch (Exception ignored) {}
            }
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public Optional<AssetRepair> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<AssetRepair> findByAssetIdOrderByRepairDateDesc(Long assetId) {
            return map.values().stream()
                    .filter(r -> r.getAsset() != null && assetId.equals(r.getAsset().getId()))
                    .sorted((a, b) -> b.getRepairDate().compareTo(a.getRepairDate()))
                    .toList();
        }
        @Override public Page<AssetRepair> findByAssetId(Long assetId, Pageable pageable) {
            List<AssetRepair> list = findByAssetIdOrderByRepairDateDesc(assetId);
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public List<AssetRepair> findByWorkOrderId(Long workOrderId) {
            return map.values().stream().filter(r -> r.getWorkOrder() != null && workOrderId.equals(r.getWorkOrder().getId())).toList();
        }
        @Override public List<AssetRepair> findByComplaintId(Long complaintId) {
            return map.values().stream().filter(r -> r.getComplaint() != null && complaintId.equals(r.getComplaint().getId())).toList();
        }
        @Override public Page<AssetRepair> findByRepairedById(Long empId, Pageable pageable) {
            List<AssetRepair> list = map.values().stream().filter(r -> r.getRepairedBy() != null && empId.equals(r.getRepairedBy().getId())).toList();
            return new PageImpl<>(list, pageable, list.size());
        }

        @Override public List<AssetRepair> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<AssetRepair> findAll(Sort sort) { return new ArrayList<>(map.values()); }
        @Override public Page<AssetRepair> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(map.values()), pageable, map.size()); }
        @Override public <S extends AssetRepair> List<S> saveAll(Iterable<S> entities) {
            List<S> res = new ArrayList<>();
            entities.forEach(e -> res.add(save(e)));
            return res;
        }
        @Override public List<AssetRepair> findAllById(Iterable<Long> ids) {
            List<AssetRepair> res = new ArrayList<>();
            ids.forEach(id -> { if (map.containsKey(id)) res.add(map.get(id)); });
            return res;
        }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(AssetRepair entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends AssetRepair> entities) { entities.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends AssetRepair> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends AssetRepair> List<S> saveAllAndFlush(Iterable<S> entities) { return saveAll(entities); }
        @Override public void deleteAllInBatch(Iterable<AssetRepair> entities) { deleteAll(entities); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public AssetRepair getOne(Long id) { return map.get(id); }
        @Override public AssetRepair getById(Long id) { return map.get(id); }
        @Override public AssetRepair getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends AssetRepair> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends AssetRepair> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
        @Override public <S extends AssetRepair> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
        @Override public <S extends AssetRepair> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends AssetRepair> long count(Example<S> example) { return 0; }
        @Override public <S extends AssetRepair> boolean exists(Example<S> example) { return false; }
        @Override public <S extends AssetRepair, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    }

    public static class MockAssetReplacementRepository implements AssetReplacementRepository {
        public final Map<Long, AssetReplacement> map = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(500);

        @Override public <S extends AssetReplacement> S save(S entity) {
            if (entity.getId() == null) {
                try {
                    var idField = AssetReplacement.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(entity, seq.incrementAndGet());
                } catch (Exception ignored) {}
            }
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override public Optional<AssetReplacement> findById(Long id) { return Optional.ofNullable(map.get(id)); }
        @Override public boolean existsById(Long id) { return map.containsKey(id); }
        @Override public List<AssetReplacement> findByAssetIdOrderByReplacementDateDesc(Long assetId) {
            return map.values().stream()
                    .filter(r -> r.getAsset() != null && assetId.equals(r.getAsset().getId()))
                    .sorted((a, b) -> b.getReplacementDate().compareTo(a.getReplacementDate()))
                    .toList();
        }
        @Override public Page<AssetReplacement> findByAssetId(Long assetId, Pageable pageable) {
            List<AssetReplacement> list = findByAssetIdOrderByReplacementDateDesc(assetId);
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public List<AssetReplacement> findByReplacedAssetId(Long replacedAssetId) {
            return map.values().stream()
                    .filter(r -> r.getReplacedAsset() != null && replacedAssetId.equals(r.getReplacedAsset().getId()))
                    .toList();
        }

        @Override public List<AssetReplacement> findAll() { return new ArrayList<>(map.values()); }
        @Override public List<AssetReplacement> findAll(Sort sort) { return new ArrayList<>(map.values()); }
        @Override public Page<AssetReplacement> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(map.values()), pageable, map.size()); }
        @Override public <S extends AssetReplacement> List<S> saveAll(Iterable<S> entities) {
            List<S> res = new ArrayList<>();
            entities.forEach(e -> res.add(save(e)));
            return res;
        }
        @Override public List<AssetReplacement> findAllById(Iterable<Long> ids) {
            List<AssetReplacement> res = new ArrayList<>();
            ids.forEach(id -> { if (map.containsKey(id)) res.add(map.get(id)); });
            return res;
        }
        @Override public long count() { return map.size(); }
        @Override public void deleteById(Long id) { map.remove(id); }
        @Override public void delete(AssetReplacement entity) { map.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) { ids.forEach(map::remove); }
        @Override public void deleteAll(Iterable<? extends AssetReplacement> entities) { entities.forEach(e -> map.remove(e.getId())); }
        @Override public void deleteAll() { map.clear(); }
        @Override public void flush() {}
        @Override public <S extends AssetReplacement> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends AssetReplacement> List<S> saveAllAndFlush(Iterable<S> entities) { return saveAll(entities); }
        @Override public void deleteAllInBatch(Iterable<AssetReplacement> entities) { deleteAll(entities); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { deleteAllById(ids); }
        @Override public void deleteAllInBatch() { deleteAll(); }
        @Override public AssetReplacement getOne(Long id) { return map.get(id); }
        @Override public AssetReplacement getById(Long id) { return map.get(id); }
        @Override public AssetReplacement getReferenceById(Long id) { return map.get(id); }
        @Override public <S extends AssetReplacement> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends AssetReplacement> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
        @Override public <S extends AssetReplacement> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
        @Override public <S extends AssetReplacement> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
        @Override public <S extends AssetReplacement> long count(Example<S> example) { return 0; }
        @Override public <S extends AssetReplacement> boolean exists(Example<S> example) { return false; }
        @Override public <S extends AssetReplacement, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    }
}
