package com.civicfix.platform.security.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.common.exception.AccountLockedException;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCKOUT_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final SecurityAuditService auditService;

    public LoginAttemptService(UserRepository userRepository, SecurityAuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void checkAccountLocked(User user) {
        if (!user.isAccountNonLocked()) {
            long minutesRemaining = 0;
            if (user.getLockoutExpiresAt() != null) {
                minutesRemaining = Math.max(1, Duration.between(Instant.now(), user.getLockoutExpiresAt()).toMinutes());
            }
            log.warn("Login blocked for locked user '{}', {} minutes remaining", user.getUsername(), minutesRemaining);
            throw new AccountLockedException(minutesRemaining);
        }
    }

    @Transactional
    public void loginSucceeded(User user, String ipAddress) {
        if (user.getFailedLoginAttempts() > 0 || user.isAccountLocked()) {
            user.resetFailedAttempts();
        }
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        auditService.record(
                "USER",
                String.valueOf(user.getId()),
                AuditAction.LOGIN_SUCCESS,
                user.getUsername(),
                ipAddress,
                "User logged in successfully"
        );
    }

    @Transactional
    public void loginFailed(User user, String ipAddress) {
        user.incrementFailedAttempts();

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.lockAccount(LOCKOUT_DURATION_MINUTES);
            userRepository.save(user);

            auditService.record(
                    "USER",
                    String.valueOf(user.getId()),
                    AuditAction.ACCOUNT_LOCKED,
                    user.getUsername(),
                    ipAddress,
                    String.format("Account locked for %d minutes due to %d consecutive failed login attempts",
                            LOCKOUT_DURATION_MINUTES, user.getFailedLoginAttempts())
            );
            log.warn("Account '{}' locked after {} failed attempts", user.getUsername(), user.getFailedLoginAttempts());
            throw new AccountLockedException(LOCKOUT_DURATION_MINUTES);
        } else {
            userRepository.save(user);
            int attemptsLeft = MAX_FAILED_ATTEMPTS - user.getFailedLoginAttempts();

            auditService.record(
                    "USER",
                    String.valueOf(user.getId()),
                    AuditAction.LOGIN_FAILURE,
                    user.getUsername(),
                    ipAddress,
                    String.format("Failed login attempt (%d/%d)", user.getFailedLoginAttempts(), MAX_FAILED_ATTEMPTS)
            );
            log.warn("Failed login attempt for user '{}' ({}/{} attempts)",
                    user.getUsername(), user.getFailedLoginAttempts(), MAX_FAILED_ATTEMPTS);
        }
    }
}
