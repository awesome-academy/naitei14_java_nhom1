package org.example.foodanddrinkproject.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Stores a file and returns the relative URL path
     * @param file the file to store
     * @param subDirectory subdirectory under uploads (e.g., "products")
     * @return relative URL path (e.g., "/uploads/products/filename.jpg")
     */
    String storeFile(MultipartFile file, String subDirectory);
}
