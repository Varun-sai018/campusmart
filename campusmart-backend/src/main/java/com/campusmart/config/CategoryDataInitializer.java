package com.campusmart.config;

import com.campusmart.category.entity.Category;
import com.campusmart.category.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "BOOKS",
            "ELECTRONICS",
            "CALCULATORS",
            "CYCLES",
            "LAB_EQUIPMENT",
            "HOSTEL_ESSENTIALS"
    );

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (String categoryName : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByNameIgnoreCase(categoryName)) {
                categoryRepository.save(Category.builder()
                        .name(categoryName)
                        .description(null)
                        .isActive(true)
                        .build());
            }
        }
    }
}

