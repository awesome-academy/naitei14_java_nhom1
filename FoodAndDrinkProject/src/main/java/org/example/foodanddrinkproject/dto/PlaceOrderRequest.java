package org.example.foodanddrinkproject.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.foodanddrinkproject.enums.PaymentMethod;

@Getter
@Setter
public class PlaceOrderRequest {
    
    // Option 1: Use a saved address by ID
    private Long addressId;
    
    // Option 2: Provide a custom address string directly
    private String shippingAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}