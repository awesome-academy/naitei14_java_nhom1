package org.example.foodanddrinkproject.service.impl;
import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.dto.OrderItemDto;
import org.example.foodanddrinkproject.dto.PlaceOrderRequest;
import org.example.foodanddrinkproject.entity.*;
import org.example.foodanddrinkproject.enums.CartStatus;
import org.example.foodanddrinkproject.enums.OrderStatus;
import org.example.foodanddrinkproject.enums.PaymentStatus;
import org.example.foodanddrinkproject.exception.BadRequestException;
import org.example.foodanddrinkproject.exception.ResourceNotFoundException;
import org.example.foodanddrinkproject.repository.CartRepository;
import org.example.foodanddrinkproject.repository.OrderRepository;
import org.example.foodanddrinkproject.repository.ProductRepository;
import org.example.foodanddrinkproject.repository.UserRepository;
import org.example.foodanddrinkproject.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartRepository cartRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OrderDto placeOrder(Long userId, PlaceOrderRequest request) {
        // 1. Get User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 2. Get ACTIVE Cart
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("No active cart found. Please add items to cart first."));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Cannot place order.");
        }

        // 3. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING); // Default status

        // 4. Process Items (With Stock Locking)
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            // A. Fetch Product with LOCK to prevent race conditions
            Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", cartItem.getProduct().getId()));

            // B. Check Stock
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Sorry, product '" + product.getName() +
                        "' is out of stock. Available: " + product.getStockQuantity());
            }

            // C. Deduct Stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // D. Create Order Item
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());

            // E. Price Logic (Use discount if available)
            BigDecimal effectivePrice = product.getDiscountPrice() != null
                    ? product.getDiscountPrice() : product.getPrice();
            orderItem.setPriceAtPurchase(effectivePrice); // Snapshot!

            order.addItem(orderItem);

            // F. Calculate Subtotal
            subtotal = subtotal.add(effectivePrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // 5. Final Calculations
        order.setSubtotal(subtotal);
        order.setShippingCost(BigDecimal.valueOf(5.00)); // Fixed shipping fee for now
        order.setDiscountAmount(BigDecimal.ZERO); // No coupons yet

        BigDecimal total = subtotal.add(order.getShippingCost()).subtract(order.getDiscountAmount());
        order.setTotalAmount(total);

        // 6. Save Order
        Order savedOrder = orderRepository.save(order);

        // 7. Close Cart (Mark as Checked Out)
        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);

        return convertToDto(savedOrder);
    }

    @Override
    public List<OrderDto> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to view this order.");
        }
        return convertToDto(order);
    }

    // --- Helper ---
    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getCreatedAt());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setSubtotal(order.getSubtotal());
        dto.setShippingCost(order.getShippingCost());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTransactionId(order.getTransactionId());

        List<OrderItemDto> itemDtos = order.getItems().stream().map(item -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setProductImageUrl(item.getProduct().getImageUrl());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setPriceAtPurchase(item.getPriceAtPurchase());
            itemDto.setSubtotal(item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())));
            return itemDto;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
}