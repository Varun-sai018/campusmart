package com.campusmart.config;

import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRoot = Path.of(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        String publicBaseUrl = fileStorageProperties.getPublicBaseUrl();
        String pattern = publicBaseUrl.endsWith("/") ? publicBaseUrl + "**" : publicBaseUrl + "/**";
        registry.addResourceHandler(pattern)
                .addResourceLocations("file:" + uploadRoot + "/");
    }
}
