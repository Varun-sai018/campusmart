package com.campusmart.order.controller;

import com.campusmart.order.dto.OrderResponseDto;
import com.campusmart.order.service.OrderService;
import com.campusmart.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders", description = "Manage orders created from user cart")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Place an order for the authenticated user's cart")
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        OrderResponseDto response = orderService.createOrder(principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get orders for the authenticated buyer")
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrders(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(orderService.getOrdersForBuyer(principal.getId()));
    }

    @Operation(summary = "Get an order by id for the authenticated buyer")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, principal.getId()));
    }

    @Operation(summary = "Get orders containing items sold by the authenticated seller")
    @GetMapping("/seller")
    public ResponseEntity<List<OrderResponseDto>> getSellerOrders(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(orderService.getOrdersForSeller(principal.getId()));
    }
}
