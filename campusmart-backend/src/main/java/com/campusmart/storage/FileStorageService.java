package com.campusmart.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile store(MultipartFile file, String subdirectory);

    void delete(String storageKey);
}
