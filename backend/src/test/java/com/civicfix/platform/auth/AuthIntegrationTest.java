package com.civicfix.platform.auth;

import com.civicfix.platform.auth.controller.AuthController;
import com.civicfix.platform.auth.dto.*;
import com.civicfix.platform.auth.entity.EmailVerificationToken;
import com.civicfix.platform.auth.entity.PasswordResetToken;
import com.civicfix.platform.auth.entity.RefreshToken;
import com.civicfix.platform.auth.repository.EmailVerificationTokenRepository;
import com.civicfix.platform.auth.repository.PasswordResetTokenRepository;
import com.civicfix.platform.auth.repository.RefreshTokenRepository;
import com.civicfix.platform.auth.service.AuthService;
import com.civicfix.platform.common.exception.AccountLockedException;
import com.civicfix.platform.common.exception.GlobalExceptionHandler;
import com.civicfix.platform.security.jwt.JwtTokenProvider;
import com.civicfix.platform.security.service.LoginAttemptService;
import com.civicfix.platform.security.service.SecurityAuditService;
import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.users.repository.RoleRepository;
import com.civicfix.platform.users.repository.UserRepository;
import com.civicfix.platform.wards.repository.WardRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AuthService authService;
    private PasswordEncoder passwordEncoder;

    private InMemoryUserRepository userRepository;
    private InMemoryRoleRepository roleRepository;
    private InMemoryRefreshTokenRepository refreshTokenRepository;
    private InMemoryPasswordResetTokenRepository passwordResetTokenRepository;
    private InMemoryEmailVerificationTokenRepository emailVerificationTokenRepository;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.passwordEncoder = new BCryptPasswordEncoder();

        this.userRepository = new InMemoryUserRepository();
        this.roleRepository = new InMemoryRoleRepository();
        this.refreshTokenRepository = new InMemoryRefreshTokenRepository();
        this.passwordResetTokenRepository = new InMemoryPasswordResetTokenRepository();
        this.emailVerificationTokenRepository = new InMemoryEmailVerificationTokenRepository();

        for (RoleType rt : RoleType.values()) {
            roleRepository.save(new Role(rt, rt.name()));
        }

        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
                "civicfixSuperSecretKeyForDevelopmentMustBe32CharactersOrLonger",
                86400000L,
                604800000L
        );

        SecurityAuditService auditService = new SecurityAuditService(null) {
            @Override
            public void record(String entityName, String entityId, com.civicfix.platform.audit.enums.AuditAction action, String performedBy, String ipAddress, String details) {}
        };

        LoginAttemptService loginAttemptService = new LoginAttemptService(userRepository, auditService);

        this.authService = new AuthService(
                userRepository,
                roleRepository,
                new WardRepositoryStub(),
                refreshTokenRepository,
                passwordResetTokenRepository,
                emailVerificationTokenRepository,
                passwordEncoder,
                jwtTokenProvider,
                loginAttemptService,
                auditService
        );

        AuthController authController = new AuthController(authService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("API Integration - Registration succeeds with 201 Created and creates user with tokens")
    void testRegistrationEndpoint() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "citizen_priya",
                "priya@example.com",
                "StrongPass#2026",
                "Priya Sharma"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("citizen_priya"))
                .andExpect(jsonPath("$.data.user.roles[0]").value("ROLE_CITIZEN"));

        User savedUser = userRepository.findByUsername("citizen_priya").orElse(null);
        assertNotNull(savedUser);
        assertTrue(passwordEncoder.matches("StrongPass#2026", savedUser.getPasswordHash()));
        assertFalse(savedUser.isEmailVerified());
        assertTrue(emailVerificationTokenRepository.findTopByUserOrderByCreatedAtDesc(savedUser).isPresent());
    }

    @Test
    @DisplayName("API Integration - Login succeeds with 200 OK and returns access + refresh tokens")
    void testLoginSuccessEndpoint() throws Exception {
        User user = new User("officer_verma", "verma@city.gov", passwordEncoder.encode("OfficerPass#2026"), "Officer Verma");
        user.setId(10L);
        user.getRoles().add(roleRepository.findByName(RoleType.ROLE_EMPLOYEE).orElseThrow());
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("officer_verma", "OfficerPass#2026");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("officer_verma"));
    }

    @Test
    @DisplayName("API Integration - Invalid credentials returns 401 Unauthorized and locks account after 5 failed attempts")
    void testInvalidCredentialsAndAccountLocking() throws Exception {
        User user = new User("locked_target", "locked@city.gov", passwordEncoder.encode("TargetPass#2026"), "Target");
        user.setId(20L);
        user.getRoles().add(roleRepository.findByName(RoleType.ROLE_CITIZEN).orElseThrow());
        userRepository.save(user);

        LoginRequest wrongLogin = new LoginRequest("locked_target", "BadPassword#123");

        // 4 failed attempts -> 401 Unauthorized
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongLogin)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
        }

        // 5th failed attempt -> 423 Locked
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_LOCKED"));

        assertTrue(user.isAccountLocked());
    }

    @Test
    @DisplayName("API Integration - Refresh token endpoint rotates token successfully")
    void testRefreshTokenRotationEndpoint() throws Exception {
        User user = new User("refresh_user", "refresh@city.gov", passwordEncoder.encode("Pass#2026"), "Refresh User");
        user.setId(30L);
        user.getRoles().add(roleRepository.findByName(RoleType.ROLE_CITIZEN).orElseThrow());
        userRepository.save(user);

        String initialRefreshToken = "test-refresh-token-123";
        RefreshToken token = new RefreshToken(user, initialRefreshToken, Instant.now().plus(7, ChronoUnit.DAYS), "127.0.0.1");
        refreshTokenRepository.save(token);

        RefreshTokenRequest request = new RefreshTokenRequest(initialRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());

        // Confirm original token was revoked and replaced
        RefreshToken oldToken = refreshTokenRepository.findByToken(initialRefreshToken).orElseThrow();
        assertTrue(oldToken.isRevoked());
        assertNotNull(oldToken.getReplacedByToken());
    }

    @Test
    @DisplayName("API Integration - Password reset workflow completes successfully")
    void testPasswordResetFlow() throws Exception {
        User user = new User("pwd_reset_user", "pwdreset@city.gov", passwordEncoder.encode("OldPass#2026"), "Password Reset User");
        user.setId(40L);
        user.getRoles().add(roleRepository.findByName(RoleType.ROLE_CITIZEN).orElseThrow());
        userRepository.save(user);

        // 1. Request password reset
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest("pwd_reset_user");
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resetToken").isNotEmpty());

        PasswordResetToken resetToken = passwordResetTokenRepository.store.values().stream().findFirst().orElseThrow();

        // 2. Reset password using token
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(resetToken.getToken(), "NewPassword#2026");
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk());

        // 3. Confirm password was updated in repository
        User updated = userRepository.findById(40L).orElseThrow();
        assertTrue(passwordEncoder.matches("NewPassword#2026", updated.getPasswordHash()));
        assertTrue(resetToken.isUsed());
    }

    @Test
    @DisplayName("API Integration - Email verification confirms email token")
    void testEmailVerificationEndpoint() throws Exception {
        User user = new User("unverified_user", "unverified@city.gov", passwordEncoder.encode("Pass#2026"), "Unverified");
        user.setId(50L);
        user.setEmailVerified(false);
        userRepository.save(user);

        EmailVerificationToken token = new EmailVerificationToken(user, "verify-token-xyz", Instant.now().plus(24, ChronoUnit.HOURS));
        emailVerificationTokenRepository.save(token);

        mockMvc.perform(get("/api/v1/auth/verify-email")
                        .param("token", "verify-token-xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Email address verified successfully"));

        User verifiedUser = userRepository.findById(50L).orElseThrow();
        assertTrue(verifiedUser.isEmailVerified());
    }

    // In-memory test doubles avoiding ByteBuddy dynamic agents
    static class InMemoryUserRepository implements UserRepository {
        Map<Long, User> store = new HashMap<>();
        long idSeq = 1;

        @Override
        public <S extends User> S save(S entity) {
            if (entity.getId() == null) entity.setId(idSeq++);
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<User> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override
        public Optional<User> findByUsername(String username) {
            return store.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
        }
        @Override
        public Optional<User> findByEmail(String email) {
            return store.values().stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }
        @Override
        public Optional<User> findByUsernameOrEmail(String username, String email) {
            return store.values().stream().filter(u -> u.getUsername().equals(username) || u.getEmail().equals(email)).findFirst();
        }
        @Override
        public boolean existsByUsername(String username) {
            return store.values().stream().anyMatch(u -> u.getUsername().equals(username));
        }
        @Override
        public boolean existsByEmail(String email) {
            return store.values().stream().anyMatch(u -> u.getEmail().equals(email));
        }

        @Override public Page<User> findAllByActiveTrue(Pageable pageable) { return null; }
        @Override public Page<User> findByOrganizationIdAndActiveTrue(Long organizationId, Pageable pageable) { return null; }
        @Override public Page<User> findByDepartmentIdAndActiveTrue(Long departmentId, Pageable pageable) { return null; }
        @Override public void flush() {}
        @Override public <S extends User> S saveAndFlush(S entity) { return null; }
        @Override public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<User> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public User getOne(Long aLong) { return null; }
        @Override public User getById(Long aLong) { return null; }
        @Override public User getReferenceById(Long aLong) { return null; }
        @Override public <S extends User> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends User> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends User> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends User> long count(Example<S> example) { return 0; }
        @Override public <S extends User> boolean exists(Example<S> example) { return false; }
        @Override public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends User> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public boolean existsById(Long aLong) { return store.containsKey(aLong); }
        @Override public List<User> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<User> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) { store.remove(aLong); }
        @Override public void delete(User entity) { store.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends User> entities) {}
        @Override public void deleteAll() { store.clear(); }
        @Override public List<User> findAll(Sort sort) { return null; }
        @Override public Page<User> findAll(Pageable pageable) { return null; }
    }

    static class InMemoryRoleRepository implements RoleRepository {
        Map<RoleType, Role> store = new HashMap<>();
        long idSeq = 1;

        @Override
        public Optional<Role> findByName(RoleType name) { return Optional.ofNullable(store.get(name)); }

        @Override
        public <S extends Role> S save(S entity) {
            if (entity.getId() == null) entity.setId(idSeq++);
            store.put(entity.getName(), entity);
            return entity;
        }

        @Override public void flush() {}
        @Override public <S extends Role> S saveAndFlush(S entity) { return null; }
        @Override public <S extends Role> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Role> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public Role getOne(Long aLong) { return null; }
        @Override public Role getById(Long aLong) { return null; }
        @Override public Role getReferenceById(Long aLong) { return null; }
        @Override public <S extends Role> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends Role> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends Role> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends Role> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends Role> long count(Example<S> example) { return 0; }
        @Override public <S extends Role> boolean exists(Example<S> example) { return false; }
        @Override public <S extends Role, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Role> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<Role> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<Role> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Role> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(Role entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends Role> entities) {}
        @Override public void deleteAll() {}
        @Override public List<Role> findAll(Sort sort) { return null; }
        @Override public Page<Role> findAll(Pageable pageable) { return null; }
    }

    static class InMemoryRefreshTokenRepository implements RefreshTokenRepository {
        Map<String, RefreshToken> store = new HashMap<>();

        @Override public Optional<RefreshToken> findByToken(String token) { return Optional.ofNullable(store.get(token)); }
        @Override public <S extends RefreshToken> S save(S entity) { store.put(entity.getToken(), entity); return entity; }
        @Override public void revokeAllUserTokens(User user, Instant now) {
            for (RefreshToken t : store.values()) {
                if (t.getUser().getId().equals(user.getId())) t.revoke(null);
            }
        }
        @Override public List<RefreshToken> findByUserAndRevokedFalseAndExpiryDateAfter(User user, Instant now) { return List.of(); }
        @Override public List<RefreshToken> findByUserId(Long userId) { return List.of(); }
        @Override public void flush() {}
        @Override public <S extends RefreshToken> S saveAndFlush(S entity) { return null; }
        @Override public <S extends RefreshToken> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<RefreshToken> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public RefreshToken getOne(Long aLong) { return null; }
        @Override public RefreshToken getById(Long aLong) { return null; }
        @Override public RefreshToken getReferenceById(Long aLong) { return null; }
        @Override public <S extends RefreshToken> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends RefreshToken> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends RefreshToken> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends RefreshToken> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends RefreshToken> long count(Example<S> example) { return 0; }
        @Override public <S extends RefreshToken> boolean exists(Example<S> example) { return false; }
        @Override public <S extends RefreshToken, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends RefreshToken> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<RefreshToken> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<RefreshToken> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<RefreshToken> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return store.size(); }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(RefreshToken entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends RefreshToken> entities) {}
        @Override public void deleteAll() {}
        @Override public List<RefreshToken> findAll(Sort sort) { return null; }
        @Override public Page<RefreshToken> findAll(Pageable pageable) { return null; }
    }

    static class InMemoryPasswordResetTokenRepository implements PasswordResetTokenRepository {
        Map<String, PasswordResetToken> store = new HashMap<>();

        @Override public Optional<PasswordResetToken> findByToken(String token) { return Optional.ofNullable(store.get(token)); }
        @Override public <S extends PasswordResetToken> S save(S entity) { store.put(entity.getToken(), entity); return entity; }
        @Override public void invalidateAllUserTokens(User user) {
            for (PasswordResetToken t : store.values()) {
                if (t.getUser().getId().equals(user.getId())) t.setUsed(true);
            }
        }
        @Override public void flush() {}
        @Override public <S extends PasswordResetToken> S saveAndFlush(S entity) { return null; }
        @Override public <S extends PasswordResetToken> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<PasswordResetToken> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public PasswordResetToken getOne(Long aLong) { return null; }
        @Override public PasswordResetToken getById(Long aLong) { return null; }
        @Override public PasswordResetToken getReferenceById(Long aLong) { return null; }
        @Override public <S extends PasswordResetToken> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends PasswordResetToken> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends PasswordResetToken> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends PasswordResetToken> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends PasswordResetToken> long count(Example<S> example) { return 0; }
        @Override public <S extends PasswordResetToken> boolean exists(Example<S> example) { return false; }
        @Override public <S extends PasswordResetToken, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends PasswordResetToken> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<PasswordResetToken> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<PasswordResetToken> findAll() { return null; }
        @Override public List<PasswordResetToken> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(PasswordResetToken entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends PasswordResetToken> entities) {}
        @Override public void deleteAll() {}
        @Override public List<PasswordResetToken> findAll(Sort sort) { return null; }
        @Override public Page<PasswordResetToken> findAll(Pageable pageable) { return null; }
    }

    static class InMemoryEmailVerificationTokenRepository implements EmailVerificationTokenRepository {
        Map<String, EmailVerificationToken> store = new HashMap<>();

        @Override public Optional<EmailVerificationToken> findByToken(String token) { return Optional.ofNullable(store.get(token)); }
        @Override public Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(User user) {
            return store.values().stream().filter(t -> t.getUser().getId().equals(user.getId())).findFirst();
        }
        @Override public <S extends EmailVerificationToken> S save(S entity) { store.put(entity.getToken(), entity); return entity; }
        @Override public void flush() {}
        @Override public <S extends EmailVerificationToken> S saveAndFlush(S entity) { return null; }
        @Override public <S extends EmailVerificationToken> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<EmailVerificationToken> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public EmailVerificationToken getOne(Long aLong) { return null; }
        @Override public EmailVerificationToken getById(Long aLong) { return null; }
        @Override public EmailVerificationToken getReferenceById(Long aLong) { return null; }
        @Override public <S extends EmailVerificationToken> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends EmailVerificationToken> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends EmailVerificationToken> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends EmailVerificationToken> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends EmailVerificationToken> long count(Example<S> example) { return 0; }
        @Override public <S extends EmailVerificationToken> boolean exists(Example<S> example) { return false; }
        @Override public <S extends EmailVerificationToken, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends EmailVerificationToken> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<EmailVerificationToken> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<EmailVerificationToken> findAll() { return null; }
        @Override public List<EmailVerificationToken> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(EmailVerificationToken entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends EmailVerificationToken> entities) {}
        @Override public void deleteAll() {}
        @Override public List<EmailVerificationToken> findAll(Sort sort) { return null; }
        @Override public Page<EmailVerificationToken> findAll(Pageable pageable) { return null; }
    }

    static class WardRepositoryStub implements WardRepository {
        @Override public List<com.civicfix.platform.wards.entity.Ward> findByOrganizationIdAndActiveTrue(Long organizationId) { return List.of(); }
        @Override public Page<com.civicfix.platform.wards.entity.Ward> findByOrganizationId(Long organizationId, Pageable pageable) { return null; }
        @Override public Optional<com.civicfix.platform.wards.entity.Ward> findByOrganizationIdAndWardNumber(Long organizationId, String wardNumber) { return Optional.empty(); }
        @Override public boolean existsByOrganizationIdAndWardNumber(Long organizationId, String wardNumber) { return false; }
        @Override public List<com.civicfix.platform.wards.entity.Ward> findByMunicipalityIdAndActiveTrue(Long municipalityId) { return List.of(); }
        @Override public Page<com.civicfix.platform.wards.entity.Ward> findByMunicipalityId(Long municipalityId, Pageable pageable) { return null; }
        @Override public Optional<com.civicfix.platform.wards.entity.Ward> findByMunicipalityIdAndWardNumber(Long municipalityId, String wardNumber) { return Optional.empty(); }
        @Override public boolean existsByMunicipalityIdAndWardNumber(Long municipalityId, String wardNumber) { return false; }
        @Override public Page<com.civicfix.platform.wards.entity.Ward> searchWards(Long municipalityId, Long organizationId, String zoneName, Boolean active, String query, Pageable pageable) { return null; }
        @Override public void flush() {}
        @Override public <S extends com.civicfix.platform.wards.entity.Ward> S saveAndFlush(S entity) { return null; }
        @Override public <S extends com.civicfix.platform.wards.entity.Ward> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<com.civicfix.platform.wards.entity.Ward> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) {}
        @Override public void deleteAllInBatch() {}
        @Override public com.civicfix.platform.wards.entity.Ward getOne(Long aLong) { return null; }
        @Override public com.civicfix.platform.wards.entity.Ward getById(Long aLong) { return null; }
        @Override public com.civicfix.platform.wards.entity.Ward getReferenceById(Long aLong) { return null; }
        @Override public <S extends com.civicfix.platform.wards.entity.Ward> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
        @Override public <S extends com.civicfix.platform.wards.entity.Ward> List<S> findAll(Example<S> example) { return null; }
        @Override public <S extends com.civicfix.platform.wards.entity.Ward> List<S> findAll(Example<S> example, Sort sort) { return null; }
        @Override public <S extends com.civicfix.platform.wards.entity.Ward> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
        @Override public <S extends com.civicfix.platform.wards.entity.Ward> long count(Example<S> example) { return 0; }
        @Override public <S extends com.civicfix.platform.wards.entity.Ward> boolean exists(Example<S> example) { return false; }
        @Override public <S extends com.civicfix.platform.wards.entity.Ward, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends com.civicfix.platform.wards.entity.Ward> S save(S entity) { return entity; }
        @Override public <S extends com.civicfix.platform.wards.entity.Ward> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<com.civicfix.platform.wards.entity.Ward> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<com.civicfix.platform.wards.entity.Ward> findAll() { return List.of(); }
        @Override public List<com.civicfix.platform.wards.entity.Ward> findAllById(Iterable<Long> longs) { return List.of(); }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(com.civicfix.platform.wards.entity.Ward entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends com.civicfix.platform.wards.entity.Ward> entities) {}
        @Override public void deleteAll() {}
        @Override public List<com.civicfix.platform.wards.entity.Ward> findAll(Sort sort) { return null; }
        @Override public Page<com.civicfix.platform.wards.entity.Ward> findAll(Pageable pageable) { return null; }
    }
}
