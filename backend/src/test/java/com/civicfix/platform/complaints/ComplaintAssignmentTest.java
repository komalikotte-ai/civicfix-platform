package com.civicfix.platform.complaints;

import com.civicfix.platform.common.exception.ForbiddenException;
import com.civicfix.platform.complaints.dto.AssignComplaintRequest;
import com.civicfix.platform.complaints.dto.ComplaintDto;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.entity.ComplaintAssignmentHistory;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintSource;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.complaints.service.ComplaintService;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.security.UserPrincipal;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.workflow.service.WorkflowEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ComplaintAssignmentTest {

    private ComplaintService complaintService;

    private ComplaintTestMocks.MockComplaintRepository complaintRepo;
    private ComplaintTestMocks.MockAssignmentHistoryRepository historyRepo;
    private ComplaintTestMocks.MockEmployeeRepository employeeRepo;
    private ComplaintTestMocks.MockDepartmentRepository departmentRepo;
    private ComplaintTestMocks.MockUserRepository userRepo;

    private User adminUser;
    private User citizenUser;
    private User employeeUser;
    private Department roadsDept;
    private Department sanitationDept;
    private Employee workerRamesh;
    private Employee workerSuresh;
    private Employee supervisorVerma;
    private Complaint testComplaint;

    @BeforeEach
    void setUp() {
        complaintRepo = new ComplaintTestMocks.MockComplaintRepository();
        historyRepo = new ComplaintTestMocks.MockAssignmentHistoryRepository();
        employeeRepo = new ComplaintTestMocks.MockEmployeeRepository();
        departmentRepo = new ComplaintTestMocks.MockDepartmentRepository();
        userRepo = new ComplaintTestMocks.MockUserRepository();
        ComplaintTestMocks.MockStatusHistoryRepository statusHistoryRepo = new ComplaintTestMocks.MockStatusHistoryRepository();
        ComplaintTestMocks.MockWorkflowTransitionRepository transitionRepo = new ComplaintTestMocks.MockWorkflowTransitionRepository();

        WorkflowEngine workflowEngine = new WorkflowEngine(transitionRepo);

        complaintService = new ComplaintService(
                complaintRepo,
                new ComplaintTestMocks.MockWardRepository(),
                departmentRepo,
                userRepo,
                employeeRepo,
                new ComplaintTestMocks.MockAttachmentRepository(),
                new ComplaintTestMocks.MockCommentRepository(),
                historyRepo,
                statusHistoryRepo,
                workflowEngine,
                new ComplaintTestMocks.MockSlaService(),
                new ComplaintTestMocks.MockAuditService(),
                new ComplaintTestMocks.MockEventPublisher()
        );

        // Seed Users
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setFullName("Platform Admin");
        userRepo.save(adminUser);

        citizenUser = new User();
        citizenUser.setId(2L);
        citizenUser.setUsername("citizen_anil");
        citizenUser.setFullName("Anil Deshmukh");
        userRepo.save(citizenUser);

        employeeUser = new User();
        employeeUser.setId(3L);
        employeeUser.setUsername("worker_ramesh");
        employeeUser.setFullName("Ramesh Kumar");
        userRepo.save(employeeUser);

        // Seed Departments
        roadsDept = new Department();
        roadsDept.setId(10L);
        roadsDept.setName("Roads & Infrastructure");
        roadsDept.setCode("ROADS");
        departmentRepo.save(roadsDept);

        sanitationDept = new Department();
        sanitationDept.setId(20L);
        sanitationDept.setName("Solid Waste Management");
        sanitationDept.setCode("SANITATION");
        departmentRepo.save(sanitationDept);

        // Seed Employees
        workerRamesh = new Employee();
        workerRamesh.setId(101L);
        workerRamesh.setEmployeeCode("EMP-RD-01");
        workerRamesh.setFullName("Ramesh Kumar");
        workerRamesh.setEmail("ramesh@civicfix.gov.in");
        workerRamesh.setUser(employeeUser);
        workerRamesh.setDepartment(roadsDept);
        workerRamesh.setRole(RoleType.ROLE_EMPLOYEE);
        employeeRepo.save(workerRamesh);

        workerSuresh = new Employee();
        workerSuresh.setId(102L);
        workerSuresh.setEmployeeCode("EMP-RD-02");
        workerSuresh.setFullName("Suresh Patel");
        workerSuresh.setEmail("suresh@civicfix.gov.in");
        workerSuresh.setDepartment(roadsDept);
        workerSuresh.setRole(RoleType.ROLE_EMPLOYEE);
        employeeRepo.save(workerSuresh);

        supervisorVerma = new Employee();
        supervisorVerma.setId(103L);
        supervisorVerma.setEmployeeCode("EMP-SUP-01");
        supervisorVerma.setFullName("Vikram Verma");
        supervisorVerma.setEmail("verma@civicfix.gov.in");
        supervisorVerma.setDepartment(roadsDept);
        supervisorVerma.setRole(RoleType.ROLE_SUPERVISOR);
        employeeRepo.save(supervisorVerma);

        // Seed Complaint in NEW status
        testComplaint = new Complaint();
        testComplaint.setId(500L);
        testComplaint.setTrackingNumber("CF-20261028-ASSIGN1");
        testComplaint.setTitle("Broken pavement");
        testComplaint.setDescription("Broken pavement tiles outside public park");
        testComplaint.setCategory("POTHOLE");
        testComplaint.setStatus(ComplaintStatus.NEW);
        testComplaint.setCitizen(citizenUser);
        testComplaint.setDepartment(roadsDept);
        complaintRepo.save(testComplaint);

        setSecurityContext(adminUser, "ROLE_ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(User user, String... roles) {
        Set<String> roleSet = new HashSet<>(Arrays.asList(roles));
        List<SimpleGrantedAuthority> authorities = roleSet.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        UserPrincipal principal = new UserPrincipal(
                user.getId(), user.getUsername(), user.getUsername() + "@civicfix.gov.in",
                "secret", 1L, 10L, 1L, true, false, true, roleSet, Set.of(), authorities
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should assign complaint to employee and supervisor, auto-transitioning status from NEW to ASSIGNED")
    void shouldAssignComplaintAndTransitionStatus() {
        AssignComplaintRequest request = new AssignComplaintRequest();
        request.setDepartmentId(roadsDept.getId());
        request.setAssignedEmployeeId(workerRamesh.getId());
        request.setAssignedSupervisorId(supervisorVerma.getId());
        request.setReason("Allocated to Ward 1 asphalt repair team");

        ComplaintDto assigned = complaintService.assignComplaint(testComplaint.getId(), request, "admin");

        assertNotNull(assigned);
        assertEquals(ComplaintStatus.ASSIGNED, assigned.getStatus());
        assertEquals(workerRamesh.getId(), assigned.getAssignedEmployeeId());
        assertEquals("Ramesh Kumar", assigned.getAssignedEmployeeName());
        assertEquals(supervisorVerma.getId(), assigned.getAssignedSupervisorId());
        assertEquals("Vikram Verma", assigned.getAssignedSupervisorName());
        assertNotNull(assigned.getAssignedAt());

        // Check assignment history recorded
        List<ComplaintAssignmentHistory> history = historyRepo.findByComplaintIdOrderByAssignedAtDesc(testComplaint.getId());
        assertEquals(1, history.size());
        assertEquals("Allocated to Ward 1 asphalt repair team", history.get(0).getReason());
        assertEquals("admin", history.get(0).getAssignedBy());
        assertEquals(workerRamesh.getId(), history.get(0).getNewAssignee().getId());
        assertNull(history.get(0).getPreviousAssignee());
    }

    @Test
    @DisplayName("Should reassign complaint to another worker and record previous/new assignee in history")
    void shouldReassignComplaintToAnotherWorker() {
        // Step 1: Initial assignment
        AssignComplaintRequest initialReq = new AssignComplaintRequest(roadsDept.getId(), workerRamesh.getId(), supervisorVerma.getId(), "Initial assignment");
        complaintService.assignComplaint(testComplaint.getId(), initialReq, "admin");

        // Step 2: Reassign to Suresh
        AssignComplaintRequest reassignReq = new AssignComplaintRequest(roadsDept.getId(), workerSuresh.getId(), supervisorVerma.getId(), "Workload re-balancing");
        ComplaintDto reassigned = complaintService.assignComplaint(testComplaint.getId(), reassignReq, "admin");

        assertEquals(workerSuresh.getId(), reassigned.getAssignedEmployeeId());
        assertEquals("Suresh Patel", reassigned.getAssignedEmployeeName());

        // Check 2 history records
        List<ComplaintAssignmentHistory> history = historyRepo.findByComplaintIdOrderByAssignedAtDesc(testComplaint.getId());
        assertEquals(2, history.size());

        ComplaintAssignmentHistory latest = history.get(1);
        assertEquals(workerRamesh.getId(), latest.getPreviousAssignee().getId());
        assertEquals(workerSuresh.getId(), latest.getNewAssignee().getId());
        assertEquals("Workload re-balancing", latest.getReason());
    }

    @Test
    @DisplayName("Should reject complaint assignment when requested by a citizen")
    void shouldRejectAssignmentByCitizen() {
        setSecurityContext(citizenUser, "ROLE_CITIZEN");

        AssignComplaintRequest request = new AssignComplaintRequest(roadsDept.getId(), workerRamesh.getId(), null, "Citizen trying to assign");

        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
                complaintService.assignComplaint(testComplaint.getId(), request, "citizen_anil"));

        assertTrue(exception.getMessage().contains("Only administrators and department supervisors can assign complaints"));
    }
}
