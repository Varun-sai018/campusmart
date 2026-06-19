package com.campusmart.wishlist.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ProductNotFoundException;
import com.campusmart.product.entity.Product;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.user.entity.User;
import com.campusmart.user.repository.UserRepository;
import com.campusmart.wishlist.dto.WishlistItemResponseDto;
import com.campusmart.wishlist.entity.WishlistItem;
import com.campusmart.wishlist.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public WishlistItemResponseDto addProductToWishlist(Long productId, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + currentUserId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ProductNotFoundException(productId);
        }

        if (product.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Users cannot wishlist their own product");
        }

        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new BadRequestException("Product is already in wishlist");
        }

        WishlistItem wishlistItem = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();
        WishlistItem saved = wishlistRepository.save(wishlistItem);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<WishlistItemResponseDto> getWishlist(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + currentUserId));

        return wishlistRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void removeProductFromWishlist(Long productId, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + currentUserId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        WishlistItem wishlistItem = wishlistRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new BadRequestException("Wishlist item not found"));

        wishlistRepository.delete(wishlistItem);
    }

    private WishlistItemResponseDto toDto(WishlistItem item) {
        return new WishlistItemResponseDto(
                item.getId(),
                item.getUser().getId(),
                item.getProduct().getId(),
                item.getProduct().getTitle(),
                item.getProduct().getIsActive(),
                item.getCreatedAt()
        );
    }
}
