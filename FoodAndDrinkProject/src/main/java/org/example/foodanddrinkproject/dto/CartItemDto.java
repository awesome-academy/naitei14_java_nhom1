package org.example.foodanddrinkproject.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CartItemDto {
    private Long id; // CartItem ID
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price; // Price per unit
    private int quantity;
    private BigDecimal subtotal; // price * quantity
}