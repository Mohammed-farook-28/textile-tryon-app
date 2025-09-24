package com.textiletryon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for AWS S3 operations
 * 
 * Handles file upload, download, and deletion operations with S3.
 * Provides image storage functionality for garments and user photos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.s3.base-url}")
    private String baseUrl;
    
    @Value("${app.file-upload.allowed-extensions:jpg,jpeg,png,gif,webp}")
    private String allowedExtensions;
    
    @Value("${app.file-upload.max-file-size:10485760}") // 10MB
    private long maxFileSize;
    
    private static final String GARMENT_FOLDER = "garments/";
    private static final String USER_PHOTOS_FOLDER = "user-photos/";
    private static final String TRYON_RESULTS_FOLDER = "tryon-results/";
    
    /**
     * Upload a garment image to S3
     * @param file the image file to upload
     * @param garmentId the ID of the garment
     * @return the S3 URL of the uploaded image
     */
    public String uploadGarmentImage(MultipartFile file, Long garmentId) {
        validateFile(file);
        String fileName = generateFileName(file, GARMENT_FOLDER + garmentId + "/");
        return uploadFile(file, fileName);
    }
    
    /**
     * Upload a user photo to S3
     * @param file the image file to upload
     * @param userProfileId the ID of the user profile
     * @return the S3 URL of the uploaded image
     */
    public String uploadUserPhoto(MultipartFile file, Long userProfileId) {
        validateFile(file);
        String fileName = generateFileName(file, USER_PHOTOS_FOLDER + userProfileId + "/");
        return uploadFile(file, fileName);
    }
    
    /**
     * Upload a try-on result image to S3
     * @param imageBytes the image data as byte array
     * @param userProfileId the ID of the user profile
     * @param garmentId the ID of the garment
     * @param contentType the MIME type of the image
     * @return the S3 URL of the uploaded image
     */
    public String uploadTryonResult(byte[] imageBytes, Long userProfileId, Long garmentId, String contentType) {
        String fileName = TRYON_RESULTS_FOLDER + userProfileId + "/" + 
                         garmentId + "_" + UUID.randomUUID() + getExtensionFromContentType(contentType);
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .contentLength((long) imageBytes.length)
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
            
            String imageUrl = baseUrl + "/" + fileName;
            log.info("Try-on result uploaded successfully: {}", imageUrl);
            return imageUrl;
            
        } catch (Exception e) {
            log.error("Failed to upload try-on result to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload try-on result", e);
        }
    }
    
    /**
     * Delete a file from S3
     * @param fileUrl the S3 URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName == null) {
                log.warn("Invalid S3 URL format: {}", fileUrl);
                return;
            }
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", fileName);
            
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }
    
    /**
     * Delete multiple files from S3
     * @param fileUrls list of S3 URLs to delete
     */
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }
        
        try {
            List<ObjectIdentifier> objectsToDelete = fileUrls.stream()
                    .map(this::extractFileNameFromUrl)
                    .filter(fileName -> fileName != null)
                    .map(fileName -> ObjectIdentifier.builder().key(fileName).build())
                    .toList();
            
            if (objectsToDelete.isEmpty()) {
                return;
            }
            
            Delete delete = Delete.builder()
                    .objects(objectsToDelete)
                    .build();
            
            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(delete)
                    .build();
            
            DeleteObjectsResponse response = s3Client.deleteObjects(deleteObjectsRequest);
            log.info("Deleted {} files from S3", response.deleted().size());
            
            if (!response.errors().isEmpty()) {
                log.warn("Some files could not be deleted: {}", response.errors());
            }
            
        } catch (Exception e) {
            log.error("Failed to delete multiple files from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete files from S3", e);
        }
    }
    
    /**
     * Check if a file exists in S3
     * @param fileUrl the S3 URL to check
     * @return true if the file exists
     */
    public boolean fileExists(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName == null) {
                return false;
            }
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence in S3: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get file size from S3
     * @param fileUrl the S3 URL
     * @return file size in bytes, or -1 if not found
     */
    public long getFileSize(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName == null) {
                return -1;
            }
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentLength();
            
        } catch (Exception e) {
            log.error("Error getting file size from S3: {}", e.getMessage(), e);
            return -1;
        }
    }
    
    private String uploadFile(MultipartFile file, String fileName) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            String imageUrl = baseUrl + "/" + fileName;
            log.info("File uploaded successfully to S3: {}", imageUrl);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("Failed to read file content: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file content", e);
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3", e);
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
    
    private String generateFileName(MultipartFile file, String folder) {
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        return folder + UUID.randomUUID() + "." + extension;
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
    
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            return null;
        }
        
        return fileUrl.substring(baseUrl.length() + 1); // +1 for the slash
    }
}
