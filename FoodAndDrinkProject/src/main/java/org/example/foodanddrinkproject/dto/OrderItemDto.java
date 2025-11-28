package org.example.foodanddrinkproject.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemDto {
    private Long productId;
    private String productName;
    private String productImageUrl;
    private int quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal;
}