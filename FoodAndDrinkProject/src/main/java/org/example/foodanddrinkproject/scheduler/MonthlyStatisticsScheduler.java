package org.example.foodanddrinkproject.scheduler;

import org.example.foodanddrinkproject.dto.DashboardStatsDto;
import org.example.foodanddrinkproject.service.ChatworkService;
import org.example.foodanddrinkproject.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduler for sending monthly statistics to Chatwork
 * Runs automatically at the end of each month
 */
@Component
public class MonthlyStatisticsScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MonthlyStatisticsScheduler.class);

    private final ChatworkService chatworkService;
    private final DashboardService dashboardService;

    public MonthlyStatisticsScheduler(ChatworkService chatworkService, DashboardService dashboardService) {
        this.chatworkService = chatworkService;
        this.dashboardService = dashboardService;
    }

    /**
     * Scheduled task to send monthly statistics
     * Runs on the last day of every month at 23:00 (11:00 PM)
     * Cron expression: "0 0 23 L * ?" means:
     * - 0: second 0
     * - 0: minute 0
     * - 23: hour 23 (11 PM)
     * - L: last day of month
     * - *: every month
     * - ?: any day of week
     */
    @Scheduled(cron = "0 0 23 L * ?")
    public void sendMonthlyStatistics() {
        try {
            logger.info("Starting monthly statistics scheduler...");

            LocalDateTime now = LocalDateTime.now();
            int month = now.getMonthValue();
            int year = now.getYear();

            // Get monthly statistics (ONLY for this month)
            DashboardStatsDto stats = dashboardService.getMonthlyStats(month, year);

            // Send to Chatwork
            chatworkService.sendMonthlyStatistics(stats, month, year);

            logger.info("Monthly statistics sent successfully for {}/{}", month, year);

        } catch (Exception e) {
            logger.error("Error occurred while sending monthly statistics", e);
        }
    }

    /**
     * Manual trigger method for testing purposes
     * Can be called from a controller to test the functionality
     */
    public void sendStatisticsManually() {
        logger.info("Manual trigger for monthly statistics");
        sendMonthlyStatistics();
    }
}
