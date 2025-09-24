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
 * Favorite entity for storing user's favorite garments
 * 
 * This entity manages:
 * - User-garment favorite relationships
 * - Unique constraint to prevent duplicate favorites
 * - Timestamps for favorite creation
 */
@Entity
@Table(name = "favorites", 
       indexes = {
           @Index(name = "idx_user_profile_id", columnList = "user_profile_id"),
           @Index(name = "idx_garment_id", columnList = "garment_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_favorite", columnNames = {"user_profile_id", "garment_id"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Favorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    @NotNull(message = "User profile is required")
    private UserProfile userProfile;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garment_id", nullable = false)
    @NotNull(message = "Garment is required")
    private Garment garment;
    
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Check if this favorite was created recently (within last 24 hours)
     * @return true if created within last 24 hours
     */
    public boolean isRecentlyAdded() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(1));
    }
    
    /**
     * Get the garment name from the associated garment
     * @return garment name or null if garment is not loaded
     */
    public String getGarmentName() {
        return garment != null ? garment.getGarmentName() : null;
    }
    
    /**
     * Get the user's session ID from the associated user profile
     * @return session ID or null if user profile is not loaded
     */
    public String getUserSessionId() {
        return userProfile != null ? userProfile.getSessionId() : null;
    }
}
