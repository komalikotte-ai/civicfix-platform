package com.civicfix.platform.organizations;

import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.GlobalExceptionHandler;
import com.civicfix.platform.departments.controller.DepartmentController;
import com.civicfix.platform.departments.controller.TeamController;
import com.civicfix.platform.departments.dto.CreateDepartmentRequest;
import com.civicfix.platform.departments.dto.CreateTeamRequest;
import com.civicfix.platform.departments.dto.DepartmentDto;
import com.civicfix.platform.departments.dto.TeamDto;
import com.civicfix.platform.departments.service.DepartmentService;
import com.civicfix.platform.departments.service.TeamService;
import com.civicfix.platform.organizations.controller.CityController;
import com.civicfix.platform.organizations.controller.MunicipalityController;
import com.civicfix.platform.organizations.dto.CityDto;
import com.civicfix.platform.organizations.dto.CreateCityRequest;
import com.civicfix.platform.organizations.dto.CreateMunicipalityRequest;
import com.civicfix.platform.organizations.dto.MunicipalityDto;
import com.civicfix.platform.organizations.service.CityService;
import com.civicfix.platform.organizations.service.MunicipalityService;
import com.civicfix.platform.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganizationAuthorizationTest {

    private MockMvc mockMvcCity;
    private MockMvc mockMvcDept;
    private MockMvc mockMvcTeam;
    private ObjectMapper objectMapper;

    private StubCityService stubCityService;
    private StubDepartmentService stubDeptService;
    private StubTeamService stubTeamService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        stubCityService = new StubCityService();
        stubDeptService = new StubDepartmentService();
        stubTeamService = new StubTeamService();

        CityController cityController = new CityController(stubCityService);
        DepartmentController deptController = new DepartmentController(stubDeptService);
        TeamController teamController = new TeamController(stubTeamService);

        mockMvcCity = MockMvcBuilders.standaloneSetup(cityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvcDept = MockMvcBuilders.standaloneSetup(deptController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvcTeam = MockMvcBuilders.standaloneSetup(teamController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private void authenticateAs(String username, String role) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        UserPrincipal principal = new UserPrincipal(
                1L, username, username + "@civicfix.org", "hash",
                1L, 1L, 1L,
                true, false, true,
                java.util.Set.of(role), java.util.Set.of(),
                List.of(authority)
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("GET Active Cities: accessible to authenticated citizen")
    void testCitizenCanListCities() throws Exception {
        authenticateAs("citizen_priya", "ROLE_CITIZEN");

        mockMvcCity.perform(get("/api/v1/cities/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST City: Admin is authorized to create city")
    void testAdminCanCreateCity() throws Exception {
        authenticateAs("admin_sharma", "ROLE_ADMIN");

        CreateCityRequest req = new CreateCityRequest();
        req.setName("Hyderabad");
        req.setCode("HYD");
        req.setState("Telangana");

        mockMvcCity.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("HYD"));
    }

    @Test
    @DisplayName("POST Department: Admin is authorized to create department")
    void testAdminCanCreateDepartment() throws Exception {
        authenticateAs("admin_sharma", "ROLE_ADMIN");

        CreateDepartmentRequest req = new CreateDepartmentRequest();
        req.setName("Water Supply");
        req.setCode("WATER");

        mockMvcDept.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("WATER"));
    }

    @Test
    @DisplayName("POST Team: Supervisor is authorized to create team")
    void testSupervisorCanCreateTeam() throws Exception {
        authenticateAs("supervisor_patil", "ROLE_SUPERVISOR");

        CreateTeamRequest req = new CreateTeamRequest();
        req.setDepartmentId(10L);
        req.setName("Night Sanitation Patrol");
        req.setCode("TEAM-SWM-NIGHT");

        mockMvcTeam.perform(post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("TEAM-SWM-NIGHT"));
    }

    // =========================================================================
    // Stubs
    // =========================================================================

    static class StubCityService extends CityService {
        public StubCityService() { super(null, null); }

        @Override
        public List<CityDto> getAllActiveCities() {
            CityDto blr = new CityDto();
            blr.setId(1L);
            blr.setName("Bengaluru");
            blr.setCode("BLR");
            return List.of(blr);
        }

        @Override
        public CityDto createCity(CreateCityRequest request, String performedBy) {
            CityDto dto = new CityDto();
            dto.setId(10L);
            dto.setName(request.getName());
            dto.setCode(request.getCode());
            dto.setState(request.getState());
            dto.setActive(true);
            return dto;
        }
    }

    static class StubDepartmentService extends DepartmentService {
        public StubDepartmentService() { super(null, null, null, null); }

        @Override
        public DepartmentDto createDepartment(CreateDepartmentRequest request, String performedBy) {
            DepartmentDto dto = new DepartmentDto();
            dto.setId(20L);
            dto.setName(request.getName());
            dto.setCode(request.getCode());
            dto.setActive(true);
            return dto;
        }
    }

    static class StubTeamService extends TeamService {
        public StubTeamService() { super(null, null, null, null); }

        @Override
        public TeamDto createTeam(CreateTeamRequest request, String performedBy) {
            TeamDto dto = new TeamDto();
            dto.setId(30L);
            dto.setDepartmentId(request.getDepartmentId());
            dto.setName(request.getName());
            dto.setCode(request.getCode());
            dto.setActive(true);
            return dto;
        }
    }
}
