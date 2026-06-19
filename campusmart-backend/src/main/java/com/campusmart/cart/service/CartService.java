package com.campusmart.cart.service;

import com.campusmart.cart.dto.CartItemRequestDto;
import com.campusmart.cart.dto.CartItemResponseDto;
import com.campusmart.cart.dto.CartSummaryDto;
import com.campusmart.cart.entity.CartItem;
import com.campusmart.cart.repository.CartItemRepository;
import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ProductNotFoundException;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.product.repository.ProductRepository;
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
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartItemResponseDto addProductToCart(Long productId, Integer quantity, Long currentUserId) {
        User user = getUser(currentUserId);
        Product product = getActiveAvailableProduct(productId);

        if (product.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Users cannot add their own product to cart");
        }

        int orderedQuantity = quantity == null ? 1 : quantity;
        if (orderedQuantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .map(existing -> updateQuantity(existing, existing.getQuantity() + orderedQuantity))
                .orElseGet(() -> createCartItem(user, product, orderedQuantity));

        return toDto(cartItemRepository.save(cartItem));
    }

    @Transactional
    public CartItemResponseDto updateCartItemQuantity(Long productId, Integer quantity, Long currentUserId) {
        User user = getUser(currentUserId);
        Product product = getActiveAvailableProduct(productId);

        if (quantity == null || quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new BadRequestException("Cart item not found for product"));

        cartItem.setQuantity(quantity);
        return toDto(cartItemRepository.save(cartItem));
    }

    @Transactional(readOnly = true)
    public CartSummaryDto getCart(Long currentUserId) {
        User user = getUser(currentUserId);
        List<CartItemResponseDto> items = cartItemRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .toList();

        int totalItems = items.size();
        int totalQuantity = items.stream().mapToInt(CartItemResponseDto::quantity).sum();
        BigDecimal totalAmount = items.stream()
                .map(CartItemResponseDto::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartSummaryDto(totalItems, totalQuantity, totalAmount, items);
    }

    @Transactional
    public void removeProductFromCart(Long productId, Long currentUserId) {
        User user = getUser(currentUserId);
        Product product = getActiveAvailableProduct(productId);

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new BadRequestException("Cart item not found for product"));

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(Long currentUserId) {
        User user = getUser(currentUserId);
        cartItemRepository.deleteByUser(user);
    }

    private User getUser(Long currentUserId) {
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + currentUserId));
    }

    private Product getActiveAvailableProduct(Long productId) {
        return productRepository.findById(productId)
                .filter(product -> Boolean.TRUE.equals(product.getIsActive()))
                .filter(product -> product.getStatus() == ProductStatus.AVAILABLE)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private CartItem createCartItem(User user, Product product, int quantity) {
        return CartItem.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .build();
    }

    private CartItem updateQuantity(CartItem existing, int quantity) {
        existing.setQuantity(quantity);
        return existing;
    }

    private CartItemResponseDto toDto(CartItem cartItem) {
        return new CartItemResponseDto(
                cartItem.getId(),
                cartItem.getUser().getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getTitle(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())),
                cartItem.getProduct().getIsActive(),
                cartItem.getCreatedAt(),
                cartItem.getUpdatedAt()
        );
    }
}
