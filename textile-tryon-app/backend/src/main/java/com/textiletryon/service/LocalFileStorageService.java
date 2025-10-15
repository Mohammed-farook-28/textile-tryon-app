package com.textiletryon.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Local file storage service for development
 *
 * Saves files to the local filesystem instead of AWS S3.
 * Useful for local development without AWS credentials.
 */
@Service
@Slf4j
public class LocalFileStorageService {

    @Value("${app.file-storage.local-path:uploads}")
    private String localStoragePath;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.file-upload.allowed-extensions:jpg,jpeg,png,gif,webp}")
    private String allowedExtensions;

    @Value("${app.file-upload.max-file-size:10485760}") // 10MB
    private long maxFileSize;

    private static final String GARMENT_FOLDER = "garments/";
    private static final String USER_PHOTOS_FOLDER = "user-photos/";
    private static final String TRYON_RESULTS_FOLDER = "tryon-results/";

    /**
     * Upload a garment image to local storage
     */
    public String uploadGarmentImage(MultipartFile file, Long garmentId) {
        validateFile(file);
        String folderPath = GARMENT_FOLDER + garmentId + "/";
        return uploadFile(file, folderPath);
    }

    /**
     * Upload a user photo to local storage
     */
    public String uploadUserPhoto(MultipartFile file, Long userProfileId) {
        validateFile(file);
        String folderPath = USER_PHOTOS_FOLDER + userProfileId + "/";
        return uploadFile(file, folderPath);
    }

    /**
     * Upload a try-on result image to local storage
     */
    public String uploadTryonResult(byte[] imageBytes, Long userProfileId, Long garmentId, String contentType) {
        String folderPath = TRYON_RESULTS_FOLDER + userProfileId + "/";
        String fileName = garmentId + "_" + UUID.randomUUID() + getExtensionFromContentType(contentType);

        try {
            Path directoryPath = Paths.get(localStoragePath, folderPath);
            Files.createDirectories(directoryPath);

            Path filePath = directoryPath.resolve(fileName);
            Files.write(filePath, imageBytes);

            String imageUrl = getFileUrl(folderPath + fileName);
            log.info("Try-on result uploaded successfully to local storage: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("Failed to upload try-on result to local storage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload try-on result", e);
        }
    }

    /**
     * Delete a file from local storage
     */
    public void deleteFile(String fileUrl) {
        try {
            String relativePath = extractRelativePathFromUrl(fileUrl);
            if (relativePath == null) {
                log.warn("Invalid file URL format: {}", fileUrl);
                return;
            }

            Path filePath = Paths.get(localStoragePath, relativePath);
            Files.deleteIfExists(filePath);
            log.info("File deleted successfully from local storage: {}", relativePath);

        } catch (IOException e) {
            log.error("Failed to delete file from local storage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from local storage", e);
        }
    }

    /**
     * Delete multiple files from local storage
     */
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        fileUrls.forEach(this::deleteFile);
    }

    /**
     * Check if a file exists in local storage
     */
    public boolean fileExists(String fileUrl) {
        String relativePath = extractRelativePathFromUrl(fileUrl);
        if (relativePath == null) {
            return false;
        }

        Path filePath = Paths.get(localStoragePath, relativePath);
        return Files.exists(filePath);
    }

    /**
     * Get file size from local storage
     */
    public long getFileSize(String fileUrl) {
        try {
            String relativePath = extractRelativePathFromUrl(fileUrl);
            if (relativePath == null) {
                return -1;
            }

            Path filePath = Paths.get(localStoragePath, relativePath);
            if (!Files.exists(filePath)) {
                return -1;
            }

            return Files.size(filePath);

        } catch (IOException e) {
            log.error("Error getting file size from local storage: {}", e.getMessage(), e);
            return -1;
        }
    }

    private String uploadFile(MultipartFile file, String folderPath) {
        try {
            // Create directory if it doesn't exist
            Path directoryPath = Paths.get(localStoragePath, folderPath);
            Files.createDirectories(directoryPath);

            // Generate unique filename
            String fileName = generateFileName(file);
            Path filePath = directoryPath.resolve(fileName);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = getFileUrl(folderPath + fileName);
            log.info("File uploaded successfully to local storage: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("Failed to upload file to local storage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to local storage", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        List<String> allowedExtensionsList = Arrays.asList(allowedExtensions.toLowerCase().split(","));

        if (!allowedExtensionsList.contains(extension)) {
            throw new IllegalArgumentException("File extension not allowed. Allowed extensions: " + allowedExtensions);
        }
    }

    private String generateFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID() + "." + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) return ".jpg";

        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private String getFileUrl(String relativePath) {
        // Return URL that will be served by the application
        return "http://localhost:" + serverPort + "/api/files/" + relativePath;
    }

    private String extractRelativePathFromUrl(String fileUrl) {
        if (fileUrl == null) {
            return null;
        }

        // Extract path after /api/files/
        String prefix = "/api/files/";
        int index = fileUrl.indexOf(prefix);
        if (index == -1) {
            return null;
        }

        return fileUrl.substring(index + prefix.length());
    }
}
