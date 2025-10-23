package com.textiletryon.controller;

import com.textiletryon.dto.ApiResponse;
import com.textiletryon.dto.GarmentDto;
import com.textiletryon.service.GarmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * Admin endpoints for managing garments (create with images, etc.).
 * Mapped under /api/admin/garments via the global context-path /api.
 */
@RestController
@RequestMapping("/admin/garments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AdminGarmentController {

    private final GarmentService garmentService;

    /**
     * Create a new garment with images (multipart form as used by Admin UI)
     * POST /api/admin/garments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GarmentDto>> createGarment(
            @RequestParam("garmentName") String garmentName,
            @RequestParam("category") String category,
            @RequestParam("color") String color,
            @RequestParam("price") String price,
            @RequestParam("stockQuantity") String stockQuantity,
            @RequestParam(value = "patternStyle", required = false) String patternStyle,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "primaryImageIndex", defaultValue = "0") Integer primaryImageIndex
    ) {
        try {
            if (images == null || images.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("At least one image is required", "VALIDATION_ERROR"));
            }

            GarmentDto garmentDto = GarmentDto.builder()
                    .garmentName(garmentName)
                    .category(category)
                    .garmentType("Traditional")
                    .color(color)
                    .price(new BigDecimal(price))
                    .stockQuantity(Integer.parseInt(stockQuantity))
                    .patternStyle(patternStyle != null ? patternStyle : "")
                    .build();

            GarmentDto created = garmentService.createGarmentWithImages(garmentDto, images, primaryImageIndex);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "Garment created successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            log.error("Error creating garment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create garment", "SERVER_ERROR"));
        }
    }
}


