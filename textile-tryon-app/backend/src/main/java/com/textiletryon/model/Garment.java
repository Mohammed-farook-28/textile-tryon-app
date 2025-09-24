package com.textiletryon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Garment entity representing clothing items in the textile shop
 * 
 * This entity stores all garment information including:
 * - Basic details (name, category, type, color)
 * - Pricing and inventory information
 * - Pattern and style attributes
 * - Timestamps for creation and updates
 */
@Entity
@Table(name = "garments", indexes = {
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_color", columnList = "color"),
    @Index(name = "idx_garment_type", columnList = "garmentType"),
    @Index(name = "idx_price", columnList = "price"),
    @Index(name = "idx_name_id", columnList = "nameId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Garment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name_id", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Name ID is required")
    @Size(max = 100, message = "Name ID must not exceed 100 characters")
    private String nameId;
    
    @Column(name = "garment_name", nullable = false)
    @NotBlank(message = "Garment name is required")
    @Size(max = 255, message = "Garment name must not exceed 255 characters")
    private String garmentName;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    @Column(length = 100)
    @Size(max = 100, message = "Subcategory must not exceed 100 characters")
    private String subcategory;
    
    @Column(name = "garment_type", nullable = false, length = 100)
    @NotBlank(message = "Garment type is required")
    @Size(max = 100, message = "Garment type must not exceed 100 characters")
    private String garmentType;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Color is required")
    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;
    
    @Column(name = "pattern_style", length = 100)
    @Size(max = 100, message = "Pattern style must not exceed 100 characters")
    private String patternStyle;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 fractional digits")
    private BigDecimal price;
    
    @Column(name = "stock_quantity")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Builder.Default
    private Integer stockQuantity = 0;
    
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // One-to-many relationship with GarmentImage
    @OneToMany(mappedBy = "garment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GarmentImage> images;
    
    // One-to-many relationship with Favorites
    @OneToMany(mappedBy = "garment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Favorite> favorites;
    
    // One-to-many relationship with TryonResult
    @OneToMany(mappedBy = "garment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TryonResult> tryonResults;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if the garment is in stock
     * @return true if stock quantity > 0
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
    
    /**
     * Check if the garment is low in stock (less than 5 items)
     * @return true if stock quantity < 5
     */
    public boolean isLowStock() {
        return stockQuantity != null && stockQuantity < 5 && stockQuantity > 0;
    }
}
