package com.textiletryon.repository;

import com.textiletryon.model.Garment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Garment entity
 * 
 * Provides CRUD operations and custom queries for garment management:
 * - Basic CRUD operations via JpaRepository
 * - Complex filtering via JpaSpecificationExecutor
 * - Custom queries for search and filtering
 * - Statistics and analytics queries
 */
@Repository
public interface GarmentRepository extends JpaRepository<Garment, Long>, JpaSpecificationExecutor<Garment> {
    
    /**
     * Find garment by unique name ID
     * @param nameId the unique name identifier
     * @return Optional containing the garment if found
     */
    Optional<Garment> findByNameId(String nameId);
    
    /**
     * Check if a garment with the given name ID exists
     * @param nameId the unique name identifier
     * @return true if exists, false otherwise
     */
    boolean existsByNameId(String nameId);
    
    /**
     * Find garments by category with pagination
     * @param category the garment category
     * @param pageable pagination information
     * @return page of garments in the specified category
     */
    Page<Garment> findByCategory(String category, Pageable pageable);
    
    /**
     * Find garments by category and subcategory with pagination
     * @param category the garment category
     * @param subcategory the garment subcategory
     * @param pageable pagination information
     * @return page of garments matching the criteria
     */
    Page<Garment> findByCategoryAndSubcategory(String category, String subcategory, Pageable pageable);
    
    /**
     * Find garments by color with pagination
     * @param color the garment color
     * @param pageable pagination information
     * @return page of garments with the specified color
     */
    Page<Garment> findByColor(String color, Pageable pageable);
    
    /**
     * Find garments by garment type with pagination
     * @param garmentType the type of garment
     * @param pageable pagination information
     * @return page of garments of the specified type
     */
    Page<Garment> findByGarmentType(String garmentType, Pageable pageable);
    
    /**
     * Find garments within a price range with pagination
     * @param minPrice minimum price (inclusive)
     * @param maxPrice maximum price (inclusive)
     * @param pageable pagination information
     * @return page of garments within the price range
     */
    Page<Garment> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * Find garments that are in stock with pagination
     * @param pageable pagination information
     * @return page of garments with stock quantity > 0
     */
    Page<Garment> findByStockQuantityGreaterThan(Integer minStock, Pageable pageable);
    
    /**
     * Search garments by name containing the search term (case-insensitive)
     * @param searchTerm the term to search for in garment names
     * @param pageable pagination information
     * @return page of garments with names containing the search term
     */
    Page<Garment> findByGarmentNameContainingIgnoreCase(String searchTerm, Pageable pageable);
    
    /**
     * Advanced search across multiple fields
     * @param searchTerm the term to search for
     * @param pageable pagination information
     * @return page of garments matching the search term in various fields
     */
    @Query("SELECT g FROM Garment g WHERE " +
           "LOWER(g.garmentName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(g.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(g.subcategory) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(g.color) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(g.patternStyle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Garment> searchGarments(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find garments with complex filtering
     * @param categories list of categories to filter by
     * @param colors list of colors to filter by
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return page of garments matching all filters
     */
    @Query("SELECT g FROM Garment g WHERE " +
           "(:categories IS NULL OR g.category IN :categories) AND " +
           "(:colors IS NULL OR g.color IN :colors) AND " +
           "g.price >= :minPrice AND g.price <= :maxPrice AND " +
           "g.stockQuantity > 0")
    Page<Garment> findWithFilters(
            @Param("categories") List<String> categories,
            @Param("colors") List<String> colors,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
    
    /**
     * Get distinct categories
     * @return list of all distinct categories
     */
    @Query("SELECT DISTINCT g.category FROM Garment g ORDER BY g.category")
    List<String> findDistinctCategories();
    
    /**
     * Get distinct subcategories for a given category
     * @param category the category to get subcategories for
     * @return list of distinct subcategories
     */
    @Query("SELECT DISTINCT g.subcategory FROM Garment g WHERE g.category = :category AND g.subcategory IS NOT NULL ORDER BY g.subcategory")
    List<String> findDistinctSubcategoriesByCategory(@Param("category") String category);
    
    /**
     * Get distinct colors
     * @return list of all distinct colors
     */
    @Query("SELECT DISTINCT g.color FROM Garment g ORDER BY g.color")
    List<String> findDistinctColors();
    
    /**
     * Get distinct garment types
     * @return list of all distinct garment types
     */
    @Query("SELECT DISTINCT g.garmentType FROM Garment g ORDER BY g.garmentType")
    List<String> findDistinctGarmentTypes();
    
    /**
     * Get distinct pattern styles
     * @return list of all distinct pattern styles
     */
    @Query("SELECT DISTINCT g.patternStyle FROM Garment g WHERE g.patternStyle IS NOT NULL ORDER BY g.patternStyle")
    List<String> findDistinctPatternStyles();
    
    /**
     * Get price range (min and max prices)
     * @return array with [minPrice, maxPrice]
     */
    @Query("SELECT MIN(g.price), MAX(g.price) FROM Garment g")
    Object[] findPriceRange();
    
    /**
     * Count garments by category
     * @return list of category counts
     */
    @Query("SELECT g.category, COUNT(g) FROM Garment g GROUP BY g.category ORDER BY COUNT(g) DESC")
    List<Object[]> countGarmentsByCategory();
    
    /**
     * Count garments that are low in stock (< 5 items)
     * @return count of low stock garments
     */
    @Query("SELECT COUNT(g) FROM Garment g WHERE g.stockQuantity < 5 AND g.stockQuantity > 0")
    Long countLowStockGarments();
    
    /**
     * Count garments that are out of stock
     * @return count of out of stock garments
     */
    @Query("SELECT COUNT(g) FROM Garment g WHERE g.stockQuantity = 0")
    Long countOutOfStockGarments();
    
    /**
     * Get top selling garments based on try-on results
     * @param limit maximum number of results
     * @return list of popular garments
     */
    @Query("SELECT g FROM Garment g LEFT JOIN g.tryonResults tr GROUP BY g ORDER BY COUNT(tr) DESC")
    List<Garment> findTopPopularGarments(Pageable pageable);
    
    /**
     * Find recently added garments
     * @param days number of days to look back
     * @param pageable pagination information
     * @return page of recently added garments
     */
    @Query("SELECT g FROM Garment g WHERE g.createdAt >= CURRENT_TIMESTAMP - :days DAY ORDER BY g.createdAt DESC")
    Page<Garment> findRecentlyAdded(@Param("days") int days, Pageable pageable);
}
