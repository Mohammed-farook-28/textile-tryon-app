package com.textiletryon.repository;

import com.textiletryon.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserProfile entity
 * 
 * Provides operations for managing user profiles:
 * - Session-based user identification
 * - User profile management
 * - Analytics and statistics queries
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    /**
     * Find user profile by session ID
     * @param sessionId the unique session identifier
     * @return Optional containing the user profile if found
     */
    Optional<UserProfile> findBySessionId(String sessionId);
    
    /**
     * Check if a user profile exists for the given session ID
     * @param sessionId the session identifier to check
     * @return true if profile exists, false otherwise
     */
    boolean existsBySessionId(String sessionId);
    
    /**
     * Find user profiles by profile name (case-insensitive)
     * @param profileName the profile name to search for
     * @return list of user profiles with matching names
     */
    List<UserProfile> findByProfileNameContainingIgnoreCase(String profileName);
    
    /**
     * Find user profiles created within a specific time range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of user profiles created within the range
     */
    List<UserProfile> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find user profiles created after a specific date
     * @param date the date to search from
     * @return list of user profiles created after the date
     */
    List<UserProfile> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Count user profiles created today
     * @return number of profiles created today
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE DATE(up.createdAt) = CURRENT_DATE")
    Long countProfilesCreatedToday();
    
    /**
     * Count user profiles created in the last N days
     * @param days number of days to look back
     * @return number of profiles created in the specified period
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.createdAt >= CURRENT_TIMESTAMP - :days DAY")
    Long countProfilesCreatedInLastDays(@Param("days") int days);
    
    /**
     * Find active user profiles (those with recent activity)
     * Based on users who have favorites, photos, or try-on results
     * @return list of active user profiles
     */
    @Query("SELECT DISTINCT up FROM UserProfile up LEFT JOIN up.favorites f LEFT JOIN up.photos p LEFT JOIN up.tryonResults tr WHERE f IS NOT NULL OR p IS NOT NULL OR tr IS NOT NULL")
    List<UserProfile> findActiveUserProfiles();
    
    /**
     * Find user profiles with favorites
     * @return list of user profiles that have at least one favorite
     */
    @Query("SELECT DISTINCT up FROM UserProfile up JOIN up.favorites f")
    List<UserProfile> findUserProfilesWithFavorites();
    
    /**
     * Find user profiles with uploaded photos
     * @return list of user profiles that have uploaded photos
     */
    @Query("SELECT DISTINCT up FROM UserProfile up JOIN up.photos p")
    List<UserProfile> findUserProfilesWithPhotos();
    
    /**
     * Find user profiles with try-on results
     * @return list of user profiles that have try-on results
     */
    @Query("SELECT DISTINCT up FROM UserProfile up JOIN up.tryonResults tr")
    List<UserProfile> findUserProfilesWithTryonResults();
    
    /**
     * Get user engagement statistics
     * @return list of arrays containing [profileId, favoriteCount, photoCount, tryonCount]
     */
    @Query("SELECT up.id, " +
           "(SELECT COUNT(f) FROM Favorite f WHERE f.userProfile = up), " +
           "(SELECT COUNT(p) FROM UserPhoto p WHERE p.userProfile = up), " +
           "(SELECT COUNT(tr) FROM TryonResult tr WHERE tr.userProfile = up) " +
           "FROM UserProfile up")
    List<Object[]> getUserEngagementStatistics();
    
    /**
     * Find top users by number of favorites
     * @param limit maximum number of results
     * @return list of user profiles ordered by favorite count
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN up.favorites f GROUP BY up ORDER BY COUNT(f) DESC")
    List<UserProfile> findTopUsersByFavorites(@Param("limit") int limit);
    
    /**
     * Find top users by number of try-on results
     * @param limit maximum number of results
     * @return list of user profiles ordered by try-on result count
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN up.tryonResults tr GROUP BY up ORDER BY COUNT(tr) DESC")
    List<UserProfile> findTopUsersByTryonResults(@Param("limit") int limit);
    
    /**
     * Count total number of user sessions
     * @return total count of user profiles
     */
    @Query("SELECT COUNT(up) FROM UserProfile up")
    Long countTotalUserSessions();
    
    /**
     * Find user profiles that haven't been active recently
     * @param days number of days to consider as inactive
     * @return list of inactive user profiles
     */
    @Query("SELECT up FROM UserProfile up WHERE up.createdAt < CURRENT_TIMESTAMP - :days DAY AND " +
           "NOT EXISTS (SELECT 1 FROM Favorite f WHERE f.userProfile = up AND f.createdAt >= CURRENT_TIMESTAMP - :days DAY) AND " +
           "NOT EXISTS (SELECT 1 FROM UserPhoto p WHERE p.userProfile = up AND p.uploadedAt >= CURRENT_TIMESTAMP - :days DAY) AND " +
           "NOT EXISTS (SELECT 1 FROM TryonResult tr WHERE tr.userProfile = up AND tr.createdAt >= CURRENT_TIMESTAMP - :days DAY)")
    List<UserProfile> findInactiveUserProfiles(@Param("days") int days);
    
    /**
     * Get daily user registration statistics for the last N days
     * @param days number of days to analyze
     * @return list of arrays containing [date, count]
     */
    @Query("SELECT DATE(up.createdAt), COUNT(up) FROM UserProfile up WHERE up.createdAt >= CURRENT_TIMESTAMP - :days DAY GROUP BY DATE(up.createdAt) ORDER BY DATE(up.createdAt)")
    List<Object[]> getDailyRegistrationStats(@Param("days") int days);
    
    /**
     * Find user profiles with profile names set
     * @return list of user profiles that have profile names
     */
    @Query("SELECT up FROM UserProfile up WHERE up.profileName IS NOT NULL AND up.profileName != ''")
    List<UserProfile> findUserProfilesWithNames();
    
    /**
     * Search user profiles by session ID pattern or profile name
     * @param searchTerm the term to search for
     * @return list of matching user profiles
     */
    @Query("SELECT up FROM UserProfile up WHERE " +
           "LOWER(up.sessionId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(up.profileName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UserProfile> searchUserProfiles(@Param("searchTerm") String searchTerm);
}
