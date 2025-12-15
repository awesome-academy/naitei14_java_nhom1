package org.example.foodanddrinkproject.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DashboardStatsDto {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalCompletedOrders;
    private long totalProducts;
    private long totalUsers;
    
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
}