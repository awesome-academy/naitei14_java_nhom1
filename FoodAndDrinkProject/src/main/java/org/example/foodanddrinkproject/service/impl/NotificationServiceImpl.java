package org.example.foodanddrinkproject.service.impl;

import java.time.format.DateTimeFormatter;

import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.dto.OrderItemDto;
import org.example.foodanddrinkproject.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@foodanddrink.com}")
    private String fromEmail;
    
    @Value("${app.admin.email:admin@foodanddrink.com}")
    private String adminEmail;

    public NotificationServiceImpl(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
        if (mailSender == null) {
            logger.warn("JavaMailSender not configured - email notifications will be disabled");
        }
    }

    @Async("asyncExecutor")
    @Override
    public void sendOrderConfirmationEmail(OrderDto order) {
        if (mailSender == null) {
            logger.info("Email disabled - skipping order confirmation email for Order ID: {}", order.getId());
            return;
        }
        try {
            logger.info("Sending order confirmation email asynchronously to user: {}", order.getUserEmail());
            
            String emailContent = buildOrderConfirmationEmail(order);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(order.getUserEmail());
            message.setSubject("Order Confirmation - Order #" + order.getId());
            message.setText(emailContent);
            
            mailSender.send(message);
            
            logger.info("Order confirmation email sent successfully to {}", order.getUserEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send order confirmation email for Order ID: {}", order.getId(), e);
        }
    }

    @Async("asyncExecutor")
    @Override
    public void sendOrderStatusUpdateEmail(OrderDto order, String oldStatus, String newStatus) {
        if (mailSender == null) {
            logger.info("Email disabled - skipping status update email for Order ID: {}", order.getId());
            return;
        }
        try {
            logger.info("Sending order status update email asynchronously to user: {}", order.getUserEmail());
            
            String emailContent = buildOrderStatusUpdateEmail(order, oldStatus, newStatus);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(order.getUserEmail());
            message.setSubject("Order Status Update - Order #" + order.getId());
            message.setText(emailContent);
            
            mailSender.send(message);
            
            logger.info("Order status update email sent successfully to {}", order.getUserEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send status update email for Order ID: {}", order.getId(), e);
        }
    }

    @Async("asyncExecutor")
    @Override
    public void sendAdminNotification(OrderDto order) {
        if (mailSender == null) {
            logger.info("Email disabled - skipping admin notification for Order ID: {}", order.getId());
            return;
        }
        try {
            logger.info("Sending admin notification email asynchronously for Order ID: {}", order.getId());
            
            String notificationContent = buildAdminNotification(order);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("🛒 New Order Alert - Order #" + order.getId());
            message.setText(notificationContent);
            
            mailSender.send(message);
            
            logger.info("Admin notification email sent successfully for Order ID: {}", order.getId());
            
        } catch (Exception e) {
            logger.error("Failed to send admin notification email for Order ID: {}", order.getId(), e);
        }
    }

    private String buildOrderConfirmationEmail(OrderDto order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(order.getUserEmail()).append(",\n\n");
        sb.append("Thank you for your order!\n\n");
        sb.append("Order Details:\n");
        sb.append("Order ID: #").append(order.getId()).append("\n");
        if (order.getOrderDate() != null) {
            sb.append("Order Date: ").append(order.getOrderDate().format(DATE_FORMATTER)).append("\n");
        }
        sb.append("Status: ").append(order.getOrderStatus()).append("\n");
        sb.append("Payment Method: ").append(order.getPaymentMethod()).append("\n");
        sb.append("Shipping Address: ").append(order.getShippingAddress()).append("\n\n");
        
        sb.append("Items:\n");
        for (OrderItemDto item : order.getItems()) {
            sb.append("- ").append(item.getProductName())
              .append(" x").append(item.getQuantity())
              .append(" - $").append(item.getPriceAtPurchase())
              .append("\n");
        }
        
        sb.append("\nSubtotal: $").append(order.getSubtotal()).append("\n");
        sb.append("Shipping: $").append(order.getShippingCost()).append("\n");
        sb.append("Discount: -$").append(order.getDiscountAmount()).append("\n");
        sb.append("Total: $").append(order.getTotalAmount()).append("\n\n");
        
        sb.append("We will send you another email when your order is shipped.\n\n");
        sb.append("Best regards,\n");
        sb.append("Food & Drink Project Team");
        
        return sb.toString();
    }

    private String buildOrderStatusUpdateEmail(OrderDto order, String oldStatus, String newStatus) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear Customer,\n\n");
        sb.append("Your order status has been updated!\n\n");
        sb.append("Order ID: #").append(order.getId()).append("\n");
        sb.append("Previous Status: ").append(oldStatus).append("\n");
        sb.append("New Status: ").append(newStatus).append("\n");
        if (order.getUpdatedAt() != null) {
            sb.append("Updated At: ").append(order.getUpdatedAt().format(DATE_FORMATTER)).append("\n\n");
        } else {
            sb.append("\n");
        }
        
        if ("SHIPPED".equals(newStatus)) {
            sb.append("Your order is on its way! You should receive it soon.\n");
        } else if ("DELIVERED".equals(newStatus)) {
            sb.append("Your order has been delivered! Enjoy your meal!\n");
        } else if ("CANCELLED".equals(newStatus)) {
            sb.append("Your order has been cancelled. If you did not request this, please contact us.\n");
        }
        
        sb.append("\nBest regards,\n");
        sb.append("Food & Drink Project Team");
        
        return sb.toString();
    }

    private String buildAdminNotification(OrderDto order) {
        StringBuilder sb = new StringBuilder();
        sb.append("🛒 New Order Alert!\n\n");
        sb.append("Order ID: #").append(order.getId()).append("\n");
        sb.append("Customer: ").append(order.getUserEmail()).append("\n");
        sb.append("Total Amount: $").append(order.getTotalAmount()).append("\n");
        sb.append("Payment Method: ").append(order.getPaymentMethod()).append("\n");
        sb.append("Payment Status: ").append(order.getPaymentStatus()).append("\n");
        sb.append("Items Count: ").append(order.getItems().size()).append("\n");
        if (order.getOrderDate() != null) {
            sb.append("Created At: ").append(order.getOrderDate().format(DATE_FORMATTER)).append("\n");
        }
        
        return sb.toString();
    }
}
