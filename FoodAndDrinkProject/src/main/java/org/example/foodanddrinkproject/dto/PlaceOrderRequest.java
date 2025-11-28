package org.example.foodanddrinkproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.foodanddrinkproject.enums.PaymentMethod;

@Getter
@Setter
public class PlaceOrderRequest {
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}