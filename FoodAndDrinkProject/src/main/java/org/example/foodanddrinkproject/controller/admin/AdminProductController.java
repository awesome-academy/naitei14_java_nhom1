package org.example.foodanddrinkproject.controller.admin;

import jakarta.validation.Valid;
import org.example.foodanddrinkproject.dto.ApiResponse;
import org.example.foodanddrinkproject.dto.ProductDto;
import org.example.foodanddrinkproject.dto.ProductRequest;
import org.example.foodanddrinkproject.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductDto createdProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductDto updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully"));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ProductDto> toggleProductStatus(@PathVariable Long id) {
        ProductDto product = productService.toggleProductStatus(id);
        return ResponseEntity.ok(product);
    }

}
