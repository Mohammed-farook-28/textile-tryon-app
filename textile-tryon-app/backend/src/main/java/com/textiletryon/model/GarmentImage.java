package com.textiletryon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * GarmentImage entity for storing garment image information
 * 
 * This entity manages:
 * - Image URLs stored in AWS S3
 * - Primary image designation
 * - Display order for multiple images
 * - Association with parent garment
 */
@Entity
@Table(name = "garment_images", indexes = {
    @Index(name = "idx_garment_id", columnList = "garment_id"),
    @Index(name = "idx_is_primary", columnList = "is_primary")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class GarmentImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garment_id", nullable = false)
    @NotNull(message = "Garment is required")
    private Garment garment;
    
    @Column(name = "image_url", nullable = false, length = 500)
    @NotBlank(message = "Image URL is required")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$", 
             message = "Image URL must be a valid HTTP(S) URL ending with jpg, jpeg, png, gif, or webp")
    private String imageUrl;
    
    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;
    
    @Column(name = "display_order")
    @Min(value = 0, message = "Display order cannot be negative")
    @Builder.Default
    private Integer displayOrder = 0;
    
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Get the file name from the image URL
     * @return the file name extracted from the URL
     */
    public String getFileName() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        int lastSlashIndex = imageUrl.lastIndexOf('/');
        return lastSlashIndex != -1 ? imageUrl.substring(lastSlashIndex + 1) : imageUrl;
    }
    
    /**
     * Check if this is a primary image
     * @return true if this is the primary image for the garment
     */
    public boolean isPrimaryImage() {
        return isPrimary != null && isPrimary;
    }
}
