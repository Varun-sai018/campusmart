package com.campusmart.productattribute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ProductAttributeNotFoundException;
import com.campusmart.exception.ProductNotFoundException;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.productattribute.dto.ProductAttributeRequestDto;
import com.campusmart.productattribute.entity.ProductAttribute;
import com.campusmart.productattribute.repository.ProductAttributeRepository;
import com.campusmart.user.entity.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ProductAttributeServiceTests {

    @Mock
    private ProductAttributeRepository productAttributeRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductAttributeService productAttributeService;

    private Product product;

    @BeforeEach
    void setUp() {
        User seller = User.builder().id(1L).firstName("Seller").lastName("Owner").build();
        product = Product.builder()
                .id(10L)
                .title("Test Product")
                .condition(ProductCondition.NEW)
                .status(ProductStatus.AVAILABLE)
                .price(new BigDecimal("19.99"))
                .seller(seller)
                .isActive(true)
                .build();
    }

    @Test
    void createAttribute_persistsAttributeForOwner() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productAttributeRepository.countByProduct(product)).thenReturn(0L);
        when(productAttributeRepository.save(any(ProductAttribute.class))).thenAnswer(invocation -> {
            ProductAttribute saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        var dto = productAttributeService.createAttribute(
                10L,
                new ProductAttributeRequestDto("Color", "Blue"),
                1L,
                false
        );

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.attributeName()).isEqualTo("Color");
        assertThat(dto.attributeValue()).isEqualTo("Blue");
        assertThat(dto.productId()).isEqualTo(10L);
        verify(productAttributeRepository).save(any(ProductAttribute.class));
    }

    @Test
    void createAttribute_trimsInputValues() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productAttributeRepository.countByProduct(product)).thenReturn(0L);
        when(productAttributeRepository.save(any(ProductAttribute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = productAttributeService.createAttribute(
                10L,
                new ProductAttributeRequestDto("  Color  ", "  Blue  "),
                1L,
                false
        );

        assertThat(dto.attributeName()).isEqualTo("Color");
        assertThat(dto.attributeValue()).isEqualTo("Blue");
    }

    @Test
    void createAttribute_rejectsWhenProductInactive() {
        product.setIsActive(false);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productAttributeService.createAttribute(
                10L,
                new ProductAttributeRequestDto("Color", "Blue"),
                1L,
                false
        )).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createAttribute_rejectsWhenMaxAttributesReached() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productAttributeRepository.countByProduct(product)).thenReturn(20L);

        assertThatThrownBy(() -> productAttributeService.createAttribute(
                10L,
                new ProductAttributeRequestDto("Color", "Blue"),
                1L,
                false
        )).isInstanceOf(BadRequestException.class)
          .hasMessage("A product can have at most 20 attributes");
    }

    @Test
    void createAttribute_rejectsNonOwner() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productAttributeService.createAttribute(
                10L,
                new ProductAttributeRequestDto("Color", "Blue"),
                99L,
                false
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getAttributesByProduct_returnsOrderedList() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productAttributeRepository.findByProductOrderByCreatedAtAsc(product)).thenReturn(
                List.of(
                        ProductAttribute.builder().id(101L).attributeName("Color").attributeValue("Blue").product(product).build(),
                        ProductAttribute.builder().id(102L).attributeName("Size").attributeValue("Medium").product(product).build()
                )
        );

        var attributes = productAttributeService.getAttributesByProduct(10L);

        assertThat(attributes).hasSize(2);
        assertThat(attributes.get(0).attributeName()).isEqualTo("Color");
        assertThat(attributes.get(1).attributeName()).isEqualTo("Size");
    }

    @Test
    void deleteAttribute_removesExistingAttributeForOwner() {
        ProductAttribute attribute = ProductAttribute.builder()
                .id(101L)
                .attributeName("Color")
                .attributeValue("Blue")
                .product(product)
                .build();
        when(productAttributeRepository.findById(101L)).thenReturn(Optional.of(attribute));

        productAttributeService.deleteAttribute(101L, 1L, false);

        verify(productAttributeRepository).delete(attribute);
    }

    @Test
    void deleteAttribute_rejectsMissingAttribute() {
        when(productAttributeRepository.findById(101L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productAttributeService.deleteAttribute(101L, 1L, false))
                .isInstanceOf(ProductAttributeNotFoundException.class);
    }

    @Test
    void deleteAttribute_rejectsNonOwner() {
        ProductAttribute attribute = ProductAttribute.builder()
                .id(101L)
                .attributeName("Color")
                .attributeValue("Blue")
                .product(product)
                .build();
        when(productAttributeRepository.findById(101L)).thenReturn(Optional.of(attribute));

        assertThatThrownBy(() -> productAttributeService.deleteAttribute(101L, 99L, false))
                .isInstanceOf(AccessDeniedException.class);
    }
}
