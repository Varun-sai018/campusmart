package com.campusmart.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusmart.cart.dto.CartItemRequestDto;
import com.campusmart.cart.dto.CartItemResponseDto;
import com.campusmart.cart.dto.CartSummaryDto;
import com.campusmart.cart.entity.CartItem;
import com.campusmart.cart.repository.CartItemRepository;
import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ProductNotFoundException;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.user.entity.User;
import com.campusmart.user.repository.UserRepository;
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
class CartServiceTests {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User buyer;
    private User seller;
    private Product activeProduct;

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
        activeProduct = Product.builder()
                .id(100L)
                .title("Test Product")
                .description("A test product")
                .price(new BigDecimal("9.99"))
                .condition(ProductCondition.NEW)
                .status(ProductStatus.AVAILABLE)
                .seller(seller)
                .isActive(true)
                .build();
    }

    @Test
    void addProductToCart_createsNewCartItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartItemRepository.findByUserAndProduct(buyer, activeProduct)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(200L);
            return item;
        });

        CartItemResponseDto result = cartService.addProductToCart(100L, 2, 1L);

        assertThat(result.id()).isEqualTo(200L);
        assertThat(result.productId()).isEqualTo(100L);
        assertThat(result.quantity()).isEqualTo(2);
        assertThat(result.totalPrice()).isEqualByComparingTo(new BigDecimal("19.98"));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addProductToCart_updatesExistingCartItemQuantity() {
        CartItem existing = CartItem.builder().id(200L).user(buyer).product(activeProduct).quantity(1).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartItemRepository.findByUserAndProduct(buyer, activeProduct)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).thenReturn(existing);

        CartItemResponseDto result = cartService.addProductToCart(100L, 2, 1L);

        assertThat(result.quantity()).isEqualTo(3);
        assertThat(result.totalPrice()).isEqualByComparingTo(new BigDecimal("29.97"));
    }

    @Test
    void addProductToCart_rejectsOwnProduct() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));

        assertThatThrownBy(() -> cartService.addProductToCart(100L, 1, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Users cannot add their own product to cart");
    }

    @Test
    void addProductToCart_rejectsInactiveProduct() {
        activeProduct.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));

        assertThatThrownBy(() -> cartService.addProductToCart(100L, 1, 1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void addProductToCart_rejectsUnavailableProduct() {
        activeProduct.setStatus(ProductStatus.SOLD);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));

        assertThatThrownBy(() -> cartService.addProductToCart(100L, 1, 1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void updateCartItemQuantity_updatesValue() {
        CartItem existing = CartItem.builder().id(200L).user(buyer).product(activeProduct).quantity(1).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartItemRepository.findByUserAndProduct(buyer, activeProduct)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).thenReturn(existing);

        CartItemResponseDto result = cartService.updateCartItemQuantity(100L, 5, 1L);

        assertThat(result.quantity()).isEqualTo(5);
        assertThat(result.totalPrice()).isEqualByComparingTo(new BigDecimal("49.95"));
    }

    @Test
    void updateCartItemQuantity_rejectsZeroQuantity() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));

        assertThatThrownBy(() -> cartService.updateCartItemQuantity(100L, 0, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Quantity must be at least 1");
    }

    @Test
    void updateCartItemQuantity_rejectsMissingCartItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartItemRepository.findByUserAndProduct(buyer, activeProduct)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateCartItemQuantity(100L, 3, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cart item not found for product");
    }

    @Test
    void getCart_returnsSummary() {
        CartItem first = CartItem.builder().id(200L).user(buyer).product(activeProduct).quantity(1).build();
        CartItem second = CartItem.builder().id(201L).user(buyer).product(activeProduct).quantity(2).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByUserOrderByCreatedAtDesc(buyer)).thenReturn(List.of(first, second));

        CartSummaryDto summary = cartService.getCart(1L);

        assertThat(summary.totalItems()).isEqualTo(2);
        assertThat(summary.totalQuantity()).isEqualTo(3);
        assertThat(summary.totalAmount()).isEqualByComparingTo(new BigDecimal("29.97"));
    }

    @Test
    void removeProductFromCart_deletesCartItem() {
        CartItem existing = CartItem.builder().id(200L).user(buyer).product(activeProduct).quantity(1).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(cartItemRepository.findByUserAndProduct(buyer, activeProduct)).thenReturn(Optional.of(existing));

        cartService.removeProductFromCart(100L, 1L);

        verify(cartItemRepository).delete(existing);
    }

    @Test
    void clearCart_deletesByUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));

        cartService.clearCart(1L);

        verify(cartItemRepository).deleteByUser(buyer);
    }
}
