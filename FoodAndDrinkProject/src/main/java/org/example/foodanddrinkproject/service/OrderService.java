package org.example.foodanddrinkproject.service;

import org.example.foodanddrinkproject.dto.AdminUpdateOrderRequest;
import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.dto.PlaceOrderRequest;
import org.example.foodanddrinkproject.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderDto placeOrder(Long userId, PlaceOrderRequest request);
    List<OrderDto> getMyOrders(Long userId);
    OrderDto getOrderById(Long userId, Long orderId);

    Page<OrderDto> getAllOrders(OrderStatus status, Long userId, Pageable pageable);
    OrderDto updateOrder(Long orderId, AdminUpdateOrderRequest request);
}