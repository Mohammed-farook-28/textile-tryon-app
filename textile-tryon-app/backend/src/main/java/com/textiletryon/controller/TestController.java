package com.textiletryon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Test controller to verify application startup and Swagger functionality
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Test endpoints for API verification")
public class TestController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns application health status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is healthy")
    })
    public Map<String, Object> healthCheck() {
        return Map.of(
                "status", "OK",
                "message", "Textile Try-On Backend is running",
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0"
        );
    }

    @GetMapping("/info")
    @Operation(summary = "Application info", description = "Returns basic application information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application info retrieved successfully")
    })
    public Map<String, Object> getInfo() {
        return Map.of(
                "name", "Textile Try-On API",
                "description", "REST API for virtual garment try-on application",
                "features", new String[]{"Virtual Try-On", "Garment Management", "User Profiles", "AI Integration"},
                "apis", new String[]{"Flux Context Pro", "Google Gemini"},
                "database", "MySQL",
                "storage", "AWS S3"
        );
    }
}
