package com.campusmart.order.service;

import com.campusmart.cart.entity.CartItem;
import com.campusmart.cart.repository.CartItemRepository;
import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ResourceNotFoundException;
import com.campusmart.order.dto.OrderItemResponseDto;
import com.campusmart.order.dto.OrderResponseDto;
import com.campusmart.order.entity.Order;
import com.campusmart.order.entity.OrderItem;
import com.campusmart.order.entity.OrderStatus;
import com.campusmart.notification.NotificationType;
import com.campusmart.notification.service.NotificationService;
import com.campusmart.order.repository.OrderRepository;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.user.entity.User;
import com.campusmart.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public OrderResponseDto createOrder(Long currentUserId) {
        User buyer = getUser(currentUserId);
        List<CartItem> cartItems = cartItemRepository.findByUserOrderByCreatedAtDesc(buyer);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Order order = Order.builder()
                .buyer(buyer)
                .orderStatus(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> buildOrderItem(order, item, currentUserId))
                .toList();

        BigDecimal totalAmount = orderItems.stream()
                .map(entry -> entry.getPriceAtPurchase().multiply(BigDecimal.valueOf(entry.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        cartItemRepository.deleteByUser(buyer);
        notificationService.createNotification(buyer, NotificationType.ORDER_PLACED,
                "Your order " + savedOrder.getId() + " has been placed successfully.");
        order.getOrderItems().stream()
                .map(orderItem -> orderItem.getSeller())
                .distinct()
                .forEach(seller -> notificationService.createNotification(seller, NotificationType.PRODUCT_RESERVED,
                        "A product in your listing has been reserved by order " + savedOrder.getId() + "."));
        return toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersForBuyer(Long currentUserId) {
        User buyer = getUser(currentUserId);
        return orderRepository.findByBuyerOrderByCreatedAtDesc(buyer).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId, Long currentUserId) {
        User buyer = getUser(currentUserId);
        Order order = orderRepository.findByIdAndBuyer(orderId, buyer)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersForSeller(Long currentUserId) {
        User seller = getUser(currentUserId);
        return orderRepository.findDistinctByOrderItemsSellerOrderByCreatedAtDesc(seller).stream()
                .map(this::toDto)
                .toList();
    }

    private OrderItem buildOrderItem(Order order, CartItem cartItem, Long currentUserId) {
        Product product = cartItem.getProduct();
        if (!Boolean.TRUE.equals(product.getIsActive()) || product.getStatus() != ProductStatus.AVAILABLE) {
            throw new BadRequestException("Product is not available: " + product.getId());
        }
        if (product.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Users cannot purchase their own product");
        }

        product.setStatus(ProductStatus.RESERVED);

        return OrderItem.builder()
                .order(order)
                .product(product)
                .seller(product.getSeller())
                .priceAtPurchase(product.getPrice())
                .quantity(cartItem.getQuantity())
                .build();
    }

    private OrderResponseDto toDto(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getBuyer().getId(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getOrderItems().stream().map(this::toOrderItemDto).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponseDto toOrderItemDto(OrderItem orderItem) {
        return new OrderItemResponseDto(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getTitle(),
                orderItem.getSeller().getId(),
                orderItem.getPriceAtPurchase(),
                orderItem.getQuantity()
        );
    }

    private User getUser(Long currentUserId) {
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + currentUserId));
    }
}
