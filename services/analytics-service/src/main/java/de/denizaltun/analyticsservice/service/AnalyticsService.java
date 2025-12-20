package de.denizaltun.analyticsservice.service;

import de.denizaltun.analyticsservice.dto.VehicleStatus;
import de.denizaltun.analyticsservice.dto.VehicleTelemetryMessage;
import de.denizaltun.analyticsservice.dto.VehicleType;
import de.denizaltun.analyticsservice.model.FleetMetrics;
import de.denizaltun.analyticsservice.model.VehicleMetrics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
                    return new VehicleMetrics(id, message.getVehicleType());
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
}
