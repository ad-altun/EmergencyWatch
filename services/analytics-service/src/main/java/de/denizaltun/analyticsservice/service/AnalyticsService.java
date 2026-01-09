package de.denizaltun.analyticsservice.service;

import de.denizaltun.analyticsservice.dto.HistoricalMetricsResponse;
import de.denizaltun.analyticsservice.dto.VehicleFuelConsumption;
import de.denizaltun.analyticsservice.dto.VehicleStatus;
import de.denizaltun.analyticsservice.dto.VehicleTelemetryMessage;
import de.denizaltun.analyticsservice.dto.VehicleType;
import de.denizaltun.analyticsservice.entity.DailyFleetMetrics;
import de.denizaltun.analyticsservice.entity.DailyVehicleMetrics;
import de.denizaltun.analyticsservice.entity.VehicleTelemetry;
import de.denizaltun.analyticsservice.model.FleetMetrics;
import de.denizaltun.analyticsservice.model.VehicleMetrics;
import de.denizaltun.analyticsservice.repository.mongo.DailyFleetMetricsRepository;
import de.denizaltun.analyticsservice.repository.mongo.DailyVehicleMetricsRepository;
import de.denizaltun.analyticsservice.repository.jpa.VehicleTelemetryRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Core analytics service that processes telemetry and maintains metrics.
 * Uses in-memory storage with thread-safe data structures.
 */
@Slf4j
@Service
public class AnalyticsService {

    // In-memory storage for vehicle metrics
    private final Map<String, VehicleMetrics> vehicleMetricsMap = new ConcurrentHashMap<>();

    // Track last known status for each vehicle (for fleet status updates)
    private final Map<String, VehicleStatus> lastKnownStatus = new ConcurrentHashMap<>();

    @Getter
    private final FleetMetrics fleetMetrics = new FleetMetrics();

    // constructor injection
    private final DailyFleetMetricsRepository fleetMetricsRepository;
    private final DailyVehicleMetricsRepository vehicleMetricsRepository;
    private final VehicleTelemetryRepository vehicleTelemetryRepository;

    public AnalyticsService(
            DailyFleetMetricsRepository fleetMetricsRepository,
            DailyVehicleMetricsRepository vehicleMetricsRepository,
            VehicleTelemetryRepository vehicleTelemetryRepository) {
        this.fleetMetricsRepository = fleetMetricsRepository;
        this.vehicleMetricsRepository = vehicleMetricsRepository;
        this.vehicleTelemetryRepository = vehicleTelemetryRepository;
    }

    /**
     * Process incoming telemetry message and update metrics.
     */
    public void processTelemetry(VehicleTelemetryMessage message) {
        String vehicleId = message.getVehicleId();

        // Get or create vehicle metrics
        VehicleMetrics metrics = vehicleMetricsMap.computeIfAbsent(
                vehicleId,
                id -> {
                    log.info("New vehicle registered: {} ({})", id, message.getVehicleType());
                    fleetMetrics.registerVehicle(message.getVehicleType());
                    return new VehicleMetrics(id, message.getVehicleType(), message.getVehicleStatus());
                }
        );

        // Update vehicle metrics
        metrics.updateMetrics(
                message.getSpeed(),
                message.getFuelLevel(),
                message.getVehicleStatus(),
                message.getTimeStamp()
        );

        // Update fleet status tracking
        VehicleStatus oldStatus = lastKnownStatus.put(vehicleId, message.getVehicleStatus());
        if (oldStatus != message.getVehicleStatus()) {
            fleetMetrics.updateVehicleStatus(oldStatus, message.getVehicleStatus());
            log.debug("Vehicle {} status changed: {} -> {}", vehicleId, oldStatus, message.getVehicleStatus());
        }

        // Update fleet telemetry counter
        fleetMetrics.incrementTelemetryCount();
    }

    // get the latest telemetry record for each vehicle from postgresql
    @Transactional(readOnly = true)
    public List<VehicleTelemetry> getLatestTelemetry() {
        return vehicleTelemetryRepository.findLatestTelemetryPerVehicle();
    }

