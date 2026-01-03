package de.denizaltun.analyticsservice.model;

import de.denizaltun.analyticsservice.dto.VehicleStatus;
import de.denizaltun.analyticsservice.dto.VehicleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * In-memory metrics for a single vehicle.
 * Uses thread-safe accumulators for concurrent updates from Kafka consumer.
 */
@Data
public class VehicleMetrics {

    private final String vehicleId;
    private final VehicleType vehicleType;
    private VehicleStatus vehicleStatus;

    // Speed metrics
    private final DoubleAdder totalSpeed = new DoubleAdder();
    private final AtomicLong speedCount = new AtomicLong(0);

    // Fuel metrics
    private final DoubleAdder totalFuelConsumed = new DoubleAdder();
    private Double lastFuelLevel = null;

    // Status distribution
    private final Map<VehicleStatus, AtomicLong> statusCounts = new EnumMap<>(VehicleStatus.class);

    // General counters
    private final AtomicLong telemetryCount = new AtomicLong(0);
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;

    public VehicleMetrics(String vehicleId, VehicleType vehicleType, VehicleStatus vehicleStatus) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.vehicleStatus = vehicleStatus;

        // Initialize status counts
        for (VehicleStatus status : VehicleStatus.values()) {
            statusCounts.put(status, new AtomicLong(0));
        }
    }

    /**
     * Update metrics with new telemetry data.
     */
    public void updateMetrics(Double speed, Double fuelLevel, VehicleStatus status, LocalDateTime timestamp) {
        telemetryCount.incrementAndGet();

        // Update speed
        if (speed != null) {
            totalSpeed.add(speed);
            speedCount.incrementAndGet();
        }

        // Update fuel consumption (track decrease in fuel level)
        if (fuelLevel != null && lastFuelLevel != null) {
            double fuelUsed = lastFuelLevel - fuelLevel;
            if (fuelUsed > 0) {
                totalFuelConsumed.add(fuelUsed);
            }
        }
        lastFuelLevel = fuelLevel;

        // Update status count
        if (status != null) {
            statusCounts.get(status).incrementAndGet();
            this.vehicleStatus = status;
        }

        // Update timestamps
        if (firstSeen == null) {
            firstSeen = timestamp;
        }
        lastSeen = timestamp;
    }

    /**
     * Calculate average speed.
     */
    public double getAverageSpeed() {
        long count = speedCount.get();
        return count > 0 ? totalSpeed.sum() / count : 0.0;
    }

    /**
     * Get total fuel consumed (percentage points).
     */
    public double getTotalFuelConsumed() {
        return totalFuelConsumed.sum();
    }

    /**
     * Get status distribution as percentages.
     */
    public Map<VehicleStatus, Double> getStatusDistribution() {
        Map<VehicleStatus, Double> distribution = new EnumMap<>(VehicleStatus.class);
        long total = statusCounts.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();

        if (total > 0) {
            for (VehicleStatus status : VehicleStatus.values()) {
                double percentage = (statusCounts.get(status).get() * 100.0) / total;
                distribution.put(status, percentage);
            }
        }
        return distribution;
    }

}
