package com.textiletryon.service;

import com.textiletryon.dto.UserPhotoDto;
import com.textiletryon.model.UserPhoto;
import com.textiletryon.model.UserProfile;
import com.textiletryon.repository.UserPhotoRepository;
import com.textiletryon.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing user sessions and profiles
 * 
 * Handles session-based user management without authentication:
 * - Creating and managing user profiles
 * - Photo upload and management
 * - Session validation and cleanup
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionService {
    
    private final UserProfileRepository userProfileRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final FileStorageService fileStorageService;
    
    /**
     * Create or get user profile for a session
     * 
     * @param sessionId Session identifier from frontend
     * @param profileName Optional profile name
     * @return User profile
     */
    @Transactional
    public UserProfile createOrGetUserProfile(String sessionId, String profileName) {
        return userProfileRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    UserProfile newProfile = UserProfile.builder()
                            .sessionId(sessionId)
                            .profileName(profileName)
                            .build();
                    
                    UserProfile savedProfile = userProfileRepository.save(newProfile);
                    log.info("Created new user profile for session: {}", sessionId);
                    return savedProfile;
                });
    }
    
    /**
     * Update user profile name
     * 
     * @param sessionId Session identifier
     * @param profileName New profile name
     * @return Updated user profile
     */
    @Transactional
    public UserProfile updateProfileName(String sessionId, String profileName) {
        UserProfile profile = getUserProfile(sessionId);
        profile.setProfileName(profileName);
        
        UserProfile updatedProfile = userProfileRepository.save(profile);
        log.info("Updated profile name for session {}: {}", sessionId, profileName);
        return updatedProfile;
    }
    
    /**
     * Get user profile by session ID
     * 
     * @param sessionId Session identifier
     * @return User profile
     * @throws IllegalArgumentException if profile not found
     */
    public UserProfile getUserProfile(String sessionId) {
        return userProfileRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for session: " + sessionId));
    }
    
    /**
     * Upload user photo
     * 
     * @param sessionId Session identifier
     * @param photoFile Photo file to upload
     * @param photoName Optional photo name
     * @return Uploaded photo DTO
     */
    @Transactional
    public UserPhotoDto uploadUserPhoto(String sessionId, MultipartFile photoFile, String photoName) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        // Upload to S3
        String photoUrl = fileStorageService.uploadUserPhoto(photoFile, userProfile.getId());
        
        // Save to database
        UserPhoto userPhoto = UserPhoto.builder()
                .userProfile(userProfile)
                .photoUrl(photoUrl)
                .photoName(photoName != null ? photoName : photoFile.getOriginalFilename())
                .build();
        
        userPhoto = userPhotoRepository.save(userPhoto);
        
        log.info("Uploaded photo for user {}: {}", sessionId, photoUrl);
        
        return convertToDto(userPhoto);
    }
    
    /**
     * Get user photos
     * 
     * @param sessionId Session identifier
     * @return List of user photos
     */
    public List<UserPhotoDto> getUserPhotos(String sessionId) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        List<UserPhoto> photos = userPhotoRepository.findByUserProfileIdOrderByUploadedAtDesc(userProfile.getId());
        
        return photos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete user photo
     * 
     * @param sessionId Session identifier
     * @param photoId Photo ID to delete
     */
    @Transactional
    public void deleteUserPhoto(String sessionId, Long photoId) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        UserPhoto photo = userPhotoRepository.findByIdAndUserProfileId(photoId, userProfile.getId())
                .orElseThrow(() -> new IllegalArgumentException("Photo not found or does not belong to user"));
        
        // Delete from S3
        fileStorageService.deleteFile(photo.getPhotoUrl());
        
        // Delete from database
        userPhotoRepository.delete(photo);
        
        log.info("Deleted photo {} for user {}", photoId, sessionId);
    }
    
    /**
     * Update photo name
     * 
     * @param sessionId Session identifier
     * @param photoId Photo ID
     * @param newName New photo name
     * @return Updated photo DTO
     */
    @Transactional
    public UserPhotoDto updatePhotoName(String sessionId, Long photoId, String newName) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        UserPhoto photo = userPhotoRepository.findByIdAndUserProfileId(photoId, userProfile.getId())
                .orElseThrow(() -> new IllegalArgumentException("Photo not found or does not belong to user"));
        
        photo.setPhotoName(newName);
        photo = userPhotoRepository.save(photo);
        
        log.info("Updated photo name for photo {} to: {}", photoId, newName);
        
        return convertToDto(photo);
    }
    
    /**
     * Generate a unique session ID
     * 
     * @return Unique session identifier
     */
    public String generateSessionId() {
        String sessionId;
        do {
            sessionId = UUID.randomUUID().toString();
        } while (userProfileRepository.existsBySessionId(sessionId));
        
        return sessionId;
    }
    
    /**
     * Validate session ID
     * 
     * @param sessionId Session identifier to validate
     * @return true if valid session
     */
    public boolean isValidSession(String sessionId) {
        return sessionId != null && userProfileRepository.existsBySessionId(sessionId);
    }
    
    /**
     * Get user statistics
     * 
     * @param sessionId Session identifier
     * @return Map of user statistics
     */
    public java.util.Map<String, Object> getUserStatistics(String sessionId) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("photoCount", userPhotoRepository.countByUserProfileId(userProfile.getId()));
        stats.put("favoriteCount", userProfile.getFavoriteCount());
        stats.put("tryonResultCount", userProfile.getTryonResultCount());
        stats.put("profileCreatedAt", userProfile.getCreatedAt());
        stats.put("hasProfileName", userProfile.hasProfileName());
        
        return stats;
    }
    
    /**
     * Clean up old unused sessions
     * This would typically be called by a scheduled job
     * 
     * @param daysOld Number of days to consider a session old
     * @return Number of cleaned up sessions
     */
    @Transactional
    public int cleanupOldSessions(int daysOld) {
        List<UserProfile> inactiveProfiles = userProfileRepository.findInactiveUserProfiles(daysOld);
        
        for (UserProfile profile : inactiveProfiles) {
            // Delete associated photos from S3
            List<String> photoUrls = userPhotoRepository.findPhotoUrlsByUserProfileId(profile.getId());
            if (!photoUrls.isEmpty()) {
                fileStorageService.deleteFiles(photoUrls);
            }
            
            // Delete from database (cascade will handle related records)
            userProfileRepository.delete(profile);
        }
        
        log.info("Cleaned up {} inactive user sessions", inactiveProfiles.size());
        return inactiveProfiles.size();
    }
    
    private UserPhotoDto convertToDto(UserPhoto photo) {
        return UserPhotoDto.builder()
                .id(photo.getId())
                .photoUrl(photo.getPhotoUrl())
                .photoName(photo.getPhotoName())
                .uploadedAt(photo.getUploadedAt())
                .hasBeenUsed(photo.hasBeenUsedInTryOn())
                .tryonCount((long) photo.getTryonResultCount())
                .build();
    }
}
