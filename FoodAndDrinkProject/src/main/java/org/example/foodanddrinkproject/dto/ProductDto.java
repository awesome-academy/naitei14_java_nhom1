package org.example.foodanddrinkproject.dto;


import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;


@Getter
@Setter
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String imageUrl;
    private String sku;
    private String brand;
    private Double weight;
    private boolean isActive;
    private String productType;
    private int stockQuantity;
    private double avgRating;


    // Category Info
    private Integer categoryId;
    private String categoryName;
}
