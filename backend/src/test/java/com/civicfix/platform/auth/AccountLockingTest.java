package com.civicfix.platform.auth;

import com.civicfix.platform.common.exception.AccountLockedException;
import com.civicfix.platform.security.service.LoginAttemptService;
import com.civicfix.platform.security.service.SecurityAuditService;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.repository.UserRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class AccountLockingTest {

    private FakeUserRepository fakeUserRepository;
    private FakeSecurityAuditService fakeAuditService;
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        this.fakeUserRepository = new FakeUserRepository();
        this.fakeAuditService = new FakeSecurityAuditService();
        this.loginAttemptService = new LoginAttemptService(fakeUserRepository, fakeAuditService);
    }

    @Test
    @DisplayName("Account Locking - Failed attempts increment until threshold is reached")
    void testFailedAttemptIncrementAndLockout() {
        User user = new User("officer_sharma", "sharma@city.gov", "hash", "Officer Sharma");
        assertEquals(0, user.getFailedLoginAttempts());
        assertFalse(user.isAccountLocked());

        // First 4 failed attempts should increment counter without locking
        for (int i = 1; i <= 4; i++) {
            loginAttemptService.loginFailed(user, "192.168.1.10");
            assertEquals(i, user.getFailedLoginAttempts());
            assertFalse(user.isAccountLocked());
        }

        // 5th failed attempt triggers account lock
        assertThrows(AccountLockedException.class, () -> {
            loginAttemptService.loginFailed(user, "192.168.1.10");
        });

        assertEquals(5, user.getFailedLoginAttempts());
        assertTrue(user.isAccountLocked());
        assertNotNull(user.getLockoutExpiresAt());
        assertTrue(user.getLockoutExpiresAt().isAfter(Instant.now()));

        assertEquals(5, fakeUserRepository.saveCount);
    }

    @Test
    @DisplayName("Account Locking - checkAccountLocked blocks login when account is locked")
    void testCheckAccountLockedThrowsException() {
        User user = new User("locked_user", "locked@city.gov", "hash", "Locked User");
        user.lockAccount(15);

        assertThrows(AccountLockedException.class, () -> {
            loginAttemptService.checkAccountLocked(user);
        });
    }

    @Test
    @DisplayName("Account Locking - Successful login resets failed attempt counter and updates last login")
    void testSuccessfulLoginResetsAttempts() {
        User user = new User("worker_ram", "ram@city.gov", "hash", "Worker Ram");
        user.setFailedLoginAttempts(3);

        loginAttemptService.loginSucceeded(user, "192.168.1.20");

        assertEquals(0, user.getFailedLoginAttempts());
        assertFalse(user.isAccountLocked());
        assertNotNull(user.getLastLoginAt());
        assertEquals(1, fakeUserRepository.saveCount);
    }

    @Test
    @DisplayName("Account Locking - Automatically unlocks after lockout duration has elapsed")
    void testAutomaticUnlockAfterExpiration() {
        User user = new User("temp_locked", "temp@city.gov", "hash", "Temp User");
        user.setAccountLocked(true);
        // Lock expired 5 minutes ago
        user.setLockoutExpiresAt(Instant.now().minus(5, ChronoUnit.MINUTES));

        assertTrue(user.isAccountNonLocked());
        assertFalse(user.isAccountLocked());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockoutExpiresAt());
    }

    // Lightweight test doubles without dynamic bytecode agents
    static class FakeUserRepository implements UserRepository {
        int saveCount = 0;

        @Override
        public <S extends User> S save(S entity) {
            saveCount++;
            return entity;
        }

        @Override public Optional<User> findByUsername(String username) { return Optional.empty(); }
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public Optional<User> findByUsernameOrEmail(String username, String email) { return Optional.empty(); }
        @Override public boolean existsByUsername(String username) { return false; }
        @Override public boolean existsByEmail(String email) { return false; }
        @Override public Page<User> findAllByActiveTrue(Pageable pageable) { return null; }
        @Override public Page<User> findByOrganizationIdAndActiveTrue(Long organizationId, Pageable pageable) { return null; }
        @Override public Page<User> findByDepartmentIdAndActiveTrue(Long departmentId, Pageable pageable) { return null; }
        @Override public <S extends User> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<User> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<User> findAll() { return null; }
        @Override public List<User> findAllById(Iterable<Long> longs) { return null; }
        @Override public long count() { return 0; }
        @Override public void deleteById(Long aLong) {}
        @Override public void delete(User entity) {}
        @Override public void deleteAllById(Iterable<? extends Long> longs) {}
        @Override public void deleteAll(Iterable<? extends User> entities) {}
        @Override public void deleteAll() {}
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
        @Override public List<User> findAll(Sort sort) { return null; }
        @Override public Page<User> findAll(Pageable pageable) { return null; }
    }

    static class FakeSecurityAuditService extends SecurityAuditService {
        FakeSecurityAuditService() {
            super(null);
        }

        @Override
        public void record(String entityName, String entityId, com.civicfix.platform.audit.enums.AuditAction action, String performedBy, String ipAddress, String details) {
            // Recorded in memory
        }
    }
}
