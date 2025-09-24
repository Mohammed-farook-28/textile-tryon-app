package com.textiletryon.repository;

import com.textiletryon.model.Favorite;
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
 * Repository interface for Favorite entity
 * 
 * Provides operations for managing user favorites:
 * - CRUD operations for favorites
 * - Queries by user profile and garment
 * - Popularity analytics
 * - Batch operations for favorites management
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    /**
     * Find all favorites for a specific user profile
     * @param userProfileId the ID of the user profile
     * @return list of favorites for the user ordered by creation date (newest first)
     */
    List<Favorite> findByUserProfileIdOrderByCreatedAtDesc(Long userProfileId);
    
    /**
     * Find favorites for a specific user profile with pagination
     * @param userProfileId the ID of the user profile
     * @param pageable pagination information
     * @return page of favorites for the user
     */
    Page<Favorite> findByUserProfileId(Long userProfileId, Pageable pageable);
    
    /**
     * Find favorite by user profile and garment
     * @param userProfileId the ID of the user profile
     * @param garmentId the ID of the garment
     * @return Optional containing the favorite if found
     */
    Optional<Favorite> findByUserProfileIdAndGarmentId(Long userProfileId, Long garmentId);
    
    /**
     * Check if a garment is favorited by a specific user
     * @param userProfileId the ID of the user profile
     * @param garmentId the ID of the garment
     * @return true if the garment is favorited by the user
     */
    boolean existsByUserProfileIdAndGarmentId(Long userProfileId, Long garmentId);
    
    /**
     * Count favorites for a specific user profile
     * @param userProfileId the ID of the user profile
     * @return number of favorites for the user
     */
    Long countByUserProfileId(Long userProfileId);
    
    /**
     * Count how many times a garment has been favorited
     * @param garmentId the ID of the garment
     * @return number of times the garment has been favorited
     */
    Long countByGarmentId(Long garmentId);
    
    /**
     * Find all users who favorited a specific garment
     * @param garmentId the ID of the garment
     * @return list of favorites for the garment
     */
    List<Favorite> findByGarmentIdOrderByCreatedAtDesc(Long garmentId);
    
    /**
     * Find favorites created within a specific time range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of favorites created within the range
     */
    List<Favorite> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find recent favorites for a user
     * @param userProfileId the ID of the user profile
     * @param days number of days to look back
     * @return list of recent favorites
     */
    @Query("SELECT f FROM Favorite f WHERE f.userProfile.id = :userProfileId AND f.createdAt >= CURRENT_TIMESTAMP - :days DAY ORDER BY f.createdAt DESC")
    List<Favorite> findRecentFavoritesByUser(@Param("userProfileId") Long userProfileId, @Param("days") int days);
    
    /**
     * Get most favorited garments
     * @param limit maximum number of results
     * @return list of arrays containing [garmentId, favoriteCount]
     */
    @Query("SELECT f.garment.id, COUNT(f) FROM Favorite f GROUP BY f.garment.id ORDER BY COUNT(f) DESC")
    List<Object[]> findMostFavoritedGarments(@Param("limit") int limit);
    
    /**
     * Get favorite statistics by category
     * @return list of arrays containing [category, favoriteCount]
     */
    @Query("SELECT g.category, COUNT(f) FROM Favorite f JOIN f.garment g GROUP BY g.category ORDER BY COUNT(f) DESC")
    List<Object[]> getFavoriteStatsByCategory();
    
    /**
     * Get favorite statistics by color
     * @return list of arrays containing [color, favoriteCount]
     */
    @Query("SELECT g.color, COUNT(f) FROM Favorite f JOIN f.garment g GROUP BY g.color ORDER BY COUNT(f) DESC")
    List<Object[]> getFavoriteStatsByColor();
    
    /**
     * Find users with most favorites
     * @param limit maximum number of results
     * @return list of arrays containing [userProfileId, favoriteCount]
     */
    @Query("SELECT f.userProfile.id, COUNT(f) FROM Favorite f GROUP BY f.userProfile.id ORDER BY COUNT(f) DESC")
    List<Object[]> findUsersWithMostFavorites(@Param("limit") int limit);
    
    /**
     * Count favorites created today
     * @return number of favorites created today
     */
    @Query("SELECT COUNT(f) FROM Favorite f WHERE DATE(f.createdAt) = CURRENT_DATE")
    Long countFavoritesCreatedToday();
    
    /**
     * Count favorites created in the last N days
     * @param days number of days to look back
     * @return number of favorites created in the specified period
     */
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.createdAt >= CURRENT_TIMESTAMP - :days DAY")
    Long countFavoritesCreatedInLastDays(@Param("days") int days);
    
    /**
     * Get daily favorite creation statistics for the last N days
     * @param days number of days to analyze
     * @return list of arrays containing [date, count]
     */
    @Query("SELECT DATE(f.createdAt), COUNT(f) FROM Favorite f WHERE f.createdAt >= CURRENT_TIMESTAMP - :days DAY GROUP BY DATE(f.createdAt) ORDER BY DATE(f.createdAt)")
    List<Object[]> getDailyFavoriteStats(@Param("days") int days);
    
    /**
     * Find favorites for garments in a specific category
     * @param userProfileId the ID of the user profile
     * @param category the garment category
     * @return list of favorites for garments in the specified category
     */
    @Query("SELECT f FROM Favorite f JOIN f.garment g WHERE f.userProfile.id = :userProfileId AND g.category = :category ORDER BY f.createdAt DESC")
    List<Favorite> findFavoritesByUserAndCategory(@Param("userProfileId") Long userProfileId, @Param("category") String category);
    
    /**
     * Find favorites for garments with specific color
     * @param userProfileId the ID of the user profile
     * @param color the garment color
     * @return list of favorites for garments with the specified color
     */
    @Query("SELECT f FROM Favorite f JOIN f.garment g WHERE f.userProfile.id = :userProfileId AND g.color = :color ORDER BY f.createdAt DESC")
    List<Favorite> findFavoritesByUserAndColor(@Param("userProfileId") Long userProfileId, @Param("color") String color);
    
    /**
     * Delete favorite by user profile and garment
     * @param userProfileId the ID of the user profile
     * @param garmentId the ID of the garment
     * @return number of deleted records (should be 1 or 0)
     */
    @Modifying
    @Transactional
    Long deleteByUserProfileIdAndGarmentId(Long userProfileId, Long garmentId);
    
    /**
     * Delete all favorites for a specific user profile
     * @param userProfileId the ID of the user profile
     * @return number of deleted favorites
     */
    @Modifying
    @Transactional
    Long deleteByUserProfileId(Long userProfileId);
    
    /**
     * Delete all favorites for a specific garment
     * @param garmentId the ID of the garment
     * @return number of deleted favorites
     */
    @Modifying
    @Transactional
    Long deleteByGarmentId(Long garmentId);
    
    /**
     * Delete old favorites (older than specified days)
     * @param days number of days to keep favorites
     * @return number of deleted favorites
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Favorite f WHERE f.createdAt < CURRENT_TIMESTAMP - :days DAY")
    int deleteOldFavorites(@Param("days") int days);
    
    /**
     * Find favorites with garment details for efficient loading
     * @param userProfileId the ID of the user profile
     * @return list of favorites with garment details
     */
    @Query("SELECT f FROM Favorite f JOIN FETCH f.garment g LEFT JOIN FETCH g.images WHERE f.userProfile.id = :userProfileId ORDER BY f.createdAt DESC")
    List<Favorite> findFavoritesWithGarmentDetails(@Param("userProfileId") Long userProfileId);
    
    /**
     * Check if any garments in a list are favorited by a user
     * @param userProfileId the ID of the user profile
     * @param garmentIds list of garment IDs to check
     * @return list of garment IDs that are favorited
     */
    @Query("SELECT f.garment.id FROM Favorite f WHERE f.userProfile.id = :userProfileId AND f.garment.id IN :garmentIds")
    List<Long> findFavoritedGarmentIds(@Param("userProfileId") Long userProfileId, @Param("garmentIds") List<Long> garmentIds);
    
    /**
     * Get trending garments based on recent favorites
     * @param days number of days to consider for trending calculation
     * @param limit maximum number of results
     * @return list of trending garment IDs
     */
    @Query("SELECT f.garment.id FROM Favorite f WHERE f.createdAt >= CURRENT_TIMESTAMP - :days DAY GROUP BY f.garment.id ORDER BY COUNT(f) DESC")
    List<Long> findTrendingGarments(@Param("days") int days, @Param("limit") int limit);
}
