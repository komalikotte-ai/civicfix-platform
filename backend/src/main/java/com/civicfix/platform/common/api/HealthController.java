package com.civicfix.platform.common.api;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok("CivicFix platform core backend is operational", Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString(),
                "apiVersion", "v1",
                "service", "civicfix-platform-backend"
        ));
    }

    @GetMapping("/liveness")
    public ApiResponse<Map<String, Object>> liveness() {
        return ApiResponse.ok("Service is alive", Map.of(
                "status", "UP",
                "probe", "LIVENESS",
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/readiness")
    public ApiResponse<Map<String, Object>> readiness() {
        return ApiResponse.ok("Service is ready to accept traffic", Map.of(
                "status", "UP",
                "probe", "READINESS",
                "timestamp", Instant.now().toString()
        ));
    }
}
