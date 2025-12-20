package de.denizaltun.analyticsservice.model;

import de.denizaltun.analyticsservice.dto.VehicleStatus;
import de.denizaltun.analyticsservice.dto.VehicleType;
import lombok.Data;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fleet-wide aggregated metrics.
 */
@Data
public class FleetMetrics {
    private final AtomicLong totalTelemetryReceived = new AtomicLong(0);
    private final AtomicLong totalVehicles = new AtomicLong(0);

    // Metrics by vehicle type
    private final Map<VehicleType, AtomicLong> vehiclesByType = new EnumMap<>(VehicleType.class);

    // Current status overview
    private final Map<VehicleStatus, AtomicLong> currentStatusCounts = new EnumMap<>(VehicleStatus.class);

    public FleetMetrics() {
        for (VehicleType type : VehicleType.values()) {
            vehiclesByType.put(type, new AtomicLong(0));
        }
        for (VehicleStatus status : VehicleStatus.values()) {
            currentStatusCounts.put(status, new AtomicLong(0));
        }
    }

    public void incrementTelemetryCount() {
        totalTelemetryReceived.incrementAndGet();
    }

    public void registerVehicle(VehicleType type) {
        totalVehicles.incrementAndGet();
        vehiclesByType.get(type).incrementAndGet();
    }

    public void updateVehicleStatus(VehicleStatus oldStatus, VehicleStatus newStatus) {
        if (oldStatus != null) {
            currentStatusCounts.get(oldStatus).decrementAndGet();
        }
        if (newStatus != null) {
            currentStatusCounts.get(newStatus).incrementAndGet();
        }
    }
}
