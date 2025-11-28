package org.example.foodanddrinkproject.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.foodanddrinkproject.enums.OrderStatus;
import org.example.foodanddrinkproject.enums.PaymentMethod;
import org.example.foodanddrinkproject.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDto {
    private Long id;
    private LocalDateTime orderDate;
    private String shippingAddress;

    // Cost Breakdown
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    // Statuses
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;

    private List<OrderItemDto> items;
}