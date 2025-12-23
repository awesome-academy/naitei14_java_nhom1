package org.example.foodanddrinkproject.service.impl;

import org.example.foodanddrinkproject.dto.DashboardStatsDto;
import org.example.foodanddrinkproject.entity.Product;
import org.example.foodanddrinkproject.entity.Rating;
import org.example.foodanddrinkproject.enums.OrderStatus;
import org.example.foodanddrinkproject.repository.OrderRepository;
import org.example.foodanddrinkproject.repository.ProductRepository;
import org.example.foodanddrinkproject.repository.RatingRepository;
import org.example.foodanddrinkproject.repository.UserRepository;
import org.example.foodanddrinkproject.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);
    private static final int LOW_STOCK_THRESHOLD = 10;
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    public DashboardServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, 
                                UserRepository userRepository, RatingRepository ratingRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public DashboardStatsDto getStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        // 1. Count totals
        stats.setTotalProducts(productRepository.count());
        stats.setTotalUsers(userRepository.count());
        stats.setTotalOrders(orderRepository.count());

        // 2. Calculate Revenue (ONLY from COMPLETED orders)
        BigDecimal revenue = orderRepository.sumTotalAmount();
        stats.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
        
        // 3. Count completed orders
        Long completedOrders = orderRepository.countCompletedOrders();
        stats.setTotalCompletedOrders(completedOrders != null ? completedOrders : 0L);
        
        // 4. Order status breakdown
        stats.setPendingOrders(countOrdersByStatus(OrderStatus.PENDING));
        stats.setProcessingOrders(countOrdersByStatus(OrderStatus.PROCESSING));
        stats.setCancelledOrders(countOrdersByStatus(OrderStatus.CANCELLED));
        
        // 5. Low stock products (threshold: 10)
        stats.setLowStockProducts(getLowStockProducts(5));
        
        // 6. Daily stats for last 7 days (for chart)
        stats.setDailyStats(getDailyStats(7));
        
        // 7. Recent 5 reviews
        stats.setRecentReviews(getRecentReviews(5));
        
        // 8. Get top 5 selling products (all time)
        stats.setTopProducts(getTopProducts(5));
        
        // 9. Get top 5 customers (all time)
        stats.setTopCustomers(getTopCustomers(5));
        
        logger.info("📊 Dashboard Stats - Revenue: {}, Completed Orders: {}/{}", 
            stats.getTotalRevenue(), stats.getTotalCompletedOrders(), stats.getTotalOrders());

        return stats;
    }
    
    private long countOrdersByStatus(OrderStatus status) {
        Long count = orderRepository.countByOrderStatus(status);
        return count != null ? count : 0L;
    }
    
    private List<DashboardStatsDto.LowStockProductDto> getLowStockProducts(int limit) {
        try {
            List<Product> products = productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD, PageRequest.of(0, limit));
            return products.stream().map(p -> {
                DashboardStatsDto.LowStockProductDto dto = new DashboardStatsDto.LowStockProductDto();
                dto.setId(p.getId());
                dto.setName(p.getName());
                dto.setStockQuantity(p.getStockQuantity());
                dto.setSku(p.getSku());
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching low stock products", e);
            return new ArrayList<>();
        }
    }
    
    private List<DashboardStatsDto.DailyStatsDto> getDailyStats(int days) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            List<Object[]> results = orderRepository.findDailyStats(startDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
            
            return results.stream().map(row -> {
                DashboardStatsDto.DailyStatsDto dto = new DashboardStatsDto.DailyStatsDto();
                // Handle different date types from database
                Object dateObj = row[0];
                if (dateObj instanceof java.sql.Date) {
                    dto.setDate(((java.sql.Date) dateObj).toLocalDate().format(formatter));
                } else if (dateObj instanceof LocalDate) {
                    dto.setDate(((LocalDate) dateObj).format(formatter));
                } else {
                    dto.setDate(dateObj.toString());
                }
                dto.setOrderCount(((Number) row[1]).longValue());
                dto.setRevenue((BigDecimal) row[2]);
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching daily stats", e);
            return new ArrayList<>();
        }
    }
    
    private List<DashboardStatsDto.RecentReviewDto> getRecentReviews(int limit) {
        try {
            List<Rating> ratings = ratingRepository.findRecentRatings(PageRequest.of(0, limit));
            return ratings.stream().map(r -> {
                DashboardStatsDto.RecentReviewDto dto = new DashboardStatsDto.RecentReviewDto();
                dto.setProductId(r.getProduct().getId());
                dto.setProductName(r.getProduct().getName());
                dto.setCustomerName(r.getUser().getFullName() != null ? r.getUser().getFullName() : r.getUser().getEmail());
                dto.setRating(r.getRatingValue());
                dto.setComment(r.getComment());
                dto.setCreatedAt(r.getCreatedAt());
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching recent reviews", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public DashboardStatsDto getMonthlyStats(int month, int year) {
        DashboardStatsDto stats = new DashboardStatsDto();

        // 1. Count totals (all time - for reference)
        stats.setTotalProducts(productRepository.count());
        stats.setTotalUsers(userRepository.count());
        
        // 2. Calculate Revenue (ONLY from COMPLETED orders IN THIS MONTH)
        BigDecimal revenue = orderRepository.sumTotalAmountByMonth(month, year);
        stats.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
        
        // 3. Count orders in this month
        Long totalOrdersThisMonth = orderRepository.countOrdersByMonth(month, year);
        stats.setTotalOrders(totalOrdersThisMonth != null ? totalOrdersThisMonth : 0L);
        
        Long completedOrdersThisMonth = orderRepository.countCompletedOrdersByMonth(month, year);
        stats.setTotalCompletedOrders(completedOrdersThisMonth != null ? completedOrdersThisMonth : 0L);
        
        // 4. Get top 5 selling products IN THIS MONTH
        stats.setTopProducts(getTopProductsByMonth(month, year, 5));
        
        // 5. Get top 5 customers IN THIS MONTH
        stats.setTopCustomers(getTopCustomersByMonth(month, year, 5));
        
        logger.info("📊 Monthly Stats ({}/{}) - Revenue: {}, Completed Orders: {}/{}", 
            month, year, stats.getTotalRevenue(), stats.getTotalCompletedOrders(), stats.getTotalOrders());

        return stats;
    }
    
    private List<DashboardStatsDto.TopProductDto> getTopProducts(int limit) {
        try {
            List<Object[]> results = orderRepository.findTopSellingProducts(PageRequest.of(0, limit));
            
            return results.stream().map(row -> {
                DashboardStatsDto.TopProductDto dto = new DashboardStatsDto.TopProductDto();
                dto.setProductName((String) row[0]);
                dto.setQuantitySold(((Number) row[1]).longValue());
                dto.setRevenue((BigDecimal) row[2]);
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching top products", e);
            return new ArrayList<>();
        }
    }
    
    private List<DashboardStatsDto.TopCustomerDto> getTopCustomers(int limit) {
        try {
            List<Object[]> results = orderRepository.findTopCustomers(PageRequest.of(0, limit));
            
            return results.stream().map(row -> {
                DashboardStatsDto.TopCustomerDto dto = new DashboardStatsDto.TopCustomerDto();
                dto.setCustomerName((String) row[0]);
                dto.setCustomerEmail((String) row[1]);
                dto.setOrderCount(((Number) row[2]).longValue());
                dto.setTotalSpent((BigDecimal) row[3]);
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching top customers", e);
            return new ArrayList<>();
        }
    }
    
    private List<DashboardStatsDto.TopProductDto> getTopProductsByMonth(int month, int year, int limit) {
        try {
            List<Object[]> results = orderRepository.findTopSellingProductsByMonth(month, year, PageRequest.of(0, limit));
            
            return results.stream().map(row -> {
                DashboardStatsDto.TopProductDto dto = new DashboardStatsDto.TopProductDto();
                dto.setProductName((String) row[0]);
                dto.setQuantitySold(((Number) row[1]).longValue());
                dto.setRevenue((BigDecimal) row[2]);
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching top products by month", e);
            return new ArrayList<>();
        }
    }
    
    private List<DashboardStatsDto.TopCustomerDto> getTopCustomersByMonth(int month, int year, int limit) {
        try {
            List<Object[]> results = orderRepository.findTopCustomersByMonth(month, year, PageRequest.of(0, limit));
            
            return results.stream().map(row -> {
                DashboardStatsDto.TopCustomerDto dto = new DashboardStatsDto.TopCustomerDto();
                dto.setCustomerName((String) row[0]);
                dto.setCustomerEmail((String) row[1]);
                dto.setOrderCount(((Number) row[2]).longValue());
                dto.setTotalSpent((BigDecimal) row[3]);
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching top customers by month", e);
            return new ArrayList<>();
        }
    }
}