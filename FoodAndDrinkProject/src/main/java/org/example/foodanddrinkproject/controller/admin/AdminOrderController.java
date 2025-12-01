package org.example.foodanddrinkproject.controller.admin;

import org.example.foodanddrinkproject.dto.AdminUpdateOrderRequest;
import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.enums.OrderStatus;
import org.example.foodanddrinkproject.service.OrderService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    @GetMapping
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long userId,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(orderService.getAllOrders(status, userId, pageable));
    }


    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable Long id,
            @RequestBody AdminUpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }
}