package com.campusmart.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class FileStorageProperties {

    private String provider = "local";
    private String uploadDir = "uploads";
    private String publicBaseUrl = "/uploads";
    private String productsSubdir = "products";
    private List<String> allowedContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private long maxFileSizeBytes = 5 * 1024 * 1024;
    private int maxImagesPerProduct = 5;
}
