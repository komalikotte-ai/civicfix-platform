package com.civicfix.platform.users;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.entity.Team;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.departments.repository.TeamRepository;
import com.civicfix.platform.security.service.SecurityAuditService;
import com.civicfix.platform.users.dto.AssignEmployeeRequest;
import com.civicfix.platform.users.dto.CreateEmployeeRequest;
import com.civicfix.platform.users.dto.EmployeeAssignmentHistoryDto;
import com.civicfix.platform.users.dto.EmployeeDto;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.EmployeeAssignmentHistory;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.users.repository.EmployeeAssignmentHistoryRepository;
import com.civicfix.platform.users.repository.EmployeeRepository;
import com.civicfix.platform.users.repository.UserRepository;
import com.civicfix.platform.users.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeAssignmentTest {

    private EmployeeService employeeService;

    private InMemoryEmployeeRepository employeeRepo;
    private InMemoryHistoryRepository historyRepo;
    private InMemoryDepartmentRepository departmentRepo;
    private InMemoryTeamRepository teamRepo;
    private DummyAuditService auditService;

    private Department sanitationDept;
    private Department roadsDept;
    private Team potholeTeam;
    private Team wasteCollectionTeam;
    private Employee supervisorSharma;

    @BeforeEach
    void setUp() {
        employeeRepo = new InMemoryEmployeeRepository();
        historyRepo = new InMemoryHistoryRepository();
        departmentRepo = new InMemoryDepartmentRepository();
        teamRepo = new InMemoryTeamRepository();
        auditService = new DummyAuditService();

        employeeService = new EmployeeService(employeeRepo, historyRepo, departmentRepo, teamRepo, null, auditService);

        // Seed Departments
        sanitationDept = new Department();
        sanitationDept.setName("Solid Waste Management");
        sanitationDept.setCode("SANITATION");
        sanitationDept = departmentRepo.save(sanitationDept);

        roadsDept = new Department();
        roadsDept.setName("Roads & Bridges");
        roadsDept.setCode("ROADS");
        roadsDept = departmentRepo.save(roadsDept);

        // Seed Teams
        potholeTeam = new Team(roadsDept, "Central Pothole Squad", "TEAM-ROADS-CPS");
        potholeTeam = teamRepo.save(potholeTeam);

        wasteCollectionTeam = new Team(sanitationDept, "Zone 1 Waste Collection", "TEAM-SWM-Z1");
        wasteCollectionTeam = teamRepo.save(wasteCollectionTeam);

        // Seed Supervisor Employee
        supervisorSharma = new Employee("EMP-SUP-01", "Ramesh Sharma", "r.sharma@civicfix.gov.in", roadsDept, RoleType.ROLE_SUPERVISOR);
        supervisorSharma = employeeRepo.save(supervisorSharma);
    }

    @Test
    @DisplayName("Employee Creation: creates employee with initial assignment history")
    void testCreateEmployeeWithInitialAssignment() {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setEmployeeCode("EMP-FW-101");
        req.setFullName("Anil Kumar");
        req.setEmail("anil.kumar@civicfix.gov.in");
        req.setPhoneNumber("+91-9876543210");
        req.setDesignation("Junior Road Maintenance Worker");
        req.setDepartmentId(roadsDept.getId());
        req.setTeamId(potholeTeam.getId());
        req.setSupervisorId(supervisorSharma.getId());
        req.setRole(RoleType.ROLE_FIELD_WORKER);

        EmployeeDto employee = employeeService.createEmployee(req, "admin_user");
        assertNotNull(employee.getId());
        assertEquals("EMP-FW-101", employee.getEmployeeCode());
        assertEquals("Anil Kumar", employee.getFullName());
        assertEquals(roadsDept.getId(), employee.getDepartmentId());
        assertEquals("Roads & Bridges", employee.getDepartmentName());
        assertEquals(potholeTeam.getId(), employee.getTeamId());
        assertEquals(supervisorSharma.getId(), employee.getSupervisorId());
        assertEquals("Ramesh Sharma", employee.getSupervisorName());
        assertEquals(RoleType.ROLE_FIELD_WORKER, employee.getRole());

        // Verify initial assignment history was recorded
        List<EmployeeAssignmentHistoryDto> history = employeeService.getAssignmentHistory(employee.getId());
        assertEquals(1, history.size());
        EmployeeAssignmentHistoryDto initialRecord = history.get(0);
        assertNull(initialRecord.getPreviousDepartmentId());
        assertEquals(roadsDept.getId(), initialRecord.getNewDepartmentId());
        assertEquals(potholeTeam.getId(), initialRecord.getNewTeamId());
        assertEquals(supervisorSharma.getId(), initialRecord.getNewSupervisorId());
        assertEquals("admin_user", initialRecord.getAssignedBy());
        assertEquals("Initial Department Assignment", initialRecord.getReason());
    }

    @Test
    @DisplayName("Department Assignment: reassign employee to another department/team and verify full history audit trail")
    void testReassignDepartmentAndTeam() {
        // Create employee in Roads dept
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setEmployeeCode("EMP-202");
        req.setFullName("Sunita Devi");
        req.setEmail("sunita.devi@civicfix.gov.in");
        req.setDepartmentId(roadsDept.getId());
        req.setTeamId(potholeTeam.getId());
        req.setSupervisorId(supervisorSharma.getId());
        EmployeeDto employee = employeeService.createEmployee(req, "admin_user");

        // Seed a new supervisor in Sanitation dept
        Employee supervisorPatil = new Employee("EMP-SUP-02", "Suresh Patil", "s.patil@civicfix.gov.in", sanitationDept, RoleType.ROLE_SUPERVISOR);
        supervisorPatil = employeeRepo.save(supervisorPatil);

        // Reassign from Roads to Sanitation
        AssignEmployeeRequest assignReq = new AssignEmployeeRequest();
        assignReq.setNewDepartmentId(sanitationDept.getId());
        assignReq.setNewTeamId(wasteCollectionTeam.getId());
        assignReq.setNewSupervisorId(supervisorPatil.getId());
        assignReq.setReason("Transferred to Solid Waste Management for monsoon preparation");

        EmployeeDto reassigned = employeeService.assignDepartmentTeamSupervisor(employee.getId(), assignReq, "dept_manager");

        assertEquals(sanitationDept.getId(), reassigned.getDepartmentId());
        assertEquals("Solid Waste Management", reassigned.getDepartmentName());
        assertEquals(wasteCollectionTeam.getId(), reassigned.getTeamId());
        assertEquals(supervisorPatil.getId(), reassigned.getSupervisorId());
        assertEquals("Suresh Patil", reassigned.getSupervisorName());

        // Verify history has 2 records (latest first)
        List<EmployeeAssignmentHistoryDto> history = employeeService.getAssignmentHistory(employee.getId());
        assertEquals(2, history.size());

        EmployeeAssignmentHistoryDto latestRecord = history.get(0);
        assertEquals(roadsDept.getId(), latestRecord.getPreviousDepartmentId());
        assertEquals("Roads & Bridges", latestRecord.getPreviousDepartmentName());
        assertEquals(sanitationDept.getId(), latestRecord.getNewDepartmentId());
        assertEquals("Solid Waste Management", latestRecord.getNewDepartmentName());
        assertEquals(potholeTeam.getId(), latestRecord.getPreviousTeamId());
        assertEquals(wasteCollectionTeam.getId(), latestRecord.getNewTeamId());
        assertEquals(supervisorSharma.getId(), latestRecord.getPreviousSupervisorId());
        assertEquals(supervisorPatil.getId(), latestRecord.getNewSupervisorId());
        assertEquals("dept_manager", latestRecord.getAssignedBy());
        assertEquals("Transferred to Solid Waste Management for monsoon preparation", latestRecord.getReason());
        assertNull(latestRecord.getEffectiveTo()); // current active assignment
    }

    @Test
    @DisplayName("Validation: self-supervision is rejected")
    void testSelfSupervisionRejected() {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setEmployeeCode("EMP-303");
        req.setFullName("Kavita Rao");
        req.setEmail("kavita.rao@civicfix.gov.in");
        req.setDepartmentId(roadsDept.getId());
        EmployeeDto emp = employeeService.createEmployee(req, "admin_user");

        AssignEmployeeRequest selfSupervisorReq = new AssignEmployeeRequest();
        selfSupervisorReq.setNewDepartmentId(roadsDept.getId());
        selfSupervisorReq.setNewSupervisorId(emp.getId()); // Self
        selfSupervisorReq.setReason("Invalid self assignment");

        assertThrows(ConflictException.class, () ->
                employeeService.assignDepartmentTeamSupervisor(emp.getId(), selfSupervisorReq, "admin_user")
        );
    }

    @Test
    @DisplayName("Validation: team from mismatched department is rejected")
    void testMismatchedDepartmentTeamRejected() {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setEmployeeCode("EMP-404");
        req.setFullName("Deepak Verma");
        req.setEmail("deepak.verma@civicfix.gov.in");
        req.setDepartmentId(roadsDept.getId());
        EmployeeDto emp = employeeService.createEmployee(req, "admin_user");

        AssignEmployeeRequest mismatchedReq = new AssignEmployeeRequest();
        mismatchedReq.setNewDepartmentId(roadsDept.getId());
        mismatchedReq.setNewTeamId(wasteCollectionTeam.getId()); // wasteCollectionTeam belongs to sanitationDept!
        mismatchedReq.setReason("Invalid mismatched team");

        assertThrows(ConflictException.class, () ->
                employeeService.assignDepartmentTeamSupervisor(emp.getId(), mismatchedReq, "admin_user")
        );
    }

    // =========================================================================
    // Lightweight Test Doubles
    // =========================================================================

    static class DummyAuditService extends SecurityAuditService {
        public DummyAuditService() { super(null); }
        @Override public void record(String entityName, String entityId, AuditAction action, String performedBy, String ipAddress, String details) {}
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
        @Override public List<Employee> findByDepartmentIdAndActiveTrue(Long departmentId) {
            return store.values().stream().filter(e -> e.getDepartment().getId().equals(departmentId) && e.isActive()).toList();
        }
        @Override public List<Employee> findByTeamIdAndActiveTrue(Long teamId) {
            return store.values().stream().filter(e -> e.getTeam() != null && e.getTeam().getId().equals(teamId) && e.isActive()).toList();
        }
        @Override public List<Employee> findBySupervisorIdAndActiveTrue(Long supervisorId) {
            return store.values().stream().filter(e -> e.getSupervisor() != null && e.getSupervisor().getId().equals(supervisorId) && e.isActive()).toList();
        }
        @Override public Page<Employee> findAllByActiveTrue(Pageable pageable) { return null; }
        @Override public Page<Employee> searchEmployees(Long departmentId, Long teamId, Long supervisorId, RoleType role, Boolean active, String query, Pageable pageable) { return null; }
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

    static class InMemoryHistoryRepository implements EmployeeAssignmentHistoryRepository {
        private final Map<Long, EmployeeAssignmentHistory> store = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override public List<EmployeeAssignmentHistory> findByEmployeeIdOrderByEffectiveFromDesc(Long employeeId) {
            return store.values().stream()
                    .filter(h -> h.getEmployee().getId().equals(employeeId))
                    .sorted((a, b) -> b.getEffectiveFrom().compareTo(a.getEffectiveFrom()))
                    .toList();
        }
        @Override public Optional<EmployeeAssignmentHistory> findActiveAssignment(Long employeeId) {
            return store.values().stream()
                    .filter(h -> h.getEmployee().getId().equals(employeeId) && h.getEffectiveTo() == null)
                    .findFirst();
        }
        @Override public Page<EmployeeAssignmentHistory> findByNewDepartmentId(Long departmentId, Pageable pageable) { return null; }
        @Override public Page<EmployeeAssignmentHistory> findByNewTeamId(Long teamId, Pageable pageable) { return null; }
        @Override public <S extends EmployeeAssignmentHistory> S save(S entity) {
            if (entity.getId() == null) entity.setId(idGen.getAndIncrement());
            store.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<EmployeeAssignmentHistory> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public boolean existsById(Long id) { return store.containsKey(id); }
        @Override public List<EmployeeAssignmentHistory> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<EmployeeAssignmentHistory> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(EmployeeAssignmentHistory entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends EmployeeAssignmentHistory> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public void flush() {}
        @Override public <S extends EmployeeAssignmentHistory> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends EmployeeAssignmentHistory> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<EmployeeAssignmentHistory> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public EmployeeAssignmentHistory getOne(Long aLong) { return store.get(aLong); }
        @Override public EmployeeAssignmentHistory getById(Long aLong) { return store.get(aLong); }
        @Override public EmployeeAssignmentHistory getReferenceById(Long aLong) { return store.get(aLong); }
        @Override public <S extends EmployeeAssignmentHistory> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends EmployeeAssignmentHistory> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends EmployeeAssignmentHistory> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends EmployeeAssignmentHistory> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends EmployeeAssignmentHistory> long count(Example<S> example) { return 0; }
        @Override public <S extends EmployeeAssignmentHistory> boolean exists(Example<S> example) { return false; }
        @Override public <S extends EmployeeAssignmentHistory, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends EmployeeAssignmentHistory> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public List<EmployeeAssignmentHistory> findAll(Sort sort) { return new ArrayList<>(store.values()); }
        @Override public Page<EmployeeAssignmentHistory> findAll(Pageable pageable) { return new PageImpl<>(new ArrayList<>(store.values())); }
    }

    static class InMemoryDepartmentRepository implements DepartmentRepository {
        private final Map<Long, Department> store = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override public List<Department> findByOrganizationIdAndActiveTrue(Long organizationId) { return List.of(); }
        @Override public Page<Department> findByOrganizationId(Long organizationId, Pageable pageable) { return null; }
        @Override public Optional<Department> findByOrganizationIdAndCode(Long organizationId, String code) { return Optional.empty(); }
        @Override public boolean existsByOrganizationIdAndCode(Long organizationId, String code) { return false; }
        @Override public List<Department> findByMunicipalityIdAndActiveTrue(Long municipalityId) { return List.of(); }
        @Override public Page<Department> findByMunicipalityId(Long municipalityId, Pageable pageable) { return null; }
        @Override public Optional<Department> findByMunicipalityIdAndCode(Long municipalityId, String code) { return Optional.empty(); }
        @Override public boolean existsByMunicipalityIdAndCode(Long municipalityId, String code) { return false; }
        @Override public Optional<Department> findByCode(String code) { return store.values().stream().filter(d -> d.getCode().equalsIgnoreCase(code)).findFirst(); }
        @Override public boolean existsByCode(String code) { return store.values().stream().anyMatch(d -> d.getCode().equalsIgnoreCase(code)); }
        @Override public Page<Department> searchDepartments(Long municipalityId, com.civicfix.platform.departments.enums.DepartmentStatus status, Boolean active, String query, Pageable pageable) { return null; }
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

        @Override public List<Team> findByDepartmentIdAndActiveTrue(Long departmentId) { return List.of(); }
        @Override public Page<Team> findByDepartmentId(Long departmentId, Pageable pageable) { return null; }
        @Override public Optional<Team> findByCode(String code) { return store.values().stream().filter(t -> t.getCode().equalsIgnoreCase(code)).findFirst(); }
        @Override public boolean existsByCode(String code) { return store.values().stream().anyMatch(t -> t.getCode().equalsIgnoreCase(code)); }
        @Override public boolean existsByDepartmentIdAndCode(Long departmentId, String code) { return false; }
        @Override public List<Team> findBySupervisorIdAndActiveTrue(Long supervisorId) { return List.of(); }
        @Override public Page<Team> searchTeams(Long departmentId, Long supervisorId, Boolean active, String query, Pageable pageable) { return null; }
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
}
