package de.denizaltun.analyticsservice.service;

import de.denizaltun.analyticsservice.entity.DailyFleetMetrics;
import de.denizaltun.analyticsservice.entity.DailyVehicleMetrics;
import de.denizaltun.analyticsservice.repository.DailyFleetMetricsRepository;
import de.denizaltun.analyticsservice.repository.DailyVehicleMetricsRepository;
import de.denizaltun.analyticsservice.repository.VehicleTelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Transactional
    public void aggregateMetricsForDate(LocalDate date) {
        logger.info("Starting metrics aggregation for date: {}", date);

        // Check idempotency
        if (fleetMetricsRepository.findByDate(date).isPresent()) {
            logger.info("Metrics for {} already exist, skipping", date);
            return;
        }

        // 1. Calculate fuel consumption by vehicle
        LocalDate bufferDate = date.minusDays(2);
        List<Object[]> fuelData = telemetryRepository.calculateFuelConsumptionByVehicle(date, date, bufferDate);

        // 2. Get vehicle metrics
        List<Object[]> vehicleData = telemetryRepository.calculateVehicleMetricsByDate(date);

        // Create map of fuel consumption by vehicle ID
        Map<String, Double> fuelByVehicle = new HashMap<>();
        for (Object[] row : fuelData) {
            String vehicleId = (String) row[0];
            Double fuelConsumed = (Double) row[2];
            fuelByVehicle.put(vehicleId, fuelConsumed);
        }

        // Calculate total daily fuel from the map (each vehicle counted once)
        double totalDailyFuel = fuelByVehicle.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // 3. Create and save vehicle metrics
        // Track which vehicles we've already assigned fuel to (avoid double-counting in per-vehicle metrics)
        Set<String> vehiclesWithFuelAssigned = new HashSet<>();
        List<DailyVehicleMetrics> vehicleMetricsEntities = new ArrayList<>();

        for (Object[] row : vehicleData) {
            String vehicleId = (String) row[0];
            String vehicleStatus = (String) row[1];
            String vehicleType = (String) row[2];
            Double avgSpeed = (Double) row[3];
            Double maxSpeed = (Double) row[4];
            Double minSpeed = (Double) row[5];
            Double avgFuel = (Double) row[6];
            Double minFuel = (Double) row[7];
            Long totalPoints = (Long) row[8];

            // Only assign fuel to first entry for each vehicle (avoid duplicates)
            Double fuelConsumed = 0.0;
            if (!vehiclesWithFuelAssigned.contains(vehicleId)) {
                fuelConsumed = fuelByVehicle.getOrDefault(vehicleId, 0.0);
                vehiclesWithFuelAssigned.add(vehicleId);
            }

            DailyVehicleMetrics vm = new DailyVehicleMetrics(
                    vehicleId, date, vehicleStatus, vehicleType,
                    avgSpeed, maxSpeed, minSpeed, avgFuel, minFuel,
                    fuelConsumed, totalPoints.intValue()
            );
            vehicleMetricsEntities.add(vm);
        }

        vehicleMetricsRepository.saveAll(vehicleMetricsEntities);

        // 4. Calculate fleet metrics
        Integer totalVehicles = telemetryRepository.countDistinctVehiclesByDate(date);
        Double avgSpeed = telemetryRepository.calculateAverageSpeedByDate(date);

        // Get average speed by vehicle status
        List<Object[]> speedByStatus = telemetryRepository.calculateAverageSpeedByStatusAndDate(date);
        Map<String, Double> speedByStatusMap = new HashMap<>();
        for (Object[] row : speedByStatus) {
            speedByStatusMap.put((String) row[0], (Double) row[1]);
        }

        // Get average speed by vehicle type
        List<Object[]> speedByType = telemetryRepository.calculateAverageSpeedByTypeAndDate(date);
        Map<String, Double> speedByTypeMap = new HashMap<>();
        for (Object[] row : speedByType) {
            speedByTypeMap.put((String) row[0], (Double) row[1]);
        }

        // 5. Create and save fleet metrics
        DailyFleetMetrics fleetMetric = new DailyFleetMetrics(
                date, totalVehicles, avgSpeed, totalDailyFuel,
                speedByStatusMap, speedByTypeMap
        );

        fleetMetricsRepository.save(fleetMetric);

        logger.info("Completed metrics aggregation for date: {} ({} vehicles, {:.1f}L fuel)",
                date, totalVehicles, totalDailyFuel);
    }

}
