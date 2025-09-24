package com.textiletryon.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for virtual try-on requests
 * 
 * Used for requesting AI-powered try-on generation.
 * Contains all necessary information for the try-on process.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TryonRequestDto {
    
    @NotNull(message = "Garment ID is required")
    private Long garmentId;
    
    @NotNull(message = "User photo ID is required")
    private Long userPhotoId;
    
    @NotBlank(message = "AI model is required")
    @Pattern(regexp = "^(google-tryon|flux-context-pro)$", 
             message = "AI model must be either 'google-tryon' or 'flux-context-pro'")
    private String aiModel;
    
    @Size(max = 500, message = "Custom prompt must not exceed 500 characters")
    private String customPrompt;
    
    // Optional parameters for fine-tuning
    private Double strength; // For Flux model
    private Integer steps;   // For Flux model
    private String style;    // Additional style instructions
    
    /**
     * Create a basic try-on request
     */
    public static TryonRequestDto createBasic(Long garmentId, Long userPhotoId, String aiModel) {
        return TryonRequestDto.builder()
                .garmentId(garmentId)
                .userPhotoId(userPhotoId)
                .aiModel(aiModel)
                .build();
    }
}
