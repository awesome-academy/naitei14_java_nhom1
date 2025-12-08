package org.example.foodanddrinkproject.dto;

import java.math.BigDecimal;

import org.example.foodanddrinkproject.enums.ProductType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Discount price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Discount price format is invalid")
    private BigDecimal discountPrice;

    @Size(max = 512, message = "Image URL must not exceed 512 characters")
    private String imageUrl;

    @NotBlank(message = "SKU is required")
    private String sku;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    private Double weight;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotNull(message = "Category ID is required")
    private Integer categoryId;

    @Min(value = 0, message = "Stock quantity must be at least 0")
    private Integer stockQuantity;

    private Boolean isActive;
}
