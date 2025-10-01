package com.textiletryon.service;

import com.textiletryon.dto.GarmentDto;
import com.textiletryon.model.Favorite;
import com.textiletryon.model.Garment;
import com.textiletryon.model.GarmentImage;
import com.textiletryon.model.UserProfile;
import com.textiletryon.repository.FavoriteRepository;
import com.textiletryon.repository.GarmentImageRepository;
import com.textiletryon.repository.GarmentRepository;
import com.textiletryon.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing user favorites
 * 
 * Handles favorite operations including:
 * - Adding/removing favorites
 * - Retrieving user favorites
 * - Checking favorite status
 * - Analytics and trending
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final UserProfileRepository userProfileRepository;
    private final GarmentRepository garmentRepository;
    private final GarmentImageRepository garmentImageRepository;
    
    /**
     * Add garment to user's favorites
     * 
     * @param sessionId User session ID
     * @param garmentId Garment ID to favorite
     * @return true if added, false if already favorited
     */
    @Transactional
    public boolean addToFavorites(String sessionId, Long garmentId) {
        UserProfile userProfile = getUserProfile(sessionId);
        Garment garment = getGarment(garmentId);
        
        // Check if already favorited
        if (favoriteRepository.existsByUserProfileIdAndGarmentId(userProfile.getId(), garmentId)) {
            log.debug("Garment {} already in favorites for user {}", garmentId, sessionId);
            return false;
        }
        
        Favorite favorite = Favorite.builder()
                .userProfile(userProfile)
                .garment(garment)
                .build();
        
        favoriteRepository.save(favorite);
        
        log.info("Added garment {} to favorites for user {}", garmentId, sessionId);
        return true;
    }
    
    /**
     * Remove garment from user's favorites
     * 
     * @param sessionId User session ID
     * @param garmentId Garment ID to unfavorite
     * @return true if removed, false if not favorited
     */
    @Transactional
    public boolean removeFromFavorites(String sessionId, Long garmentId) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        Long deletedCount = favoriteRepository.deleteByUserProfileIdAndGarmentId(
                userProfile.getId(), garmentId);
        
        if (deletedCount > 0) {
            log.info("Removed garment {} from favorites for user {}", garmentId, sessionId);
            return true;
        } else {
            log.debug("Garment {} was not in favorites for user {}", garmentId, sessionId);
            return false;
        }
    }
    
    /**
     * Toggle favorite status for a garment
     * 
     * @param sessionId User session ID
     * @param garmentId Garment ID
     * @return true if now favorited, false if unfavorited
     */
    @Transactional
    public boolean toggleFavorite(String sessionId, Long garmentId) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        if (favoriteRepository.existsByUserProfileIdAndGarmentId(userProfile.getId(), garmentId)) {
            removeFromFavorites(sessionId, garmentId);
            return false;
        } else {
            addToFavorites(sessionId, garmentId);
            return true;
        }
    }
    
    /**
     * Check if garment is favorited by user
     * 
     * @param sessionId User session ID
     * @param garmentId Garment ID
     * @return true if favorited
     */
    public boolean isFavorited(String sessionId, Long garmentId) {
        UserProfile userProfile = getUserProfile(sessionId);
        return favoriteRepository.existsByUserProfileIdAndGarmentId(userProfile.getId(), garmentId);
    }
    
    /**
     * Get user's favorites with pagination
     * 
     * @param sessionId User session ID
     * @param page Page number
     * @param size Page size
     * @return Page of favorite garments
     */
    public Page<GarmentDto> getUserFavorites(String sessionId, int page, int size) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Favorite> favorites = favoriteRepository.findByUserProfileId(userProfile.getId(), pageable);
        
        return favorites.map(favorite -> convertGarmentToDto(favorite.getGarment()));
    }
    
    /**
     * Get user's favorite garments (all)
     * 
     * @param sessionId User session ID
     * @return List of favorite garments
     */
    public List<GarmentDto> getAllUserFavorites(String sessionId) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        List<Favorite> favorites = favoriteRepository.findFavoritesWithGarmentDetails(userProfile.getId());
        
        return favorites.stream()
                .map(favorite -> convertGarmentToDto(favorite.getGarment()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get favorites by category
     * 
     * @param sessionId User session ID
     * @param category Garment category
     * @return List of favorite garments in the category
     */
    public List<GarmentDto> getFavoritesByCategory(String sessionId, String category) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        List<Favorite> favorites = favoriteRepository.findFavoritesByUserAndCategory(
                userProfile.getId(), category);
        
        return favorites.stream()
                .map(favorite -> convertGarmentToDto(favorite.getGarment()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get recently favorited items
     * 
     * @param sessionId User session ID
     * @param days Number of days to look back
     * @return List of recently favorited garments
     */
    public List<GarmentDto> getRecentFavorites(String sessionId, int days) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        List<Favorite> favorites = favoriteRepository.findRecentFavoritesByUser(
                userProfile.getId(), days);
        
        return favorites.stream()
                .map(favorite -> convertGarmentToDto(favorite.getGarment()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get count of user's favorites
     * 
     * @param sessionId User session ID
     * @return Number of favorites
     */
    public long getFavoriteCount(String sessionId) {
        UserProfile userProfile = getUserProfile(sessionId);
        return favoriteRepository.countByUserProfileId(userProfile.getId());
    }
    
    /**
     * Check favorite status for multiple garments
     * 
     * @param sessionId User session ID
     * @param garmentIds List of garment IDs to check
     * @return Set of favorited garment IDs
     */
    public Set<Long> getFavoritedGarmentIds(String sessionId, List<Long> garmentIds) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        return favoriteRepository.findFavoritedGarmentIds(userProfile.getId(), garmentIds)
                .stream()
                .collect(Collectors.toSet());
    }
    
    /**
     * Clear all favorites for a user
     * 
     * @param sessionId User session ID
     * @return Number of favorites removed
     */
    @Transactional
    public long clearAllFavorites(String sessionId) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        long deletedCount = favoriteRepository.deleteByUserProfileId(userProfile.getId());
        
        log.info("Cleared {} favorites for user {}", deletedCount, sessionId);
        return deletedCount;
    }
    
    /**
     * Get trending garments based on recent favorites
     * 
     * @param days Number of days to consider for trending
     * @param limit Maximum number of results
     * @return List of trending garment IDs
     */
    public List<Long> getTrendingGarments(int days, int limit) {
        List<Long> trendingGarments = favoriteRepository.findTrendingGarments(days);
        return trendingGarments.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get popular garments (most favorited)
     * 
     * @param limit Maximum number of results
     * @return List of popular garments with their favorite counts
     */
    public List<java.util.Map<String, Object>> getPopularGarments(int limit) {
        List<Object[]> results = favoriteRepository.findMostFavoritedGarments(limit);
        
        return results.stream()
                .map(result -> {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("garmentId", result[0]);
                    item.put("favoriteCount", result[1]);
                    return item;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get favorite statistics by category
     * 
     * @return List of category statistics
     */
    public List<java.util.Map<String, Object>> getFavoriteStatsByCategory() {
        List<Object[]> results = favoriteRepository.getFavoriteStatsByCategory();
        
        return results.stream()
                .map(result -> {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("category", result[0]);
                    item.put("favoriteCount", result[1]);
                    return item;
                })
                .collect(Collectors.toList());
    }
    
    private UserProfile getUserProfile(String sessionId) {
        return userProfileRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for session: " + sessionId));
    }
    
    private Garment getGarment(Long garmentId) {
        return garmentRepository.findById(garmentId)
                .orElseThrow(() -> new IllegalArgumentException("Garment not found with ID: " + garmentId));
    }
    
    private GarmentDto convertGarmentToDto(Garment garment) {
        // Get primary image
        Optional<GarmentImage> primaryImage = garmentImageRepository.findByGarmentIdAndIsPrimaryTrue(garment.getId());
        
        // Get all image URLs
        List<String> imageUrls = garmentImageRepository.findImageUrlsByGarmentId(garment.getId());
        
        return GarmentDto.builder()
                .id(garment.getId())
                .nameId(garment.getNameId())
                .garmentName(garment.getGarmentName())
                .category(garment.getCategory())
                .subcategory(garment.getSubcategory())
                .garmentType(garment.getGarmentType())
                .color(garment.getColor())
                .patternStyle(garment.getPatternStyle())
                .price(garment.getPrice())
                .stockQuantity(garment.getStockQuantity())
                .createdAt(garment.getCreatedAt())
                .updatedAt(garment.getUpdatedAt())
                .primaryImageUrl(primaryImage.map(GarmentImage::getImageUrl).orElse(null))
                .imageUrls(imageUrls)
                .inStock(garment.isInStock())
                .lowStock(garment.isLowStock())
                .build();
    }
}
