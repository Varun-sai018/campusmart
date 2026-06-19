package com.campusmart.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ResourceNotFoundException;
import com.campusmart.order.repository.OrderRepository;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.review.dto.ReviewRequestDto;
import com.campusmart.review.entity.Review;
import com.campusmart.review.repository.ReviewRepository;
import com.campusmart.notification.service.NotificationService;
import com.campusmart.user.entity.User;
import com.campusmart.user.repository.UserRepository;
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
class ReviewServiceTests {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReviewService reviewService;

    private User buyer;
    private User seller;
    private Product product;
    private ReviewRequestDto requestDto;

    @BeforeEach
    void setUp() {
        buyer = User.builder()
                .id(1L)
                .firstName("Buyer")
                .lastName("Test")
                .email("buyer@example.com")
                .password("secret")
                .isActive(true)
                .build();
        seller = User.builder()
                .id(2L)
                .firstName("Seller")
                .lastName("Test")
                .email("seller@example.com")
                .password("secret")
                .isActive(true)
                .build();
        product = Product.builder()
                .id(10L)
                .title("Product")
                .description("Product description")
                .price(java.math.BigDecimal.valueOf(19.99))
                .condition(ProductCondition.NEW)
                .status(ProductStatus.AVAILABLE)
                .seller(seller)
                .isActive(true)
                .build();
        requestDto = new ReviewRequestDto(5, "Great product");
    }

    @Test
    void createReview_savesReviewWhenBuyerPurchasedProduct() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.existsByOrderItemsProductAndBuyer(product, buyer)).thenReturn(true);
        when(reviewRepository.findByBuyerAndProduct(buyer, product)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = reviewService.createReview(10L, 1L, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.comment()).isEqualTo("Great product");
        assertThat(result.productId()).isEqualTo(10L);
        assertThat(result.buyerId()).isEqualTo(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_throwsWhenProductNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(10L, 1L, requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void createReview_throwsWhenBuyerIsSeller() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> reviewService.createReview(10L, 2L, requestDto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Product owner cannot review own product");
    }

    @Test
    void createReview_throwsWhenBuyerDidNotPurchaseProduct() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.existsByOrderItemsProductAndBuyer(product, buyer)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createReview(10L, 1L, requestDto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only buyers who purchased this product can review it");
    }

    @Test
    void createReview_throwsWhenReviewAlreadyExists() {
        var existingReview = Review.builder().id(5L).buyer(buyer).product(product).rating(4).comment("Good").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.existsByOrderItemsProductAndBuyer(product, buyer)).thenReturn(true);
        when(reviewRepository.findByBuyerAndProduct(buyer, product)).thenReturn(Optional.of(existingReview));

        assertThatThrownBy(() -> reviewService.createReview(10L, 1L, requestDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("One review per buyer per product is allowed");
    }
}
