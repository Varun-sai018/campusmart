package com.campusmart.review.service;

import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ResourceNotFoundException;
import com.campusmart.notification.NotificationType;
import com.campusmart.notification.service.NotificationService;
import com.campusmart.order.repository.OrderRepository;
import com.campusmart.product.entity.Product;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.review.dto.ProductRatingSummaryDto;
import com.campusmart.review.dto.ReviewRequestDto;
import com.campusmart.review.dto.ReviewResponseDto;
import com.campusmart.review.entity.Review;
import com.campusmart.review.repository.ReviewRepository;
import com.campusmart.user.entity.User;
import com.campusmart.user.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReviewResponseDto createReview(Long productId, Long currentUserId, ReviewRequestDto request) {
        User buyer = getUser(currentUserId);
        Product product = getProduct(productId);

        validateBuyerIsNotSeller(buyer, product);
        validateBuyerPurchasedProduct(buyer, product);
        validateReviewNotExists(buyer, product);

        Review review = Review.builder()
                .buyer(buyer)
                .product(product)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        Review savedReview = reviewRepository.save(review);
        notificationService.createNotification(product.getSeller(), NotificationType.REVIEW_RECEIVED,
                "Your product '" + product.getTitle() + "' received a new review.");
        notificationService.createNotification(buyer, NotificationType.REVIEW_POSTED,
                "Your review for '" + product.getTitle() + "' has been posted.");
        return toDto(savedReview);
    }

    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, Long currentUserId, ReviewRequestDto request) {
        Review review = getReview(reviewId);
        validateReviewOwner(review, currentUserId);

        review.setRating(request.rating());
        review.setComment(request.comment());

        return toDto(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        Review review = getReview(reviewId);
        validateReviewOwner(review, currentUserId);
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByProduct(Long productId) {
        Product product = getProduct(productId);
        return reviewRepository.findByProductOrderByCreatedAtDesc(product).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductRatingSummaryDto getRatingSummary(Long productId) {
        Product product = getProduct(productId);
        List<Review> reviews = reviewRepository.findByProduct(product);
        long totalReviews = reviews.size();
        double averageRating = totalReviews == 0 ? 0.0 : reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Map<Integer, Long> ratingBreakdown = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        for (int rating = 1; rating <= 5; rating++) {
            ratingBreakdown.putIfAbsent(rating, 0L);
        }

        return new ProductRatingSummaryDto(averageRating, totalReviews, ratingBreakdown);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
    }

    private void validateBuyerIsNotSeller(User buyer, Product product) {
        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new AccessDeniedException("Product owner cannot review own product");
        }
    }

    private void validateBuyerPurchasedProduct(User buyer, Product product) {
        boolean purchased = orderRepository.existsByOrderItemsProductAndBuyer(product, buyer);
        if (!purchased) {
            throw new AccessDeniedException("Only buyers who purchased this product can review it");
        }
    }

    private void validateReviewNotExists(User buyer, Product product) {
        if (reviewRepository.findByBuyerAndProduct(buyer, product).isPresent()) {
            throw new BadRequestException("One review per buyer per product is allowed");
        }
    }

    private void validateReviewOwner(Review review, Long currentUserId) {
        if (!review.getBuyer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the review author can modify this review");
        }
    }

    private ReviewResponseDto toDto(Review review) {
        return new ReviewResponseDto(
                review.getId(),
                review.getBuyer().getId(),
                review.getBuyer().getFirstName() + " " + review.getBuyer().getLastName(),
                review.getProduct().getId(),
                review.getProduct().getTitle(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
