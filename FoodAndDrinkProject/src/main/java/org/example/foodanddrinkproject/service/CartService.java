package org.example.foodanddrinkproject.service;


import org.example.foodanddrinkproject.dto.AddItemToCartRequest;
import org.example.foodanddrinkproject.dto.CartDto;

public interface CartService {
    CartDto getMyCart(Long userId);
    CartDto addItemToCart(Long userId, AddItemToCartRequest request);
    CartDto decrementItemQuantity(Long userId, AddItemToCartRequest request);
    CartDto removeItem(Long userId, Long productId);
    void clearCart(Long userId);
}