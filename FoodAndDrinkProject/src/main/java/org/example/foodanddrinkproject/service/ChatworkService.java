package org.example.foodanddrinkproject.service;

import org.example.foodanddrinkproject.dto.DashboardStatsDto;
import org.example.foodanddrinkproject.dto.OrderDto;

public interface ChatworkService {
    void sendOrderNotification(OrderDto order);
    void sendMonthlyStatistics(DashboardStatsDto stats, int month, int year);
}