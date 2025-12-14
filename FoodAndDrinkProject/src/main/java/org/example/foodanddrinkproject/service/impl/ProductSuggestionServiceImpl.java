package org.example.foodanddrinkproject.service.impl;

import org.example.foodanddrinkproject.dto.CreateSuggestionRequest;
import org.example.foodanddrinkproject.dto.ProductSuggestionDto;
import org.example.foodanddrinkproject.entity.Product;
import org.example.foodanddrinkproject.entity.ProductSuggestion;
import org.example.foodanddrinkproject.entity.User;
import org.example.foodanddrinkproject.exception.ResourceNotFoundException;
import org.example.foodanddrinkproject.repository.ProductRepository;
import org.example.foodanddrinkproject.repository.ProductSuggestionRepository;
import org.example.foodanddrinkproject.repository.UserRepository;
import org.example.foodanddrinkproject.service.ProductSuggestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductSuggestionServiceImpl implements ProductSuggestionService {

    private final ProductSuggestionRepository suggestionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductSuggestionServiceImpl(ProductSuggestionRepository suggestionRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.suggestionRepository = suggestionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void createSuggestion(Long userId, Long productId, CreateSuggestionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        ProductSuggestion suggestion = new ProductSuggestion();
        suggestion.setUser(user);
        suggestion.setProduct(product);
        suggestion.setContent(request.getContent());

        suggestionRepository.save(suggestion);
    }

    @Override
    public Page<ProductSuggestionDto> getAllSuggestions(Pageable pageable) {
        return suggestionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::convertToDto);
    }

    private ProductSuggestionDto convertToDto(ProductSuggestion entity) {
        ProductSuggestionDto dto = new ProductSuggestionDto();
        dto.setId(entity.getId());
        dto.setUserName(entity.getUser().getFullName()); // Or getEmail()
        dto.setProductName(entity.getProduct().getName());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}