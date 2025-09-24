package com.textiletryon.repository;

import com.textiletryon.model.UserPhoto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserPhoto entity
 * 
 * Provides operations for managing user uploaded photos:
 * - CRUD operations for user photos
 * - Queries by user profile
 * - Photo analytics and statistics
 * - Cleanup operations for orphaned photos
 */
@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {
    
    /**
     * Find all photos for a specific user profile
     * @param userProfileId the ID of the user profile
     * @return list of photos for the user
     */
    List<UserPhoto> findByUserProfileIdOrderByUploadedAtDesc(Long userProfileId);
    
    /**
     * Find photos for a specific user profile with pagination
     * @param userProfileId the ID of the user profile
     * @param pageable pagination information
     * @return page of photos for the user
     */
    Page<UserPhoto> findByUserProfileId(Long userProfileId, Pageable pageable);
    
    /**
     * Find photo by user profile and photo ID
     * @param userProfileId the ID of the user profile
     * @param photoId the ID of the photo
     * @return Optional containing the photo if found and belongs to the user
     */
    Optional<UserPhoto> findByIdAndUserProfileId(Long photoId, Long userProfileId);
    
    /**
     * Find photo by URL
     * @param photoUrl the photo URL to search for
     * @return Optional containing the photo if found
     */
    Optional<UserPhoto> findByPhotoUrl(String photoUrl);
    
    /**
     * Check if a photo URL already exists
     * @param photoUrl the photo URL to check
     * @return true if the URL exists, false otherwise
     */
    boolean existsByPhotoUrl(String photoUrl);
    
    /**
     * Count photos for a specific user profile
     * @param userProfileId the ID of the user profile
     * @return number of photos for the user
     */
    Long countByUserProfileId(Long userProfileId);
    
    /**
     * Find photos uploaded within a specific time range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of photos uploaded within the range
     */
    List<UserPhoto> findByUploadedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find photos uploaded after a specific date
     * @param date the date to search from
     * @return list of photos uploaded after the date
     */
    List<UserPhoto> findByUploadedAtAfter(LocalDateTime date);
    
    /**
     * Find photos by name containing search term (case-insensitive)
     * @param searchTerm the term to search for in photo names
     * @param userProfileId the ID of the user profile
     * @return list of photos with names containing the search term
     */
    List<UserPhoto> findByUserProfileIdAndPhotoNameContainingIgnoreCase(Long userProfileId, String searchTerm);
    
    /**
     * Find photos that have been used in try-on results
     * @param userProfileId the ID of the user profile
     * @return list of photos that have try-on results
     */
    @Query("SELECT DISTINCT up FROM UserPhoto up JOIN up.tryonResults tr WHERE up.userProfile.id = :userProfileId")
    List<UserPhoto> findPhotosWithTryonResults(@Param("userProfileId") Long userProfileId);
    
    /**
     * Find photos that haven't been used in try-on results
     * @param userProfileId the ID of the user profile
     * @return list of unused photos
     */
    @Query("SELECT up FROM UserPhoto up WHERE up.userProfile.id = :userProfileId AND NOT EXISTS (SELECT 1 FROM TryonResult tr WHERE tr.userPhoto = up)")
    List<UserPhoto> findUnusedPhotos(@Param("userProfileId") Long userProfileId);
    
    /**
     * Get photo usage statistics
     * @param userProfileId the ID of the user profile
     * @return list of arrays containing [photoId, photoName, tryonCount]
     */
    @Query("SELECT up.id, up.photoName, COUNT(tr) FROM UserPhoto up LEFT JOIN up.tryonResults tr WHERE up.userProfile.id = :userProfileId GROUP BY up.id, up.photoName ORDER BY COUNT(tr) DESC")
    List<Object[]> getPhotoUsageStatistics(@Param("userProfileId") Long userProfileId);
    
    /**
     * Find most used photos across all users
     * @param limit maximum number of results
     * @return list of most used photos
     */
    @Query("SELECT up FROM UserPhoto up LEFT JOIN up.tryonResults tr GROUP BY up ORDER BY COUNT(tr) DESC")
    List<UserPhoto> findMostUsedPhotos(@Param("limit") int limit);
    
    /**
     * Count photos uploaded today
     * @return number of photos uploaded today
     */
    @Query("SELECT COUNT(up) FROM UserPhoto up WHERE DATE(up.uploadedAt) = CURRENT_DATE")
    Long countPhotosUploadedToday();
    
    /**
     * Count photos uploaded in the last N days
     * @param days number of days to look back
     * @return number of photos uploaded in the specified period
     */
    @Query("SELECT COUNT(up) FROM UserPhoto up WHERE up.uploadedAt >= CURRENT_TIMESTAMP - :days DAY")
    Long countPhotosUploadedInLastDays(@Param("days") int days);
    
    /**
     * Get daily photo upload statistics for the last N days
     * @param days number of days to analyze
     * @return list of arrays containing [date, count]
     */
    @Query("SELECT DATE(up.uploadedAt), COUNT(up) FROM UserPhoto up WHERE up.uploadedAt >= CURRENT_TIMESTAMP - :days DAY GROUP BY DATE(up.uploadedAt) ORDER BY DATE(up.uploadedAt)")
    List<Object[]> getDailyUploadStats(@Param("days") int days);
    
    /**
     * Find orphaned photos (photos without valid user profiles)
     * @return list of photo URLs that may need cleanup
     */
    @Query("SELECT up.photoUrl FROM UserPhoto up WHERE up.userProfile IS NULL")
    List<String> findOrphanedPhotoUrls();
    
    /**
     * Delete all photos for a specific user profile
     * @param userProfileId the ID of the user profile
     * @return number of deleted photos
     */
    @Modifying
    @Transactional
    Long deleteByUserProfileId(Long userProfileId);
    
    /**
     * Delete photos older than specified days that haven't been used in try-ons
     * @param days number of days to keep photos
     * @return number of deleted photos
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserPhoto up WHERE up.uploadedAt < CURRENT_TIMESTAMP - :days DAY AND NOT EXISTS (SELECT 1 FROM TryonResult tr WHERE tr.userPhoto = up)")
    int deleteOldUnusedPhotos(@Param("days") int days);
    
    /**
     * Get all photo URLs for a specific user profile
     * @param userProfileId the ID of the user profile
     * @return list of photo URLs
     */
    @Query("SELECT up.photoUrl FROM UserPhoto up WHERE up.userProfile.id = :userProfileId ORDER BY up.uploadedAt DESC")
    List<String> findPhotoUrlsByUserProfileId(@Param("userProfileId") Long userProfileId);
    
    /**
     * Find recently uploaded photos across all users
     * @param days number of days to look back
     * @return list of recently uploaded photos
     */
    @Query("SELECT up FROM UserPhoto up WHERE up.uploadedAt >= CURRENT_TIMESTAMP - :days DAY ORDER BY up.uploadedAt DESC")
    List<UserPhoto> findRecentlyUploaded(@Param("days") int days);
    
    /**
     * Count total storage used by photos for a user (requires file size tracking)
     * Note: This would need additional file size field in the entity
     * @param userProfileId the ID of the user profile
     * @return total number of photos (as proxy for storage)
     */
    @Query("SELECT COUNT(up) FROM UserPhoto up WHERE up.userProfile.id = :userProfileId")
    Long getTotalPhotoCountForUser(@Param("userProfileId") Long userProfileId);
    
    /**
     * Find photos by multiple user profile IDs for batch operations
     * @param userProfileIds list of user profile IDs
     * @return list of photos for the specified users
     */
    List<UserPhoto> findByUserProfileIdInOrderByUploadedAtDesc(List<Long> userProfileIds);
}
