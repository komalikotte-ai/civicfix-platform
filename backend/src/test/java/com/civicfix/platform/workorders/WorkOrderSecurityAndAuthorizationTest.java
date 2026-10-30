package com.civicfix.platform.workorders;

import com.civicfix.platform.common.exception.ForbiddenException;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.security.UserPrincipal;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.workorders.controller.WorkOrderController;
import com.civicfix.platform.workorders.dto.AssignWorkOrderRequest;
import com.civicfix.platform.workorders.dto.CreateWorkOrderRequest;
import com.civicfix.platform.workorders.dto.VerifyWorkOrderRequest;
import com.civicfix.platform.workorders.dto.WorkOrderDto;
import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import com.civicfix.platform.workorders.service.WorkOrderSchedulingService;
import com.civicfix.platform.workorders.service.WorkOrderService;
import com.civicfix.platform.workorders.service.WorkOrderWorkflowEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorkOrderSecurityAndAuthorizationTest {

    private WorkOrderController controller;

    private WorkOrderTestMocks.MockWorkOrderRepository workOrderRepo;
    private ComplaintTestMocks.MockEmployeeRepository employeeRepo;
    private ComplaintTestMocks.MockUserRepository userRepo;

    private Department department;
    private Employee workerEmployee;
    private Employee otherWorkerEmployee;
    private User workerUser;
    private User citizenUser;
    private User supervisorUser;
    private User adminUser;
    private WorkOrder assignedWorkOrder;

    @BeforeEach
    void setUp() {
        workOrderRepo = new WorkOrderTestMocks.MockWorkOrderRepository();
        employeeRepo = new ComplaintTestMocks.MockEmployeeRepository();
        userRepo = new ComplaintTestMocks.MockUserRepository();
        WorkOrderTestMocks.MockWorkOrderDependencyRepository dependencyRepo = new WorkOrderTestMocks.MockWorkOrderDependencyRepository();
        WorkOrderTestMocks.MockWorkOrderStatusHistoryRepository statusHistoryRepo = new WorkOrderTestMocks.MockWorkOrderStatusHistoryRepository();
        WorkOrderTestMocks.MockWorkOrderAssignmentHistoryRepository assignmentHistoryRepo = new WorkOrderTestMocks.MockWorkOrderAssignmentHistoryRepository();
        WorkOrderTestMocks.MockWorkOrderCommentRepository commentRepo = new WorkOrderTestMocks.MockWorkOrderCommentRepository();
        ComplaintTestMocks.MockDepartmentRepository departmentRepo = new ComplaintTestMocks.MockDepartmentRepository();

        Organization org = new Organization();
        org.setId(1L);
        org.setName("CivicFix Municipal");
        org.setCode("CFM");

        department = new Department(org, "Sanitation", "SAN");
        department.setId(10L);
        departmentRepo.save(department);

        // Worker User & Employee
        workerUser = new User();
        workerUser.setId(101L);
        workerUser.setUsername("worker_ram");
        workerUser.setFullName("Ram Field Worker");
        workerUser.setRoles(Set.of(new Role(RoleType.ROLE_FIELD_WORKER, "Field Worker")));
        userRepo.save(workerUser);

        workerEmployee = new Employee("FW-101", "Ram Field Worker", "ram@civicfix.gov", department, RoleType.ROLE_FIELD_WORKER);
        workerEmployee.setId(501L);
        workerEmployee.setUser(workerUser);
        employeeRepo.save(workerEmployee);

        // Other Worker Employee
        otherWorkerEmployee = new Employee("FW-102", "Other Worker", "other@civicfix.gov", department, RoleType.ROLE_FIELD_WORKER);
        otherWorkerEmployee.setId(502L);
        employeeRepo.save(otherWorkerEmployee);

        // Supervisor User
        supervisorUser = new User();
        supervisorUser.setId(102L);
        supervisorUser.setUsername("supervisor_vijay");
        supervisorUser.setFullName("Vijay Supervisor");
        supervisorUser.setRoles(Set.of(new Role(RoleType.ROLE_SUPERVISOR, "Supervisor")));
        userRepo.save(supervisorUser);

        // Admin User
        adminUser = new User();
        adminUser.setId(103L);
        adminUser.setUsername("admin_kavita");
        adminUser.setFullName("Kavita Admin");
        adminUser.setRoles(Set.of(new Role(RoleType.ROLE_ADMIN, "Admin")));
        userRepo.save(adminUser);

        // Citizen User
        citizenUser = new User();
        citizenUser.setId(104L);
        citizenUser.setUsername("citizen_anil");
        citizenUser.setFullName("Anil Citizen");
        citizenUser.setRoles(Set.of(new Role(RoleType.ROLE_CITIZEN, "Citizen")));
        userRepo.save(citizenUser);

        WorkOrderWorkflowEngine workflowEngine = new WorkOrderWorkflowEngine();
        WorkOrderSchedulingService schedulingService = new WorkOrderSchedulingService(dependencyRepo, workOrderRepo);
        WorkOrderTestMocks.MockAuditService auditService = new WorkOrderTestMocks.MockAuditService();

        WorkOrderService workOrderService = new WorkOrderService(
                workOrderRepo, statusHistoryRepo, assignmentHistoryRepo, commentRepo,
                new ComplaintTestMocks.MockComplaintRepository(), departmentRepo,
                new ComplaintTestMocks.MockWardRepository(), employeeRepo, userRepo,
                workflowEngine, schedulingService, auditService
        );

        controller = new WorkOrderController(workOrderService, schedulingService, employeeRepo);

        // Create work order assigned to workerEmployee
        assignedWorkOrder = new WorkOrder();
        assignedWorkOrder.setWorkOrderNumber("WO-20261030-9001");
        assignedWorkOrder.setTitle("Street Light Repair");
        assignedWorkOrder.setDescription("Fix flickering street lights");
        assignedWorkOrder.setDepartment(department);
        assignedWorkOrder.setOrganization(org);
        assignedWorkOrder.setAssignedWorker(workerEmployee);
        assignedWorkOrder.setStatus(WorkOrderStatus.ASSIGNED);
        assignedWorkOrder.setPriority(WorkOrderPriority.MEDIUM);
        workOrderRepo.save(assignedWorkOrder);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user, String role) {
        UserPrincipal principal = new UserPrincipal(
                user.getId(), user.getUsername(), user.getEmail(), "secret",
                1L, department.getId(), null, true, false, true,
                Set.of(role), Set.of(), List.of(new SimpleGrantedAuthority(role))
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("1. Field worker can access their own assigned work order")
    void testFieldWorkerCanAccessAssignedWorkOrder() {
        authenticateAs(workerUser, "ROLE_FIELD_WORKER");

        var response = controller.getWorkOrderById(assignedWorkOrder.getId());

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(assignedWorkOrder.getId(), response.getData().getId());
    }

    @Test
    @DisplayName("2. Field worker is blocked (403) from accessing another worker's work order")
    void testFieldWorkerCannotAccessOtherWorkOrder() {
        WorkOrder otherWorkOrder = new WorkOrder();
        otherWorkOrder.setWorkOrderNumber("WO-20261030-9002");
        otherWorkOrder.setTitle("Park Maintenance");
        otherWorkOrder.setDescription("Mow the lawn");
        otherWorkOrder.setDepartment(department);
        otherWorkOrder.setOrganization(assignedWorkOrder.getOrganization());
        otherWorkOrder.setAssignedWorker(otherWorkerEmployee); // Assigned to other worker
        otherWorkOrder.setStatus(WorkOrderStatus.ASSIGNED);
        workOrderRepo.save(otherWorkOrder);

        authenticateAs(workerUser, "ROLE_FIELD_WORKER");

        assertThrows(ForbiddenException.class, () -> {
            controller.getWorkOrderById(otherWorkOrder.getId());
        });
    }

    @Test
    @DisplayName("3. Supervisor can access any department work order and verify completion")
    void testSupervisorCanAccessAndVerifyWorkOrder() {
        assignedWorkOrder.setStatus(WorkOrderStatus.COMPLETED);
        authenticateAs(supervisorUser, "ROLE_SUPERVISOR");

        var response = controller.getWorkOrderById(assignedWorkOrder.getId());
        assertTrue(response.isSuccess());

        VerifyWorkOrderRequest verifyReq = new VerifyWorkOrderRequest(true, "Supervised inspection verified", null);
        var verifyResp = controller.verifyWorkOrder(assignedWorkOrder.getId(), verifyReq);
        assertEquals(WorkOrderStatus.VERIFIED, verifyResp.getData().getStatus());
    }

    @Test
    @DisplayName("4. Admin has full management access across work orders")
    void testAdminHasFullAccess() {
        authenticateAs(adminUser, "ROLE_ADMIN");

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setDepartmentId(department.getId());
        req.setTitle("Drain repair");
        req.setDescription("Fix stormwater drain");
        req.setPriority(WorkOrderPriority.HIGH);

        var response = controller.createWorkOrder(req);
        assertNotNull(response.getData());
        assertEquals("Drain repair", response.getData().getTitle());
    }
}
