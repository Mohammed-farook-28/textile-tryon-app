package com.textiletryon.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * File Storage Service that delegates to either S3 or Local storage
 *
 * This service automatically chooses between AWS S3 and local file storage
 * based on configuration. For local development without AWS credentials,
 * it uses local file storage.
 */
@Service
@Slf4j
public class FileStorageService {

    private final LocalFileStorageService localFileStorageService;
    private final S3Service s3Service;

    @Value("${app.file-storage.use-local:true}")
    private boolean useLocalStorage;

    @Autowired
    public FileStorageService(LocalFileStorageService localFileStorageService,
                              @Autowired(required = false) S3Service s3Service) {
        this.localFileStorageService = localFileStorageService;
        this.s3Service = s3Service;
    }

    /**
     * Upload a garment image
     */
    public String uploadGarmentImage(MultipartFile file, Long garmentId) {
        if (useLocalStorage || s3Service == null) {
            log.debug("Using local file storage for garment image");
            return localFileStorageService.uploadGarmentImage(file, garmentId);
        } else {
            log.debug("Using S3 for garment image");
            return s3Service.uploadGarmentImage(file, garmentId);
        }
    }

    /**
     * Upload a user photo
     */
    public String uploadUserPhoto(MultipartFile file, Long userProfileId) {
        if (useLocalStorage || s3Service == null) {
            log.debug("Using local file storage for user photo");
            return localFileStorageService.uploadUserPhoto(file, userProfileId);
        } else {
            log.debug("Using S3 for user photo");
            return s3Service.uploadUserPhoto(file, userProfileId);
        }
    }

    /**
     * Upload a try-on result image
     */
    public String uploadTryonResult(byte[] imageBytes, Long userProfileId, Long garmentId, String contentType) {
        if (useLocalStorage || s3Service == null) {
            log.debug("Using local file storage for try-on result");
            return localFileStorageService.uploadTryonResult(imageBytes, userProfileId, garmentId, contentType);
        } else {
            log.debug("Using S3 for try-on result");
            return s3Service.uploadTryonResult(imageBytes, userProfileId, garmentId, contentType);
        }
    }

    /**
     * Delete a file
     */
    public void deleteFile(String fileUrl) {
        if (useLocalStorage || s3Service == null) {
            localFileStorageService.deleteFile(fileUrl);
        } else {
            s3Service.deleteFile(fileUrl);
        }
    }

    /**
     * Delete multiple files
     */
    public void deleteFiles(List<String> fileUrls) {
        if (useLocalStorage || s3Service == null) {
            localFileStorageService.deleteFiles(fileUrls);
        } else {
            s3Service.deleteFiles(fileUrls);
        }
    }

    /**
     * Check if a file exists
     */
    public boolean fileExists(String fileUrl) {
        if (useLocalStorage || s3Service == null) {
            return localFileStorageService.fileExists(fileUrl);
        } else {
            return s3Service.fileExists(fileUrl);
        }
    }

    /**
     * Get file size
     */
    public long getFileSize(String fileUrl) {
        if (useLocalStorage || s3Service == null) {
            return localFileStorageService.getFileSize(fileUrl);
        } else {
            return s3Service.getFileSize(fileUrl);
        }
    }
}
