package com.civicfix.platform.common;

import com.civicfix.platform.auth.controller.AuthController;
import com.civicfix.platform.auth.dto.AuthResponse;
import com.civicfix.platform.auth.dto.LoginRequest;
import com.civicfix.platform.auth.dto.RegisterRequest;
import com.civicfix.platform.auth.dto.UserProfileDto;
import com.civicfix.platform.auth.service.AuthService;
import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.CorrelationIdContext;
import com.civicfix.platform.common.api.CorrelationIdFilter;
import com.civicfix.platform.common.api.HealthController;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.GlobalExceptionHandler;
import com.civicfix.platform.organizations.controller.CityController;
import com.civicfix.platform.organizations.dto.CityDto;
import com.civicfix.platform.organizations.service.CityService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiInfrastructureHardeningTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    static class StubCityService extends CityService {
        public StubCityService() {
            super(null, null);
        }

        @Override
        public PagedResponse<CityDto> getCities(String query, Boolean active, Pageable pageable) {
            CityDto c1 = new CityDto();
            c1.setId(1L);
            c1.setName("Hyderabad");
            c1.setCode("HYD");
            c1.setState("Telangana");
            c1.setCountry("India");
            c1.setActive(true);

            CityDto c2 = new CityDto();
            c2.setId(2L);
            c2.setName("Bengaluru");
            c2.setCode("BLR");
            c2.setState("Karnataka");
            c2.setCountry("India");
            c2.setActive(true);

            return new PagedResponse<>(List.of(c1, c2), 0, 20, 2L, 1, true, true, false, false);
        }

        @Override
        public CityDto getCityById(Long id) {
            CityDto c = new CityDto();
            c.setId(id);
            c.setName("City " + id);
            return c;
        }
    }

    static class StubAuthService extends AuthService {
        public StubAuthService() {
            super(null, null, null, null, null, null, null, null, null, null);
        }

        @Override
        public AuthResponse login(LoginRequest request, String ipAddress) {
            UserProfileDto profile = new UserProfileDto();
            profile.setId(1L);
            profile.setUsername(request.getUsernameOrEmail());
            profile.setEmail("user@civic.local");
            profile.setFullName("Test User");
            return new AuthResponse("mock-jwt-token", "mock-refresh-token", 86400L, profile);
        }

        @Override
        public AuthResponse register(RegisterRequest request, String ipAddress) {
            UserProfileDto profile = new UserProfileDto();
            profile.setId(2L);
            profile.setUsername(request.getUsername());
            profile.setEmail(request.getEmail());
            profile.setFullName(request.getFullName());
            return new AuthResponse("mock-jwt-token", "mock-refresh-token", 86400L, profile);
        }
    }

    @RestController
    @RequestMapping("/api/v1/test-fault")
    static class TestFaultController {
        @GetMapping("/fatal")
        public ApiResponse<String> fatalError() {
            throw new RuntimeException("Simulated unexpected internal database failure: Table 'users_v2' does not exist in schema civicfix_prod");
        }

        @GetMapping("/required-param")
        public ApiResponse<String> requiredParam(@RequestParam String code) {
            return ApiResponse.ok("Code: " + code);
        }
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        CityService cityService = new StubCityService();
        AuthService authService = new StubAuthService();

        HealthController healthController = new HealthController();
        CityController cityController = new CityController(cityService);
        AuthController authController = new AuthController(authService);
        TestFaultController faultController = new TestFaultController();

        mockMvc = MockMvcBuilders
                .standaloneSetup(healthController, cityController, authController, faultController)
                .addFilters(new CorrelationIdFilter())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Correlation ID: Generated automatically when missing in incoming request")
    void testCorrelationIdGeneratedWhenMissing() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(CorrelationIdContext.HEADER_CORRELATION_ID))
                .andExpect(header().exists(CorrelationIdContext.HEADER_REQUEST_ID))
                .andReturn();

        String headerCorrId = result.getResponse().getHeader(CorrelationIdContext.HEADER_CORRELATION_ID);
        assertNotNull(headerCorrId);
        assertFalse(headerCorrId.isBlank());

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(json.get("success").asBoolean());
        assertEquals(200, json.get("statusCode").asInt());
        assertEquals(headerCorrId, json.get("requestId").asText(), "Body requestId must match response correlation header");
    }

    @Test
    @DisplayName("Correlation ID: Preserved and echoed back when provided by client")
    void testCorrelationIdPropagatedWhenProvided() throws Exception {
        String clientCorrelationId = "custom-client-audit-trace-12345";

        MvcResult result = mockMvc.perform(get("/api/v1/health")
                        .header(CorrelationIdContext.HEADER_CORRELATION_ID, clientCorrelationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdContext.HEADER_CORRELATION_ID, clientCorrelationId))
                .andExpect(header().string(CorrelationIdContext.HEADER_REQUEST_ID, clientCorrelationId))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(clientCorrelationId, json.get("requestId").asText());
    }

    @Test
    @DisplayName("Health, Liveness, and Readiness Probe Endpoints Return Standard Up Status")
    void testHealthChecksAndProbes() throws Exception {
        // Main Health Check
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.apiVersion").value("v1"));

        // Liveness Probe
        mockMvc.perform(get("/api/v1/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.probe").value("LIVENESS"));

        // Readiness Probe
        mockMvc.perform(get("/api/v1/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.probe").value("READINESS"));
    }

    @Test
    @DisplayName("Validation Failures: Return 400 BAD_REQUEST with Structured Field Errors")
    void testValidationFailureResponseFormat() throws Exception {
        // RegisterRequest with blank username, invalid email format, and blank password
        RegisterRequest invalidRequest = new RegisterRequest("", "invalid-email-format", "", "");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Malformed JSON Body: Returns 400 BAD_REQUEST with Security-Aware Message")
    void testMalformedJsonErrorHandling() throws Exception {
        String brokenJson = "{\"username\": \"priya\", \"password\": ";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_REQUEST_BODY"))
                .andExpect(jsonPath("$.message").value("Request body is missing, malformed, or contains invalid data types"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    @DisplayName("Type Mismatch: Returns 400 BAD_REQUEST for Invalid Path Variable Types")
    void testTypeMismatchErrorHandling() throws Exception {
        mockMvc.perform(get("/api/v1/cities/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errorCode").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.message").value("Parameter 'id' should be of type 'Long'"));
    }

    @Test
    @DisplayName("Missing Parameter: Returns 400 BAD_REQUEST when Required Query Param Omitted")
    void testMissingParameterHandling() throws Exception {
        mockMvc.perform(get("/api/v1/test-fault/required-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errorCode").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Required request parameter 'code' is missing"));
    }

    @Test
    @DisplayName("Method Not Allowed: Returns 405 METHOD_NOT_ALLOWED for Unsupported HTTP Verbs")
    void testMethodNotAllowedHandling() throws Exception {
        mockMvc.perform(delete("/api/v1/health"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(405))
                .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("Security-Aware Error Masking: Unexpected Exceptions Mask Database/Internal Details")
    void testSecurityAwareInternalErrorMasking() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/test-fault/fatal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        // Crucial security verification: Ensure internal database/schema details are NOT leaked to the caller
        assertFalse(body.contains("Table 'users_v2' does not exist"), "Must not expose internal table details");
        assertFalse(body.contains("civicfix_prod"), "Must not expose schema name");
        assertTrue(body.contains("An unexpected server error occurred. Please contact system support with Request ID:"));
    }

    @Test
    @DisplayName("Pagination and Sorting: Standard PagedResponse Contract Verification")
    void testPaginationAndSortingContract() throws Exception {
        mockMvc.perform(get("/api/v1/cities?page=0&size=20&sortBy=name&sortDirection=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.hasPrevious").value(false))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("Hyderabad"))
                .andExpect(jsonPath("$.data.content[1].name").value("Bengaluru"));
    }
}
