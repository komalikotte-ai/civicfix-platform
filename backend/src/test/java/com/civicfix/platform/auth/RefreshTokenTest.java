package com.civicfix.platform.auth;

import com.civicfix.platform.auth.dto.AuthResponse;
import com.civicfix.platform.auth.entity.RefreshToken;
import com.civicfix.platform.auth.repository.EmailVerificationTokenRepository;
import com.civicfix.platform.auth.repository.PasswordResetTokenRepository;
import com.civicfix.platform.auth.repository.RefreshTokenRepository;
import com.civicfix.platform.auth.service.AuthService;
import com.civicfix.platform.common.exception.TokenExpiredException;
import com.civicfix.platform.common.exception.TokenReuseDetectedException;
import com.civicfix.platform.security.jwt.JwtTokenProvider;
import com.civicfix.platform.security.service.LoginAttemptService;
import com.civicfix.platform.security.service.SecurityAuditService;
import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenTest {

    private FakeRefreshTokenRepository fakeRefreshTokenRepository;
    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        this.fakeRefreshTokenRepository = new FakeRefreshTokenRepository();
        this.jwtTokenProvider = new JwtTokenProvider(
                "civicfixSuperSecretKeyForDevelopmentMustBe32CharactersOrLonger",
                60000L,
                120000L
        );

        this.authService = new AuthService(
                null,
                null,
                null,
                fakeRefreshTokenRepository,
                null,
                null,
                null,
                jwtTokenProvider,
                null,
                new SecurityAuditService(null) {
                    @Override
                    public void record(String entityName, String entityId, com.civicfix.platform.audit.enums.AuditAction action, String performedBy, String ipAddress, String details) {}
                }
        );
    }

    @Test
    @DisplayName("Refresh Token - Rotation revokes old token and issues a new pair")
    void testRefreshTokenRotation() {
        User user = new User("employee_neha", "neha@city.gov", "hash", "Neha Employee");
        user.setId(55L);
        user.getRoles().add(new Role(RoleType.ROLE_EMPLOYEE, "Employee"));

        String oldTokenStr = "valid-refresh-token-xyz";
        RefreshToken existingToken = new RefreshToken(
                user,
                oldTokenStr,
                Instant.now().plus(7, ChronoUnit.DAYS),
                "127.0.0.1"
        );
        fakeRefreshTokenRepository.store.put(oldTokenStr, existingToken);

        AuthResponse response = authService.refreshToken(oldTokenStr, "127.0.0.1");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(oldTokenStr, response.getRefreshToken());

        // Verify old token was revoked
        assertTrue(existingToken.isRevoked());
        assertNotNull(existingToken.getRevokedAt());
        assertEquals(response.getRefreshToken(), existingToken.getReplacedByToken());

        // Verify new token exists in store
        assertTrue(fakeRefreshTokenRepository.store.containsKey(response.getRefreshToken()));
    }

    @Test
    @DisplayName("Refresh Token - Token reuse detection terminates all user sessions")
    void testTokenReuseDetectionAlert() {
        User user = new User("compromised_user", "comp@city.gov", "hash", "Compromised User");
        user.setId(77L);

        String reusedTokenStr = "already-revoked-token";
        RefreshToken revokedToken = new RefreshToken(
                user,
                reusedTokenStr,
                Instant.now().plus(7, ChronoUnit.DAYS),
                "127.0.0.1"
        );
        revokedToken.revoke("subsequent-token-123");
        fakeRefreshTokenRepository.store.put(reusedTokenStr, revokedToken);

        assertThrows(TokenReuseDetectedException.class, () -> {
            authService.refreshToken(reusedTokenStr, "192.168.1.100");
        });

        assertTrue(fakeRefreshTokenRepository.allTokensRevoked);
    }

    @Test
    @DisplayName("Refresh Token - Expired refresh token is rejected")
    void testExpiredRefreshTokenRejection() {
        User user = new User("expired_token_user", "exp@city.gov", "hash", "Expired User");
        String expiredTokenStr = "expired-token-val";
        RefreshToken expiredToken = new RefreshToken(
                user,
                expiredTokenStr,
                Instant.now().minus(1, ChronoUnit.DAYS),
                "127.0.0.1"
        );
        fakeRefreshTokenRepository.store.put(expiredTokenStr, expiredToken);

        assertThrows(TokenExpiredException.class, () -> {
            authService.refreshToken(expiredTokenStr, "127.0.0.1");
        });
    }

    static class FakeRefreshTokenRepository implements RefreshTokenRepository {
        Map<String, RefreshToken> store = new HashMap<>();
        boolean allTokensRevoked = false;

        @Override
        public Optional<RefreshToken> findByToken(String token) {
            return Optional.ofNullable(store.get(token));
        }

        @Override
        public <S extends RefreshToken> S save(S entity) {
            store.put(entity.getToken(), entity);
            return entity;
        }

        @Override
        public void revokeAllUserTokens(User user, Instant now) {
            allTokensRevoked = true;
            for (RefreshToken t : store.values()) {
                if (t.getUser().equals(user)) {
                    t.revoke(null);
                }
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
        @Override public List<RefreshToken> findAll() { return null; }
        @Override public List<RefreshToken> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(RefreshToken entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends RefreshToken> entities) {}
        @Override public void deleteAll() {}
        @Override public List<RefreshToken> findAll(Sort sort) { return null; }
        @Override public Page<RefreshToken> findAll(Pageable pageable) { return null; }
    }
}
