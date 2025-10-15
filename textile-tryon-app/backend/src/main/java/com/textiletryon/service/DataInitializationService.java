package com.textiletryon.service;

import com.textiletryon.model.Garment;
import com.textiletryon.model.GarmentImage;
import com.textiletryon.repository.GarmentRepository;
import com.textiletryon.repository.GarmentImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Service to initialize sample data for development
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    private final GarmentRepository garmentRepository;
    private final GarmentImageRepository garmentImageRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no garments exist
        if (garmentRepository.count() == 0) {
            log.info("Initializing sample data...");
            initializeSampleData();
            log.info("Sample data initialization completed");
        } else {
            log.info("Sample data already exists, skipping initialization");
        }
    }

    private void initializeSampleData() {
        // Create sample garments - 6 Sarees as requested
        List<Garment> garments = Arrays.asList(
            Garment.builder()
                .nameId("SAR001")
                .garmentName("Elegant Silk Saree")
                .category("Saree")
                .subcategory("Silk")
                .garmentType("Traditional")
                .color("Red")
                .patternStyle("Embroidered")
                .price(new BigDecimal("2500.00"))
                .stockQuantity(10)
                .build(),
            
            Garment.builder()
                .nameId("SAR002")
                .garmentName("Cotton Casual Saree")
                .category("Saree")
                .subcategory("Cotton")
                .garmentType("Casual")
                .color("Blue")
                .patternStyle("Printed")
                .price(new BigDecimal("1200.00"))
                .stockQuantity(15)
                .build(),
            
            Garment.builder()
                .nameId("SAR003")
                .garmentName("Designer Saree Collection 1")
                .category("Saree")
                .subcategory("Designer")
                .garmentType("Traditional")
                .color("Multi")
                .patternStyle("Embroidered")
                .price(new BigDecimal("3200.00"))
                .stockQuantity(8)
                .build(),
            
            Garment.builder()
                .nameId("SAR004")
                .garmentName("Designer Saree Collection 2")
                .category("Saree")
                .subcategory("Designer")
                .garmentType("Traditional")
                .color("Multi")
                .patternStyle("Embroidered")
                .price(new BigDecimal("3200.00"))
                .stockQuantity(8)
                .build(),
            
            Garment.builder()
                .nameId("SAR005")
                .garmentName("Designer Saree Collection 3")
                .category("Saree")
                .subcategory("Designer")
                .garmentType("Traditional")
                .color("Multi")
                .patternStyle("Embroidered")
                .price(new BigDecimal("3200.00"))
                .stockQuantity(8)
                .build(),
            
            Garment.builder()
                .nameId("SAR006")
                .garmentName("Designer Saree Collection 4")
                .category("Saree")
                .subcategory("Designer")
                .garmentType("Traditional")
                .color("Multi")
                .patternStyle("Embroidered")
                .price(new BigDecimal("3200.00"))
                .stockQuantity(8)
                .build()
        );

        // Save garments
        List<Garment> savedGarments = garmentRepository.saveAll(garments);
        log.info("Created {} sample garments", savedGarments.size());

        // Create sample garment images using the provided S3 URLs
        List<GarmentImage> images = Arrays.asList(
            // SAR001 images
            GarmentImage.builder()
                .garment(savedGarments.get(0))
                .imageUrl("https://textile-images-dev.s3.us-east-1.amazonaws.com/WhatsApp+Image+2025-09-24+at+04.37.00_6329e5b7.jpg")
                .isPrimary(true)
                .displayOrder(1)
                .build(),
            
            // SAR002 images
            GarmentImage.builder()
                .garment(savedGarments.get(1))
                .imageUrl("https://textile-images-dev.s3.us-east-1.amazonaws.com/WhatsApp+Image+2025-09-24+at+04.37.01_3d42d1b2.jpg")
                .isPrimary(true)
                .displayOrder(1)
                .build(),
            
            // SAR003 images
            GarmentImage.builder()
                .garment(savedGarments.get(2))
                .imageUrl("https://textile-images-dev.s3.us-east-1.amazonaws.com/WhatsApp+Image+2025-09-24+at+04.37.01_ab6d98d0.jpg")
                .isPrimary(true)
                .displayOrder(1)
                .build(),
            
            // SAR004 images
            GarmentImage.builder()
                .garment(savedGarments.get(3))
                .imageUrl("https://textile-images-dev.s3.us-east-1.amazonaws.com/WhatsApp+Image+2025-09-24+at+04.37.02_6bdfba38.jpg")
                .isPrimary(true)
                .displayOrder(1)
                .build(),
            
            // SAR005 images
            GarmentImage.builder()
                .garment(savedGarments.get(4))
                .imageUrl("https://textile-images-dev.s3.us-east-1.amazonaws.com/WhatsApp+Image+2025-09-24+at+04.37.02_cb1c6060.jpg")
                .isPrimary(true)
                .displayOrder(1)
                .build(),
            
            // SAR006 images - using the first image as primary for the last saree
            GarmentImage.builder()
                .garment(savedGarments.get(5))
                .imageUrl("https://textile-images-dev.s3.us-east-1.amazonaws.com/WhatsApp+Image+2025-09-24+at+04.37.00_6329e5b7.jpg")
                .isPrimary(true)
                .displayOrder(1)
                .build()
        );

        // Save images
        garmentImageRepository.saveAll(images);
        log.info("Created {} sample garment images", images.size());
    }
}
