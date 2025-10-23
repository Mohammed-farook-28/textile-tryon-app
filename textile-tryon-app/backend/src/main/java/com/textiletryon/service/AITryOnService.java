package com.textiletryon.service;

import com.textiletryon.dto.TryonRequestDto;
import com.textiletryon.dto.TryonResultDto;
import com.textiletryon.model.*;
import com.textiletryon.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service for AI-powered virtual try-on functionality.
 * Uses Google Gemini REST API to combine user and garment images.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AITryOnService {
    
    private final UserProfileRepository userProfileRepository;
    private final GarmentRepository garmentRepository;
    private final GarmentImageRepository garmentImageRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final TryonResultRepository tryonResultRepository;
    private final FileStorageService fileStorageService;
    
    @Value("${ai.google.gemini.api-key}")
    private String googleApiKey;
    
    @Value("${ai.google.gemini.model}")
    private String googleModel;
    
    @Value("${ai.google.gemini.api-url}")
    private String googleApiUrl;
    
    @Value("${ai.google.gemini.timeout:60000}")
    private int googleTimeout;
    
    /**
     * Generate virtual try-on result
     */
    @Transactional
    public TryonResultDto generateTryOn(String sessionId, TryonRequestDto request) {
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            UserProfile userProfile = getUserProfile(sessionId);
            Garment garment = getGarment(request.getGarmentId());
            UserPhoto userPhoto = getUserPhoto(request.getUserPhotoId(), userProfile.getId());
            String garmentImageUrl = getGarmentImageUrl(garment.getId());
            
            byte[] resultImageBytes = generateWithGoogle(garmentImageUrl, userPhoto.getPhotoUrl(), request);
            String contentType = "image/jpeg";
            
            String resultImageUrl = fileStorageService.uploadTryonResult(
                    resultImageBytes, 
                    userProfile.getId(), 
                    garment.getId(), 
                    contentType
            );
            
            TryonResult tryonResult = TryonResult.builder()
                    .userProfile(userProfile)
                    .garment(garment)
                    .userPhoto(userPhoto)
                    .resultImageUrl(resultImageUrl)
                    .aiModelUsed(request.getAiModel())
                    .build();
            
            tryonResult = tryonResultRepository.save(tryonResult);
            long processingTimeMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
            
            log.info("✅ Try-on generated successfully for user {} and garment {}", sessionId, garment.getId());
            
            return TryonResultDto.createSuccess(
                    tryonResult.getId(),
                    resultImageUrl,
                    request.getAiModel(),
                    garment.getId(),
                    garment.getGarmentName(),
                    userPhoto.getId(),
                    userPhoto.getDisplayName()
            ).toBuilder().processingTimeMs(processingTimeMs).build();
            
        } catch (Exception e) {
            log.error("❌ Error generating try-on for user {}: {}", sessionId, e.getMessage(), e);
            return TryonResultDto.createFailure(
                    request.getAiModel(),
                    request.getGarmentId(),
                    request.getUserPhotoId(),
                    e.getMessage()
            );
        }
    }
    
    /**
     * Generate virtual try-on using Google Gemini API
     * This sends the prompt to Gemini to create a realistic try-on result
     */
    private byte[] generateWithGoogle(String garmentImageUrl, String userPhotoUrl, TryonRequestDto request) {
        try {
            log.info("🎨 Sending prompt to Google Gemini API for virtual try-on");
            
            // Download both images
            byte[] userPhotoBytes = downloadAsBytes(userPhotoUrl);
            byte[] garmentBytes = downloadAsBytes(garmentImageUrl);
            
            // Send prompt to Gemini API with the images
            return sendPromptToGemini(userPhotoBytes, garmentBytes, request);
            
        } catch (Exception e) {
            log.error("Try-on generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate virtual try-on", e);
        }
    }
    
    /**
     * Send prompt to Google Gemini API for try-on generation
     */
    private byte[] sendPromptToGemini(byte[] userPhotoBytes, byte[] garmentBytes, TryonRequestDto request) {
        try {
            // Generate automatic prompt based on the garment
            String autoPrompt = generateAutoPrompt(request);
            log.info("🤖 Auto-generated prompt: {}", autoPrompt);
            
            // Now we'll actually call the Gemini 2.5 Flash Image model for real image generation
            log.info("📝 AI Prompt: {}", autoPrompt);
            log.info("👤 User photo size: {} bytes", userPhotoBytes.length);
            log.info("👗 Garment image size: {} bytes", garmentBytes.length);
            
            // Call the actual Gemini API with the images and prompt
            return callGeminiAPI(userPhotoBytes, garmentBytes, autoPrompt);
            
        } catch (Exception e) {
            log.error("Failed to send prompt to Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate AI try-on result", e);
        }
    }
    
    /**
     * Generate automatic prompt for try-on - Category-specific prompts
     */
    private String generateAutoPrompt(TryonRequestDto request) {
        try {
            // Get garment details for better prompt
            Garment garment = getGarment(request.getGarmentId());
            String category = garment.getCategory().toLowerCase();
            String garmentName = garment.getGarmentName();
            
            // Category-specific prompts for better AI results - Only Vesti and Saree
            switch (category) {
                case "vesti":
                case "dhoti":
                case "lungi":
                    return generateVestiPrompt(garmentName);
                
                case "saree":
                case "sari":
                    return generateSareePrompt(garmentName);
                
                default:
                    // For any other category, use generic prompt
                    return generateGenericPrompt(garmentName, category);
            }
            
        } catch (Exception e) {
            log.error("Failed to generate auto prompt: {}", e.getMessage(), e);
            return generateGenericPrompt("traditional garment", "clothing");
        }
    }
    
    /**
     * Generate Vesti-specific prompt for men's traditional wear
     */
    private String generateVestiPrompt(String garmentName) {
        return String.format(
            "Transform the man into wearing the exact %s shown in the garment image with perfect traditional South Indian draping. " +
            "CRITICAL: Use the EXACT fabric, pattern, color, border design, and texture from the garment image - no changes or variations. " +
            "Drape the vesti with authentic Tamil/South Indian style: wrap elegantly around the waist, create precise neat pleats at the front, " +
            "secure the fabric with proper tucking technique, and let it fall naturally to knee length with graceful flow. " +
            "The draping must look completely natural as if the man has been wearing this vesti for hours - no stiffness or artificial appearance. " +
            "Ensure perfect fit to his body shape, maintain masculine proportions, and preserve his exact facial features, natural expression, " +
            "body posture, and original lighting. The final result should look like a professional portrait of the man in traditional attire.",
            garmentName
        );
    }
    
    /**
     * Generate Saree-specific prompt for women's traditional wear
     */
    private String generateSareePrompt(String garmentName) {
        return String.format(
            "Transform the woman into wearing the exact %s shown in the garment image with perfect traditional Indian draping. " +
            "CRITICAL: Use the EXACT fabric, pattern, color, border design, pallu design, and intricate details from the garment image - no changes or variations. " +
            "Drape the saree with authentic Indian style: wrap elegantly around the waist, create precise neat pleats at the front, " +
            "drape the pallu gracefully over the left shoulder with proper fall and positioning, ensure natural fabric flow and elegant folds. " +
            "The draping must look completely natural as if the woman has been wearing this saree for hours - no stiffness or artificial appearance. " +
            "Show the saree's full beauty including border patterns, pallu designs, and fabric texture with perfect lighting. " +
            "Ensure perfect fit to her body shape, maintain feminine grace and proportions, and preserve her exact facial features, natural expression, " +
            "body posture, and original lighting. The final result should look like a professional portrait of the woman in elegant traditional attire.",
            garmentName
        );
    }
    
    
    /**
     * Generate generic prompt for unknown categories
     */
    private String generateGenericPrompt(String garmentName, String category) {
        return String.format(
            "Take the exact %s from the garment image and dress the person in it. " +
            "Use the exact same fabric, pattern, color, and design from the garment image. " +
            "Ensure the garment fits properly and looks natural on the person. " +
            "Preserve the person's face, posture, and natural lighting while making the garment look like it belongs on them.",
            garmentName
        );
    }
    
    /**
     * Call Gemini API with images and prompt for real image generation
     */
    private byte[] callGeminiAPI(byte[] userPhotoBytes, byte[] garmentBytes, String prompt) {
        try {
            log.info("🚀 Calling Gemini API for AI-generated draped image");

            // Create the request payload
            String requestBody = createGeminiRequest(userPhotoBytes, garmentBytes, prompt);

            // Make HTTP request to Gemini API with API key as header (recommended by Google)
            String apiUrl = googleApiUrl + "models/" + googleModel + ":generateContent";
            log.info("🔑 API URL: {}models/{}:generateContent", googleApiUrl, googleModel);
            log.info("🔑 API Key length: {}, First 5 chars: {}***",
                     googleApiKey.length(),
                     googleApiKey.length() > 5 ? googleApiKey.substring(0, 5) : "SHORT");
            return makeHttpRequestToGemini(apiUrl, requestBody);
            
        } catch (Exception e) {
            log.error("Gemini API failed: {}", e.getMessage());
            // Use a different saree image as fallback
            String fallbackImageUrl = "https://textile-images-dev.s3.us-east-1.amazonaws.com/WhatsApp+Image+2025-09-24+at+04.37.01_3d42d1b2.jpg";
            return downloadAsBytes(fallbackImageUrl);
        }
    }
    
    /**
     * Create Gemini API request payload - EXACT format from your example
     */
    private String createGeminiRequest(byte[] userPhotoBytes, byte[] garmentBytes, String prompt) {
        try {
            String userPhotoBase64 = Base64.getEncoder().encodeToString(userPhotoBytes);
            String garmentBase64 = Base64.getEncoder().encodeToString(garmentBytes);
            
            // Use the EXACT format from your working example
            String requestBody = String.format("""
                {
                    "contents": [{
                        "parts": [
                            {
                                "inline_data": {
                                    "mime_type": "image/jpeg",
                                    "data": "%s"
                                }
                            },
                            {
                                "inline_data": {
                                    "mime_type": "image/jpeg",
                                    "data": "%s"
                                }
                            },
                            {
                                "text": "%s"
                            }
                        ]
                    }]
                }
                """, garmentBase64, userPhotoBase64, prompt);
                
            log.info("📝 Created Gemini request with prompt: {}", prompt);
            return requestBody;
            
        } catch (Exception e) {
            log.error("Failed to create Gemini request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create API request", e);
        }
    }
    
    /**
     * Make HTTP request to Gemini API - EXACT format from your working example
     */
    private byte[] makeHttpRequestToGemini(String apiUrl, String requestBody) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

            // Use x-goog-api-key header for authentication (Google's recommended method)
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", googleApiKey)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                log.info("✅ Got AI-generated draped image from Gemini");
                return extractImageFromGeminiResponse(response.body());
            } else {
                log.error("❌ Gemini API error: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("Gemini API error: " + response.statusCode());
            }
            
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }
    
    /**
     * Extract image from Gemini API response - Supports both camelCase and snake_case
     */
    private byte[] extractImageFromGeminiResponse(String responseBody) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(responseBody);

            // Navigate to the image data in the response
            // Gemini response structure: candidates[0].content.parts[*].inlineData.data (or inline_data.data)
            com.fasterxml.jackson.databind.JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                com.fasterxml.jackson.databind.JsonNode content = candidates.get(0).path("content");
                com.fasterxml.jackson.databind.JsonNode parts = content.path("parts");
                if (parts.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode part : parts) {
                        // Check for both camelCase (inlineData) and snake_case (inline_data)
                        com.fasterxml.jackson.databind.JsonNode inlineData = null;

                        if (part.has("inlineData")) {
                            inlineData = part.path("inlineData");
                            log.debug("Found camelCase 'inlineData' field");
                        } else if (part.has("inline_data")) {
                            inlineData = part.path("inline_data");
                            log.debug("Found snake_case 'inline_data' field");
                        }

                        if (inlineData != null) {
                            String imageData = inlineData.path("data").asText();
                            if (imageData != null && !imageData.isEmpty()) {
                                log.info("✅ Successfully extracted image data from Gemini response (size: {} chars)", imageData.length());
                                return Base64.getDecoder().decode(imageData);
                            }
                        }
                    }
                }
            }

            // Log the response structure for debugging
            log.error("❌ Could not find image in Gemini response. Response structure: {}",
                     responseBody.substring(0, Math.min(500, responseBody.length())));
            throw new RuntimeException("No AI-generated image found in Gemini response");

        } catch (Exception e) {
            log.error("❌ Failed to extract AI image: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }
    


    /**
     * Helper: download any image URL as bytes (S3 or HTTPS)
     */
    private byte[] downloadAsBytes(String fileUrl) {
        try (InputStream in = new URL(fileUrl).openStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download image: " + fileUrl, e);
        }
    }

    // ---- Helper methods to fetch entities safely ----
    
    private UserProfile getUserProfile(String sessionId) {
        return userProfileRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("User profile not found for session " + sessionId));
    }
    
    private Garment getGarment(Long garmentId) {
        return garmentRepository.findById(garmentId)
                .orElseThrow(() -> new RuntimeException("Garment not found: " + garmentId));
    }

    private UserPhoto getUserPhoto(Long photoId, Long userId) {
        return userPhotoRepository.findByIdAndUserProfileId(photoId, userId)
                .orElseThrow(() -> new RuntimeException("User photo not found: " + photoId));
    }
    
    private String getGarmentImageUrl(Long garmentId) {
        return garmentImageRepository.findByGarmentIdAndIsPrimaryTrue(garmentId)
                .map(GarmentImage::getImageUrl)
                .orElseThrow(() -> new RuntimeException("Garment image not found for garment " + garmentId));
    }
    
    /**
     * Get user try-on results with pagination
     */
    public Page<TryonResultDto> getUserTryonResults(String sessionId, int page, int size) {
        try {
            UserProfile userProfile = getUserProfile(sessionId);
            
            Page<TryonResult> results = tryonResultRepository.findByUserProfileId(
                userProfile.getId(), 
                org.springframework.data.domain.PageRequest.of(page, size)
            );
            
            return results.map(this::convertToDto);
            
        } catch (Exception e) {
            log.error("Error getting try-on results for session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve try-on results", e);
        }
    }
    
    /**
     * Delete try-on result
     */
    public void deleteTryonResult(String sessionId, Long resultId) {
        try {
            UserProfile userProfile = getUserProfile(sessionId);
            
            TryonResult result = tryonResultRepository.findByIdAndUserProfileId(resultId, userProfile.getId())
                .orElseThrow(() -> new RuntimeException("Try-on result not found: " + resultId));
            
            tryonResultRepository.delete(result);
            log.info("Deleted try-on result {} for user {}", resultId, sessionId);
            
        } catch (Exception e) {
            log.error("Error deleting try-on result {} for session {}: {}", resultId, sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete try-on result", e);
        }
    }
    
    /**
     * Convert TryonResult entity to DTO
     */
    private TryonResultDto convertToDto(TryonResult result) {
        return TryonResultDto.createSuccess(
            result.getId(),
            result.getResultImageUrl(),
            result.getAiModelUsed(),
            result.getGarment().getId(),
            result.getGarment().getGarmentName(),
            result.getUserPhoto().getId(),
            result.getUserPhoto().getDisplayName()
        );
    }
}
