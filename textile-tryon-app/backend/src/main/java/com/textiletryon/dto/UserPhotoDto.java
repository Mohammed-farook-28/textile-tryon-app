package com.textiletryon.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for user photos
 * 
 * Used for API requests and responses involving user photo data.
 * Includes validation for photo uploads and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPhotoDto {
    
    private Long id;
    
    @NotBlank(message = "Photo URL is required")
    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;
    
    @Size(max = 100, message = "Photo name must not exceed 100 characters")
    private String photoName;
    
    private LocalDateTime uploadedAt;
    
    // Additional metadata
    private Long fileSizeBytes;
    private String mimeType;
    private Integer width;
    private Integer height;
    
    // Usage statistics
    private Long tryonCount;
    private boolean hasBeenUsed;
    
    /**
     * Create a basic photo DTO
     */
    public static UserPhotoDto createBasic(Long id, String photoUrl, String photoName) {
        return UserPhotoDto.builder()
                .id(id)
                .photoUrl(photoUrl)
                .photoName(photoName)
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}
