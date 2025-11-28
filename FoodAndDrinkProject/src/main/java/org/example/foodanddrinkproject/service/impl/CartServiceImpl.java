package org.example.foodanddrinkproject.service.impl;
import org.example.foodanddrinkproject.dto.AddItemToCartRequest;
import org.example.foodanddrinkproject.dto.CartDto;
import org.example.foodanddrinkproject.dto.CartItemDto;
import org.example.foodanddrinkproject.entity.Cart;
import org.example.foodanddrinkproject.entity.CartItem;
import org.example.foodanddrinkproject.entity.Product;
import org.example.foodanddrinkproject.entity.User;
import org.example.foodanddrinkproject.enums.CartStatus;
import org.example.foodanddrinkproject.exception.BadRequestException;
import org.example.foodanddrinkproject.exception.ResourceNotFoundException;
import org.example.foodanddrinkproject.repository.CartRepository;
import org.example.foodanddrinkproject.repository.ProductRepository;
import org.example.foodanddrinkproject.repository.UserRepository;
import org.example.foodanddrinkproject.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CartDto getMyCart(Long userId) {
        Cart cart = getActiveCartOrCreate(userId);
        return mapToDto(cart);
    }

    @Override
    @Transactional
    public CartDto addItemToCart(Long userId, AddItemToCartRequest request) {
        Cart cart = getActiveCartOrCreate(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.isActive()) {
            throw new BadRequestException("Product is not available.");
        }

        // Check if item exists in cart
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        int newQuantity = request.getQuantity();
        if (existingItemOpt.isPresent()) {
            newQuantity += existingItemOpt.get().getQuantity();
        }

        // Critical: Check Stock
        if (newQuantity > product.getStockQuantity()) {
            throw new BadRequestException("Not enough stock. Available: " + product.getStockQuantity());
        }

        if (existingItemOpt.isPresent()) {
            existingItemOpt.get().setQuantity(newQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(newQuantity);
            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Override
    @Transactional
    public CartDto decrementItemQuantity(Long userId, AddItemToCartRequest request) {
        Cart cart = getActiveCartOrCreate(userId);
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart", "productId", request.getProductId()));

        // --- THE FIX IS HERE ---
        // Instead of -1, we subtract the requested amount
        int amountToRemove = request.getQuantity();
        int newQuantity = cartItem.getQuantity() - amountToRemove;

        if (newQuantity <= 0) {
            // If the result is 0 or negative, remove the item entirely
            cart.removeItem(cartItem);
        } else {
            // Otherwise, update with the new lower value
            cartItem.setQuantity(newQuantity);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Override
    @Transactional
    public CartDto removeItem(Long userId, Long productId) {
        Cart cart = getActiveCartOrCreate(userId);
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found", "productId", productId));

        cart.removeItem(cartItem);
        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getActiveCartOrCreate(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // --- Helpers ---

    private Cart getActiveCartOrCreate(Long userId) {
        // Logic: Find the cart with status ACTIVE
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                });
    }

    private CartDto mapToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());

        List<CartItemDto> items = cart.getItems().stream().map(item -> {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setImageUrl(item.getProduct().getImageUrl());
            // Use discount price if available, else normal price
            BigDecimal unitPrice = item.getProduct().getDiscountPrice() != null
                    ? item.getProduct().getDiscountPrice()
                    : item.getProduct().getPrice();
            itemDto.setPrice(unitPrice);
            itemDto.setQuantity(item.getQuantity());
            itemDto.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            return itemDto;
        }).collect(Collectors.toList());

        dto.setItems(items);

        // Calculate Total
        BigDecimal total = items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalAmount(total);

        return dto;
    }
}