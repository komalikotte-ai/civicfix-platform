package com.civicfix.platform.auth.service;

import com.civicfix.platform.audit.enums.AuditAction;
import com.civicfix.platform.auth.dto.*;
import com.civicfix.platform.auth.entity.EmailVerificationToken;
import com.civicfix.platform.auth.entity.PasswordResetToken;
import com.civicfix.platform.auth.entity.RefreshToken;
import com.civicfix.platform.auth.repository.EmailVerificationTokenRepository;
import com.civicfix.platform.auth.repository.PasswordResetTokenRepository;
import com.civicfix.platform.auth.repository.RefreshTokenRepository;
import com.civicfix.platform.common.exception.*;
import com.civicfix.platform.security.SecurityUtils;
import com.civicfix.platform.security.UserPrincipal;
import com.civicfix.platform.security.jwt.JwtTokenProvider;
import com.civicfix.platform.security.service.LoginAttemptService;
import com.civicfix.platform.security.service.SecurityAuditService;
import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.users.repository.RoleRepository;
import com.civicfix.platform.users.repository.UserRepository;
import com.civicfix.platform.wards.entity.Ward;
import com.civicfix.platform.wards.repository.WardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int RESET_TOKEN_VALIDITY_MINUTES = 30;
    private static final int VERIFICATION_TOKEN_VALIDITY_HOURS = 24;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WardRepository wardRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final SecurityAuditService securityAuditService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       WardRepository wardRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       LoginAttemptService loginAttemptService,
                       SecurityAuditService securityAuditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.wardRepository = wardRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.loginAttemptService = loginAttemptService;
        this.securityAuditService = securityAuditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmailVerified(false);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        RoleType targetRole = request.getRole() != null ? request.getRole() : RoleType.ROLE_CITIZEN;
        Role role = roleRepository.findByName(targetRole)
                .orElseGet(() -> roleRepository.save(new Role(targetRole, "System role " + targetRole.name())));
        user.getRoles().add(role);

        if (request.getWardId() != null) {
            Ward ward = wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ward", "id", request.getWardId()));
            user.setWard(ward);
            user.setOrganization(ward.getOrganization());
        }

        User saved = userRepository.save(user);

        // Generate email verification token
        createEmailVerificationToken(saved);

        securityAuditService.record("USER", String.valueOf(saved.getId()), AuditAction.CREATE,
                saved.getUsername(), ipAddress, "User registered with role " + targetRole.name());

        return issueAuthResponse(saved, ipAddress);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElse(null);

        if (user == null) {
            securityAuditService.record("AUTH", "UNKNOWN", AuditAction.LOGIN_FAILURE,
                    request.getUsernameOrEmail(), ipAddress, "Login failed: User not found");
            throw new BadCredentialsException("Invalid username or password");
        }

        // Verify account lock status
        loginAttemptService.checkAccountLocked(user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.loginFailed(user, ipAddress);
            throw new BadCredentialsException("Invalid username or password");
        }

        // Password verified successfully
        loginAttemptService.loginSucceeded(user, ipAddress);

        return issueAuthResponse(user, ipAddress);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenString, String ipAddress) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        User user = token.getUser();

        // Detect token reuse
        if (token.isRevoked()) {
            refreshTokenRepository.revokeAllUserTokens(user, Instant.now());
            securityAuditService.record("AUTH", String.valueOf(user.getId()),
                    AuditAction.REFRESH_TOKEN_REUSED_ALERT, user.getUsername(), ipAddress,
                    "Security alert: Revoked refresh token reuse detected. All sessions terminated.");
            log.error("CRITICAL: Revoked refresh token reused by user '{}'. Terminating all active sessions.", user.getUsername());
            throw new TokenReuseDetectedException("Security alert: Revoked refresh token reuse detected. All active sessions have been terminated.");
        }

        if (token.isExpired()) {
            throw new TokenExpiredException("Refresh token has expired. Please log in again.");
        }

        // Rotate token
        String newRefreshTokenValue = UUID.randomUUID().toString();
        token.revoke(newRefreshTokenValue);
        refreshTokenRepository.save(token);

        RefreshToken newToken = new RefreshToken(
                user,
                newRefreshTokenValue,
                Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs()),
                ipAddress
        );
        refreshTokenRepository.save(newToken);

        securityAuditService.record("AUTH", String.valueOf(user.getId()),
                AuditAction.REFRESH_TOKEN_ROTATED, user.getUsername(), ipAddress,
                "Refresh token rotated successfully");

        return buildAuthResponse(user, newRefreshTokenValue);
    }

    @Transactional
    public void logout(String refreshTokenString, String ipAddress) {
        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenString).ifPresent(t -> {
                t.revoke(null);
                refreshTokenRepository.save(t);
            });
        }

        SecurityUtils.getCurrentUsername().ifPresent(username -> {
            securityAuditService.record("AUTH", username, AuditAction.LOGOUT,
                    username, ipAddress, "User logged out");
        });
    }

    @Transactional
    public void logoutAllSessions(String ipAddress) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        refreshTokenRepository.revokeAllUserTokens(user, Instant.now());

        securityAuditService.record("AUTH", String.valueOf(user.getId()),
                AuditAction.SESSION_REVOKED, user.getUsername(), ipAddress,
                "All user sessions and refresh tokens invalidated");
    }

    @Transactional(readOnly = true)
    public List<UserSessionDto> getUserSessions() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        return refreshTokenRepository.findByUserId(principal.getId()).stream()
                .map(UserSessionDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeSession(Long sessionId) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        RefreshToken session = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));

        if (!session.getUser().getId().equals(principal.getId()) && !SecurityUtils.hasRole("ADMIN")) {
            throw new ForbiddenException("Cannot revoke session belonging to another user");
        }

        session.revoke(null);
        refreshTokenRepository.save(session);
    }

    @Transactional
    public String requestPasswordReset(String emailOrUsername, String ipAddress) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(emailOrUsername, emailOrUsername);

        if (userOpt.isEmpty()) {
            // Silently return to prevent username enumeration
            return "If an account exists, a password reset link has been dispatched.";
        }

        User user = userOpt.get();
        passwordResetTokenRepository.invalidateAllUserTokens(user);

        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(RESET_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES);

        PasswordResetToken resetToken = new PasswordResetToken(user, token, expiryDate);
        passwordResetTokenRepository.save(resetToken);

        securityAuditService.record("AUTH", String.valueOf(user.getId()),
                AuditAction.PASSWORD_RESET_REQUEST, user.getUsername(), ipAddress,
                "Password reset requested");

        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String ipAddress) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (!resetToken.isValid()) {
            throw new InvalidTokenException("Password reset token is invalid, already used, or expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.unlockAccount();
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all existing refresh tokens for security
        refreshTokenRepository.revokeAllUserTokens(user, Instant.now());

        securityAuditService.record("AUTH", String.valueOf(user.getId()),
                AuditAction.PASSWORD_RESET_SUCCESS, user.getUsername(), ipAddress,
                "Password reset successfully completed");
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String ipAddress) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate other active sessions
        refreshTokenRepository.revokeAllUserTokens(user, Instant.now());

        securityAuditService.record("AUTH", String.valueOf(user.getId()),
                AuditAction.PASSWORD_CHANGED, user.getUsername(), ipAddress,
                "User changed password successfully");
    }

    @Transactional
    public void verifyEmail(String token, String ipAddress) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid email verification token"));

        if (!verificationToken.isValid()) {
            throw new InvalidTokenException("Verification token is expired or already confirmed");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.confirm();
        emailVerificationTokenRepository.save(verificationToken);

        securityAuditService.record("AUTH", String.valueOf(user.getId()),
                AuditAction.EMAIL_VERIFIED, user.getUsername(), ipAddress,
                "Email address successfully verified");
    }

    @Transactional
    public String resendEmailVerification(String emailOrUsername, String ipAddress) {
        User user = userRepository.findByUsernameOrEmail(emailOrUsername, emailOrUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "identifier", emailOrUsername));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email address is already verified");
        }

        return createEmailVerificationToken(user).getToken();
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        return mapToUserProfileDto(user);
    }

    private EmailVerificationToken createEmailVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(VERIFICATION_TOKEN_VALIDITY_HOURS, ChronoUnit.HOURS);
        EmailVerificationToken verificationToken = new EmailVerificationToken(user, token, expiryDate);
        return emailVerificationTokenRepository.save(verificationToken);
    }

    private AuthResponse issueAuthResponse(User user, String ipAddress) {
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(
                user,
                refreshTokenValue,
                Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs()),
                ipAddress
        );
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(user, refreshTokenValue);
    }

    private AuthResponse buildAuthResponse(User user, String refreshTokenValue) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getName().name()));
            if (role.getPermissions() != null) {
                role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName().name())));
            }
        }

        Long orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;

        String accessToken = tokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                authorities,
                orgId
        );

        UserProfileDto profile = mapToUserProfileDto(user);

        return new AuthResponse(accessToken, refreshTokenValue, tokenProvider.getTokenExpirationMs(), profile);
    }

    private UserProfileDto mapToUserProfileDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        Set<String> permissions = new HashSet<>();
        for (Role role : user.getRoles()) {
            if (role.getPermissions() != null) {
                role.getPermissions().forEach(p -> permissions.add(p.getName().name()));
            }
        }

        Long orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        Long deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        Long wardId = user.getWard() != null ? user.getWard().getId() : null;

        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                orgId,
                deptId,
                wardId,
                roles,
                permissions,
                user.isEmailVerified()
        );
    }
}
