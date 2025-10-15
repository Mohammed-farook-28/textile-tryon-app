package com.textiletryon.controller;

import com.textiletryon.dto.ApiResponse;
import com.textiletryon.dto.GarmentDto;
import com.textiletryon.dto.GarmentFilterDto;
import com.textiletryon.service.GarmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for garment operations
 * 
 * Provides endpoints for:
 * - Browsing and searching garments
 * - Getting garment details
 * - Managing garment images
 * - Getting filter options
 */
@RestController
@RequestMapping("/garments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class GarmentController {
    
    private final GarmentService garmentService;
    
    /**
     * Search and filter garments
     * GET /api/garments?category=Saree&color=Red&minPrice=100&maxPrice=5000&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GarmentDto>>> searchGarments(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            log.debug("Search parameters - searchTerm: {}, categories: {}, colors: {}, minPrice: {}, maxPrice: {}", 
                     searchTerm, categories, colors, minPrice, maxPrice);
            
            GarmentFilterDto filter = GarmentFilterDto.builder()
                    .searchTerm(searchTerm)
                    .categories(categories)
                    .colors(colors)
                    .minPrice(minPrice != null ? new java.math.BigDecimal(minPrice) : null)
                    .maxPrice(maxPrice != null ? new java.math.BigDecimal(maxPrice) : null)
                    .sortBy(sortBy)
                    .page(page)
                    .size(size)
                    .build();
            
            Page<GarmentDto> garments = garmentService.searchGarments(filter);
            
            return ResponseEntity.ok(ApiResponse.success(garments, "Garments retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error searching garments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error searching garments: " + e.getMessage()));
        }
    }
    
    /**
     * Get garment by ID
     * GET /api/garments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GarmentDto>> getGarmentById(@PathVariable Long id) {
        try {
            GarmentDto garment = garmentService.getGarmentById(id);
            return ResponseEntity.ok(ApiResponse.success(garment, "Garment retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Garment not found", "GARMENT_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error getting garment by ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving garment: " + e.getMessage()));
        }
    }
    
    /**
     * Get garment by name ID
     * GET /api/garments/name/{nameId}
     */
    @GetMapping("/name/{nameId}")
    public ResponseEntity<ApiResponse<GarmentDto>> getGarmentByNameId(@PathVariable String nameId) {
        try {
            GarmentDto garment = garmentService.getGarmentByNameId(nameId);
            return ResponseEntity.ok(ApiResponse.success(garment, "Garment retrieved successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Garment not found", "GARMENT_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error getting garment by nameId {}: {}", nameId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving garment: " + e.getMessage()));
        }
    }
    
    /**
     * Get all categories
     * GET /api/garments/filters/categories
     */
    @GetMapping("/filters/categories")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        try {
            List<String> categories = garmentService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error getting categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving categories: " + e.getMessage()));
        }
    }
    
    /**
     * Get subcategories for a category
     * GET /api/garments/filters/subcategories?category=Saree
     */
    @GetMapping("/filters/subcategories")
    public ResponseEntity<ApiResponse<List<String>>> getSubcategories(@RequestParam String category) {
        try {
            List<String> subcategories = garmentService.getSubcategories(category);
            return ResponseEntity.ok(ApiResponse.success(subcategories, "Subcategories retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error getting subcategories for category {}: {}", category, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving subcategories: " + e.getMessage()));
        }
    }
    
    /**
     * Get all colors
     * GET /api/garments/filters/colors
     */
    @GetMapping("/filters/colors")
    public ResponseEntity<ApiResponse<List<String>>> getAllColors() {
        try {
            List<String> colors = garmentService.getAllColors();
            return ResponseEntity.ok(ApiResponse.success(colors, "Colors retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error getting colors: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving colors: " + e.getMessage()));
        }
    }
    
    /**
     * Get all garment types
     * GET /api/garments/filters/types
     */
    @GetMapping("/filters/types")
    public ResponseEntity<ApiResponse<List<String>>> getAllGarmentTypes() {
        try {
            List<String> types = garmentService.getAllGarmentTypes();
            return ResponseEntity.ok(ApiResponse.success(types, "Garment types retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error getting garment types: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving garment types: " + e.getMessage()));
        }
    }
    
    /**
     * Get price range
     * GET /api/garments/filters/price-range
     */
    @GetMapping("/filters/price-range")
    public ResponseEntity<ApiResponse<com.textiletryon.dto.PriceRangeDto>> getPriceRange() {
        try {
            com.textiletryon.dto.PriceRangeDto priceRange = garmentService.getPriceRange();
            return ResponseEntity.ok(ApiResponse.success(priceRange, "Price range retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error getting price range: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving price range: " + e.getMessage()));
        }
    }
}
