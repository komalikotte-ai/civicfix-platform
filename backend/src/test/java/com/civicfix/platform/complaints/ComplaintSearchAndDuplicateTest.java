package com.civicfix.platform.complaints;

import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.complaints.dto.ComplaintDto;
import com.civicfix.platform.complaints.dto.DuplicateLinkRequest;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.complaints.event.ComplaintCreatedEvent;
import com.civicfix.platform.complaints.service.ComplaintDuplicateService;
import com.civicfix.platform.complaints.service.ComplaintService;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.security.UserPrincipal;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.workflow.service.WorkflowEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ComplaintSearchAndDuplicateTest {

    private ComplaintService complaintService;
    private ComplaintDuplicateService duplicateService;

    private ComplaintTestMocks.MockComplaintRepository complaintRepo;
    private ComplaintTestMocks.MockUserRepository userRepo;
    private ComplaintTestMocks.MockEmployeeRepository employeeRepo;

    private User citizen1;
    private User citizen2;
    private User admin;
    private Department deptSanitation;
    private Department deptRoads;
    private Complaint complaint1;
    private Complaint complaint2;
    private Complaint complaint3;

    @BeforeEach
    void setUp() {
        complaintRepo = new ComplaintTestMocks.MockComplaintRepository();
        userRepo = new ComplaintTestMocks.MockUserRepository();
        employeeRepo = new ComplaintTestMocks.MockEmployeeRepository();

        ComplaintTestMocks.MockAuditService auditService = new ComplaintTestMocks.MockAuditService();
        ComplaintTestMocks.MockSlaService slaService = new ComplaintTestMocks.MockSlaService();
        ComplaintTestMocks.MockEventPublisher eventPublisher = new ComplaintTestMocks.MockEventPublisher();
        ComplaintTestMocks.MockWorkflowTransitionRepository transitionRepo =
                new ComplaintTestMocks.MockWorkflowTransitionRepository();
        WorkflowEngine workflowEngine = new WorkflowEngine(transitionRepo);

        duplicateService = new ComplaintDuplicateService(complaintRepo, auditService);

        complaintService = new ComplaintService(
                complaintRepo,
                new ComplaintTestMocks.MockWardRepository(),
                new ComplaintTestMocks.MockDepartmentRepository(),
                userRepo,
                employeeRepo,
                new ComplaintTestMocks.MockAttachmentRepository(),
                new ComplaintTestMocks.MockCommentRepository(),
                new ComplaintTestMocks.MockAssignmentHistoryRepository(),
                new ComplaintTestMocks.MockStatusHistoryRepository(),
                workflowEngine,
                slaService,
                auditService,
                eventPublisher
        );

        citizen1 = new User();
        citizen1.setId(101L);
        citizen1.setUsername("citizen_1");
        userRepo.save(citizen1);

        citizen2 = new User();
        citizen2.setId(102L);
        citizen2.setUsername("citizen_2");
        userRepo.save(citizen2);

        admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        userRepo.save(admin);

        deptSanitation = new Department();
        deptSanitation.setId(10L);
        deptSanitation.setName("Sanitation");

        deptRoads = new Department();
        deptRoads.setId(20L);
        deptRoads.setName("Roads");

        complaint1 = new Complaint();
        complaint1.setId(1L);
        complaint1.setTrackingNumber("CF-20261028-0001");
        complaint1.setTitle("Garbage overflow on 1st Main");
        complaint1.setDescription("Bins not cleared for 3 days");
        complaint1.setCategory("GARBAGE_DUMP");
        complaint1.setStatus(ComplaintStatus.NEW);
        complaint1.setPriority(ComplaintPriority.MEDIUM);
        complaint1.setSeverity(ComplaintSeverity.MEDIUM);
        complaint1.setCitizen(citizen1);
        complaint1.setDepartment(deptSanitation);
        complaintRepo.save(complaint1);

        complaint2 = new Complaint();
        complaint2.setId(2L);
        complaint2.setTrackingNumber("CF-20261028-0002");
        complaint2.setTitle("Identical Garbage dump on 1st Main");
        complaint2.setDescription("Same garbage bins overflowing near corner");
        complaint2.setCategory("GARBAGE_DUMP");
        complaint2.setStatus(ComplaintStatus.NEW);
        complaint2.setPriority(ComplaintPriority.MEDIUM);
        complaint2.setSeverity(ComplaintSeverity.MEDIUM);
        complaint2.setCitizen(citizen2);
        complaint2.setDepartment(deptSanitation);
        complaintRepo.save(complaint2);

        complaint3 = new Complaint();
        complaint3.setId(3L);
        complaint3.setTrackingNumber("CF-20261028-0003");
        complaint3.setTitle("Major pothole near flyover");
        complaint3.setDescription("Hazardous pothole");
        complaint3.setCategory("POTHOLE");
        complaint3.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint3.setPriority(ComplaintPriority.HIGH);
        complaint3.setSeverity(ComplaintSeverity.HIGH);
        complaint3.setCitizen(citizen2);
        complaint3.setDepartment(deptRoads);
        complaintRepo.save(complaint3);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(User user, Long deptId, String... roles) {
        Set<String> roleSet = new HashSet<>(Arrays.asList(roles));
        List<SimpleGrantedAuthority> authorities = roleSet.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        UserPrincipal principal = new UserPrincipal(
                user.getId(), user.getUsername(), user.getUsername() + "@civicfix.gov.in",
                "secret", 1L, deptId, 1L, true, false, true, roleSet, Set.of(), authorities
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Citizen search should automatically scope results strictly to citizen's own complaints")
    void citizenSearchIsScopedToOwnComplaints() {
        setSecurityContext(citizen1, null, "ROLE_CITIZEN");

        PagedResponse<ComplaintDto> response = complaintService.searchComplaints(
                null, null, null, null, null, null, null, null, null, null, PageRequest.of(0, 10)
        );

        assertEquals(1, response.getContent().size());
        assertEquals("CF-20261028-0001", response.getContent().get(0).getTrackingNumber());
    }

    @Test
    @DisplayName("Admin search can view all complaints across citizens and departments")
    void adminSearchCanViewAllComplaints() {
        setSecurityContext(admin, null, "ROLE_ADMIN");

        PagedResponse<ComplaintDto> response = complaintService.searchComplaints(
                null, null, null, null, null, null, null, null, null, null, PageRequest.of(0, 10)
        );

        assertEquals(3, response.getContent().size());
    }

    @Test
    @DisplayName("Should successfully link target complaint as duplicate of primary complaint")
    void shouldLinkDuplicateComplaint() {
        DuplicateLinkRequest req = new DuplicateLinkRequest(complaint1.getId(), 0.94, "Same location and issue");

        ComplaintDto result = duplicateService.linkAsDuplicate(complaint2.getId(), req, "admin");

        assertTrue(result.isDuplicate());
        assertTrue(result.isDuplicateConfirmed());
        assertEquals(complaint1.getId(), result.getDuplicateOfId());
        assertEquals("CF-20261028-0001", result.getDuplicateOfTrackingNumber());
        assertEquals(0.94, result.getDuplicateSimilarityScore());
        assertNotNull(result.getDuplicateGroupId());

        // Both complaints should share the same duplicateGroupId
        Complaint primaryUpdated = complaintRepo.findById(complaint1.getId()).orElseThrow();
        assertEquals(result.getDuplicateGroupId(), primaryUpdated.getDuplicateGroupId());

        // Query duplicates of primary
        List<ComplaintDto> duplicates = duplicateService.getDuplicatesOf(complaint1.getId());
        assertEquals(1, duplicates.size());
        assertEquals("CF-20261028-0002", duplicates.get(0).getTrackingNumber());

        // Query duplicate group
        List<ComplaintDto> group = duplicateService.getDuplicateGroup(result.getDuplicateGroupId());
        assertEquals(2, group.size());
    }

    @Test
    @DisplayName("Should unlink a duplicate complaint and restore its standalone status")
    void shouldUnlinkDuplicateComplaint() {
        // Link first
        DuplicateLinkRequest req = new DuplicateLinkRequest(complaint1.getId(), 0.91, "Duplicate");
        duplicateService.linkAsDuplicate(complaint2.getId(), req, "admin");

        // Now unlink
        ComplaintDto unlinked = duplicateService.unlinkDuplicate(complaint2.getId(), "admin");

        assertFalse(unlinked.isDuplicate());
        assertNull(unlinked.getDuplicateOfId());
        assertFalse(unlinked.isDuplicateConfirmed());
        assertNull(unlinked.getDuplicateSimilarityScore());
    }

    @Test
    @DisplayName("ComplaintCreatedEvent triggers AI duplicate listener hook cleanly")
    void shouldPublishComplaintCreatedEvent() {
        ComplaintCreatedEvent event = new ComplaintCreatedEvent(this, complaint1);
        assertDoesNotThrow(() -> duplicateService.onComplaintCreated(event));
    }
}
