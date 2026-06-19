package com.campusmart.storage;

import com.campusmart.config.FileStorageProperties;
import com.campusmart.exception.FileStorageException;
import com.campusmart.exception.InvalidFileTypeException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final FileStorageProperties properties;

    @Override
    public StoredFile store(MultipartFile file, String subdirectory) {
        validateFile(file);

        String extension = resolveExtension(file);
        String filename = UUID.randomUUID() + extension;
        String storageKey = subdirectory + "/" + filename;

        Path targetDir = resolveUploadRoot().resolve(subdirectory);
        Path targetFile = targetDir.resolve(filename);

        try {
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file", ex);
        }

        String publicUrl = normalizePublicUrl(properties.getPublicBaseUrl())
                + "/" + subdirectory + "/" + filename;
        return new StoredFile(storageKey, publicUrl);
    }

    @Override
    public void delete(String storageKey) {
        Path filePath = resolveUploadRoot().resolve(storageKey);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to delete file: " + storageKey, ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("Image file is required");
        }
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new InvalidFileTypeException("Image must not exceed 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !properties.getAllowedContentTypes().contains(contentType)) {
            throw new InvalidFileTypeException(
                    "Unsupported file type. Allowed: image/jpeg, image/png, image/webp"
            );
        }
    }

    private String resolveExtension(MultipartFile file) {
        String original = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : ""
        );
        String ext = StringUtils.getFilenameExtension(original);
        if (ext != null && !ext.isBlank()) {
            return "." + ext.toLowerCase(Locale.ROOT);
        }
        return switch (file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }

    private Path resolveUploadRoot() {
        return Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
    }

    private String normalizePublicUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
