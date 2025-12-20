package de.denizaltun.analyticsservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Response DTO for fleet-wide analytics.
 */
@Data
@Builder
public class FleetAnalyticsResponse {
    private int totalVehicles;
    private long totalTelemetryReceived;
    private double fleetAverageSpeed;
    private double totalFuelConsumed;
    private Map<VehicleType, Long> vehiclesByType;
    private Map<VehicleType, Double> averageSpeedByType;
    private Map<VehicleStatus, Long> currentStatusOverview;
}
