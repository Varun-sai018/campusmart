package com.campusmart.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusmart.category.dto.CategoryRequestDto;
import com.campusmart.category.entity.Category;
import com.campusmart.category.repository.CategoryRepository;
import com.campusmart.exception.CategoryNotFoundException;
import com.campusmart.exception.DuplicateCategoryException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getActiveCategoriesReturnsMappedResponses() {
        Category category = category(1L, "BOOKS", true);
        when(categoryRepository.findByIsActiveTrueOrderByNameAsc()).thenReturn(List.of(category));

        var response = categoryService.getActiveCategories();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).name()).isEqualTo("BOOKS");
        assertThat(response.get(0).isActive()).isTrue();
    }

    @Test
    void createCategoryRejectsDuplicateName() {
        when(categoryRepository.existsByNameIgnoreCase("BOOKS")).thenReturn(true);
        CategoryRequestDto request = new CategoryRequestDto("books", "Text books", true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateCategoryException.class)
                .hasMessageContaining("BOOKS");
    }

    @Test
    void createCategoryNormalizesNameAndDefaultsActive() {
        when(categoryRepository.existsByNameIgnoreCase("BOOKS")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        var response = categoryService.createCategory(new CategoryRequestDto(" books ", "  Text books  ", null));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("BOOKS");
        assertThat(captor.getValue().getDescription()).isEqualTo("Text books");
        assertThat(captor.getValue().getIsActive()).isTrue();
        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void updateCategoryRejectsDuplicateDifferentCategory() {
        Category existing = category(1L, "BOOKS", true);
        Category duplicate = category(2L, "ELECTRONICS", true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByNameIgnoreCase("ELECTRONICS")).thenReturn(Optional.of(duplicate));

        assertThatThrownBy(() -> categoryService.updateCategory(
                1L,
                new CategoryRequestDto("electronics", null, true)
        )).isInstanceOf(DuplicateCategoryException.class);
    }

    @Test
    void deleteCategorySoftDeletes() {
        Category category = category(1L, "BOOKS", true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        assertThat(category.getIsActive()).isFalse();
        verify(categoryRepository).save(category);
    }

    @Test
    void getCategoryByIdThrowsWhenMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    private Category category(Long id, String name, Boolean active) {
        return Category.builder()
                .id(id)
                .name(name)
                .description("Description")
                .isActive(active)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

