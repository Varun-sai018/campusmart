package com.campusmart.order.repository;

import com.campusmart.order.entity.Order;
import com.campusmart.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

    Optional<Order> findByIdAndBuyer(Long id, User buyer);

    List<Order> findDistinctByOrderItemsSellerOrderByCreatedAtDesc(User seller);
}
