package com.campusmart;

import com.campusmart.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class CampusmartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusmartApplication.class, args);
    }
}

