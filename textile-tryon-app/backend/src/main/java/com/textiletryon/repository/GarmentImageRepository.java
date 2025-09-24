package com.textiletryon.repository;

import com.textiletryon.model.GarmentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GarmentImage entity
 * 
 * Provides operations for managing garment images:
 * - CRUD operations for garment images
 * - Queries for finding images by garment
 * - Primary image management
 * - Display order management
 */
@Repository
public interface GarmentImageRepository extends JpaRepository<GarmentImage, Long> {
    
    /**
     * Find all images for a specific garment ordered by display order
     * @param garmentId the ID of the garment
     * @return list of images for the garment
     */
    List<GarmentImage> findByGarmentIdOrderByDisplayOrderAsc(Long garmentId);
    
    /**
     * Find the primary image for a specific garment
     * @param garmentId the ID of the garment
     * @return Optional containing the primary image if found
     */
    Optional<GarmentImage> findByGarmentIdAndIsPrimaryTrue(Long garmentId);
    
    /**
     * Find all primary images for multiple garments
     * @param garmentIds list of garment IDs
     * @return list of primary images
     */
    List<GarmentImage> findByGarmentIdInAndIsPrimaryTrue(List<Long> garmentIds);
    
    /**
     * Count images for a specific garment
     * @param garmentId the ID of the garment
     * @return number of images for the garment
     */
    Long countByGarmentId(Long garmentId);
    
    /**
     * Find images by garment and check if they are primary
     * @param garmentId the ID of the garment
     * @param isPrimary whether to find primary or non-primary images
     * @return list of images matching the criteria
     */
    List<GarmentImage> findByGarmentIdAndIsPrimary(Long garmentId, Boolean isPrimary);
    
    /**
     * Find image by URL
     * @param imageUrl the image URL to search for
     * @return Optional containing the image if found
     */
    Optional<GarmentImage> findByImageUrl(String imageUrl);
    
    /**
     * Check if an image URL already exists
     * @param imageUrl the image URL to check
     * @return true if the URL exists, false otherwise
     */
    boolean existsByImageUrl(String imageUrl);
    
    /**
     * Get the maximum display order for a garment's images
     * @param garmentId the ID of the garment
     * @return the maximum display order, or null if no images exist
     */
    @Query("SELECT MAX(gi.displayOrder) FROM GarmentImage gi WHERE gi.garment.id = :garmentId")
    Integer findMaxDisplayOrderByGarmentId(@Param("garmentId") Long garmentId);
    
    /**
     * Update all images of a garment to not be primary
     * @param garmentId the ID of the garment
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE GarmentImage gi SET gi.isPrimary = false WHERE gi.garment.id = :garmentId")
    int clearPrimaryImageForGarment(@Param("garmentId") Long garmentId);
    
    /**
     * Set a specific image as primary for its garment
     * @param imageId the ID of the image to set as primary
     * @param garmentId the ID of the garment
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE GarmentImage gi SET gi.isPrimary = CASE WHEN gi.id = :imageId THEN true ELSE false END WHERE gi.garment.id = :garmentId")
    int setPrimaryImage(@Param("imageId") Long imageId, @Param("garmentId") Long garmentId);
    
    /**
     * Update display orders for images of a garment
     * @param garmentId the ID of the garment
     * @param imageId the ID of the image
     * @param newDisplayOrder the new display order
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE GarmentImage gi SET gi.displayOrder = :newDisplayOrder WHERE gi.id = :imageId AND gi.garment.id = :garmentId")
    int updateDisplayOrder(@Param("imageId") Long imageId, @Param("garmentId") Long garmentId, @Param("newDisplayOrder") Integer newDisplayOrder);
    
    /**
     * Delete all images for a specific garment
     * @param garmentId the ID of the garment
     * @return number of deleted records
     */
    @Modifying
    @Transactional
    Long deleteByGarmentId(Long garmentId);
    
    /**
     * Find images that need cleanup (orphaned images)
     * @return list of image URLs that may need to be deleted from S3
     */
    @Query("SELECT gi.imageUrl FROM GarmentImage gi WHERE gi.garment IS NULL")
    List<String> findOrphanedImageUrls();
    
    /**
     * Get all image URLs for a specific garment
     * @param garmentId the ID of the garment
     * @return list of image URLs
     */
    @Query("SELECT gi.imageUrl FROM GarmentImage gi WHERE gi.garment.id = :garmentId ORDER BY gi.displayOrder ASC")
    List<String> findImageUrlsByGarmentId(@Param("garmentId") Long garmentId);
    
    /**
     * Find images by multiple garment IDs for batch operations
     * @param garmentIds list of garment IDs
     * @return list of all images for the specified garments
     */
    List<GarmentImage> findByGarmentIdInOrderByGarmentIdAscDisplayOrderAsc(List<Long> garmentIds);
    
    /**
     * Count total number of images in the system
     * @return total count of garment images
     */
    @Query("SELECT COUNT(gi) FROM GarmentImage gi")
    Long countAllImages();
    
    /**
     * Find images uploaded recently for monitoring
     * @param days number of days to look back
     * @return list of recently added images
     */
    @Query("SELECT gi FROM GarmentImage gi WHERE gi.createdAt >= CURRENT_TIMESTAMP - :days DAY ORDER BY gi.createdAt DESC")
    List<GarmentImage> findRecentlyAdded(@Param("days") int days);
}
