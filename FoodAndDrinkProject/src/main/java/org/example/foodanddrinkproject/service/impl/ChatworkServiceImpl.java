package org.example.foodanddrinkproject.service.impl;

import org.example.foodanddrinkproject.dto.DashboardStatsDto;
import org.example.foodanddrinkproject.dto.OrderDto;
import org.example.foodanddrinkproject.service.ChatworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatworkServiceImpl implements ChatworkService {

    private static final Logger logger = LoggerFactory.getLogger(ChatworkServiceImpl.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.chatwork.api-token}")
    private String apiToken;

    @Value("${app.chatwork.room-id}")
    private String roomId;

    @Value("${app.chatwork.api-url}")
    private String apiUrl;

    @Override
    public void sendOrderNotification(OrderDto order) {
        try {
            String url = apiUrl + "/rooms/" + roomId + "/messages";
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-ChatWorkToken", apiToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String messageContent = String.format(
                    "[info][title]🛒 New Order Received! #%d[/title]" +
                            "Total Amount: $%.2f\n" +
                            "Payment Method: %s\n" +
                            "Status: %s[/info]",
                    order.getId(),
                    order.getTotalAmount(),
                    order.getPaymentMethod(),
                    order.getOrderStatus()
            );

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("body", messageContent);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            restTemplate.postForObject(url, request, String.class);

            logger.info("Chatwork notification sent for Order ID: {}", order.getId());

        } catch (Exception e) {
            logger.error("Failed to send Chatwork message", e);
        }
    }

    @Override
    public void sendMonthlyStatistics(DashboardStatsDto stats, int month, int year) {
        try {
            String url = apiUrl + "/rooms/" + roomId + "/messages";
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-ChatWorkToken", apiToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Build message with enhanced statistics
            StringBuilder messageBuilder = new StringBuilder();
            
            // Header
            messageBuilder.append(String.format(
                "[info][title]📊 Monthly Statistics Report - %02d/%d[/title]", month, year));
            messageBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // Revenue section (ONLY from COMPLETED orders IN THIS MONTH)
            messageBuilder.append(String.format(
                "💰 Revenue (This Month): $%,.2f\n", stats.getTotalRevenue()));
            messageBuilder.append(String.format(
                "📦 Orders (This Month): %,d (Completed: %,d)\n", 
                stats.getTotalOrders(), stats.getTotalCompletedOrders()));
            messageBuilder.append(String.format(
                "🛍️ Total Products: %,d\n", stats.getTotalProducts()));
            messageBuilder.append(String.format(
                "👥 Total Users: %,d\n", stats.getTotalUsers()));
            
            // Top selling products (IN THIS MONTH)
            if (stats.getTopProducts() != null && !stats.getTopProducts().isEmpty()) {
                messageBuilder.append("\n🏆 TOP PRODUCTS (This Month):\n");
                int rank = 1;
                for (DashboardStatsDto.TopProductDto product : stats.getTopProducts()) {
                    messageBuilder.append(String.format(
                        "   %d. %s - %,d sold ($%,.2f)\n",
                        rank++, product.getProductName(), 
                        product.getQuantitySold(), product.getRevenue()));
                }
            }
            
            // Top customers (IN THIS MONTH)
            if (stats.getTopCustomers() != null && !stats.getTopCustomers().isEmpty()) {
                messageBuilder.append("\n⭐ TOP CUSTOMERS (This Month):\n");
                int rank = 1;
                for (DashboardStatsDto.TopCustomerDto customer : stats.getTopCustomers()) {
                    messageBuilder.append(String.format(
                        "   %d. %s - %,d orders ($%,.2f)\n",
                        rank++, customer.getCustomerName(), 
                        customer.getOrderCount(), customer.getTotalSpent()));
                }
            }
            
            // Footer
            messageBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            messageBuilder.append(String.format(
                "Generated: %s[/info]",
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("body", messageBuilder.toString());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            restTemplate.postForObject(url, request, String.class);

            logger.info("Monthly statistics sent to Chatwork for {}/{}", month, year);

        } catch (Exception e) {
            logger.error("Failed to send monthly statistics to Chatwork", e);
        }
    }
}