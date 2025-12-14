package org.example.foodanddrinkproject.service;

import org.example.foodanddrinkproject.dto.OrderDto;

public interface NotificationService {
    void sendOrderConfirmationEmail(OrderDto order);
    void sendOrderStatusUpdateEmail(OrderDto order, String oldStatus, String newStatus);
    void sendAdminNotification(OrderDto order);
}
