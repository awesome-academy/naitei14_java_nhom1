package org.example.foodanddrinkproject.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DashboardStatsDto {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalCompletedOrders;
    private long totalProducts;
    private long totalUsers;
    
    // Order status breakdown
    private long pendingOrders;
    private long processingOrders;
    private long cancelledOrders;
    
    // Low stock products
    private List<LowStockProductDto> lowStockProducts;
    
    // 7-day trend data for charts
    private List<DailyStatsDto> dailyStats;
    
    // Recent reviews
    private List<RecentReviewDto> recentReviews;
    
    // Top selling products
    private List<TopProductDto> topProducts;
    
    // Top customers
    private List<TopCustomerDto> topCustomers;
    
    @Getter
    @Setter
    public static class TopProductDto {
        private String productName;
        private Long quantitySold;
        private BigDecimal revenue;
    }
    
    @Getter
    @Setter
    public static class TopCustomerDto {
        private String customerName;
        private String customerEmail;
        private Long orderCount;
        private BigDecimal totalSpent;
    }
    
    @Getter
    @Setter
    public static class LowStockProductDto {
        private Long id;
        private String name;
        private int stockQuantity;
        private String sku;
    }
    
    @Getter
    @Setter
    public static class DailyStatsDto {
        private String date;
        private long orderCount;
        private BigDecimal revenue;
    }
    
    @Getter
    @Setter
    public static class RecentReviewDto {
        private Long productId;
        private String productName;
        private String customerName;
        private int rating;
        private String comment;
        private LocalDateTime createdAt;
    }
}