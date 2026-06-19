package com.campusmart.wishlist.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ProductNotFoundException;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.user.entity.User;
import com.campusmart.user.repository.UserRepository;
import com.campusmart.wishlist.dto.WishlistItemResponseDto;
import com.campusmart.wishlist.entity.WishlistItem;
import com.campusmart.wishlist.repository.WishlistRepository;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTests {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistService wishlistService;

    private User buyer;
    private User seller;
    private Product activeProduct;

    @BeforeEach
    void setUp() {
        buyer = User.builder().id(1L).firstName("First").lastName("Buyer").email("buyer@example.com").password("secret").isActive(true).build();
        seller = User.builder().id(2L).firstName("Seller").lastName("User").email("seller@example.com").password("secret").isActive(true).build();
        activeProduct = Product.builder()
                .id(100L)
                .title("Test Book")
                .description("A test product")
                .price(new BigDecimal("9.99"))
                .condition(ProductCondition.NEW)
                .status(ProductStatus.AVAILABLE)
                .seller(seller)
                .isActive(true)
                .build();
    }

    @Test
    void addProductToWishlist_persistsWishlistItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(wishlistRepository.existsByUserAndProduct(buyer, activeProduct)).thenReturn(false);
        when(wishlistRepository.save(any(WishlistItem.class))).thenAnswer(invocation -> {
            WishlistItem item = invocation.getArgument(0);
            item.setId(200L);
            return item;
        });

        WishlistItemResponseDto result = wishlistService.addProductToWishlist(100L, 1L);

        assertThat(result.id()).isEqualTo(200L);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.productId()).isEqualTo(100L);
        assertThat(result.productTitle()).isEqualTo("Test Book");
        verify(wishlistRepository).save(any(WishlistItem.class));
    }

    @Test
    void addProductToWishlist_rejectsInactiveProduct() {
        activeProduct.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));

        assertThatThrownBy(() -> wishlistService.addProductToWishlist(100L, 1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void addProductToWishlist_rejectsOwnProduct() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));

        assertThatThrownBy(() -> wishlistService.addProductToWishlist(100L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Users cannot wishlist their own product");
    }

    @Test
    void addProductToWishlist_rejectsDuplicateWishlistEntry() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(wishlistRepository.existsByUserAndProduct(buyer, activeProduct)).thenReturn(true);

        assertThatThrownBy(() -> wishlistService.addProductToWishlist(100L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Product is already in wishlist");
    }

    @Test
    void getWishlist_returnsOrderedWishlistItems() {
        WishlistItem item = WishlistItem.builder().id(200L).product(activeProduct).user(buyer).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(wishlistRepository.findByUserOrderByCreatedAtDesc(buyer)).thenReturn(List.of(item));

        List<WishlistItemResponseDto> results = wishlistService.getWishlist(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).productId()).isEqualTo(100L);
    }

    @Test
    void removeProductFromWishlist_deletesExistingWishlistItem() {
        WishlistItem item = WishlistItem.builder().id(200L).product(activeProduct).user(buyer).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(wishlistRepository.findByUserAndProduct(buyer, activeProduct)).thenReturn(Optional.of(item));

        wishlistService.removeProductFromWishlist(100L, 1L);

        verify(wishlistRepository).delete(item);
    }
}