    /**
     * Get metrics for a specific vehicle.
     */
    public VehicleMetrics getVehicleMetrics(String vehicleId) {
        return vehicleMetricsMap.get(vehicleId);
    }

    /**
     * Get all vehicle metrics.
     */
    public Collection<VehicleMetrics> getAllVehicleMetrics() {
        return vehicleMetricsMap.values();
    }

    /**
     * Get metrics filtered by vehicle type.
     */
    public Collection<VehicleMetrics> getMetricsByType(VehicleType type) {
        return vehicleMetricsMap.values().stream()
                .filter(m -> m.getVehicleType() == type)
                .toList();
    }

    /**
     * Calculate fleet-wide average speed.
     */
    public double getFleetAverageSpeed() {
        return vehicleMetricsMap.values().stream()
                .mapToDouble(VehicleMetrics::getAverageSpeed)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculate average speed by vehicle type.
     */
    public Map<VehicleType, Double> getAverageSpeedByType() {
        Map<VehicleType, Double> result = new java.util.EnumMap<>(VehicleType.class);

        for (VehicleType type : VehicleType.values()) {
            double avgSpeed = vehicleMetricsMap.values().stream()
                    .filter(m -> m.getVehicleType() == type)
                    .mapToDouble(VehicleMetrics::getAverageSpeed)
                    .average()
                    .orElse(0.0);
            result.put(type, avgSpeed);
        }

        return result;
    }

    /**
     * Get total fuel consumed across all vehicles.
     */
    public double getTotalFuelConsumed() {
        return vehicleMetricsMap.values().stream()
                .mapToDouble(VehicleMetrics::getTotalFuelConsumed)
                .sum();
    }

    /**
     * Get number of tracked vehicles.
     */
    public int getTrackedVehicleCount() {
        return vehicleMetricsMap.size();
    }

    @Transactional(readOnly = true)
    public HistoricalMetricsResponse getHistoricalMetrics(LocalDate from, LocalDate to) {

        // 1. Fetch Pre-Calculated Data
        List<DailyFleetMetrics> fleetMetrics =
                fleetMetricsRepository.findByDateBetweenOrderByDateAsc(from, to);

        List<DailyVehicleMetrics> vehicleMetrics =
                vehicleMetricsRepository.findByDateBetweenOrderByVehicleIdAsc(from, to);

        // 2. Aggregate vehicle fuel consumption from daily metrics
        List<VehicleFuelConsumption> fuelConsumption = vehicleMetrics.stream()
                .collect(Collectors.groupingBy(DailyVehicleMetrics::getVehicleId))
                .entrySet().stream()
                .map(entry -> new VehicleFuelConsumption(
                        entry.getKey(),
                        entry.getValue().get(0).getVehicleType(), // Get type from first entry
                        entry.getValue().stream()
                                .filter(m -> m.getFuelConsumed() != null)
                                .mapToDouble(DailyVehicleMetrics::getFuelConsumed)
                                .sum()
                ))
                .toList();

        // Calculate summary statistics
        Double avgSpeed = fleetMetrics.stream()
                .filter(m -> m.getFleetAverageSpeed() != null)
                .mapToDouble(DailyFleetMetrics::getFleetAverageSpeed)
                .average()
                .orElse(0.0);

        Double totalFuel = fleetMetrics.stream()
                .filter(m -> m.getTotalFuelConsumed() != null)
                .mapToDouble(DailyFleetMetrics::getTotalFuelConsumed)
                .sum();

        Integer totalPoints = vehicleMetrics.stream()
                .filter(m -> m.getTotalTelemetryPoints() != null)
                .mapToInt(DailyVehicleMetrics::getTotalTelemetryPoints)
                .sum();

        return HistoricalMetricsResponse.builder()
                .fromDate(from)
                .toDate(to)
                .totalDays(fleetMetrics.size())
                .averageFleetSpeed(avgSpeed)
                .totalFuelConsumed(totalFuel)
                .totalDataPoints(totalPoints)
                .dailyFleetMetrics(fleetMetrics)
                .dailyVehicleMetrics(vehicleMetrics)
                .vehicleFuelConsumption(fuelConsumption)
                .build();
    }
}
