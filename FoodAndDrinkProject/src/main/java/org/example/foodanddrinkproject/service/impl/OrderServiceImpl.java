package org.example.foodanddrinkproject.service.impl;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.example.foodanddrinkproject.dto.AdminUpdateOrderRequest;
import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.dto.OrderItemDto;
import org.example.foodanddrinkproject.dto.PlaceOrderRequest;
import org.example.foodanddrinkproject.entity.Cart;
import org.example.foodanddrinkproject.entity.CartItem;
import org.example.foodanddrinkproject.entity.Order;
import org.example.foodanddrinkproject.entity.OrderItem;
import org.example.foodanddrinkproject.entity.Product;
import org.example.foodanddrinkproject.entity.User;
import org.example.foodanddrinkproject.enums.CartStatus;
import org.example.foodanddrinkproject.enums.OrderStatus;
import org.example.foodanddrinkproject.enums.PaymentStatus;
import org.example.foodanddrinkproject.event.OrderPlacedEvent;
import org.example.foodanddrinkproject.event.OrderStatusChangedEvent;
import org.example.foodanddrinkproject.exception.BadRequestException;
import org.example.foodanddrinkproject.exception.ResourceNotFoundException;
import org.example.foodanddrinkproject.repository.CartRepository;
import org.example.foodanddrinkproject.repository.OrderRepository;
import org.example.foodanddrinkproject.repository.ProductRepository;
import org.example.foodanddrinkproject.repository.UserRepository;
import org.example.foodanddrinkproject.repository.specification.OrderSpecification;
import org.example.foodanddrinkproject.service.OrderService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartRepository cartRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderDto placeOrder(Long userId, PlaceOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("No active cart found. Please add items to cart first."));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Cannot place order.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", cartItem.getProduct().getId()));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Sorry, product '" + product.getName() +
                        "' is out of stock. Available: " + product.getStockQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());

            BigDecimal effectivePrice = product.getDiscountPrice() != null
                    ? product.getDiscountPrice() : product.getPrice();
            orderItem.setPriceAtPurchase(effectivePrice); 

            order.addItem(orderItem);

            subtotal = subtotal.add(effectivePrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setSubtotal(subtotal);
        order.setShippingCost(BigDecimal.valueOf(5.00));
        order.setDiscountAmount(BigDecimal.ZERO);

        BigDecimal total = subtotal.add(order.getShippingCost()).subtract(order.getDiscountAmount());
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);

        OrderDto orderDto = convertToDto(savedOrder);

        eventPublisher.publishEvent(new OrderPlacedEvent(this, orderDto));

        return orderDto;
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

    @Override
    public Page<OrderDto> getAllOrders(OrderStatus status, Long userId, Pageable pageable) {
        Specification<Order> spec =
                OrderSpecification.hasStatus(status)
                .and(OrderSpecification.hasUserId(userId));

        return orderRepository.findAll(spec, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public OrderDto updateOrder(Long orderId, AdminUpdateOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus oldStatus = order.getOrderStatus();
        boolean statusChanged = false;

        if (request.getOrderStatus() != null) {
            OrderStatus newStatus = request.getOrderStatus();

            if (newStatus == OrderStatus.COMPLETED && request.getPaymentStatus() == null) {
                order.setPaymentStatus(PaymentStatus.PAID);
            }
            
            if (!oldStatus.equals(newStatus)) {
                order.setOrderStatus(newStatus);
                statusChanged = true;
            }
        }

        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
        }

        Order savedOrder = orderRepository.save(order);
        OrderDto orderDto = convertToDto(savedOrder);
        
        // Publish event if status changed
        if (statusChanged) {
            eventPublisher.publishEvent(
                new OrderStatusChangedEvent(this, orderDto, 
                    oldStatus.toString(), 
                    savedOrder.getOrderStatus().toString())
            );
        }
        
        return orderDto;
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) { 
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getCreatedAt());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setUserEmail(order.getUser().getEmail());
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