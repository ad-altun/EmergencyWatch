package de.denizaltun.analyticsservice.service;

import de.denizaltun.analyticsservice.repository.DailyFleetMetricsRepository;
import de.denizaltun.analyticsservice.repository.DailyVehicleMetricsRepository;
import de.denizaltun.analyticsservice.repository.VehicleTelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricsAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsAggregationService.class);

    private final VehicleTelemetryRepository telemetryRepository;
    private final DailyFleetMetricsRepository fleetMetricsRepository;
    private final DailyVehicleMetricsRepository vehicleMetricsRepository;

    public MetricsAggregationService(VehicleTelemetryRepository telemetryRepository,
                                     DailyFleetMetricsRepository fleetMetricsRepository,
                                     DailyVehicleMetricsRepository vehicleMetricsRepository) {
        this.telemetryRepository = telemetryRepository;
        this.fleetMetricsRepository = fleetMetricsRepository;
        this.vehicleMetricsRepository = vehicleMetricsRepository;
    }

    @Transactional(readOnly = true)
    public void aggregateMetricsForDate(LocalDate date) {
        logger.info("Starting metrics aggregation for date: {}", date);

        // 1. Aggregate fleet-wide metrics
        aggregateFleetMetrics(date);

        // 2. Aggregate per-vehicle metrics
        aggregateVehicleMetrics(date);

        logger.info("Completed metrics aggregation for date: {}", date);
    }

    private void aggregateFleetMetrics(LocalDate date) {
        logger.info("Aggregating fleet metrics for {}", date);

        // Check if already exists (Idempotence)
        if (fleetMetricsRepository.findByDate(date).isPresent()) {
            logger.info("Fleet metrics for {} already exist, skipping", date);
            return;
        }

        // PostgreSQL aggregation

        Integer totalVehicles = telemetryRepository.countDistinctVehiclesByDate(date);
        Double avgSpeed = telemetryRepository.calculateAverageSpeedByDate(date);
        Double totalFuel = telemetryRepository.calculateTotalFuelByDate(date);

        // Get average speed by vehicle type
        List<Object[]> speedByType = telemetryRepository.calculateAverageSpeedByTypeAndDate(date);
        Map<String, Double> speedByTypeMap = new HashMap<>();
        for (Object[] row : speedByType) {
            String type = (String) row[0];
            Double speed = (Double) row[1];
            speedByTypeMap.put(type, speed);
        }

        DailyFleetMetrics fleetMetrics = new DailyFleetMetrics(
                date, totalVehicles, avgSpeed, totalFuel, speedByTypeMap
        );

        // Try-Catch with logging
        try {
            fleetMetricsRepository.save(fleetMetrics);
            logger.info("Saved fleet metrics: {} vehicles, avg speed: {}", totalVehicles, avgSpeed);
        } catch (Exception e) {
            logger.error("Failed to save fleet metrics for {}: {}", date, e.getMessage(), e);
            throw e; // Re-throw damit der Job als "failed" markiert wird
        }
    }

    private void aggregateVehicleMetrics(LocalDate date) {
        logger.info("Aggregating vehicle metrics for {}", date);

        List<Object[]> vehicleData = telemetryRepository.calculateVehicleMetricsByDate(date);

        int savedCount = 0;
        int failedCount = 0;

        for (Object[] row : vehicleData) {
            String vehicleId = (String) row[0];

            // Check if already exists
            if (vehicleMetricsRepository.findByVehicleIdAndDate(vehicleId, date).isPresent()) {
                logger.debug("Metrics for vehicle {} on {} already exist, skipping", vehicleId, date);
                continue;
            }

            String vehicleType = (String) row[1];
            Double avgSpeed = (Double) row[2];
            Double maxSpeed = (Double) row[3];
            Double minSpeed = (Double) row[4];
            Double avgFuel = (Double) row[5];
            Double minFuel = (Double) row[6];
            Long totalPoints = (Long) row[7];

            DailyVehicleMetrics vehicleMetrics = new DailyVehicleMetrics(
                    vehicleId, date, vehicleType,
                    avgSpeed, maxSpeed, minSpeed,
                    avgFuel, minFuel, totalPoints.intValue()
            );

            // Try-Catch for each vehicle
            try {
                vehicleMetricsRepository.save(vehicleMetrics);
                savedCount++;
            } catch (Exception e) {
                logger.error("Failed to save metrics for vehicle {} on {}: {}",
                        vehicleId, date, e.getMessage());
                failedCount++;
            }
        }

        logger.info("Saved metrics for {} vehicles", savedCount);

        if (failedCount > 0) {
            logger.warn("Some vehicle metrics failed to save. Manual intervention may be required.");
        }
    }
}
