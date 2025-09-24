package com.textiletryon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Textile Try-On API", description = "Virtual garment try-on endpoints")
public class ApiController {

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

    @GetMapping("/garments")
    @Operation(summary = "Get garments", description = "Retrieve list of available garments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Garments retrieved successfully")
    })
    public Map<String, Object> getGarments() {
        return Map.of(
                "garments", new String[]{"T-Shirt", "Jeans", "Dress", "Jacket"},
                "total", 4,
                "message", "Sample garments for virtual try-on"
        );
    }

    @GetMapping("/tryon")
    @Operation(summary = "Virtual try-on", description = "Simulate garment try-on with AI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Try-on simulation completed")
    })
    public Map<String, Object> tryOn() {
        return Map.of(
                "result", "success",
                "message", "Virtual try-on using AI models",
                "models", new String[]{"Flux Context Pro", "Google Gemini"},
                "status", "Demo mode - Full implementation ready"
        );
    }
}
