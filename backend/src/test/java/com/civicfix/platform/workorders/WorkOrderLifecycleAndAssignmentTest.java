package com.civicfix.platform.workorders;

import com.civicfix.platform.common.exception.BusinessException;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.workorders.dto.*;
import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.enums.DependencyType;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import com.civicfix.platform.workorders.service.WorkOrderSchedulingService;
import com.civicfix.platform.workorders.service.WorkOrderService;
import com.civicfix.platform.workorders.service.WorkOrderWorkflowEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorkOrderLifecycleAndAssignmentTest {

    private WorkOrderService workOrderService;
    private WorkOrderSchedulingService schedulingService;

    private WorkOrderTestMocks.MockWorkOrderRepository workOrderRepo;
    private WorkOrderTestMocks.MockWorkOrderStatusHistoryRepository statusHistoryRepo;
    private WorkOrderTestMocks.MockWorkOrderAssignmentHistoryRepository assignmentHistoryRepo;
    private WorkOrderTestMocks.MockWorkOrderCommentRepository commentRepo;
    private WorkOrderTestMocks.MockWorkOrderDependencyRepository dependencyRepo;
    private ComplaintTestMocks.MockDepartmentRepository departmentRepo;
    private ComplaintTestMocks.MockEmployeeRepository employeeRepo;
    private ComplaintTestMocks.MockUserRepository userRepo;

    private Department department;
    private Employee worker1;
    private Employee worker2;
    private Employee supervisor1;
    private User supervisorUser;
    private WorkOrder testWorkOrder;

    @BeforeEach
    void setUp() {
        workOrderRepo = new WorkOrderTestMocks.MockWorkOrderRepository();
        statusHistoryRepo = new WorkOrderTestMocks.MockWorkOrderStatusHistoryRepository();
        assignmentHistoryRepo = new WorkOrderTestMocks.MockWorkOrderAssignmentHistoryRepository();
        commentRepo = new WorkOrderTestMocks.MockWorkOrderCommentRepository();
        dependencyRepo = new WorkOrderTestMocks.MockWorkOrderDependencyRepository();
        departmentRepo = new ComplaintTestMocks.MockDepartmentRepository();
        employeeRepo = new ComplaintTestMocks.MockEmployeeRepository();
        userRepo = new ComplaintTestMocks.MockUserRepository();

        WorkOrderWorkflowEngine workflowEngine = new WorkOrderWorkflowEngine();
        schedulingService = new WorkOrderSchedulingService(dependencyRepo, workOrderRepo);
        WorkOrderTestMocks.MockAuditService auditService = new WorkOrderTestMocks.MockAuditService();

        workOrderService = new WorkOrderService(
                workOrderRepo,
                statusHistoryRepo,
                assignmentHistoryRepo,
                commentRepo,
                new ComplaintTestMocks.MockComplaintRepository(),
                departmentRepo,
                new ComplaintTestMocks.MockWardRepository(),
                employeeRepo,
                userRepo,
                workflowEngine,
                schedulingService,
                auditService
        );

        Organization org = new Organization();
        org.setId(1L);
        org.setName("CivicFix Municipal");
        org.setCode("CFM");

        department = new Department(org, "Roads & Bridges", "ROADS");
        department.setId(11L);
        departmentRepo.save(department);

        worker1 = new Employee("FW-01", "Worker One", "w1@civicfix.gov", department, RoleType.ROLE_FIELD_WORKER);
        worker1.setId(201L);
        employeeRepo.save(worker1);

        worker2 = new Employee("FW-02", "Worker Two", "w2@civicfix.gov", department, RoleType.ROLE_FIELD_WORKER);
        worker2.setId(202L);
        employeeRepo.save(worker2);

        supervisor1 = new Employee("SP-01", "Supervisor One", "s1@civicfix.gov", department, RoleType.ROLE_SUPERVISOR);
        supervisor1.setId(203L);
        employeeRepo.save(supervisor1);

        supervisorUser = new User();
        supervisorUser.setId(301L);
        supervisorUser.setUsername("supervisor_bob");
        supervisorUser.setFullName("Supervisor Bob");
        Role supRole = new Role(RoleType.ROLE_SUPERVISOR, "Supervisor");
        supervisorUser.setRoles(Set.of(supRole));
        userRepo.save(supervisorUser);

        testWorkOrder = new WorkOrder();
        testWorkOrder.setWorkOrderNumber("WO-20261030-1001");
        testWorkOrder.setTitle("Road Resurfacing");
        testWorkOrder.setDescription("Bitumen surfacing on Cross Road 5");
        testWorkOrder.setDepartment(department);
        testWorkOrder.setOrganization(org);
        testWorkOrder.setStatus(WorkOrderStatus.CREATED);
        testWorkOrder.setPriority(WorkOrderPriority.HIGH);
        workOrderRepo.save(testWorkOrder);
    }

    @Test
    @DisplayName("1. Assign and reassign worker and supervisor with assignment history tracking")
    void testAssignmentAndReassignment() {
        // Step 1: Assign worker1 and supervisor1
        AssignWorkOrderRequest assignReq = new AssignWorkOrderRequest();
        assignReq.setWorkerId(worker1.getId());
        assignReq.setSupervisorId(supervisor1.getId());
        assignReq.setReason("Allocated to Team Alpha");

        WorkOrderDto assigned = workOrderService.assignWorkOrder(testWorkOrder.getId(), assignReq, "dept_manager");

        assertEquals(WorkOrderStatus.ASSIGNED, assigned.getStatus());
        assertEquals(worker1.getId(), assigned.getAssignedWorkerId());
        assertEquals(supervisor1.getId(), assigned.getAssignedSupervisorId());

        // Step 2: Reassign to worker2
        AssignWorkOrderRequest reassignReq = new AssignWorkOrderRequest();
        reassignReq.setWorkerId(worker2.getId());
        reassignReq.setSupervisorId(supervisor1.getId());
        reassignReq.setReason("Worker 1 unavailable due to emergency duty");

        WorkOrderDto reassigned = workOrderService.assignWorkOrder(testWorkOrder.getId(), reassignReq, "supervisor_bob");

        assertEquals(worker2.getId(), reassigned.getAssignedWorkerId());
        assertEquals(supervisor1.getId(), reassigned.getAssignedSupervisorId());

        // Verify assignment history has 2 records
        var history = workOrderService.getAssignmentHistory(testWorkOrder.getId());
        assertEquals(2, history.size());
        assertEquals("Worker One", history.get(0).getNewWorkerName());
        assertEquals("Worker One", history.get(1).getPreviousWorkerName());
        assertEquals("Worker Two", history.get(1).getNewWorkerName());
        assertEquals("Worker 1 unavailable due to emergency duty", history.get(1).getReason());
    }

    @Test
    @DisplayName("2. Full valid lifecycle progression: CREATED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED -> VERIFIED")
    void testFullLifecycleProgressionToVerification() {
        // CREATED -> ASSIGNED
        AssignWorkOrderRequest assignReq = new AssignWorkOrderRequest();
        assignReq.setWorkerId(worker1.getId());
        workOrderService.assignWorkOrder(testWorkOrder.getId(), assignReq, "manager");

        // ASSIGNED -> ACCEPTED
        WorkOrderDto accepted = workOrderService.acceptWorkOrder(testWorkOrder.getId(), "Ready to commence", "worker_user");
        assertEquals(WorkOrderStatus.ACCEPTED, accepted.getStatus());

        // ACCEPTED -> IN_PROGRESS
        WorkOrderDto started = workOrderService.startWorkOrder(testWorkOrder.getId(), "Started road clearing", "worker_user");
        assertEquals(WorkOrderStatus.IN_PROGRESS, started.getStatus());
        assertNotNull(started.getActualStartDate());

        // IN_PROGRESS -> COMPLETED
        CompleteWorkOrderRequest completeReq = new CompleteWorkOrderRequest(
                "Completed paving 200m bitumen layer",
                new BigDecimal("45000.00"),
                "{\"photos\": [\"before.jpg\", \"after.jpg\"]}"
        );
        WorkOrderDto completed = workOrderService.completeWorkOrder(testWorkOrder.getId(), completeReq, "worker_user");
        assertEquals(WorkOrderStatus.COMPLETED, completed.getStatus());
        assertEquals(new BigDecimal("45000.00"), completed.getActualCost());
        assertNotNull(completed.getActualCompletionDate());

        // COMPLETED -> VERIFIED (Supervisor approves)
        VerifyWorkOrderRequest verifyReq = new VerifyWorkOrderRequest(true, "Quality inspection passed", null);
        WorkOrderDto verified = workOrderService.verifyWorkOrder(testWorkOrder.getId(), verifyReq, supervisorUser.getUsername());
        assertEquals(WorkOrderStatus.VERIFIED, verified.getStatus());
        assertEquals(supervisorUser.getId(), verified.getVerifiedById());
        assertNotNull(verified.getVerifiedAt());
        assertNull(verified.getRejectionReason());
    }

    @Test
    @DisplayName("3. Supervisor rejection and rework handling: COMPLETED -> REJECTED -> IN_PROGRESS")
    void testRejectionAndReworkFlow() {
        // Move to COMPLETED
        AssignWorkOrderRequest assignReq = new AssignWorkOrderRequest();
        assignReq.setWorkerId(worker1.getId());
        workOrderService.assignWorkOrder(testWorkOrder.getId(), assignReq, "manager");
        workOrderService.acceptWorkOrder(testWorkOrder.getId(), null, "worker_user");
        workOrderService.startWorkOrder(testWorkOrder.getId(), null, "worker_user");
        workOrderService.completeWorkOrder(testWorkOrder.getId(), null, "worker_user");

        // Reject verification
        VerifyWorkOrderRequest rejectReq = new VerifyWorkOrderRequest(false, "Surface uneven", "Bitumen thickness below standard");
        WorkOrderDto rejected = workOrderService.verifyWorkOrder(testWorkOrder.getId(), rejectReq, supervisorUser.getUsername());

        assertEquals(WorkOrderStatus.REJECTED, rejected.getStatus());
        assertEquals("Bitumen thickness below standard", rejected.getRejectionReason());
        assertEquals(1, rejected.getRejectionCount());

        // Initiate rework
        WorkOrderDto reworked = workOrderService.reworkWorkOrder(testWorkOrder.getId(), "Applying extra bitumen layer", "manager");
        assertEquals(WorkOrderStatus.IN_PROGRESS, reworked.getStatus());
    }

    @Test
    @DisplayName("4. Enforce valid status transition constraints (e.g. cannot jump CREATED -> COMPLETED)")
    void testInvalidStatusTransitionRejected() {
        UpdateWorkOrderStatusRequest invalidReq = new UpdateWorkOrderStatusRequest(WorkOrderStatus.COMPLETED, "Direct completion");

        assertThrows(BusinessException.class, () -> {
            workOrderService.updateStatus(testWorkOrder.getId(), invalidReq, "user");
        });
    }

    @Test
    @DisplayName("5. Work order dependencies and circular dependency detection")
    void testWorkOrderDependenciesAndCycleDetection() {
        WorkOrder woB = new WorkOrder();
        woB.setWorkOrderNumber("WO-20261030-1002");
        woB.setTitle("Lay Drainage Pipes");
        woB.setDescription("Pipes under road");
        woB.setDepartment(department);
        woB.setOrganization(testWorkOrder.getOrganization());
        woB.setStatus(WorkOrderStatus.CREATED);
        workOrderRepo.save(woB);

        WorkOrder woC = new WorkOrder();
        woC.setWorkOrderNumber("WO-20261030-1003");
        woC.setTitle("Excavate Trench");
        woC.setDescription("Trench for drainage");
        woC.setDepartment(department);
        woC.setOrganization(testWorkOrder.getOrganization());
        woC.setStatus(WorkOrderStatus.CREATED);
        workOrderRepo.save(woC);

        // woA depends on woB
        schedulingService.addDependency(testWorkOrder.getId(), new AddWorkOrderDependencyRequest(woB.getId(), DependencyType.FINISH_TO_START));

        // woB depends on woC
        schedulingService.addDependency(woB.getId(), new AddWorkOrderDependencyRequest(woC.getId(), DependencyType.FINISH_TO_START));

        // Adding woC depends on woA would create a cycle (woA -> woB -> woC -> woA), must throw BadRequestException
        assertThrows(RuntimeException.class, () -> {
            schedulingService.addDependency(woC.getId(), new AddWorkOrderDependencyRequest(testWorkOrder.getId(), DependencyType.FINISH_TO_START));
        });

        // Prerequisite check: woA prerequisites are not met because woB is only CREATED
        assertFalse(schedulingService.arePrerequisitesMet(testWorkOrder.getId()));

        // Cannot start woA while prerequisites are unmet
        testWorkOrder.setStatus(WorkOrderStatus.ACCEPTED);
        assertThrows(BusinessException.class, () -> {
            workOrderService.startWorkOrder(testWorkOrder.getId(), "Trying to start", "worker");
        });

        // Mark woB and woC completed
        woC.setStatus(WorkOrderStatus.COMPLETED);
        woB.setStatus(WorkOrderStatus.COMPLETED);
        assertTrue(schedulingService.arePrerequisitesMet(testWorkOrder.getId()));

        // Now woA can start
        WorkOrderDto started = workOrderService.startWorkOrder(testWorkOrder.getId(), "Prerequisites done", "worker");
        assertEquals(WorkOrderStatus.IN_PROGRESS, started.getStatus());
    }

    @Test
    @DisplayName("6. Work order comments: adding internal and public comments")
    void testWorkOrderComments() {
        AddWorkOrderCommentRequest internalComment = new AddWorkOrderCommentRequest("Internal crew notes: bring extra machinery", true);
        AddWorkOrderCommentRequest publicComment = new AddWorkOrderCommentRequest("Work scheduled for tomorrow morning", false);

        workOrderService.addComment(testWorkOrder.getId(), internalComment, supervisorUser.getUsername());
        workOrderService.addComment(testWorkOrder.getId(), publicComment, supervisorUser.getUsername());

        var allComments = workOrderService.getComments(testWorkOrder.getId(), true);
        assertEquals(2, allComments.size());

        var publicOnlyComments = workOrderService.getComments(testWorkOrder.getId(), false);
        assertEquals(1, publicOnlyComments.size());
        assertEquals("Work scheduled for tomorrow morning", publicOnlyComments.get(0).getComment());
    }

    @Test
    @DisplayName("7. Hold and cancel work orders")
    void testHoldAndCancelWorkOrders() {
        testWorkOrder.setStatus(WorkOrderStatus.IN_PROGRESS);

        WorkOrderDto onHold = workOrderService.holdWorkOrder(testWorkOrder.getId(), "Heavy rain delay", "worker");
        assertEquals(WorkOrderStatus.ON_HOLD, onHold.getStatus());

        WorkOrderDto cancelled = workOrderService.cancelWorkOrder(testWorkOrder.getId(), "Cancelled by city council directive", "admin");
        assertEquals(WorkOrderStatus.CANCELLED, cancelled.getStatus());
    }
}
