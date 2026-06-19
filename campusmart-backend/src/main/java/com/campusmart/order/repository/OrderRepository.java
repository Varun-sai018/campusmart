package com.campusmart.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusmart.order.entity.Order;
import com.campusmart.product.entity.Product;
import com.campusmart.user.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

    Optional<Order> findByIdAndBuyer(Long id, User buyer);

    List<Order> findDistinctByOrderItemsSellerOrderByCreatedAtDesc(User seller);

    boolean existsByOrderItemsProductAndBuyer(Product product, User buyer);
}
