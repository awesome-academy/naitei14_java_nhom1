package org.example.foodanddrinkproject.event;

import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.service.ChatworkService;
import org.example.foodanddrinkproject.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    private final ChatworkService chatworkService;
    private final NotificationService notificationService;

    public OrderEventListener(ChatworkService chatworkService, 
                            NotificationService notificationService) {
        this.chatworkService = chatworkService;
        this.notificationService = notificationService;
    }

    @Async("asyncExecutor")
    @EventListener
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        logger.info("OrderPlacedEvent received. Processing notifications asynchronously...");

        OrderDto order = event.getOrderDto();
        
        try {
            // Send order confirmation email to customer
            notificationService.sendOrderConfirmationEmail(order);
            
            // Send Chatwork notification to admin
            chatworkService.sendOrderNotification(order);
            
            // Send admin notification
            notificationService.sendAdminNotification(order);
            
            logger.info("All notifications for Order ID: {} sent successfully", order.getId());
        } catch (Exception e) {
            logger.error("Error processing notifications for Order ID: {}", order.getId(), e);
        }
    }
    
    @Async("asyncExecutor")
    @EventListener
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        logger.info("OrderStatusChangedEvent received. Order ID: {}, Status changed from {} to {}", 
                   event.getOrderDto().getId(), event.getOldStatus(), event.getNewStatus());

        OrderDto order = event.getOrderDto();
        
        try {
            // Send order status update email to customer
            notificationService.sendOrderStatusUpdateEmail(order, event.getOldStatus(), event.getNewStatus());
            
            logger.info("Status update notification for Order ID: {} sent successfully", order.getId());
        } catch (Exception e) {
            logger.error("Error sending status update notification for Order ID: {}", order.getId(), e);
        }
    }
}
