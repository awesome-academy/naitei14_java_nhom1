package org.example.foodanddrinkproject.service;


import org.example.foodanddrinkproject.dto.ProductDto;
import org.example.foodanddrinkproject.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;


public interface ProductService {
    ProductDto getProductById(Long productId);
}
