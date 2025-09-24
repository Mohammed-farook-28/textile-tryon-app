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
 * UserPhoto entity for storing user uploaded photos
 * 
 * This entity manages:
 * - User photo URLs stored in AWS S3
 * - Photo names and metadata
 * - Association with user profile
 * - Try-on result relationships
 */
@Entity
@Table(name = "user_photos", indexes = {
    @Index(name = "idx_user_profile_id", columnList = "user_profile_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class UserPhoto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    @NotNull(message = "User profile is required")
    private UserProfile userProfile;
    
    @Column(name = "photo_url", nullable = false, length = 500)
    @NotBlank(message = "Photo URL is required")
    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$", 
             message = "Photo URL must be a valid HTTP(S) URL ending with jpg, jpeg, png, gif, or webp")
    private String photoUrl;
    
    @Column(name = "photo_name", length = 100)
    @Size(max = 100, message = "Photo name must not exceed 100 characters")
    private String photoName;
    
    @Column(name = "uploaded_at", updatable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
    
    // One-to-many relationship with TryonResult
    @OneToMany(mappedBy = "userPhoto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TryonResult> tryonResults;
    
    /**
     * Get the file name from the photo URL
     * @return the file name extracted from the URL
     */
    public String getFileName() {
        if (photoUrl == null || photoUrl.isEmpty()) {
            return null;
        }
        int lastSlashIndex = photoUrl.lastIndexOf('/');
        return lastSlashIndex != -1 ? photoUrl.substring(lastSlashIndex + 1) : photoUrl;
    }
    
    /**
     * Get display name for the photo
     * @return photo name if available, otherwise file name from URL
     */
    public String getDisplayName() {
        if (photoName != null && !photoName.trim().isEmpty()) {
            return photoName;
        }
        String fileName = getFileName();
        return fileName != null ? fileName : "Untitled Photo";
    }
    
    /**
     * Check if this photo has been used in try-on results
     * @return true if there are any try-on results using this photo
     */
    public boolean hasBeenUsedInTryOn() {
        return tryonResults != null && !tryonResults.isEmpty();
    }
    
    /**
     * Get the number of try-on results using this photo
     * @return count of try-on results
     */
    public int getTryonResultCount() {
        return tryonResults != null ? tryonResults.size() : 0;
    }
}
