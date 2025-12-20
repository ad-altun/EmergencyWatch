package de.denizaltun.analyticsservice.controller;

import de.denizaltun.analyticsservice.dto.FleetAnalyticsResponse;
import de.denizaltun.analyticsservice.dto.VehicleAnalyticsResponse;
import de.denizaltun.analyticsservice.dto.VehicleType;
import de.denizaltun.analyticsservice.model.FleetMetrics;
import de.denizaltun.analyticsservice.model.VehicleMetrics;
import de.denizaltun.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * REST API for querying analytics data.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get fleet-wide analytics summary.
     */
    @GetMapping("/fleet")
    public ResponseEntity<FleetAnalyticsResponse> getFleetAnalytics() {
        FleetMetrics fleetMetrics = analyticsService.getFleetMetrics();

        FleetAnalyticsResponse response = FleetAnalyticsResponse.builder()
                .totalVehicles(analyticsService.getTrackedVehicleCount())
                .totalTelemetryReceived(fleetMetrics.getTotalTelemetryReceived().get())
                .fleetAverageSpeed(analyticsService.getFleetAverageSpeed())
                .totalFuelConsumed(analyticsService.getTotalFuelConsumed())
                .vehiclesByType(convertAtomicMap(fleetMetrics.getVehiclesByType()))
                .averageSpeedByType(analyticsService.getAverageSpeedByType())
                .currentStatusOverview(convertStatusMap(fleetMetrics.getCurrentStatusCounts()))
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get analytics for a specific vehicle.
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<VehicleAnalyticsResponse> getVehicleAnalytics(@PathVariable String vehicleId) {
        VehicleMetrics metrics = analyticsService.getVehicleMetrics(vehicleId);

        if (metrics == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(metrics));
    }

    /**
     * Get analytics for all vehicles.
     */
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleAnalyticsResponse>> getAllVehicleAnalytics() {
        List<VehicleAnalyticsResponse> responses = analyticsService.getAllVehicleMetrics().stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Get analytics filtered by vehicle type.
     */
    @GetMapping("/vehicles/type/{type}")
    public ResponseEntity<List<VehicleAnalyticsResponse>> getAnalyticsByType(@PathVariable VehicleType type) {
        List<VehicleAnalyticsResponse> responses = analyticsService.getMetricsByType(type).stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Simple health/stats endpoint.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
                "trackedVehicles", analyticsService.getTrackedVehicleCount(),
                "totalTelemetry", analyticsService.getFleetMetrics().getTotalTelemetryReceived().get(),
                "fleetAverageSpeed", String.format("%.2f km/h", analyticsService.getFleetAverageSpeed())
        ));
    }

    // Helper methods

    private VehicleAnalyticsResponse mapToResponse(VehicleMetrics metrics) {
        return VehicleAnalyticsResponse.builder()
                .vehicleId(metrics.getVehicleId())
                .vehicleType(metrics.getVehicleType())
                .averageSpeed(metrics.getAverageSpeed())
                .totalFuelConsumed(metrics.getTotalFuelConsumed())
                .telemetryCount(metrics.getTelemetryCount().get())
                .statusDistribution(metrics.getStatusDistribution())
                .firstSeen(metrics.getFirstSeen())
                .lastSeen(metrics.getLastSeen())
                .build();
    }

    private <K extends Enum<K>> Map<K, Long> convertAtomicMap(Map<K, AtomicLong> atomicMap) {
        return atomicMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()
                ));
    }

    private Map<de.denizaltun.analyticsservice.dto.VehicleStatus, Long> convertStatusMap(
            Map<de.denizaltun.analyticsservice.dto.VehicleStatus, AtomicLong> atomicMap) {
        return atomicMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()
                ));
    }
}
