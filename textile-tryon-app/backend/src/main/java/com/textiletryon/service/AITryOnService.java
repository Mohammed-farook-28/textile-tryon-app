package com.textiletryon.service;

import com.textiletryon.dto.TryonRequestDto;
import com.textiletryon.dto.TryonResultDto;
import com.textiletryon.model.*;
import com.textiletryon.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for AI-powered virtual try-on functionality
 * 
 * Integrates with external AI APIs:
 * - Google Virtual Try-On API
 * - Flux Context Pro API
 * 
 * Handles the complete try-on workflow from request to result storage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AITryOnService {
    
    private final WebClient webClient;
    private final UserProfileRepository userProfileRepository;
    private final GarmentRepository garmentRepository;
    private final GarmentImageRepository garmentImageRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final TryonResultRepository tryonResultRepository;
    private final S3Service s3Service;
    
    @Value("${ai.flux.api-key}")
    private String fluxApiKey;
    
    @Value("${ai.flux.api-url}")
    private String fluxApiUrl;
    
    @Value("${ai.google.gemini.api-key}")
    private String googleApiKey;
    
    @Value("${ai.google.gemini.api-url}")
    private String googleApiUrl;
    
    @Value("${ai.google.gemini.model}")
    private String googleModel;
    
    @Value("${ai.flux.timeout:60000}")
    private int fluxTimeout;
    
    @Value("${ai.google.gemini.timeout:60000}")
    private int googleTimeout;
    
    /**
     * Generate virtual try-on result
     * 
     * @param sessionId User session ID
     * @param request Try-on request with garment, photo, and model selection
     * @return Try-on result with generated image
     */
    @Transactional
    public TryonResultDto generateTryOn(String sessionId, TryonRequestDto request) {
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // Validate and get entities
            UserProfile userProfile = getUserProfile(sessionId);
            Garment garment = getGarment(request.getGarmentId());
            UserPhoto userPhoto = getUserPhoto(request.getUserPhotoId(), userProfile.getId());
            
            // Get garment image URL (primary image)
            String garmentImageUrl = getGarmentImageUrl(garment.getId());
            
            // Generate try-on based on selected AI model
            byte[] resultImageBytes;
            String contentType = "image/jpeg";
            
            switch (request.getAiModel().toLowerCase()) {
                case "flux-context-pro" -> {
                    resultImageBytes = generateWithFlux(garmentImageUrl, userPhoto.getPhotoUrl(), request);
                }
                case "google-tryon" -> {
                    resultImageBytes = generateWithGoogle(garmentImageUrl, userPhoto.getPhotoUrl(), request);
                }
                default -> throw new IllegalArgumentException("Unsupported AI model: " + request.getAiModel());
            }
            
            // Upload result to S3
            String resultImageUrl = s3Service.uploadTryonResult(
                    resultImageBytes, 
                    userProfile.getId(), 
                    garment.getId(), 
                    contentType
            );
            
            // Save try-on result to database
            TryonResult tryonResult = TryonResult.builder()
                    .userProfile(userProfile)
                    .garment(garment)
                    .userPhoto(userPhoto)
                    .resultImageUrl(resultImageUrl)
                    .aiModelUsed(request.getAiModel())
                    .build();
            
            tryonResult = tryonResultRepository.save(tryonResult);
            
            // Calculate processing time
            long processingTimeMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
            
            log.info("Try-on generated successfully for user {} with garment {} using model {}", 
                    sessionId, request.getGarmentId(), request.getAiModel());
            
            return TryonResultDto.createSuccess(
                    tryonResult.getId(),
                    resultImageUrl,
                    request.getAiModel(),
                    garment.getId(),
                    garment.getGarmentName(),
                    userPhoto.getId(),
                    userPhoto.getDisplayName()
            ).toBuilder()
                    .processingTimeMs(processingTimeMs)
                    .build();
            
        } catch (Exception e) {
            log.error("Error generating try-on for user {} with garment {}: {}", 
                    sessionId, request.getGarmentId(), e.getMessage(), e);
            
            return TryonResultDto.createFailure(
                    request.getAiModel(),
                    request.getGarmentId(),
                    request.getUserPhotoId(),
                    e.getMessage()
            );
        }
    }
    
    /**
     * Generate try-on using Flux Context Pro API
     */
    private byte[] generateWithFlux(String garmentImageUrl, String userPhotoUrl, TryonRequestDto request) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("garment_image", garmentImageUrl);
            requestBody.put("person_image", userPhotoUrl);
            requestBody.put("prompt", createFluxPrompt(request));
            
            // Optional parameters
            if (request.getStrength() != null) {
                requestBody.put("strength", request.getStrength());
            }
            if (request.getSteps() != null) {
                requestBody.put("steps", request.getSteps());
            }
            
            String response = webClient.post()
                    .uri(fluxApiUrl + "virtual-tryon")
                    .header("Authorization", "Bearer " + fluxApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(fluxTimeout))
                    .block();
            
            // Parse response and extract image data
            return parseFluxResponse(response);
            
        } catch (Exception e) {
            log.error("Error calling Flux API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate try-on with Flux API", e);
        }
    }
    
    /**
     * Generate enhanced try-on using Google Gemini for intelligent prompts + Flux for generation
     */
    private byte[] generateWithGoogle(String garmentImageUrl, String userPhotoUrl, TryonRequestDto request) {
        try {
            // First, use Google Gemini to enhance the prompt
            String enhancedPrompt = enhancePromptWithGemini(request);
            
            // Then use Flux to generate the actual try-on image with enhanced prompt
            TryonRequestDto enhancedRequest = TryonRequestDto.builder()
                    .garmentId(request.getGarmentId())
                    .userPhotoId(request.getUserPhotoId())
                    .aiModel(request.getAiModel())
                    .customPrompt(enhancedPrompt)
                    .strength(request.getStrength())
                    .steps(request.getSteps())
                    .style(request.getStyle())
                    .build();
                    
            return generateWithFlux(garmentImageUrl, userPhotoUrl, enhancedRequest);
            
        } catch (Exception e) {
            log.error("Error in Google-enhanced try-on generation: {}", e.getMessage(), e);
            // Fallback to regular Flux generation if Gemini fails
            return generateWithFlux(garmentImageUrl, userPhotoUrl, request);
        }
    }
    
    /**
     * Use Google Gemini to enhance the try-on prompt
     */
    private String enhancePromptWithGemini(TryonRequestDto request) {
        try {
            // Get garment details for context
            Garment garment = getGarment(request.getGarmentId());
            
            // Create prompt for Gemini to enhance the try-on description
            String basePrompt = String.format(
                "Create a detailed, natural prompt for virtual try-on of a %s %s %s. " +
                "The garment is %s colored with %s pattern. " +
                "Focus on realistic fit, natural draping, and appropriate styling. " +
                "User's custom request: %s. " +
                "Return only the enhanced prompt, no additional text.",
                garment.getCategory(),
                garment.getGarmentType(),
                garment.getGarmentName(),
                garment.getColor(),
                garment.getPatternStyle() != null ? garment.getPatternStyle() : "no specific",
                request.getCustomPrompt() != null ? request.getCustomPrompt() : "make it look natural"
            );
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            
            part.put("text", basePrompt);
            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});
            
            String response = webClient.post()
                    .uri(googleApiUrl + "models/" + googleModel + ":generateContent")
                    .header("X-goog-api-key", googleApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(googleTimeout))
                    .block();
            
            // Parse Gemini response and extract enhanced prompt
            return parseGeminiResponse(response);
            
        } catch (Exception e) {
            log.error("Error calling Google Gemini API: {}", e.getMessage(), e);
            // Return a fallback prompt if Gemini fails
            return createFallbackPrompt(request);
        }
    }
    
    /**
     * Get user's try-on results
     */
    public Page<TryonResultDto> getUserTryonResults(String sessionId, int page, int size) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TryonResult> results = tryonResultRepository.findByUserProfileId(userProfile.getId(), pageable);
        
        return results.map(this::convertToDto);
    }
    
    /**
     * Delete try-on result
     */
    @Transactional
    public void deleteTryonResult(String sessionId, Long resultId) {
        UserProfile userProfile = getUserProfile(sessionId);
        
        TryonResult result = tryonResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Try-on result not found"));
        
        if (!result.getUserProfile().getId().equals(userProfile.getId())) {
            throw new IllegalArgumentException("Try-on result does not belong to the user");
        }
        
        // Delete image from S3
        s3Service.deleteFile(result.getResultImageUrl());
        
        // Delete from database
        tryonResultRepository.delete(result);
        
        log.info("Deleted try-on result {} for user {}", resultId, sessionId);
    }
    
    private UserProfile getUserProfile(String sessionId) {
        return userProfileRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found for session: " + sessionId));
    }
    
    private Garment getGarment(Long garmentId) {
        return garmentRepository.findById(garmentId)
                .orElseThrow(() -> new IllegalArgumentException("Garment not found with ID: " + garmentId));
    }
    
    private UserPhoto getUserPhoto(Long photoId, Long userProfileId) {
        return userPhotoRepository.findByIdAndUserProfileId(photoId, userProfileId)
                .orElseThrow(() -> new IllegalArgumentException("User photo not found or does not belong to user"));
    }
    
    private String getGarmentImageUrl(Long garmentId) {
        return garmentImageRepository.findByGarmentIdAndIsPrimaryTrue(garmentId)
                .map(GarmentImage::getImageUrl)
                .orElseThrow(() -> new IllegalArgumentException("No primary image found for garment"));
    }
    
    private String createFluxPrompt(TryonRequestDto request) {
        String basePrompt = "Make the person wear this garment naturally and realistically";
        
        if (request.getCustomPrompt() != null && !request.getCustomPrompt().trim().isEmpty()) {
            return request.getCustomPrompt();
        }
        
        if (request.getStyle() != null) {
            basePrompt += ", " + request.getStyle();
        }
        
        return basePrompt;
    }
    
    private String createGooglePrompt(TryonRequestDto request) {
        String basePrompt = "Virtual try-on: person wearing the garment";
        
        if (request.getCustomPrompt() != null && !request.getCustomPrompt().trim().isEmpty()) {
            return request.getCustomPrompt();
        }
        
        return basePrompt;
    }
    
    private String parseGeminiResponse(String response) {
        try {
            // Parse JSON response from Gemini and extract the enhanced prompt
            // This is a simplified parser - in production, use a proper JSON library
            if (response.contains("\"text\"")) {
                int startIndex = response.indexOf("\"text\":\"") + 8;
                int endIndex = response.indexOf("\"", startIndex);
                if (startIndex > 7 && endIndex > startIndex) {
                    return response.substring(startIndex, endIndex)
                            .replace("\\n", " ")
                            .replace("\\\"", "\"")
                            .trim();
                }
            }
            
            log.warn("Could not parse Gemini response, using fallback");
            return "Natural virtual try-on with realistic fit and draping";
            
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage());
            return "Natural virtual try-on with realistic fit and draping";
        }
    }
    
    private String createFallbackPrompt(TryonRequestDto request) {
        String fallback = "Natural virtual try-on with realistic fit and draping";
        
        if (request.getCustomPrompt() != null && !request.getCustomPrompt().trim().isEmpty()) {
            fallback += ". " + request.getCustomPrompt();
        }
        
        if (request.getStyle() != null) {
            fallback += ". Style: " + request.getStyle();
        }
        
        return fallback;
    }
    
    private byte[] parseFluxResponse(String response) {
        try {
            // Parse JSON response and extract base64 image
            // This is a simplified implementation - actual parsing would depend on Flux API response format
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(response);
            
            String base64Image = jsonNode.get("image").asText();
            return Base64.getDecoder().decode(base64Image);
            
        } catch (Exception e) {
            log.error("Error parsing Flux response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Flux API response", e);
        }
    }
    
    private byte[] parseGoogleResponse(String response) {
        try {
            // Parse JSON response and extract base64 image
            // This is a simplified implementation - actual parsing would depend on Google API response format
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(response);
            
            String base64Image = jsonNode.get("generatedImage").asText();
            return Base64.getDecoder().decode(base64Image);
            
        } catch (Exception e) {
            log.error("Error parsing Google response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Google API response", e);
        }
    }
    
    private TryonResultDto convertToDto(TryonResult result) {
        return TryonResultDto.builder()
                .id(result.getId())
                .resultImageUrl(result.getResultImageUrl())
                .aiModelUsed(result.getAiModelUsed())
                .createdAt(result.getCreatedAt())
                .garmentId(result.getGarment().getId())
                .garmentName(result.getGarment().getGarmentName())
                .userPhotoId(result.getUserPhoto().getId())
                .userPhotoName(result.getUserPhoto().getDisplayName())
                .userPhotoUrl(result.getUserPhoto().getPhotoUrl())
                .status("SUCCESS")
                .build();
    }
}
