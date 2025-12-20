package de.denizaltun.analyticsservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for individual vehicle analytics.
 */
@Data
@Builder
public class VehicleAnalyticsResponse {
    private String vehicleId;
    private VehicleType vehicleType;
    private double averageSpeed;
    private double totalFuelConsumed;
    private long telemetryCount;
    private Map<VehicleStatus, Double> statusDistribution;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
}
