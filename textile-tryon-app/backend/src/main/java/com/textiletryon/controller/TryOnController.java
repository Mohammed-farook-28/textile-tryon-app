package com.textiletryon.controller;

import com.textiletryon.dto.ApiResponse;
import com.textiletryon.dto.TryonRequestDto;
import com.textiletryon.dto.TryonResultDto;
import com.textiletryon.service.AITryOnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for virtual try-on operations
 * 
 * Handles AI-powered try-on generation and result management.
 */
@RestController
@RequestMapping("/tryon")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class TryOnController {
    
    private final AITryOnService aiTryOnService;
    
    /**
     * Generate virtual try-on
     * POST /api/tryon/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<TryonResultDto>> generateTryOn(
            @RequestParam String sessionId,
            @Valid @RequestBody TryonRequestDto request) {
        try {
            TryonResultDto result = aiTryOnService.generateTryOn(sessionId, request);
            
            if ("SUCCESS".equals(result.getStatus())) {
                return ResponseEntity.ok(ApiResponse.success(result, "Try-on generated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Try-on generation failed: " + result.getErrorMessage(), 
                              "TRYON_FAILED"));
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            log.error("Error generating try-on for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error generating try-on: " + e.getMessage()));
        }
    }
    
    /**
     * Get user try-on results
     * GET /api/tryon/results?sessionId=xxx&page=0&size=20
     */
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<Page<TryonResultDto>>> getUserTryonResults(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<TryonResultDto> results = aiTryOnService.getUserTryonResults(sessionId, page, size);
            return ResponseEntity.ok(ApiResponse.success(results, "Try-on results retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error getting try-on results for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving try-on results: " + e.getMessage()));
        }
    }
    
    /**
     * Delete try-on result
     * DELETE /api/tryon/results/{resultId}
     */
    @DeleteMapping("/results/{resultId}")
    public ResponseEntity<ApiResponse<String>> deleteTryonResult(
            @RequestParam String sessionId,
            @PathVariable Long resultId) {
        try {
            aiTryOnService.deleteTryonResult(sessionId, resultId);
            return ResponseEntity.ok(ApiResponse.success("Try-on result deleted successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error deleting try-on result {} for session {}: {}", 
                    resultId, sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting try-on result: " + e.getMessage()));
        }
    }
    
    /**
     * Get available AI models
     * GET /api/tryon/models
     */
    @GetMapping("/models")
    public ResponseEntity<ApiResponse<String[]>> getAvailableModels() {
        try {
            String[] models = {"google-tryon", "flux-context-pro"};
            return ResponseEntity.ok(ApiResponse.success(models, "Available AI models retrieved"));
            
        } catch (Exception e) {
            log.error("Error getting available models: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving models: " + e.getMessage()));
        }
    }
}
