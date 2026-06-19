package com.campusmart.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusmart.cart.entity.CartItem;
import com.campusmart.cart.repository.CartItemRepository;
import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ResourceNotFoundException;
import com.campusmart.order.entity.Order;
import com.campusmart.order.entity.OrderStatus;
import com.campusmart.order.repository.OrderRepository;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.notification.service.NotificationService;
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

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private User buyer;
    private User seller;
    private Product product;
    private CartItem cartItem;

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
                .id(100L)
                .title("Order Product")
                .description("A product for orders")
                .price(new BigDecimal("25.00"))
                .condition(ProductCondition.NEW)
                .status(ProductStatus.AVAILABLE)
                .seller(seller)
                .isActive(true)
                .build();
        cartItem = CartItem.builder()
                .id(200L)
                .user(buyer)
                .product(product)
                .quantity(2)
                .build();
    }

    @Test
    void createOrder_savesOrderAndClearsCart() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByUserOrderByCreatedAtDesc(buyer)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(300L);
            order.getOrderItems().forEach(item -> item.setId(400L));
            return order;
        });

        var result = orderService.createOrder(1L);

        assertThat(result.id()).isEqualTo(300L);
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.orderItems()).hasSize(1);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.RESERVED);
        verify(cartItemRepository).deleteByUser(buyer);
    }

    @Test
    void createOrder_throwsWhenCartIsEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByUserOrderByCreatedAtDesc(buyer)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cart is empty");
    }

    @Test
    void createOrder_throwsWhenOrderNotFoundByBuyer() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(orderRepository.findByIdAndBuyer(500L, buyer)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(500L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with id: 500");
    }

    @Test
    void createOrder_rejectsSelfPurchase() {
        product.setSeller(buyer);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByUserOrderByCreatedAtDesc(buyer)).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.createOrder(1L))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessage("Users cannot purchase their own product");
    }

    @Test
    void createOrder_rejectsUnavailableProduct() {
        product.setStatus(ProductStatus.SOLD);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByUserOrderByCreatedAtDesc(buyer)).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.createOrder(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Product is not available: 100");
    }
}
