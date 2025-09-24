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
 * TryonResult entity for storing virtual try-on results
 * 
 * This entity manages:
 * - Generated try-on result images
 * - AI model information used for generation
 * - Associations with user, garment, and source photo
 * - Timestamps for result creation
 */
@Entity
@Table(name = "tryon_results", indexes = {
    @Index(name = "idx_user_profile_id", columnList = "user_profile_id"),
    @Index(name = "idx_garment_id", columnList = "garment_id"),
    @Index(name = "idx_user_photo_id", columnList = "user_photo_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class TryonResult {
    
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_photo_id", nullable = false)
    @NotNull(message = "User photo is required")
    private UserPhoto userPhoto;
    
    @Column(name = "result_image_url", nullable = false, length = 500)
    @NotBlank(message = "Result image URL is required")
    @Size(max = 500, message = "Result image URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$", 
             message = "Result image URL must be a valid HTTP(S) URL ending with jpg, jpeg, png, gif, or webp")
    private String resultImageUrl;
    
    @Column(name = "ai_model_used", length = 50)
    @Size(max = 50, message = "AI model name must not exceed 50 characters")
    private String aiModelUsed;
    
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Enumeration of supported AI models for try-on generation
     */
    public enum AIModel {
        GOOGLE_TRYON("google-tryon", "Google Virtual Try-On"),
        FLUX_CONTEXT_PRO("flux-context-pro", "Flux Context Pro");
        
        private final String code;
        private final String displayName;
        
        AIModel(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static AIModel fromCode(String code) {
            for (AIModel model : values()) {
                if (model.code.equals(code)) {
                    return model;
                }
            }
            return null;
        }
    }
    
    /**
     * Get the file name from the result image URL
     * @return the file name extracted from the URL
     */
    public String getResultFileName() {
        if (resultImageUrl == null || resultImageUrl.isEmpty()) {
            return null;
        }
        int lastSlashIndex = resultImageUrl.lastIndexOf('/');
        return lastSlashIndex != -1 ? resultImageUrl.substring(lastSlashIndex + 1) : resultImageUrl;
    }
    
    /**
     * Check if this result was created recently (within last hour)
     * @return true if created within last hour
     */
    public boolean isRecentlyCreated() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusHours(1));
    }
    
    /**
     * Get the AI model enum from the stored string
     * @return AIModel enum or null if not found
     */
    public AIModel getAIModelEnum() {
        return AIModel.fromCode(aiModelUsed);
    }
    
    /**
     * Get display name for the AI model used
     * @return display name or the raw model string if not found
     */
    public String getAIModelDisplayName() {
        AIModel model = getAIModelEnum();
        return model != null ? model.getDisplayName() : aiModelUsed;
    }
    
    /**
     * Get the garment name from the associated garment
     * @return garment name or null if garment is not loaded
     */
    public String getGarmentName() {
        return garment != null ? garment.getGarmentName() : null;
    }
    
    /**
     * Get the user photo name from the associated user photo
     * @return photo display name or null if user photo is not loaded
     */
    public String getUserPhotoName() {
        return userPhoto != null ? userPhoto.getDisplayName() : null;
    }
}
