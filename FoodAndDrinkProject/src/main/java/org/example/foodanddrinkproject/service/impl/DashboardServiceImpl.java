package org.example.foodanddrinkproject.service.impl;

import org.example.foodanddrinkproject.dto.DashboardStatsDto;
import org.example.foodanddrinkproject.repository.OrderRepository;
import org.example.foodanddrinkproject.repository.ProductRepository;
import org.example.foodanddrinkproject.repository.UserRepository;
import org.example.foodanddrinkproject.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardServiceImpl implements DashboardService {

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

        // 2. Calculate Revenue (Sum of COMPLETED orders)
        // We need a custom query for this in the Repo, or we can just sum ALL orders for now to keep it simple.
        // Let's add a custom query to OrderRepository for accuracy.
        BigDecimal revenue = orderRepository.sumTotalAmount();
        stats.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        return stats;
    }
}