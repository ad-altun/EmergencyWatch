package de.denizaltun.analyticsservice.scheduler;

import de.denizaltun.analyticsservice.service.MetricsAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailyAggregationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DailyAggregationScheduler.class);

    private final MetricsAggregationService metricsAggregationService;

    public DailyAggregationScheduler(MetricsAggregationService metricsAggregationService) {
        this.metricsAggregationService = metricsAggregationService;
    }

    /**
     * Runs daily at 7:00 AM (07:00:00).
     * Aggregates yesterday's telemetry data
     * <p>
     * Note: Scheduled at 7:00 AM to align with container scaling rules
     * (container is active 6:55 AM - 5:00 PM Mon-Fri)
     * <p>
     * Cron expression: "0 0 7 * * *"
     * - Second: 0
     * - Minute: 0
     * - Hour: 7 (7:00 AM)
     * - Day of month: * (every day)
     * - Month: * (every month)
     * - Day of week: * (every day)
     */
    @Scheduled(cron = "0 0 7 * * *", zone = "Europe/Berlin")
    public void aggregateYesterdayMetrics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        logger.info("=== STARTING DAILY AGGREGATION JOB ===");
        logger.info("Aggregating metrics for: {}", yesterday);

        long startTime = System.currentTimeMillis();

        try {
            metricsAggregationService.aggregateMetricsForDate(yesterday);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("=== DAILY AGGREGATION COMPLETED in {} ms ===", duration);

        } catch (Exception e) {
            logger.error("=== DAILY AGGREGATION FAILED ===", e);
            // In Production: hier könnte man einen Alert/Notification senden
        }
    }

    /**
     * Manual trigger for testing - runs aggregation for a specific date.
     * Useful for backfilling historical data or re-running failed aggregations.
     */
    public void triggerManualAggregation(LocalDate date) {
        logger.info("Manual aggregation triggered for: {}", date);
        metricsAggregationService.aggregateMetricsForDate(date);
    }
}
