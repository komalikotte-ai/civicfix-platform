package com.civicfix.platform.organizations;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.departments.dto.*;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.entity.Team;
import com.civicfix.platform.departments.enums.DepartmentStatus;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.departments.repository.TeamRepository;
import com.civicfix.platform.departments.service.DepartmentService;
import com.civicfix.platform.departments.service.TeamService;
import com.civicfix.platform.organizations.dto.*;
import com.civicfix.platform.organizations.entity.City;
import com.civicfix.platform.organizations.entity.Municipality;
import com.civicfix.platform.organizations.enums.MunicipalityType;
import com.civicfix.platform.organizations.repository.CityRepository;
import com.civicfix.platform.organizations.repository.MunicipalityRepository;
import com.civicfix.platform.organizations.service.CityService;
import com.civicfix.platform.organizations.service.MunicipalityService;
import com.civicfix.platform.security.service.SecurityAuditService;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.repository.EmployeeRepository;
import com.civicfix.platform.wards.dto.CreateWardRequest;
import com.civicfix.platform.wards.dto.WardDto;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.wards.repository.WardRepository;
import com.civicfix.platform.wards.service.WardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationCrudTest {

    private CityService cityService;
    private MunicipalityService municipalityService;
    private WardService wardService;
    private DepartmentService departmentService;
    private TeamService teamService;

    private InMemoryCityRepository cityRepo;
    private InMemoryMunicipalityRepository municipalityRepo;
    private InMemoryWardRepository wardRepo;
    private InMemoryDepartmentRepository departmentRepo;
    private InMemoryTeamRepository teamRepo;
    private InMemoryEmployeeRepository employeeRepo;
    private DummyAuditService auditService;

    @BeforeEach
    void setUp() {
        cityRepo = new InMemoryCityRepository();
        municipalityRepo = new InMemoryMunicipalityRepository();
        wardRepo = new InMemoryWardRepository();
        departmentRepo = new InMemoryDepartmentRepository();
        teamRepo = new InMemoryTeamRepository();
        employeeRepo = new InMemoryEmployeeRepository();
        auditService = new DummyAuditService();

        cityService = new CityService(cityRepo, auditService);
        municipalityService = new MunicipalityService(municipalityRepo, cityRepo, auditService);
        wardService = new WardService(wardRepo, municipalityRepo, null, auditService);
        departmentService = new DepartmentService(departmentRepo, municipalityRepo, null, auditService);
        teamService = new TeamService(teamRepo, departmentRepo, employeeRepo, auditService);
    }

    @Test
    @DisplayName("City CRUD: successfully create, query and deactivate a City")
    void testCityCrud() {
        CreateCityRequest req = new CreateCityRequest();
        req.setName("Bengaluru");
        req.setCode("BLR");
        req.setState("Karnataka");

        CityDto created = cityService.createCity(req, "admin_user");
        assertNotNull(created.getId());
        assertEquals("Bengaluru", created.getName());
        assertEquals("BLR", created.getCode());
        assertTrue(created.isActive());

        // Duplicate code rejection
        CreateCityRequest duplicateReq = new CreateCityRequest();
        duplicateReq.setName("Bangalore City");
        duplicateReq.setCode("BLR");
        duplicateReq.setState("Karnataka");
        assertThrows(ConflictException.class, () -> cityService.createCity(duplicateReq, "admin_user"));

        // Retrieve by ID
        CityDto retrieved = cityService.getCityById(created.getId());
        assertEquals("Bengaluru", retrieved.getName());

        // Update
        UpdateCityRequest updateReq = new UpdateCityRequest();
        updateReq.setState("Karnataka State");
        CityDto updated = cityService.updateCity(created.getId(), updateReq, "admin_user");
        assertEquals("Karnataka State", updated.getState());

        // Soft delete
        cityService.deleteCity(created.getId(), "admin_user");
        CityDto deactivated = cityService.getCityById(created.getId());
        assertFalse(deactivated.isActive());
    }

    @Test
    @DisplayName("Municipality CRUD: create municipality linked to city and validate constraints")
    void testMunicipalityCrud() {
        // Seed City
        City city = new City("Mumbai", "BOM", "Maharashtra");
        city = cityRepo.save(city);

        CreateMunicipalityRequest req = new CreateMunicipalityRequest();
        req.setCityId(city.getId());
        req.setName("Brihanmumbai Municipal Corporation");
        req.setCode("BMC");
        req.setType(MunicipalityType.MUNICIPAL_CORPORATION);
        req.setContactEmail("commissioner@mcgm.gov.in");

        MunicipalityDto mDto = municipalityService.createMunicipality(req, "admin_user");
        assertNotNull(mDto.getId());
        assertEquals("BMC", mDto.getCode());
        assertEquals(city.getId(), mDto.getCityId());
        assertEquals("Mumbai", mDto.getCityName());
        assertEquals(MunicipalityType.MUNICIPAL_CORPORATION, mDto.getType());

        // Duplicate code throws ConflictException
        CreateMunicipalityRequest duplicateReq = new CreateMunicipalityRequest();
        duplicateReq.setCityId(city.getId());
        duplicateReq.setName("Another Corporation");
        duplicateReq.setCode("BMC");
        assertThrows(ConflictException.class, () -> municipalityService.createMunicipality(duplicateReq, "admin_user"));

        // Query by City
        List<MunicipalityDto> inCity = municipalityService.getActiveMunicipalitiesByCity(city.getId());
        assertEquals(1, inCity.size());
        assertEquals("BMC", inCity.get(0).getCode());
    }

    @Test
    @DisplayName("Ward CRUD: create ward with geographic metadata and zone")
    void testWardCrudWithGeoMetadata() {
        City city = cityRepo.save(new City("Delhi", "DEL", "Delhi"));
        Municipality m = municipalityRepo.save(new Municipality(city, "New Delhi Municipal Council", "NDMC", MunicipalityType.MUNICIPAL_COUNCIL));

        CreateWardRequest req = new CreateWardRequest();
        req.setMunicipalityId(m.getId());
        req.setWardNumber("W-01");
        req.setName("Connaught Place Central");
        req.setZoneName("Central Zone");
        req.setCenterLatitude(28.6315);
        req.setCenterLongitude(77.2167);
        req.setAreaSqKm(4.85);
        req.setPopulation(52000);
        req.setBoundaryGeoJson("{\"type\":\"Polygon\",\"coordinates\":[[[77.21,28.63],[77.22,28.63],[77.22,28.64],[77.21,28.63]]]}");

        WardDto ward = wardService.createWard(req, "admin_user");
        assertNotNull(ward.getId());
        assertEquals("W-01", ward.getWardNumber());
        assertEquals("Connaught Place Central", ward.getName());
        assertEquals(28.6315, ward.getCenterLatitude());
        assertEquals(77.2167, ward.getCenterLongitude());
        assertEquals(4.85, ward.getAreaSqKm());
        assertEquals(52000, ward.getPopulation());

        // Duplicate ward number in same municipality throws ConflictException
        CreateWardRequest dupReq = new CreateWardRequest();
        dupReq.setMunicipalityId(m.getId());
        dupReq.setWardNumber("W-01");
        dupReq.setName("Duplicate Ward");
        assertThrows(ConflictException.class, () -> wardService.createWard(dupReq, "admin_user"));
    }

    @Test
    @DisplayName("Department & Team CRUD: create department, transition status, and assign teams")
    void testDepartmentAndTeamCrud() {
        City city = cityRepo.save(new City("Bengaluru", "BLR", "Karnataka"));
        Municipality m = municipalityRepo.save(new Municipality(city, "BBMP", "BBMP", MunicipalityType.MUNICIPAL_CORPORATION));

        // Create Department
        CreateDepartmentRequest deptReq = new CreateDepartmentRequest();
        deptReq.setMunicipalityId(m.getId());
        deptReq.setName("Roads and Infrastructure");
        deptReq.setCode("ROADS");
        deptReq.setDescription("Maintenance and repair of arterial municipal roads");
        deptReq.setEscalationEmail("roads-escalation@bbmp.gov.in");

        DepartmentDto dept = departmentService.createDepartment(deptReq, "admin_user");
        assertNotNull(dept.getId());
        assertEquals("ROADS", dept.getCode());
        assertEquals(DepartmentStatus.ACTIVE, dept.getStatus());

        // Create Team
        CreateTeamRequest teamReq = new CreateTeamRequest();
        teamReq.setDepartmentId(dept.getId());
        teamReq.setName("Pothole Rapid Response Team 1");
        teamReq.setCode("TEAM-ROADS-PRR-1");
        teamReq.setDescription("Emergency road pothole patching unit");

        TeamDto team = teamService.createTeam(teamReq, "dept_manager");
        assertNotNull(team.getId());
        assertEquals("TEAM-ROADS-PRR-1", team.getCode());
        assertEquals(dept.getId(), team.getDepartmentId());

        // Update Department Status to SUSPENDED
        DepartmentDto suspended = departmentService.changeStatus(dept.getId(), DepartmentStatus.SUSPENDED, "admin_user");
        assertEquals(DepartmentStatus.SUSPENDED, suspended.getStatus());
    }

    // =========================================================================
    // Lightweight Test Doubles for Pure-Java Deterministic Execution
    // =========================================================================

    static class DummyAuditService extends SecurityAuditService {
        public DummyAuditService() { super(null); }
        @Override public void record(String entityName, String entityId, AuditAction action, String performedBy, String ipAddress, String details) {}
    }

    static class InMemoryCityRepository implements CityRepository {
        private final Map<Long, City> store = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override public Optional<City> findByCode(String code) {
            return store.values().stream().filter(c -> c.getCode().equalsIgnoreCase(code)).findFirst();
        }
        @Override public boolean existsByCode(String code) {
            return store.values().stream().anyMatch(c -> c.getCode().equalsIgnoreCase(code));
        }
        @Override public boolean existsByName(String name) {
            return store.values().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name));
        }
        @Override public Page<City> findAllByActiveTrue(Pageable pageable) {
            List<City> list = store.values().stream().filter(City::isActive).toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public List<City> findAllByActiveTrue() {
            return store.values().stream().filter(City::isActive).toList();
        }
        @Override public Page<City> searchCities(String query, Boolean active, Pageable pageable) {
            List<City> list = store.values().stream()
                    .filter(c -> active == null || c.isActive() == active)
                    .filter(c -> query == null || c.getName().toLowerCase().contains(query.toLowerCase()) || c.getCode().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public <S extends City> S save(S entity) {
            if (entity.getId() == null) entity.setId(idGen.getAndIncrement());
            store.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<City> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public boolean existsById(Long id) { return store.containsKey(id); }
        @Override public List<City> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<City> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(City entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends City> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public void flush() {}
        @Override public <S extends City> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends City> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<City> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public City getOne(Long aLong) { return store.get(aLong); }
        @Override public City getById(Long aLong) { return store.get(aLong); }
        @Override public City getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends City> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends City> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends City> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends City> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends City> long count(Example<S> example) { return 0; }
        @Override public <S extends City> boolean exists(Example<S> example) { return false; }
        @Override public <S extends City, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends City> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public List<City> findAll(Sort sort) { return new ArrayList<>(store.values()); }
        @Override public Page<City> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(store.values())); }
    }

    static class InMemoryMunicipalityRepository implements MunicipalityRepository {
        private final Map<Long, Municipality> store = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override public Optional<Municipality> findByCode(String code) {
            return store.values().stream().filter(m -> m.getCode().equalsIgnoreCase(code)).findFirst();
        }
        @Override public boolean existsByCode(String code) {
            return store.values().stream().anyMatch(m -> m.getCode().equalsIgnoreCase(code));
        }
        @Override public boolean existsByName(String name) {
            return store.values().stream().anyMatch(m -> m.getName().equalsIgnoreCase(name));
        }
        @Override public Page<Municipality> findByCityIdAndActiveTrue(Long cityId, Pageable pageable) {
            List<Municipality> list = store.values().stream().filter(m -> m.getCity() != null && m.getCity().getId().equals(cityId) && m.isActive()).toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public List<Municipality> findByCityIdAndActiveTrue(Long cityId) {
            return store.values().stream().filter(m -> m.getCity() != null && m.getCity().getId().equals(cityId) && m.isActive()).toList();
        }
        @Override public Page<Municipality> findAllByActiveTrue(Pageable pageable) {
            List<Municipality> list = store.values().stream().filter(Municipality::isActive).toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public Page<Municipality> searchMunicipalities(Long cityId, MunicipalityType type, Boolean active, String query, Pageable pageable) {
            List<Municipality> list = store.values().stream()
                    .filter(m -> cityId == null || (m.getCity() != null && m.getCity().getId().equals(cityId)))
                    .filter(m -> type == null || m.getType() == type)
                    .filter(m -> active == null || m.isActive() == active)
                    .filter(m -> query == null || m.getName().toLowerCase().contains(query.toLowerCase()) || m.getCode().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public <S extends Municipality> S save(S entity) {
            if (entity.getId() == null) entity.setId(idGen.getAndIncrement());
            store.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Municipality> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public boolean existsById(Long id) { return store.containsKey(id); }
        @Override public List<Municipality> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Municipality> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(Municipality entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Municipality> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public void flush() {}
        @Override public <S extends Municipality> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Municipality> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Municipality> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Municipality getOne(Long aLong) { return store.get(aLong); }
        @Override public Municipality getById(Long aLong) { return store.get(aLong); }
        @Override public Municipality getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends Municipality> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Municipality> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Municipality> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Municipality> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Municipality> long count(Example<S> example) { return 0; }
        @Override public <S extends Municipality> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Municipality, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Municipality> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public List<Municipality> findAll(Sort sort) { return new ArrayList<>(store.values()); }
        @Override public Page<Municipality> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(store.values())); }
    }

    static class InMemoryWardRepository implements WardRepository {
        private final Map<Long, Ward> store = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override public List<Ward> findByOrganizationIdAndActiveTrue(Long organizationId) { return List.of(); }
        @Override public Page<Ward> findByOrganizationId(Long organizationId, Pageable pageable) { return null; }
        @Override public Optional<Ward> findByOrganizationIdAndWardNumber(Long organizationId, String wardNumber) { return Optional.empty(); }
        @Override public boolean existsByOrganizationIdAndWardNumber(Long organizationId, String wardNumber) { return false; }
        @Override public List<Ward> findByMunicipalityIdAndActiveTrue(Long municipalityId) {
            return store.values().stream().filter(w -> w.getMunicipality() != null && w.getMunicipality().getId().equals(municipalityId) && w.isActive()).toList();
        }
        @Override public Page<Ward> findByMunicipalityId(Long municipalityId, Pageable pageable) {
            List<Ward> list = findByMunicipalityIdAndActiveTrue(municipalityId);
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public Optional<Ward> findByMunicipalityIdAndWardNumber(Long municipalityId, String wardNumber) {
            return store.values().stream().filter(w -> w.getMunicipality() != null && w.getMunicipality().getId().equals(municipalityId) && w.getWardNumber().equalsIgnoreCase(wardNumber)).findFirst();
        }
        @Override public boolean existsByMunicipalityIdAndWardNumber(Long municipalityId, String wardNumber) {
            return findByMunicipalityIdAndWardNumber(municipalityId, wardNumber).isPresent();
        }
        @Override public Page<Ward> searchWards(Long municipalityId, Long organizationId, String zoneName, Boolean active, String query, Pageable pageable) {
            List<Ward> list = store.values().stream()
                    .filter(w -> municipalityId == null || (w.getMunicipality() != null && w.getMunicipality().getId().equals(municipalityId)))
                    .filter(w -> active == null || w.isActive() == active)
                    .filter(w -> query == null || w.getName().toLowerCase().contains(query.toLowerCase()) || w.getWardNumber().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public <S extends Ward> S save(S entity) {
            if (entity.getId() == null) entity.setId(idGen.getAndIncrement());
            store.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Ward> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public boolean existsById(Long id) { return store.containsKey(id); }
        @Override public List<Ward> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Ward> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(Ward entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Ward> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public void flush() {}
        @Override public <S extends Ward> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Ward> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Ward> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Ward getOne(Long aLong) { return store.get(aLong); }
        @Override public Ward getById(Long aLong) { return store.get(aLong); }
        @Override public Ward getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends Ward> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Ward> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Ward> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Ward> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Ward> long count(Example<S> example) { return 0; }
        @Override public <S extends Ward> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Ward, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Ward> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public List<Ward> findAll(Sort sort) { return new ArrayList<>(store.values()); }
        @Override public Page<Ward> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(store.values())); }
    }

    static class InMemoryDepartmentRepository implements DepartmentRepository {
        private final Map<Long, Department> store = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override public List<Department> findByOrganizationIdAndActiveTrue(Long organizationId) { return List.of(); }
        @Override public Page<Department> findByOrganizationId(Long organizationId, Pageable pageable) { return null; }
        @Override public Optional<Department> findByOrganizationIdAndCode(Long organizationId, String code) { return Optional.empty(); }
        @Override public boolean existsByOrganizationIdAndCode(Long organizationId, String code) { return false; }
        @Override public List<Department> findByMunicipalityIdAndActiveTrue(Long municipalityId) {
            return store.values().stream().filter(d -> d.getMunicipality() != null && d.getMunicipality().getId().equals(municipalityId) && d.isActive()).toList();
        }
        @Override public Page<Department> findByMunicipalityId(Long municipalityId, Pageable pageable) {
            List<Department> list = findByMunicipalityIdAndActiveTrue(municipalityId);
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public Optional<Department> findByMunicipalityIdAndCode(Long municipalityId, String code) {
            return store.values().stream().filter(d -> d.getMunicipality() != null && d.getMunicipality().getId().equals(municipalityId) && d.getCode().equalsIgnoreCase(code)).findFirst();
        }
        @Override public boolean existsByMunicipalityIdAndCode(Long municipalityId, String code) {
            return findByMunicipalityIdAndCode(municipalityId, code).isPresent();
        }
        @Override public Optional<Department> findByCode(String code) {
            return store.values().stream().filter(d -> d.getCode().equalsIgnoreCase(code)).findFirst();
        }
        @Override public boolean existsByCode(String code) {
            return store.values().stream().anyMatch(d -> d.getCode().equalsIgnoreCase(code));
        }
        @Override public Page<Department> searchDepartments(Long municipalityId, DepartmentStatus status, Boolean active, String query, Pageable pageable) {
            List<Department> list = store.values().stream()
                    .filter(d -> municipalityId == null || (d.getMunicipality() != null && d.getMunicipality().getId().equals(municipalityId)))
                    .filter(d -> status == null || d.getStatus() == status)
                    .filter(d -> active == null || d.isActive() == active)
                    .filter(d -> query == null || d.getName().toLowerCase().contains(query.toLowerCase()) || d.getCode().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public <S extends Department> S save(S entity) {
            if (entity.getId() == null) entity.setId(idGen.getAndIncrement());
            store.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Department> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public boolean existsById(Long id) { return store.containsKey(id); }
        @Override public List<Department> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Department> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(Department entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Department> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public void flush() {}
        @Override public <S extends Department> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Department> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Department> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Department getOne(Long aLong) { return store.get(aLong); }
        @Override public Department getById(Long aLong) { return store.get(aLong); }
        @Override public Department getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends Department> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Department> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Department> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Department> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Department> long count(Example<S> example) { return 0; }
        @Override public <S extends Department> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Department, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Department> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public List<Department> findAll(Sort sort) { return new ArrayList<>(store.values()); }
        @Override public Page<Department> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(store.values())); }
    }

    static class InMemoryTeamRepository implements TeamRepository {
        private final Map<Long, Team> store = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override public List<Team> findByDepartmentIdAndActiveTrue(Long departmentId) {
            return store.values().stream().filter(t -> t.getDepartment().getId().equals(departmentId) && t.isActive()).toList();
        }
        @Override public Page<Team> findByDepartmentId(Long departmentId, Pageable pageable) {
            List<Team> list = findByDepartmentIdAndActiveTrue(departmentId);
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public Optional<Team> findByCode(String code) {
            return store.values().stream().filter(t -> t.getCode().equalsIgnoreCase(code)).findFirst();
        }
        @Override public boolean existsByCode(String code) {
            return store.values().stream().anyMatch(t -> t.getCode().equalsIgnoreCase(code));
        }
        @Override public boolean existsByDepartmentIdAndCode(Long departmentId, String code) {
            return store.values().stream().anyMatch(t -> t.getDepartment().getId().equals(departmentId) && t.getCode().equalsIgnoreCase(code));
        }
        @Override public List<Team> findBySupervisorIdAndActiveTrue(Long supervisorId) {
            return store.values().stream().filter(t -> t.getSupervisor() != null && t.getSupervisor().getId().equals(supervisorId) && t.isActive()).toList();
        }
        @Override public Page<Team> searchTeams(Long departmentId, Long supervisorId, Boolean active, String query, Pageable pageable) {
            List<Team> list = store.values().stream()
                    .filter(t -> departmentId == null || t.getDepartment().getId().equals(departmentId))
                    .filter(t -> supervisorId == null || (t.getSupervisor() != null && t.getSupervisor().getId().equals(supervisorId)))
                    .filter(t -> active == null || t.isActive() == active)
                    .filter(t -> query == null || t.getName().toLowerCase().contains(query.toLowerCase()) || t.getCode().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            return new PageImpl<>(list, pageable, list.size());
        }
        @Override public <S extends Team> S save(S entity) {
            if (entity.getId() == null) entity.setId(idGen.getAndIncrement());
            store.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Team> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public boolean existsById(Long id) { return store.containsKey(id); }
        @Override public List<Team> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Team> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(Team entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Team> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public void flush() {}
        @Override public <S extends Team> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Team> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Team> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Team getOne(Long aLong) { return store.get(aLong); }
        @Override public Team getById(Long aLong) { return store.get(aLong); }
        @Override public Team getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends Team> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Team> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Team> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Team> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Team> long count(Example<S> example) { return 0; }
        @Override public <S extends Team> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Team, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Team> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public List<Team> findAll(Sort sort) { return new ArrayList<>(store.values()); }
        @Override public Page<Team> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(store.values())); }
    }

    static class InMemoryEmployeeRepository implements EmployeeRepository {
        private final Map<Long, Employee> store = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override public Optional<Employee> findByEmployeeCode(String employeeCode) {
            return store.values().stream().filter(e -> e.getEmployeeCode().equalsIgnoreCase(employeeCode)).findFirst();
        }
        @Override public Optional<Employee> findByEmail(String email) {
            return store.values().stream().filter(e -> e.getEmail().equalsIgnoreCase(email)).findFirst();
        }
        @Override public Optional<Employee> findByUserId(Long userId) {
            return store.values().stream().filter(e -> e.getUser() != null && e.getUser().getId().equals(userId)).findFirst();
        }
        @Override public boolean existsByEmployeeCode(String employeeCode) {
            return store.values().stream().anyMatch(e -> e.getEmployeeCode().equalsIgnoreCase(employeeCode));
        }
        @Override public boolean existsByEmail(String email) {
            return store.values().stream().anyMatch(e -> e.getEmail().equalsIgnoreCase(email));
        }
        @Override public List<Employee> findByDepartmentIdAndActiveTrue(Long departmentId) { return List.of(); }
        @Override public List<Employee> findByTeamIdAndActiveTrue(Long teamId) { return List.of(); }
        @Override public List<Employee> findBySupervisorIdAndActiveTrue(Long supervisorId) { return List.of(); }
        @Override public Page<Employee> findAllByActiveTrue(Pageable pageable) { return null; }
        @Override public Page<Employee> searchEmployees(Long departmentId, Long teamId, Long supervisorId, com.civicfix.platform.users.enums.RoleType role, Boolean active, String query, Pageable pageable) { return null; }
        @Override public <S extends Employee> S save(S entity) {
            if (entity.getId() == null) entity.setId(idGen.getAndIncrement());
            store.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Employee> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public boolean existsById(Long id) { return store.containsKey(id); }
        @Override public List<Employee> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Employee> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(Employee entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Employee> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public void flush() {}
        @Override public <S extends Employee> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Employee> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Employee> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Employee getOne(Long aLong) { return store.get(aLong); }
        @Override public Employee getById(Long aLong) { return store.get(aLong); }
        @Override public Employee getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends Employee> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Employee> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Employee> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Employee> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Employee> long count(Example<S> example) { return 0; }
        @Override public <S extends Employee> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Employee, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Employee> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public List<Employee> findAll(Sort sort) { return new ArrayList<>(store.values()); }
        @Override public Page<Employee> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(store.values())); }
    }
}
