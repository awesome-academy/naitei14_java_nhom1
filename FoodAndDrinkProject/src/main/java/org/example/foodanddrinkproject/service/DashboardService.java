package org.example.foodanddrinkproject.service;

import org.example.foodanddrinkproject.dto.DashboardStatsDto;

public interface DashboardService {
    DashboardStatsDto getStats();
    DashboardStatsDto getMonthlyStats(int month, int year);
}