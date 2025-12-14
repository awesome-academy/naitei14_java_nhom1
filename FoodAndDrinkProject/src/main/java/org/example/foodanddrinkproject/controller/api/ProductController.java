package org.example.foodanddrinkproject.controller.api;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import org.example.foodanddrinkproject.dto.ApiResponse;
import org.example.foodanddrinkproject.dto.CreateSuggestionRequest;
import org.example.foodanddrinkproject.dto.ProductDto;
import org.example.foodanddrinkproject.enums.ProductType;
import org.example.foodanddrinkproject.security.UserPrincipal;
import org.example.foodanddrinkproject.service.ProductService;
import org.example.foodanddrinkproject.service.ProductSuggestionService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductSuggestionService suggestionService;

    public ProductController(ProductService productService,
                             ProductSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        return ResponseEntity.ok(productService.getAllProducts(
                name, brand, categoryId, type, minPrice, maxPrice, minRating, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/{productId}/suggestions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> createSuggestion(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long productId,
            @Valid @RequestBody CreateSuggestionRequest request) {

        suggestionService.createSuggestion(currentUser.getId(), productId, request);
        return ResponseEntity.ok(new ApiResponse(true, "Suggestion sent successfully"));
    }
}
