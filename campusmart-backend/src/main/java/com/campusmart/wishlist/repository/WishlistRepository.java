package com.campusmart.wishlist.repository;

import com.campusmart.product.entity.Product;
import com.campusmart.user.entity.User;
import com.campusmart.wishlist.entity.WishlistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByUserAndProduct(User user, Product product);

    List<WishlistItem> findByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndProduct(User user, Product product);
}
