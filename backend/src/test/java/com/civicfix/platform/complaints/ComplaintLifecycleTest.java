package com.civicfix.platform.complaints;

import com.civicfix.platform.common.exception.BusinessException;
import com.civicfix.platform.complaints.dto.ComplaintDto;
import com.civicfix.platform.complaints.dto.UpdateComplaintStatusRequest;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.entity.ComplaintStatusHistory;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintSource;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.complaints.service.ComplaintService;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.security.UserPrincipal;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.wards.entity.Ward;
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

class ComplaintLifecycleTest {

    private ComplaintService complaintService;
    private WorkflowEngine workflowEngine;

    private ComplaintTestMocks.MockComplaintRepository complaintRepo;
    private ComplaintTestMocks.MockWorkflowTransitionRepository transitionRepo;
    private ComplaintTestMocks.MockStatusHistoryRepository statusHistoryRepo;
    private ComplaintTestMocks.MockUserRepository userRepo;

    private User adminUser;
    private User citizenUser;
    private Complaint testComplaint;

    @BeforeEach
    void setUp() {
        complaintRepo = new ComplaintTestMocks.MockComplaintRepository();
        transitionRepo = new ComplaintTestMocks.MockWorkflowTransitionRepository();
        statusHistoryRepo = new ComplaintTestMocks.MockStatusHistoryRepository();
        userRepo = new ComplaintTestMocks.MockUserRepository();

        workflowEngine = new WorkflowEngine(transitionRepo);

        complaintService = new ComplaintService(
                complaintRepo,
                new ComplaintTestMocks.MockWardRepository(),
                new ComplaintTestMocks.MockDepartmentRepository(),
                userRepo,
                new ComplaintTestMocks.MockEmployeeRepository(),
                new ComplaintTestMocks.MockAttachmentRepository(),
                new ComplaintTestMocks.MockCommentRepository(),
                new ComplaintTestMocks.MockAssignmentHistoryRepository(),
                statusHistoryRepo,
                workflowEngine,
                new ComplaintTestMocks.MockSlaService(),
                new ComplaintTestMocks.MockAuditService(),
                new ComplaintTestMocks.MockEventPublisher()
        );

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin_user");
        adminUser.setFullName("Super Admin");
        userRepo.save(adminUser);

        citizenUser = new User();
        citizenUser.setId(2L);
        citizenUser.setUsername("citizen_raj");
        citizenUser.setFullName("Raj Kumar");
        userRepo.save(citizenUser);

        Organization org = new Organization();
        org.setId(10L);

        Ward ward = new Ward();
        ward.setId(100L);
        ward.setOrganization(org);

        Department dept = new Department();
        dept.setId(200L);
        dept.setName("Sanitation");
        dept.setCode("SANITATION");

        testComplaint = new Complaint();
        testComplaint.setId(1000L);
        testComplaint.setTrackingNumber("CF-20261028-TEST01");
        testComplaint.setTitle("Pothole on 5th Main");
        testComplaint.setDescription("Deep pothole damaging vehicles");
        testComplaint.setCategory("POTHOLE");
        testComplaint.setStatus(ComplaintStatus.NEW);
        testComplaint.setCitizen(citizenUser);
        testComplaint.setOrganization(org);
        testComplaint.setWard(ward);
        testComplaint.setDepartment(dept);
        testComplaint.setPriority(ComplaintPriority.MEDIUM);
        testComplaint.setSeverity(ComplaintSeverity.MEDIUM);
        testComplaint.setSource(ComplaintSource.CITIZEN_PORTAL);
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
                "secret", 10L, 200L, 100L, true, false, true, roleSet, Set.of(), authorities
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should successfully transition through standard lifecycle: NEW -> ACKNOWLEDGED -> ASSIGNED -> IN_PROGRESS -> RESOLVED -> CLOSED")
    void shouldTransitionThroughFullLifecycle() {
        assertEquals(ComplaintStatus.NEW, testComplaint.getStatus());

        // 1. NEW -> ACKNOWLEDGED
        UpdateComplaintStatusRequest req1 = new UpdateComplaintStatusRequest();
        req1.setTargetStatus(ComplaintStatus.ACKNOWLEDGED);
        req1.setRemarks("Complaint received and verified by control room");
        ComplaintDto res1 = complaintService.updateStatus(testComplaint.getId(), req1);
        assertEquals(ComplaintStatus.ACKNOWLEDGED, res1.getStatus());

        // 2. ACKNOWLEDGED -> ASSIGNED
        UpdateComplaintStatusRequest req2 = new UpdateComplaintStatusRequest();
        req2.setTargetStatus(ComplaintStatus.ASSIGNED);
        req2.setRemarks("Assigned to Ward 100 road team");
        ComplaintDto res2 = complaintService.updateStatus(testComplaint.getId(), req2);
        assertEquals(ComplaintStatus.ASSIGNED, res2.getStatus());

        // 3. ASSIGNED -> IN_PROGRESS
        UpdateComplaintStatusRequest req3 = new UpdateComplaintStatusRequest();
        req3.setTargetStatus(ComplaintStatus.IN_PROGRESS);
        req3.setRemarks("Road repair crew deployed to site");
        ComplaintDto res3 = complaintService.updateStatus(testComplaint.getId(), req3);
        assertEquals(ComplaintStatus.IN_PROGRESS, res3.getStatus());

        // 4. IN_PROGRESS -> RESOLVED
        UpdateComplaintStatusRequest req4 = new UpdateComplaintStatusRequest();
        req4.setTargetStatus(ComplaintStatus.RESOLVED);
        req4.setRemarks("Pothole filled with cold mix asphalt");
        ComplaintDto res4 = complaintService.updateStatus(testComplaint.getId(), req4);
        assertEquals(ComplaintStatus.RESOLVED, res4.getStatus());
        assertNotNull(res4.getResolvedAt());

        // 5. RESOLVED -> CLOSED
        UpdateComplaintStatusRequest req5 = new UpdateComplaintStatusRequest();
        req5.setTargetStatus(ComplaintStatus.CLOSED);
        req5.setRemarks("Citizen satisfied, case closed");
        ComplaintDto res5 = complaintService.updateStatus(testComplaint.getId(), req5);
        assertEquals(ComplaintStatus.CLOSED, res5.getStatus());

        // Verify status history trail
        List<ComplaintStatusHistory> history = statusHistoryRepo.findByComplaintIdOrderByTransitionTimeAsc(testComplaint.getId());
        assertEquals(5, history.size());
        assertEquals(ComplaintStatus.NEW, history.get(0).getFromStatus());
        assertEquals(ComplaintStatus.ACKNOWLEDGED, history.get(0).getToStatus());
        assertEquals(ComplaintStatus.RESOLVED, history.get(4).getFromStatus());
        assertEquals(ComplaintStatus.CLOSED, history.get(4).getToStatus());
    }

    @Test
    @DisplayName("Should reject invalid state transition NEW -> RESOLVED with INVALID_STATUS_TRANSITION error")
    void shouldRejectInvalidDirectTransition() {
        assertEquals(ComplaintStatus.NEW, testComplaint.getStatus());

        UpdateComplaintStatusRequest req = new UpdateComplaintStatusRequest();
        req.setTargetStatus(ComplaintStatus.RESOLVED);
        req.setRemarks("Direct jump to resolved should fail");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                complaintService.updateStatus(testComplaint.getId(), req));

        assertEquals("INVALID_STATUS_TRANSITION", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Invalid status transition from 'NEW' to 'RESOLVED'"));
    }

    @Test
    @DisplayName("Should reject invalid transition CLOSED -> IN_PROGRESS")
    void shouldRejectInvalidTransitionFromClosed() {
        testComplaint.setStatus(ComplaintStatus.CLOSED);

        UpdateComplaintStatusRequest req = new UpdateComplaintStatusRequest();
        req.setTargetStatus(ComplaintStatus.IN_PROGRESS);
        req.setRemarks("Cannot directly move closed complaint to in progress");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                complaintService.updateStatus(testComplaint.getId(), req));

        assertEquals("INVALID_STATUS_TRANSITION", exception.getErrorCode());
    }

    @Test
    @DisplayName("Workflow engine properly exposes allowed next statuses for any given state")
    void shouldExposeAllowedNextStatuses() {
        Set<ComplaintStatus> newNext = workflowEngine.getAllowedNextStatuses(ComplaintStatus.NEW);
        assertTrue(newNext.contains(ComplaintStatus.ACKNOWLEDGED));
        assertTrue(newNext.contains(ComplaintStatus.ASSIGNED));
        assertTrue(newNext.contains(ComplaintStatus.REJECTED));
        assertFalse(newNext.contains(ComplaintStatus.RESOLVED));

        Set<ComplaintStatus> resolvedNext = workflowEngine.getAllowedNextStatuses(ComplaintStatus.RESOLVED);
        assertTrue(resolvedNext.contains(ComplaintStatus.CLOSED));
        assertTrue(resolvedNext.contains(ComplaintStatus.REOPENED));
        assertFalse(resolvedNext.contains(ComplaintStatus.NEW));
    }
}
