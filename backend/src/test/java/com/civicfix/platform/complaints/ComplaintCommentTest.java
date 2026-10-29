package com.civicfix.platform.complaints;

import com.civicfix.platform.common.exception.ForbiddenException;
import com.civicfix.platform.complaints.dto.AddComplaintCommentRequest;
import com.civicfix.platform.complaints.dto.ComplaintCommentDto;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.complaints.service.ComplaintService;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.security.UserPrincipal;
import com.civicfix.platform.users.entity.User;
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

public class ComplaintCommentTest {

    private ComplaintService complaintService;

    private ComplaintTestMocks.MockComplaintRepository complaintRepo;
    private ComplaintTestMocks.MockCommentRepository commentRepo;
    private ComplaintTestMocks.MockUserRepository userRepo;

    private User citizenOwner;
    private User citizenIntruder;
    private User supervisorUser;
    private Complaint testComplaint;

    @BeforeEach
    void setUp() {
        complaintRepo = new ComplaintTestMocks.MockComplaintRepository();
        commentRepo = new ComplaintTestMocks.MockCommentRepository();
        userRepo = new ComplaintTestMocks.MockUserRepository();
        ComplaintTestMocks.MockWorkflowTransitionRepository transitionRepo =
                new ComplaintTestMocks.MockWorkflowTransitionRepository();
        ComplaintTestMocks.MockStatusHistoryRepository statusRepo =
                new ComplaintTestMocks.MockStatusHistoryRepository();

        WorkflowEngine workflowEngine = new WorkflowEngine(transitionRepo);

        complaintService = new ComplaintService(
                complaintRepo,
                new ComplaintTestMocks.MockWardRepository(),
                new ComplaintTestMocks.MockDepartmentRepository(),
                userRepo,
                new ComplaintTestMocks.MockEmployeeRepository(),
                new ComplaintTestMocks.MockAttachmentRepository(),
                commentRepo,
                new ComplaintTestMocks.MockAssignmentHistoryRepository(),
                statusRepo,
                workflowEngine,
                new ComplaintTestMocks.MockSlaService(),
                new ComplaintTestMocks.MockAuditService(),
                new ComplaintTestMocks.MockEventPublisher()
        );

        citizenOwner = new User();
        citizenOwner.setId(10L);
        citizenOwner.setUsername("citizen_owner");
        citizenOwner.setFullName("Aarti Roy");
        userRepo.save(citizenOwner);

        citizenIntruder = new User();
        citizenIntruder.setId(20L);
        citizenIntruder.setUsername("citizen_intruder");
        citizenIntruder.setFullName("Unauthorized Citizen");
        userRepo.save(citizenIntruder);

        supervisorUser = new User();
        supervisorUser.setId(30L);
        supervisorUser.setUsername("supervisor_patil");
        supervisorUser.setFullName("Sanjay Patil");
        userRepo.save(supervisorUser);

        Department dept = new Department();
        dept.setId(100L);
        dept.setName("Electrical");

        testComplaint = new Complaint();
        testComplaint.setId(700L);
        testComplaint.setTrackingNumber("CF-20261028-COMM1");
        testComplaint.setTitle("Streetlight broken");
        testComplaint.setDescription("Streetlight bulb fused on main cross road");
        testComplaint.setCategory("STREETLIGHT_OUT");
        testComplaint.setStatus(ComplaintStatus.IN_PROGRESS);
        testComplaint.setCitizen(citizenOwner);
        testComplaint.setDepartment(dept);
        complaintRepo.save(testComplaint);
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
                "secret", 1L, 100L, 1L, true, false, true, roleSet, Set.of(), authorities
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Citizen can add public comment to their complaint")
    void citizenCanAddPublicComment() {
        setSecurityContext(citizenOwner, "ROLE_CITIZEN");

        AddComplaintCommentRequest request = new AddComplaintCommentRequest("Please expedite, dark street causing accidents", false);
        ComplaintCommentDto dto = complaintService.addComment(testComplaint.getId(), request, "citizen_owner");

        assertNotNull(dto);
        assertEquals("Please expedite, dark street causing accidents", dto.getComment());
        assertFalse(dto.isInternalOnly());
        assertEquals("Aarti Roy", dto.getAuthorName());
    }

    @Test
    @DisplayName("Citizen cannot add internal-only comment, throwing ForbiddenException")
    void citizenCannotAddInternalComment() {
        setSecurityContext(citizenOwner, "ROLE_CITIZEN");

        AddComplaintCommentRequest request = new AddComplaintCommentRequest("Secret internal note", true);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
                complaintService.addComment(testComplaint.getId(), request, "citizen_owner"));

        assertTrue(exception.getMessage().contains("Citizens cannot post internal-only notes"));
    }

    @Test
    @DisplayName("Staff can add internal note, which is hidden from citizen comment list")
    void staffCanAddInternalNoteHiddenFromCitizen() {
        // Staff adds internal note
        setSecurityContext(supervisorUser, "ROLE_SUPERVISOR");
        AddComplaintCommentRequest internalReq = new AddComplaintCommentRequest("Transformer part on order from warehouse", true);
        complaintService.addComment(testComplaint.getId(), internalReq, "supervisor_patil");

        // Staff adds public update
        AddComplaintCommentRequest publicReq = new AddComplaintCommentRequest("Replacement work scheduled for tomorrow", false);
        complaintService.addComment(testComplaint.getId(), publicReq, "supervisor_patil");

        // Citizen views comments: only public comments returned
        setSecurityContext(citizenOwner, "ROLE_CITIZEN");
        List<ComplaintCommentDto> citizenView = complaintService.getComments(testComplaint.getId());
        assertEquals(1, citizenView.size());
        assertEquals("Replacement work scheduled for tomorrow", citizenView.get(0).getComment());
        assertFalse(citizenView.get(0).isInternalOnly());

        // Supervisor views comments: both public and internal returned
        setSecurityContext(supervisorUser, "ROLE_SUPERVISOR");
        List<ComplaintCommentDto> staffView = complaintService.getComments(testComplaint.getId());
        assertEquals(2, staffView.size());
    }

    @Test
    @DisplayName("Citizen cannot view comments on another citizen's complaint")
    void citizenCannotViewOtherCitizenComments() {
        setSecurityContext(citizenIntruder, "ROLE_CITIZEN");

        assertThrows(ForbiddenException.class, () ->
                complaintService.getComments(testComplaint.getId()));
    }
}
