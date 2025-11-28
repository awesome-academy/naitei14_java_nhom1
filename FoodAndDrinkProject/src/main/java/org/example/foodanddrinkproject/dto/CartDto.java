package org.example.foodanddrinkproject.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CartDto {
    private Long id; // Cart ID
    private List<CartItemDto> items = new ArrayList<>();
    private BigDecimal totalAmount;
}