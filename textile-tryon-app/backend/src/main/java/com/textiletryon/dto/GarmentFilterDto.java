package com.textiletryon.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for garment filtering criteria
 * 
 * Used for filtering garments in search and browse operations.
 * Supports multiple filter types and combinations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarmentFilterDto {
    
    // Text search
    private String searchTerm;
    
    // Category filters
    private List<String> categories;
    private List<String> subcategories;
    private List<String> garmentTypes;
    
    // Style filters
    private List<String> colors;
    private List<String> patternStyles;
    
    // Price range
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Stock filters
    private Boolean inStockOnly;
    private Boolean lowStockOnly;
    
    // Sorting options
    private String sortBy; // price_asc, price_desc, newest, oldest, name_asc, name_desc, popular
    
    // Pagination
    private Integer page;
    private Integer size;
    
    /**
     * Create a simple search filter
     */
    public static GarmentFilterDto createSearch(String searchTerm) {
        return GarmentFilterDto.builder()
                .searchTerm(searchTerm)
                .build();
    }
    
    /**
     * Create a category filter
     */
    public static GarmentFilterDto createCategoryFilter(List<String> categories) {
        return GarmentFilterDto.builder()
                .categories(categories)
                .build();
    }
    
    /**
     * Create a price range filter
     */
    public static GarmentFilterDto createPriceFilter(BigDecimal minPrice, BigDecimal maxPrice) {
        return GarmentFilterDto.builder()
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
    }
}
