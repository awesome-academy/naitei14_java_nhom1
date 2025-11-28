package org.example.foodanddrinkproject.controller.api;

import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.dto.PlaceOrderRequest;
import org.example.foodanddrinkproject.security.CurrentUser;
import org.example.foodanddrinkproject.security.UserPrincipal;
import org.example.foodanddrinkproject.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDto> placeOrder(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody PlaceOrderRequest request) {
        OrderDto order = orderService.placeOrder(userPrincipal.getId(), request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getMyOrders(@CurrentUser UserPrincipal userPrincipal) {
        return ResponseEntity.ok(orderService.getMyOrders(userPrincipal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(userPrincipal.getId(), id));
    }
}