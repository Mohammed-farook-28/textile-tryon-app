package com.textiletryon.service;

import com.textiletryon.dto.GarmentDto;
import com.textiletryon.dto.GarmentFilterDto;
import com.textiletryon.model.Garment;
import com.textiletryon.model.GarmentImage;
import com.textiletryon.repository.GarmentRepository;
import com.textiletryon.repository.GarmentImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for garment management operations
 * 
 * Handles business logic for garments including:
 * - CRUD operations
 * - Search and filtering
 * - Image management
 * - Statistics and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GarmentService {
    
    private final GarmentRepository garmentRepository;
    private final GarmentImageRepository garmentImageRepository;
    private final S3Service s3Service;
    
    /**
     * Create a new garment
     */
    @Transactional
    public GarmentDto createGarment(GarmentDto garmentDto) {
        // Check if nameId already exists
        if (garmentRepository.existsByNameId(garmentDto.getNameId())) {
            throw new IllegalArgumentException("Garment with nameId " + garmentDto.getNameId() + " already exists");
        }
        
        Garment garment = convertToEntity(garmentDto);
        garment = garmentRepository.save(garment);
        
        log.info("Created garment with ID: {} and nameId: {}", garment.getId(), garment.getNameId());
        return convertToDto(garment);
    }
    
    /**
     * Update an existing garment
     */
    @Transactional
    public GarmentDto updateGarment(Long id, GarmentDto garmentDto) {
        Garment existingGarment = garmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Garment not found with ID: " + id));
        
        // Check if nameId is being changed and if it conflicts
        if (!existingGarment.getNameId().equals(garmentDto.getNameId()) && 
            garmentRepository.existsByNameId(garmentDto.getNameId())) {
            throw new IllegalArgumentException("Garment with nameId " + garmentDto.getNameId() + " already exists");
        }
        
        updateGarmentFields(existingGarment, garmentDto);
        existingGarment = garmentRepository.save(existingGarment);
        
        log.info("Updated garment with ID: {}", id);
        return convertToDto(existingGarment);
    }
    
    /**
     * Get garment by ID
     */
    public GarmentDto getGarmentById(Long id) {
        Garment garment = garmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Garment not found with ID: " + id));
        return convertToDto(garment);
    }
    
    /**
     * Get garment by nameId
     */
    public GarmentDto getGarmentByNameId(String nameId) {
        Garment garment = garmentRepository.findByNameId(nameId)
                .orElseThrow(() -> new IllegalArgumentException("Garment not found with nameId: " + nameId));
        return convertToDto(garment);
    }
    
    /**
     * Delete garment by ID
     */
    @Transactional
    public void deleteGarment(Long id) {
        Garment garment = garmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Garment not found with ID: " + id));
        
        // Delete associated images from S3
        List<String> imageUrls = garmentImageRepository.findImageUrlsByGarmentId(id);
        if (!imageUrls.isEmpty()) {
            s3Service.deleteFiles(imageUrls);
        }
        
        garmentRepository.delete(garment);
        log.info("Deleted garment with ID: {} and associated images", id);
    }
    
    /**
     * Search and filter garments
     */
    public Page<GarmentDto> searchGarments(GarmentFilterDto filter) {
        Pageable pageable = createPageable(filter);
        Page<Garment> garments;
        
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().trim().isEmpty()) {
            garments = garmentRepository.searchGarments(filter.getSearchTerm().trim(), pageable);
        } else if (hasFilters(filter)) {
            garments = garmentRepository.findWithFilters(
                    filter.getCategories(),
                    filter.getColors(),
                    filter.getMinPrice() != null ? filter.getMinPrice() : BigDecimal.ZERO,
                    filter.getMaxPrice() != null ? filter.getMaxPrice() : new BigDecimal("999999.99"),
                    pageable
            );
        } else {
            garments = garmentRepository.findAll(pageable);
        }
        
        return garments.map(this::convertToDto);
    }
    
    /**
     * Get all categories
     */
    public List<String> getAllCategories() {
        return garmentRepository.findDistinctCategories();
    }
    
    /**
     * Get subcategories for a category
     */
    public List<String> getSubcategories(String category) {
        return garmentRepository.findDistinctSubcategoriesByCategory(category);
    }
    
    /**
     * Get all colors
     */
    public List<String> getAllColors() {
        return garmentRepository.findDistinctColors();
    }
    
    /**
     * Get all garment types
     */
    public List<String> getAllGarmentTypes() {
        return garmentRepository.findDistinctGarmentTypes();
    }
    
    /**
     * Get price range
     */
    public BigDecimal[] getPriceRange() {
        Object[] range = garmentRepository.findPriceRange();
        return new BigDecimal[]{(BigDecimal) range[0], (BigDecimal) range[1]};
    }
    
    /**
     * Add image to garment
     */
    @Transactional
    public void addGarmentImage(Long garmentId, MultipartFile imageFile, boolean isPrimary) {
        Garment garment = garmentRepository.findById(garmentId)
                .orElseThrow(() -> new IllegalArgumentException("Garment not found with ID: " + garmentId));
        
        String imageUrl = s3Service.uploadGarmentImage(imageFile, garmentId);
        
        // If this is set as primary, clear other primary images
        if (isPrimary) {
            garmentImageRepository.clearPrimaryImageForGarment(garmentId);
        }
        
        // Get next display order
        Integer maxOrder = garmentImageRepository.findMaxDisplayOrderByGarmentId(garmentId);
        int displayOrder = maxOrder != null ? maxOrder + 1 : 1;
        
        GarmentImage garmentImage = GarmentImage.builder()
                .garment(garment)
                .imageUrl(imageUrl)
                .isPrimary(isPrimary)
                .displayOrder(displayOrder)
                .build();
        
        garmentImageRepository.save(garmentImage);
        log.info("Added image to garment {}: {}", garmentId, imageUrl);
    }
    
    /**
     * Remove image from garment
     */
    @Transactional
    public void removeGarmentImage(Long garmentId, Long imageId) {
        GarmentImage image = garmentImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + imageId));
        
        if (!image.getGarment().getId().equals(garmentId)) {
            throw new IllegalArgumentException("Image does not belong to the specified garment");
        }
        
        s3Service.deleteFile(image.getImageUrl());
        garmentImageRepository.delete(image);
        
        log.info("Removed image {} from garment {}", imageId, garmentId);
    }
    
    private Garment convertToEntity(GarmentDto dto) {
        return Garment.builder()
                .nameId(dto.getNameId())
                .garmentName(dto.getGarmentName())
                .category(dto.getCategory())
                .subcategory(dto.getSubcategory())
                .garmentType(dto.getGarmentType())
                .color(dto.getColor())
                .patternStyle(dto.getPatternStyle())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0)
                .build();
    }
    
    private GarmentDto convertToDto(Garment garment) {
        // Get primary image
        Optional<GarmentImage> primaryImage = garmentImageRepository.findByGarmentIdAndIsPrimaryTrue(garment.getId());
        
        // Get all image URLs
        List<String> imageUrls = garmentImageRepository.findImageUrlsByGarmentId(garment.getId());
        
        return GarmentDto.builder()
                .id(garment.getId())
                .nameId(garment.getNameId())
                .garmentName(garment.getGarmentName())
                .category(garment.getCategory())
                .subcategory(garment.getSubcategory())
                .garmentType(garment.getGarmentType())
                .color(garment.getColor())
                .patternStyle(garment.getPatternStyle())
                .price(garment.getPrice())
                .stockQuantity(garment.getStockQuantity())
                .createdAt(garment.getCreatedAt())
                .updatedAt(garment.getUpdatedAt())
                .primaryImageUrl(primaryImage.map(GarmentImage::getImageUrl).orElse(null))
                .imageUrls(imageUrls)
                .inStock(garment.isInStock())
                .lowStock(garment.isLowStock())
                .build();
    }
    
    private void updateGarmentFields(Garment garment, GarmentDto dto) {
        garment.setNameId(dto.getNameId());
        garment.setGarmentName(dto.getGarmentName());
        garment.setCategory(dto.getCategory());
        garment.setSubcategory(dto.getSubcategory());
        garment.setGarmentType(dto.getGarmentType());
        garment.setColor(dto.getColor());
        garment.setPatternStyle(dto.getPatternStyle());
        garment.setPrice(dto.getPrice());
        garment.setStockQuantity(dto.getStockQuantity());
    }
    
    private Pageable createPageable(GarmentFilterDto filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        
        Sort sort = Sort.by("createdAt").descending(); // Default sort
        
        if (filter.getSortBy() != null) {
            sort = switch (filter.getSortBy()) {
                case "price_asc" -> Sort.by("price").ascending();
                case "price_desc" -> Sort.by("price").descending();
                case "name_asc" -> Sort.by("garmentName").ascending();
                case "name_desc" -> Sort.by("garmentName").descending();
                case "newest" -> Sort.by("createdAt").descending();
                case "oldest" -> Sort.by("createdAt").ascending();
                default -> Sort.by("createdAt").descending();
            };
        }
        
        return PageRequest.of(page, size, sort);
    }
    
    private boolean hasFilters(GarmentFilterDto filter) {
        return (filter.getCategories() != null && !filter.getCategories().isEmpty()) ||
               (filter.getColors() != null && !filter.getColors().isEmpty()) ||
               filter.getMinPrice() != null ||
               filter.getMaxPrice() != null;
    }
}
