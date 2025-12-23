package org.example.foodanddrinkproject.controller.api;

import org.example.foodanddrinkproject.dto.AddItemToCartRequest;
import org.example.foodanddrinkproject.dto.ApiResponse;
import org.example.foodanddrinkproject.dto.CartDto;
import org.example.foodanddrinkproject.security.CurrentUser;
import org.example.foodanddrinkproject.security.UserPrincipal;
import org.example.foodanddrinkproject.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getMyCart(@CurrentUser UserPrincipal userPrincipal) {
        return ResponseEntity.ok(cartService.getMyCart(userPrincipal.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(@CurrentUser UserPrincipal userPrincipal,
                                           @Valid @RequestBody AddItemToCartRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(userPrincipal.getId(), request));
    }

    @PostMapping("/items/reduce")
    public ResponseEntity<CartDto> reduceItemQuantity(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody AddItemToCartRequest request) { // Reuse the DTO

        return ResponseEntity.ok(cartService.decrementItemQuantity(userPrincipal.getId(), request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItem(@CurrentUser UserPrincipal userPrincipal,
                                              @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(userPrincipal.getId(), productId));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(@CurrentUser UserPrincipal userPrincipal) {
        cartService.clearCart(userPrincipal.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Cart cleared successfully"));
    }
}