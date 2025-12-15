package org.example.foodanddrinkproject.service.impl;

import org.example.foodanddrinkproject.dto.DashboardStatsDto;
import org.example.foodanddrinkproject.repository.OrderRepository;
import org.example.foodanddrinkproject.repository.ProductRepository;
import org.example.foodanddrinkproject.repository.UserRepository;
import org.example.foodanddrinkproject.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
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
        
        // 4. Get top 5 selling products (all time)
        stats.setTopProducts(getTopProducts(5));
        
        // 5. Get top 5 customers (all time)
        stats.setTopCustomers(getTopCustomers(5));
        
        logger.info("📊 Dashboard Stats - Revenue: {}, Completed Orders: {}/{}", 
            stats.getTotalRevenue(), stats.getTotalCompletedOrders(), stats.getTotalOrders());

        return stats;
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