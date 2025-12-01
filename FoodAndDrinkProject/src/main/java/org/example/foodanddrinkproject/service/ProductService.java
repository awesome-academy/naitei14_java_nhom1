package org.example.foodanddrinkproject.service;

import java.math.BigDecimal;

import org.example.foodanddrinkproject.dto.CreateProductRequest;
import org.example.foodanddrinkproject.dto.ProductDto;
import org.example.foodanddrinkproject.dto.UpdateProductRequest;
import org.example.foodanddrinkproject.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductDto> getAllProducts(
            String name, String brand, Integer categoryId, ProductType type,
            BigDecimal minPrice, BigDecimal maxPrice, Double minRating,
            Pageable pageable
    );

    ProductDto getProductById(Long productId);
    
    ProductDto createProduct(CreateProductRequest request);
    
    ProductDto updateProduct(Long productId, UpdateProductRequest request);
    
    void deleteProduct(Long productId);
    
    ProductDto toggleProductStatus(Long productId);
}
