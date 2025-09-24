package com.textiletryon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserProfile entity for session-based user management
 * 
 * This entity manages:
 * - Session-based user identification (no authentication)
 * - User profile information
 * - Associations with photos, favorites, and try-on results
 */
@Entity
@Table(name = "user_profiles", indexes = {
    @Index(name = "idx_session_id", columnList = "sessionId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", unique = true, nullable = false)
    @NotBlank(message = "Session ID is required")
    @Size(max = 255, message = "Session ID must not exceed 255 characters")
    private String sessionId;
    
    @Column(name = "profile_name", length = 100)
    @Size(max = 100, message = "Profile name must not exceed 100 characters")
    private String profileName;
    
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // One-to-many relationship with UserPhoto
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserPhoto> photos;
    
    // One-to-many relationship with Favorite
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Favorite> favorites;
    
    // One-to-many relationship with TryonResult
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TryonResult> tryonResults;
    
    /**
     * Check if the user has a profile name set
     * @return true if profile name is not null and not empty
     */
    public boolean hasProfileName() {
        return profileName != null && !profileName.trim().isEmpty();
    }
    
    /**
     * Get display name for the user
     * @return profile name if available, otherwise a default name
     */
    public String getDisplayName() {
        return hasProfileName() ? profileName : "Guest User";
    }
    
    /**
     * Check if the user has uploaded photos
     * @return true if the user has any photos
     */
    public boolean hasPhotos() {
        return photos != null && !photos.isEmpty();
    }
    
    /**
     * Get the number of favorites for this user
     * @return count of favorites
     */
    public int getFavoriteCount() {
        return favorites != null ? favorites.size() : 0;
    }
    
    /**
     * Get the number of try-on results for this user
     * @return count of try-on results
     */
    public int getTryonResultCount() {
        return tryonResults != null ? tryonResults.size() : 0;
    }
}
