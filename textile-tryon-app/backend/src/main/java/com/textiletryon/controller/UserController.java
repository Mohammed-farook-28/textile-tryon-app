package com.textiletryon.controller;

import com.textiletryon.dto.ApiResponse;
import com.textiletryon.dto.UserPhotoDto;
import com.textiletryon.model.UserProfile;
import com.textiletryon.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for user operations
 * 
 * Handles session-based user management and photo operations.
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class UserController {
    
    private final UserSessionService userSessionService;
    
    /**
     * Create or get user profile
     * POST /api/user/profile
     */
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfile>> createOrGetProfile(
            @RequestParam String sessionId,
            @RequestParam(required = false) String profileName) {
        try {
            UserProfile profile = userSessionService.createOrGetUserProfile(sessionId, profileName);
            return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error creating/getting profile for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error managing profile: " + e.getMessage()));
        }
    }
    
    /**
     * Update profile name
     * PUT /api/user/profile/name
     */
    @PutMapping("/profile/name")
    public ResponseEntity<ApiResponse<UserProfile>> updateProfileName(
            @RequestParam String sessionId,
            @RequestParam String profileName) {
        try {
            UserProfile profile = userSessionService.updateProfileName(sessionId, profileName);
            return ResponseEntity.ok(ApiResponse.success(profile, "Profile name updated successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User profile not found", "PROFILE_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error updating profile name for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating profile: " + e.getMessage()));
        }
    }
    
    /**
     * Upload user photo
     * POST /api/user/photos/upload
     */
    @PostMapping("/photos/upload")
    public ResponseEntity<ApiResponse<UserPhotoDto>> uploadPhoto(
            @RequestParam String sessionId,
            @RequestParam("photo") MultipartFile photoFile,
            @RequestParam(required = false) String photoName) {
        try {
            UserPhotoDto photo = userSessionService.uploadUserPhoto(sessionId, photoFile, photoName);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(photo, "Photo uploaded successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            log.error("Error uploading photo for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error uploading photo: " + e.getMessage()));
        }
    }
    
    /**
     * Get user photos
     * GET /api/user/photos
     */
    @GetMapping("/photos")
    public ResponseEntity<ApiResponse<List<UserPhotoDto>>> getUserPhotos(@RequestParam String sessionId) {
        try {
            List<UserPhotoDto> photos = userSessionService.getUserPhotos(sessionId);
            return ResponseEntity.ok(ApiResponse.success(photos, "Photos retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User profile not found", "PROFILE_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error getting photos for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving photos: " + e.getMessage()));
        }
    }
    
    /**
     * Delete user photo
     * DELETE /api/user/photos/{photoId}
     */
    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<ApiResponse<String>> deletePhoto(
            @RequestParam String sessionId,
            @PathVariable Long photoId) {
        try {
            userSessionService.deleteUserPhoto(sessionId, photoId);
            return ResponseEntity.ok(ApiResponse.success("Photo deleted successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "PHOTO_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error deleting photo {} for session {}: {}", photoId, sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting photo: " + e.getMessage()));
        }
    }
    
    /**
     * Update photo name
     * PUT /api/user/photos/{photoId}/name
     */
    @PutMapping("/photos/{photoId}/name")
    public ResponseEntity<ApiResponse<UserPhotoDto>> updatePhotoName(
            @RequestParam String sessionId,
            @PathVariable Long photoId,
            @RequestParam String newName) {
        try {
            UserPhotoDto photo = userSessionService.updatePhotoName(sessionId, photoId, newName);
            return ResponseEntity.ok(ApiResponse.success(photo, "Photo name updated successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "PHOTO_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error updating photo name for photo {} and session {}: {}", 
                    photoId, sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating photo name: " + e.getMessage()));
        }
    }
    
    /**
     * Get user statistics
     * GET /api/user/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics(@RequestParam String sessionId) {
        try {
            Map<String, Object> stats = userSessionService.getUserStatistics(sessionId);
            return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User profile not found", "PROFILE_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error getting statistics for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Generate new session ID
     * GET /api/user/session/generate
     */
    @GetMapping("/session/generate")
    public ResponseEntity<ApiResponse<String>> generateSessionId() {
        try {
            String sessionId = userSessionService.generateSessionId();
            return ResponseEntity.ok(ApiResponse.success(sessionId, "Session ID generated successfully"));
            
        } catch (Exception e) {
            log.error("Error generating session ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error generating session ID: " + e.getMessage()));
        }
    }
    
    /**
     * Validate session ID
     * GET /api/user/session/validate
     */
    @GetMapping("/session/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateSession(@RequestParam String sessionId) {
        try {
            boolean isValid = userSessionService.isValidSession(sessionId);
            return ResponseEntity.ok(ApiResponse.success(isValid, 
                    isValid ? "Session is valid" : "Session is invalid"));
            
        } catch (Exception e) {
            log.error("Error validating session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error validating session: " + e.getMessage()));
        }
    }
}
