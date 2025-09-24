package com.textiletryon.controller;

import com.textiletryon.dto.ApiResponse;
import com.textiletryon.dto.GarmentDto;
import com.textiletryon.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST Controller for favorite operations
 * 
 * Handles user favorite management including add, remove, and retrieve operations.
 */
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    
    /**
     * Add garment to favorites
     * POST /api/favorites/add
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Boolean>> addToFavorites(
            @RequestParam String sessionId,
            @RequestParam Long garmentId) {
        try {
            boolean added = favoriteService.addToFavorites(sessionId, garmentId);
            
            if (added) {
                return ResponseEntity.ok(ApiResponse.success(true, "Garment added to favorites"));
            } else {
                return ResponseEntity.ok(ApiResponse.success(false, "Garment already in favorites"));
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error adding garment {} to favorites for session {}: {}", 
                    garmentId, sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error adding to favorites: " + e.getMessage()));
        }
    }
    
    /**
     * Remove garment from favorites
     * DELETE /api/favorites/remove/{garmentId}
     */
    @DeleteMapping("/remove/{garmentId}")
    public ResponseEntity<ApiResponse<Boolean>> removeFromFavorites(
            @RequestParam String sessionId,
            @PathVariable Long garmentId) {
        try {
            boolean removed = favoriteService.removeFromFavorites(sessionId, garmentId);
            
            if (removed) {
                return ResponseEntity.ok(ApiResponse.success(true, "Garment removed from favorites"));
            } else {
                return ResponseEntity.ok(ApiResponse.success(false, "Garment was not in favorites"));
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error removing garment {} from favorites for session {}: {}", 
                    garmentId, sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error removing from favorites: " + e.getMessage()));
        }
    }
    
    /**
     * Toggle favorite status
     * POST /api/favorites/toggle
     */
    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<Boolean>> toggleFavorite(
            @RequestParam String sessionId,
            @RequestParam Long garmentId) {
        try {
            boolean isFavorited = favoriteService.toggleFavorite(sessionId, garmentId);
            
            String message = isFavorited ? "Garment added to favorites" : "Garment removed from favorites";
            return ResponseEntity.ok(ApiResponse.success(isFavorited, message));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error toggling favorite for garment {} and session {}: {}", 
                    garmentId, sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error toggling favorite: " + e.getMessage()));
        }
    }
    
    /**
     * Check if garment is favorited
     * GET /api/favorites/check
     */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> isFavorited(
            @RequestParam String sessionId,
            @RequestParam Long garmentId) {
        try {
            boolean isFavorited = favoriteService.isFavorited(sessionId, garmentId);
            return ResponseEntity.ok(ApiResponse.success(isFavorited, "Favorite status retrieved"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error checking favorite status for garment {} and session {}: {}", 
                    garmentId, sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error checking favorite status: " + e.getMessage()));
        }
    }
    
    /**
     * Get user favorites with pagination
     * GET /api/favorites?sessionId=xxx&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GarmentDto>>> getUserFavorites(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<GarmentDto> favorites = favoriteService.getUserFavorites(sessionId, page, size);
            return ResponseEntity.ok(ApiResponse.success(favorites, "Favorites retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error getting favorites for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving favorites: " + e.getMessage()));
        }
    }
    
    /**
     * Get all user favorites (no pagination)
     * GET /api/favorites/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GarmentDto>>> getAllUserFavorites(@RequestParam String sessionId) {
        try {
            List<GarmentDto> favorites = favoriteService.getAllUserFavorites(sessionId);
            return ResponseEntity.ok(ApiResponse.success(favorites, "All favorites retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error getting all favorites for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving favorites: " + e.getMessage()));
        }
    }
    
    /**
     * Get favorites by category
     * GET /api/favorites/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<GarmentDto>>> getFavoritesByCategory(
            @RequestParam String sessionId,
            @PathVariable String category) {
        try {
            List<GarmentDto> favorites = favoriteService.getFavoritesByCategory(sessionId, category);
            return ResponseEntity.ok(ApiResponse.success(favorites, "Category favorites retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error getting favorites by category {} for session {}: {}", 
                    category, sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving category favorites: " + e.getMessage()));
        }
    }
    
    /**
     * Get recent favorites
     * GET /api/favorites/recent?days=7
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<GarmentDto>>> getRecentFavorites(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<GarmentDto> favorites = favoriteService.getRecentFavorites(sessionId, days);
            return ResponseEntity.ok(ApiResponse.success(favorites, "Recent favorites retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error getting recent favorites for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving recent favorites: " + e.getMessage()));
        }
    }
    
    /**
     * Get favorite count
     * GET /api/favorites/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getFavoriteCount(@RequestParam String sessionId) {
        try {
            long count = favoriteService.getFavoriteCount(sessionId);
            return ResponseEntity.ok(ApiResponse.success(count, "Favorite count retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error getting favorite count for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving favorite count: " + e.getMessage()));
        }
    }
    
    /**
     * Check favorite status for multiple garments
     * POST /api/favorites/check-multiple
     */
    @PostMapping("/check-multiple")
    public ResponseEntity<ApiResponse<Set<Long>>> checkMultipleFavorites(
            @RequestParam String sessionId,
            @RequestBody List<Long> garmentIds) {
        try {
            Set<Long> favoritedIds = favoriteService.getFavoritedGarmentIds(sessionId, garmentIds);
            return ResponseEntity.ok(ApiResponse.success(favoritedIds, "Favorite status checked for multiple garments"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error checking multiple favorites for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error checking favorite status: " + e.getMessage()));
        }
    }
    
    /**
     * Clear all favorites
     * DELETE /api/favorites/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Long>> clearAllFavorites(@RequestParam String sessionId) {
        try {
            long deletedCount = favoriteService.clearAllFavorites(sessionId);
            return ResponseEntity.ok(ApiResponse.success(deletedCount, 
                    "All favorites cleared successfully. Removed " + deletedCount + " items."));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error clearing favorites for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error clearing favorites: " + e.getMessage()));
        }
    }
    
    /**
     * Get trending garments
     * GET /api/favorites/trending?days=7&limit=10
     */
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<Long>>> getTrendingGarments(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Long> trendingIds = favoriteService.getTrendingGarments(days, limit);
            return ResponseEntity.ok(ApiResponse.success(trendingIds, "Trending garments retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error getting trending garments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving trending garments: " + e.getMessage()));
        }
    }
    
    /**
     * Get popular garments (most favorited)
     * GET /api/favorites/popular?limit=10
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularGarments(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> popular = favoriteService.getPopularGarments(limit);
            return ResponseEntity.ok(ApiResponse.success(popular, "Popular garments retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error getting popular garments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving popular garments: " + e.getMessage()));
        }
    }
}
