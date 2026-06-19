package com.campusmart.review.repository;

import com.campusmart.product.entity.Product;
import com.campusmart.review.entity.Review;
import com.campusmart.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBuyerAndProduct(User buyer, Product product);

    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    List<Review> findByProduct(Product product);
}
