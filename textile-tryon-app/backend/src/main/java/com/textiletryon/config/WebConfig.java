package com.textiletryon.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 *
 * Configures resource handlers for serving local files.
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file-storage.local-path:uploads}")
    private String localStoragePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convert to absolute path
        String absolutePath = new java.io.File(localStoragePath).getAbsolutePath();
        String resourceLocation = "file:" + absolutePath + "/";

        log.info("Configuring resource handler for /files/** -> {}", resourceLocation);

        // Add custom resource handler for local file storage
        // Note: Pattern is /files/** (not /api/files/**) because context path /api is stripped by Spring
        registry.addResourceHandler("/files/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600)
                .resourceChain(true);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configuring CORS for /files/** endpoint");

        // Add CORS configuration for file serving endpoints
        registry.addMapping("/files/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
