package org.example.foodanddrinkproject.service;

import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.dto.PlaceOrderRequest;

import java.util.List;

public interface OrderService {
    OrderDto placeOrder(Long userId, PlaceOrderRequest request);
    List<OrderDto> getMyOrders(Long userId);
    OrderDto getOrderById(Long userId, Long orderId);
}