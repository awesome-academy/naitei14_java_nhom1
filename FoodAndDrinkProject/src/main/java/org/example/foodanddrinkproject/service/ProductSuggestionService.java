package org.example.foodanddrinkproject.service;

import org.example.foodanddrinkproject.dto.CreateSuggestionRequest;
import org.example.foodanddrinkproject.dto.ProductSuggestionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSuggestionService {
    void createSuggestion(Long userId, Long productId, CreateSuggestionRequest request);
    Page<ProductSuggestionDto> getAllSuggestions(Pageable pageable);
}