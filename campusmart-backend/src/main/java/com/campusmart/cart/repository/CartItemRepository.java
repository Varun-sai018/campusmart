package com.campusmart.cart.repository;

import com.campusmart.cart.entity.CartItem;
import com.campusmart.product.entity.Product;
import com.campusmart.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    List<CartItem> findByUserOrderByCreatedAtDesc(User user);

    void deleteByUserAndProduct(User user, Product product);

    void deleteByUser(User user);
}
