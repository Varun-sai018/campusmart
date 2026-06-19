package com.campusmart.category.service;

import com.campusmart.category.dto.CategoryRequestDto;
import com.campusmart.category.dto.CategoryResponseDto;
import com.campusmart.category.entity.Category;
import com.campusmart.category.repository.CategoryRepository;
import com.campusmart.exception.CategoryNotFoundException;
import com.campusmart.exception.DuplicateCategoryException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto request) {
        String normalizedName = normalizeName(request.name());
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateCategoryException(normalizedName);
        }

        Category category = Category.builder()
                .name(normalizedName)
                .description(normalizeDescription(request.description()))
                .isActive(request.isActive() == null ? true : request.isActive())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        String normalizedName = normalizeName(request.name());
        categoryRepository.findByNameIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateCategoryException(normalizedName);
                });

        category.setName(normalizedName);
        category.setDescription(normalizeDescription(request.description()));
        if (request.isActive() != null) {
            category.setIsActive(request.isActive());
        }

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    private CategoryResponseDto toResponse(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIsActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private String normalizeName(String name) {
        return name.trim().toUpperCase();
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}

