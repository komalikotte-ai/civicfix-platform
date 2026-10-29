package com.civicfix.platform.auth.controller;

import com.civicfix.platform.auth.dto.*;
import com.civicfix.platform.auth.service.AuthService;
import com.civicfix.platform.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        return ApiResponse.created("Registration successful. Verification token generated.", authService.register(request, ipAddress));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        return ApiResponse.ok("Login successful", authService.login(request, ipAddress));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        return ApiResponse.ok("Token refreshed successfully", authService.refreshToken(request.getRefreshToken(), ipAddress));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, String>> logout(@RequestBody(required = false) RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String token = request != null ? request.getRefreshToken() : null;
        authService.logout(token, ipAddress);
        return ApiResponse.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/logout-all")
    public ApiResponse<Map<String, String>> logoutAll(HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        authService.logoutAllSessions(ipAddress);
        return ApiResponse.ok(Map.of("message", "All active sessions and refresh tokens have been revoked"));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<UserSessionDto>> getSessions() {
        return ApiResponse.ok(authService.getUserSessions());
    }

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Map<String, String>> revokeSession(@PathVariable Long id) {
        authService.revokeSession(id);
        return ApiResponse.ok(Map.of("message", "Session revoked successfully"));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String token = authService.requestPasswordReset(request.getEmailOrUsername(), ipAddress);
        return ApiResponse.ok(Map.of(
                "message", "Password reset instructions dispatched",
                "resetToken", token
        ));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        authService.resetPassword(request.getToken(), request.getNewPassword(), ipAddress);
        return ApiResponse.ok(Map.of("message", "Password has been successfully reset. Please log in with your new credentials."));
    }

    @PostMapping("/change-password")
    public ApiResponse<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        authService.changePassword(request, ipAddress);
        return ApiResponse.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/verify-email")
    public ApiResponse<Map<String, String>> verifyEmail(@RequestParam String token, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        authService.verifyEmail(token, ipAddress);
        return ApiResponse.ok(Map.of("message", "Email address verified successfully"));
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Map<String, String>> resendVerification(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String token = authService.resendEmailVerification(request.getEmailOrUsername(), ipAddress);
        return ApiResponse.ok(Map.of(
                "message", "Verification token generated",
                "verificationToken", token
        ));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileDto> getCurrentUser() {
        return ApiResponse.ok(authService.getCurrentUserProfile());
    }

    // Role-Based Access Control Verification Endpoints
    @GetMapping("/rbac/admin-only")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, String>> adminOnly() {
        return ApiResponse.ok(Map.of("access", "GRANTED", "roleRequired", "ADMIN"));
    }

    @GetMapping("/rbac/supervisor-only")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'DEPARTMENT_MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, String>> supervisorOnly() {
        return ApiResponse.ok(Map.of("access", "GRANTED", "roleRequired", "SUPERVISOR/DEPT_MANAGER/ADMIN"));
    }

    @GetMapping("/rbac/worker-only")
    @PreAuthorize("hasAnyRole('FIELD_WORKER', 'EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, String>> workerOnly() {
        return ApiResponse.ok(Map.of("access", "GRANTED", "roleRequired", "FIELD_WORKER/EMPLOYEE/ADMIN"));
    }

    @GetMapping("/rbac/citizen-only")
    @PreAuthorize("hasRole('CITIZEN')")
    public ApiResponse<Map<String, String>> citizenOnly() {
        return ApiResponse.ok(Map.of("access", "GRANTED", "roleRequired", "CITIZEN"));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
