package com.civicfix.platform.workorders;

import com.civicfix.platform.common.exception.BusinessException;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.complaints.ComplaintTestMocks;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.workorders.dto.ConvertComplaintRequest;
import com.civicfix.platform.workorders.dto.CreateWorkOrderRequest;
import com.civicfix.platform.workorders.dto.WorkOrderDto;
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
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class WorkOrderCreationAndConversionTest {

    private WorkOrderService workOrderService;

    private WorkOrderTestMocks.MockWorkOrderRepository workOrderRepo;
    private WorkOrderTestMocks.MockWorkOrderStatusHistoryRepository statusHistoryRepo;
    private WorkOrderTestMocks.MockWorkOrderAssignmentHistoryRepository assignmentHistoryRepo;
    private WorkOrderTestMocks.MockWorkOrderCommentRepository commentRepo;
    private WorkOrderTestMocks.MockWorkOrderDependencyRepository dependencyRepo;
    private ComplaintTestMocks.MockComplaintRepository complaintRepo;
    private ComplaintTestMocks.MockDepartmentRepository departmentRepo;
    private ComplaintTestMocks.MockWardRepository wardRepo;
    private ComplaintTestMocks.MockEmployeeRepository employeeRepo;
    private ComplaintTestMocks.MockUserRepository userRepo;

    private Organization organization;
    private Department department;
    private Ward ward;
    private Employee fieldWorker;
    private Employee supervisor;
    private Complaint activeComplaint;

    @BeforeEach
    void setUp() {
        workOrderRepo = new WorkOrderTestMocks.MockWorkOrderRepository();
        statusHistoryRepo = new WorkOrderTestMocks.MockWorkOrderStatusHistoryRepository();
        assignmentHistoryRepo = new WorkOrderTestMocks.MockWorkOrderAssignmentHistoryRepository();
        commentRepo = new WorkOrderTestMocks.MockWorkOrderCommentRepository();
        dependencyRepo = new WorkOrderTestMocks.MockWorkOrderDependencyRepository();
        complaintRepo = new ComplaintTestMocks.MockComplaintRepository();
        departmentRepo = new ComplaintTestMocks.MockDepartmentRepository();
        wardRepo = new ComplaintTestMocks.MockWardRepository();
        employeeRepo = new ComplaintTestMocks.MockEmployeeRepository();
        userRepo = new ComplaintTestMocks.MockUserRepository();

        WorkOrderWorkflowEngine workflowEngine = new WorkOrderWorkflowEngine();
        WorkOrderSchedulingService schedulingService = new WorkOrderSchedulingService(dependencyRepo, workOrderRepo);
        WorkOrderTestMocks.MockAuditService auditService = new WorkOrderTestMocks.MockAuditService();

        workOrderService = new WorkOrderService(
                workOrderRepo,
                statusHistoryRepo,
                assignmentHistoryRepo,
                commentRepo,
                complaintRepo,
                departmentRepo,
                wardRepo,
                employeeRepo,
                userRepo,
                workflowEngine,
                schedulingService,
                auditService
        );

        organization = new Organization();
        organization.setId(1L);
        organization.setName("Bengaluru Municipal Corporation");
        organization.setCode("BMC");

        department = new Department(organization, "Sanitation Department", "SAN");
        department.setId(10L);
        departmentRepo.save(department);

        ward = new Ward(organization, "Ward 150", "Bellandur");
        ward.setId(20L);
        wardRepo.save(ward);

        fieldWorker = new Employee("EMP-FW-01", "Ramesh Kumar", "ramesh@civicfix.gov", department, RoleType.ROLE_FIELD_WORKER);
        fieldWorker.setId(101L);
        employeeRepo.save(fieldWorker);

        supervisor = new Employee("EMP-SP-01", "Suresh Patel", "suresh@civicfix.gov", department, RoleType.ROLE_SUPERVISOR);
        supervisor.setId(102L);
        employeeRepo.save(supervisor);

        activeComplaint = new Complaint();
        activeComplaint.setId(500L);
        activeComplaint.setTrackingNumber("CMP-20261030-001");
        activeComplaint.setTitle("Pothole on Main Road");
        activeComplaint.setDescription("Deep pothole causing traffic jam near Bellandur junction");
        activeComplaint.setOrganization(organization);
        activeComplaint.setDepartment(department);
        activeComplaint.setWard(ward);
        activeComplaint.setStatus(ComplaintStatus.SUBMITTED);
        activeComplaint.setPriority(ComplaintPriority.HIGH);
        activeComplaint.setAddressLine("100 Feet Ring Road");
        complaintRepo.save(activeComplaint);
    }

    @Test
    @DisplayName("1. Create standalone work order successfully without assigned worker (Status: CREATED)")
    void testCreateStandaloneWorkOrderUnassigned() {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest();
        request.setDepartmentId(department.getId());
        request.setWardId(ward.getId());
        request.setTitle("Repair Water Pipe");
        request.setDescription("Fix leaking water main line");
        request.setPriority(WorkOrderPriority.HIGH);
        request.setEstimatedCost(new BigDecimal("15000.00"));
        request.setAddressLine("Near City Market");

        WorkOrderDto result = workOrderService.createWorkOrder(request, "admin_user");

        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.getWorkOrderNumber().startsWith("WO-"));
        assertEquals("Repair Water Pipe", result.getTitle());
        assertEquals(WorkOrderStatus.CREATED, result.getStatus());
        assertEquals(WorkOrderPriority.HIGH, result.getPriority());
        assertEquals(department.getId(), result.getDepartmentId());
        assertEquals(ward.getId(), result.getWardId());
        assertNull(result.getAssignedWorkerId());
        assertEquals(1, statusHistoryRepo.findByWorkOrderIdOrderByCreatedAtAsc(result.getId()).size());
    }

    @Test
    @DisplayName("2. Create standalone work order with assigned field worker (Status: ASSIGNED)")
    void testCreateStandaloneWorkOrderWithWorker() {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest();
        request.setDepartmentId(department.getId());
        request.setTitle("Clear Fallen Tree");
        request.setDescription("Tree blocking 2nd cross road");
        request.setPriority(WorkOrderPriority.URGENT);
        request.setAssignedWorkerId(fieldWorker.getId());
        request.setAssignedSupervisorId(supervisor.getId());

        WorkOrderDto result = workOrderService.createWorkOrder(request, "supervisor_user");

        assertNotNull(result);
        assertEquals(WorkOrderStatus.ASSIGNED, result.getStatus());
        assertEquals(fieldWorker.getId(), result.getAssignedWorkerId());
        assertEquals("Ramesh Kumar", result.getAssignedWorkerName());
        assertEquals(supervisor.getId(), result.getAssignedSupervisorId());
        assertNotNull(result.getAssignedAt());

        // Check history records
        assertEquals(1, statusHistoryRepo.findByWorkOrderIdOrderByCreatedAtAsc(result.getId()).size());
        assertEquals(1, assignmentHistoryRepo.findByWorkOrderIdOrderByAssignedAtAsc(result.getId()).size());
    }

    @Test
    @DisplayName("3. Convert eligible complaint into work order successfully")
    void testConvertComplaintSuccess() {
        ConvertComplaintRequest request = new ConvertComplaintRequest();
        request.setComplaintId(activeComplaint.getId());
        request.setAssignedWorkerId(fieldWorker.getId());
        request.setAssignedSupervisorId(supervisor.getId());
        request.setScheduledStartDate(Instant.now());
        request.setExpectedCompletionDate(Instant.now().plus(48, ChronoUnit.HOURS));

        WorkOrderDto result = workOrderService.convertComplaint(request, "admin_user");

        assertNotNull(result);
        assertEquals(activeComplaint.getId(), result.getComplaintId());
        assertEquals(activeComplaint.getTrackingNumber(), result.getComplaintTrackingNumber());
        assertEquals(activeComplaint.getTitle(), result.getTitle());
        assertEquals(activeComplaint.getDescription(), result.getDescription());
        assertEquals(WorkOrderPriority.HIGH, result.getPriority());
        assertEquals(WorkOrderStatus.ASSIGNED, result.getStatus());
        assertEquals(fieldWorker.getId(), result.getAssignedWorkerId());
        assertEquals(supervisor.getId(), result.getAssignedSupervisorId());
    }

    @Test
    @DisplayName("4. Prevent duplicate work-order conversion for the same complaint")
    void testPreventDuplicateWorkOrderConversion() {
        ConvertComplaintRequest request1 = new ConvertComplaintRequest();
        request1.setComplaintId(activeComplaint.getId());
        workOrderService.convertComplaint(request1, "admin_user");

        // Attempting to convert the same complaint again must fail with ConflictException
        ConvertComplaintRequest request2 = new ConvertComplaintRequest();
        request2.setComplaintId(activeComplaint.getId());

        assertThrows(ConflictException.class, () -> {
            workOrderService.convertComplaint(request2, "admin_user");
        });
    }

    @Test
    @DisplayName("5. Conversion fails if complaint is REJECTED or CLOSED")
    void testConvertIneligibleComplaintFails() {
        Complaint rejectedComplaint = new Complaint();
        rejectedComplaint.setId(501L);
        rejectedComplaint.setTrackingNumber("CMP-20261030-002");
        rejectedComplaint.setTitle("Spam request");
        rejectedComplaint.setDescription("Not a valid grievance");
        rejectedComplaint.setStatus(ComplaintStatus.REJECTED);
        rejectedComplaint.setDepartment(department);
        complaintRepo.save(rejectedComplaint);

        ConvertComplaintRequest request = new ConvertComplaintRequest();
        request.setComplaintId(rejectedComplaint.getId());

        assertThrows(BusinessException.class, () -> {
            workOrderService.convertComplaint(request, "admin_user");
        });
    }

    @Test
    @DisplayName("6. Create work order fails when assigned worker does not exist or is inactive")
    void testCreateWorkOrderFailsForInactiveWorker() {
        Employee inactiveWorker = new Employee("EMP-INACTIVE", "Inactive Worker", "in@civicfix.gov", department, RoleType.ROLE_FIELD_WORKER);
        inactiveWorker.setId(103L);
        inactiveWorker.setActive(false);
        employeeRepo.save(inactiveWorker);

        CreateWorkOrderRequest request = new CreateWorkOrderRequest();
        request.setDepartmentId(department.getId());
        request.setTitle("Paving repair");
        request.setDescription("Fix footpath slabs");
        request.setAssignedWorkerId(inactiveWorker.getId());

        assertThrows(BusinessException.class, () -> {
            workOrderService.createWorkOrder(request, "admin_user");
        });
    }

    @Test
    @DisplayName("7. Create work order fails when scheduled start date is after expected completion date")
    void testCreateWorkOrderInvalidScheduleDates() {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest();
        request.setDepartmentId(department.getId());
        request.setTitle("Drainage Desilting");
        request.setDescription("Desilting stormwater drain");
        request.setScheduledStartDate(Instant.now().plus(5, ChronoUnit.DAYS));
        request.setExpectedCompletionDate(Instant.now().plus(2, ChronoUnit.DAYS)); // Before start date

        assertThrows(RuntimeException.class, () -> {
            workOrderService.createWorkOrder(request, "admin_user");
        });
    }
}
