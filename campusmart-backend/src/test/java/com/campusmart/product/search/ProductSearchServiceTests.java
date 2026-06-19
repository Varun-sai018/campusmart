package com.campusmart.product.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.campusmart.category.entity.Category;
import com.campusmart.product.dto.ProductResponseDto;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.user.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductSearchService productSearchService;

    private Product product;

    @BeforeEach
    void setUp() {
        User seller = User.builder().id(2L).firstName("Seller").lastName("User").build();
        Category category = Category.builder().id(3L).name("BOOKS").build();

        product = Product.builder()
                .id(10L)
                .title("Laptop bag")
                .description("A padded laptop bag for daily use")
                .price(new BigDecimal("1999.99"))
                .condition(ProductCondition.GOOD)
                .status(ProductStatus.AVAILABLE)
                .seller(seller)
                .category(category)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void searchProducts_appliesKeywordAndPriceAndSorting() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "laptop",
                3L,
                2L,
                ProductCondition.GOOD,
                ProductStatus.AVAILABLE,
                new BigDecimal("1000"),
                new BigDecimal("5000"),
                0,
                10,
                "price,asc"
        );

        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), eq(PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("price")))))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponseDto> result = productSearchService.searchProducts(criteria);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Laptop bag");
    }

    @Test
    void searchProducts_usesDefaultPaginationAndSortWhenNotProvided() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), eq(PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponseDto> result = productSearchService.searchProducts(criteria);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Laptop bag");
    }
}
