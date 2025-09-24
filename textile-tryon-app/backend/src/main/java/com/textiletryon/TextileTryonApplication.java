package com.textiletryon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Textile Try-On Application
 * 
 * This Spring Boot application provides a comprehensive platform for:
 * - Virtual try-on capabilities using AI models
 * - Garment management and browsing
 * - User session management
 * - AWS S3 integration for image storage
 * - RESTful API endpoints for frontend integration
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties
public class TextileTryonApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextileTryonApplication.class, args);
    }
}
