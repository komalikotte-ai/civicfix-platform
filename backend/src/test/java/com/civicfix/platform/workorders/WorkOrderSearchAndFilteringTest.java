package com.civicfix.platform.workorders;

import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.workorders.dto.WorkOrderDto;
import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import com.civicfix.platform.workorders.service.WorkOrderSchedulingService;
import com.civicfix.platform.workorders.service.WorkOrderService;
import com.civicfix.platform.workorders.service.WorkOrderWorkflowEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class WorkOrderSearchAndFilteringTest {

    private WorkOrderService workOrderService;

    private WorkOrderTestMocks.MockWorkOrderRepository workOrderRepo;
    private Department deptSanitation;
    private Department deptRoads;
    private Employee workerA;
    private Employee workerB;
    private Complaint complaint1;

    @BeforeEach
    void setUp() {
        workOrderRepo = new WorkOrderTestMocks.MockWorkOrderRepository();
        WorkOrderTestMocks.MockWorkOrderStatusHistoryRepository statusHistoryRepo = new WorkOrderTestMocks.MockWorkOrderStatusHistoryRepository();
        WorkOrderTestMocks.MockWorkOrderAssignmentHistoryRepository assignmentHistoryRepo = new WorkOrderTestMocks.MockWorkOrderAssignmentHistoryRepository();
        WorkOrderTestMocks.MockWorkOrderCommentRepository commentRepo = new WorkOrderTestMocks.MockWorkOrderCommentRepository();
        WorkOrderTestMocks.MockWorkOrderDependencyRepository dependencyRepo = new WorkOrderTestMocks.MockWorkOrderDependencyRepository();

        ComplaintTestMocks.MockComplaintRepository complaintRepo = new ComplaintTestMocks.MockComplaintRepository();
        ComplaintTestMocks.MockDepartmentRepository departmentRepo = new ComplaintTestMocks.MockDepartmentRepository();
        ComplaintTestMocks.MockWardRepository wardRepo = new ComplaintTestMocks.MockWardRepository();
        ComplaintTestMocks.MockEmployeeRepository employeeRepo = new ComplaintTestMocks.MockEmployeeRepository();
        ComplaintTestMocks.MockUserRepository userRepo = new ComplaintTestMocks.MockUserRepository();

        WorkOrderWorkflowEngine workflowEngine = new WorkOrderWorkflowEngine();
        WorkOrderSchedulingService schedulingService = new WorkOrderSchedulingService(dependencyRepo, workOrderRepo);
        WorkOrderTestMocks.MockAuditService auditService = new WorkOrderTestMocks.MockAuditService();

        workOrderService = new WorkOrderService(
                workOrderRepo, statusHistoryRepo, assignmentHistoryRepo, commentRepo,
                complaintRepo, departmentRepo, wardRepo, employeeRepo, userRepo,
                workflowEngine, schedulingService, auditService
        );

        Organization org = new Organization();
        org.setId(1L);
        org.setName("CivicFix Municipal");
        org.setCode("CFM");

        deptSanitation = new Department(org, "Sanitation", "SAN");
        deptSanitation.setId(10L);
        departmentRepo.save(deptSanitation);

        deptRoads = new Department(org, "Roads", "ROADS");
        deptRoads.setId(20L);
        departmentRepo.save(deptRoads);

        Ward ward1 = new Ward(org, "W-01", "Central Ward");
        ward1.setId(100L);
        wardRepo.save(ward1);

        workerA = new Employee("FW-A", "Worker A", "a@civicfix.gov", deptSanitation, RoleType.ROLE_FIELD_WORKER);
        workerA.setId(301L);
        employeeRepo.save(workerA);

        workerB = new Employee("FW-B", "Worker B", "b@civicfix.gov", deptRoads, RoleType.ROLE_FIELD_WORKER);
        workerB.setId(302L);
        employeeRepo.save(workerB);

        complaint1 = new Complaint();
        complaint1.setId(501L);
        complaint1.setTrackingNumber("CMP-TRACK-999");
        complaint1.setTitle("Garbage spill");
        complaint1.setDescription("Garbage overflow");
        complaintRepo.save(complaint1);

        // Seed Work Orders
        // WO 1: Sanitation, workerA, status IN_PROGRESS, priority HIGH
        WorkOrder wo1 = new WorkOrder();
        wo1.setWorkOrderNumber("WO-20261030-0001");
        wo1.setTitle("Garbage Clearance at Market");
        wo1.setDescription("Clear solid waste near central vegetable market");
        wo1.setDepartment(deptSanitation);
        wo1.setOrganization(org);
        wo1.setWard(ward1);
        wo1.setAssignedWorker(workerA);
        wo1.setComplaint(complaint1);
        wo1.setStatus(WorkOrderStatus.IN_PROGRESS);
        wo1.setPriority(WorkOrderPriority.HIGH);
        wo1.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS));
        workOrderRepo.save(wo1);

        // WO 2: Roads, workerB, status ASSIGNED, priority MEDIUM
        WorkOrder wo2 = new WorkOrder();
        wo2.setWorkOrderNumber("WO-20261030-0002");
        wo2.setTitle("Pothole Patching on Ring Road");
        wo2.setDescription("Fill dangerous potholes on outer ring road");
        wo2.setDepartment(deptRoads);
        wo2.setOrganization(org);
        wo2.setAssignedWorker(workerB);
        wo2.setStatus(WorkOrderStatus.ASSIGNED);
        wo2.setPriority(WorkOrderPriority.MEDIUM);
        wo2.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        workOrderRepo.save(wo2);

        // WO 3: Sanitation, unassigned, status CREATED, priority LOW
        WorkOrder wo3 = new WorkOrder();
        wo3.setWorkOrderNumber("WO-20261030-0003");
        wo3.setTitle("Sanitization of Bus Shelters");
        wo3.setDescription("Disinfect public bus stops along route 5");
        wo3.setDepartment(deptSanitation);
        wo3.setOrganization(org);
        wo3.setStatus(WorkOrderStatus.CREATED);
        wo3.setPriority(WorkOrderPriority.LOW);
        wo3.setCreatedAt(Instant.now());
        workOrderRepo.save(wo3);
    }

    @Test
    @DisplayName("1. Filter work orders by work order number")
    void testFilterByWorkOrderNumber() {
        PagedResponse<WorkOrderDto> result = workOrderService.searchWorkOrders(
                "0002", null, null, null, null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("WO-20261030-0002", result.getContent().get(0).getWorkOrderNumber());
    }

    @Test
    @DisplayName("2. Filter work orders by department")
    void testFilterByDepartment() {
        PagedResponse<WorkOrderDto> result = workOrderService.searchWorkOrders(
                null, null, null, deptSanitation.getId(), null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(wo -> deptSanitation.getId().equals(wo.getDepartmentId())));
    }

    @Test
    @DisplayName("3. Filter work orders by assigned worker")
    void testFilterByAssignedWorker() {
        PagedResponse<WorkOrderDto> result = workOrderService.searchWorkOrders(
                null, null, null, null, null, workerA.getId(), null, null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("Worker A", result.getContent().get(0).getAssignedWorkerName());
    }

    @Test
    @DisplayName("4. Filter work orders by status and priority")
    void testFilterByStatusAndPriority() {
        PagedResponse<WorkOrderDto> result = workOrderService.searchWorkOrders(
                null, null, null, null, null, null, null, WorkOrderStatus.IN_PROGRESS, WorkOrderPriority.HIGH, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("WO-20261030-0001", result.getContent().get(0).getWorkOrderNumber());
    }

    @Test
    @DisplayName("5. Filter work orders by complaint tracking number")
    void testFilterByComplaintTrackingNumber() {
        PagedResponse<WorkOrderDto> result = workOrderService.searchWorkOrders(
                null, null, "TRACK-999", null, null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals(complaint1.getId(), result.getContent().get(0).getComplaintId());
    }

    @Test
    @DisplayName("6. Search work orders by free text keyword")
    void testKeywordSearch() {
        PagedResponse<WorkOrderDto> result = workOrderService.searchWorkOrders(
                null, null, null, null, null, null, null, null, null, null, null, "pothole", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("Pothole Patching on Ring Road", result.getContent().get(0).getTitle());
    }
}
