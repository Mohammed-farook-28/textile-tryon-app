package com.textiletryon.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for virtual try-on results
 * 
 * Used for returning try-on results to the frontend.
 * Contains result image and metadata about the generation process.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TryonResultDto {
    
    private Long id;
    private String resultImageUrl;
    private String aiModelUsed;
    private LocalDateTime createdAt;
    
    // Related entity information
    private Long garmentId;
    private String garmentName;
    private String garmentImageUrl;
    
    private Long userPhotoId;
    private String userPhotoName;
    private String userPhotoUrl;
    
    // Processing information
    private Long processingTimeMs;
    private String status; // SUCCESS, FAILED, PROCESSING
    private String errorMessage;
    
    /**
     * Create a successful result DTO
     */
    public static TryonResultDto createSuccess(Long id, String resultImageUrl, String aiModelUsed,
                                             Long garmentId, String garmentName, 
                                             Long userPhotoId, String userPhotoName) {
        return TryonResultDto.builder()
                .id(id)
                .resultImageUrl(resultImageUrl)
                .aiModelUsed(aiModelUsed)
                .garmentId(garmentId)
                .garmentName(garmentName)
                .userPhotoId(userPhotoId)
                .userPhotoName(userPhotoName)
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Create a failed result DTO
     */
    public static TryonResultDto createFailure(String aiModelUsed, Long garmentId, 
                                             Long userPhotoId, String errorMessage) {
        return TryonResultDto.builder()
                .aiModelUsed(aiModelUsed)
                .garmentId(garmentId)
                .userPhotoId(userPhotoId)
                .status("FAILED")
                .errorMessage(errorMessage)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
