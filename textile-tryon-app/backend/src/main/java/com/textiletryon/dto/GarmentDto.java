package com.textiletryon.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Garment entity
 * 
 * Used for API requests and responses involving garment data.
 * Includes validation annotations for input validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarmentDto {
    
    private Long id;
    
    @NotBlank(message = "Name ID is required")
    @Size(max = 100, message = "Name ID must not exceed 100 characters")
    private String nameId;
    
    @NotBlank(message = "Garment name is required")
    @Size(max = 255, message = "Garment name must not exceed 255 characters")
    private String garmentName;
    
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    @Size(max = 100, message = "Subcategory must not exceed 100 characters")
    private String subcategory;
    
    @NotBlank(message = "Garment type is required")
    @Size(max = 100, message = "Garment type must not exceed 100 characters")
    private String garmentType;
    
    @NotBlank(message = "Color is required")
    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;
    
    @Size(max = 100, message = "Pattern style must not exceed 100 characters")
    private String patternStyle;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 fractional digits")
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Image URLs for the garment
    private List<String> imageUrls;
    
    // Primary image URL
    private String primaryImageUrl;
    
    // Additional computed fields
    private boolean inStock;
    private boolean lowStock;
    private Long favoriteCount;
    private Long tryonCount;
    
    /**
     * Create a basic DTO with essential fields only
     */
    public static GarmentDto createBasic(Long id, String nameId, String garmentName, 
                                        String category, String color, BigDecimal price, 
                                        String primaryImageUrl) {
        return GarmentDto.builder()
                .id(id)
                .nameId(nameId)
                .garmentName(garmentName)
                .category(category)
                .color(color)
                .price(price)
                .primaryImageUrl(primaryImageUrl)
                .build();
    }
}
