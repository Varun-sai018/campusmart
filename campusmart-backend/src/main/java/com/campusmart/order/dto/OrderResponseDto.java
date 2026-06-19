package com.campusmart.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.campusmart.order.entity.OrderStatus;

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
