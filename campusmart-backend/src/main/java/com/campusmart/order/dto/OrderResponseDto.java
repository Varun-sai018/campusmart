package com.campusmart.order.dto;

import com.campusmart.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Long buyerId,
        BigDecimal totalAmount,
        OrderStatus orderStatus,
        List<OrderItemResponseDto> orderItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
