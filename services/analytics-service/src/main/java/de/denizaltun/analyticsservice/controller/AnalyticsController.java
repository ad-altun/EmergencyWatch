package de.denizaltun.analyticsservice.controller;

import de.denizaltun.analyticsservice.dto.FleetAnalyticsResponse;
import de.denizaltun.analyticsservice.dto.HistoricalMetricsResponse;
import de.denizaltun.analyticsservice.dto.VehicleAnalyticsResponse;
import de.denizaltun.analyticsservice.dto.VehicleType;
import de.denizaltun.analyticsservice.model.FleetMetrics;
import de.denizaltun.analyticsservice.model.VehicleMetrics;
import de.denizaltun.analyticsservice.scheduler.DailyAggregationScheduler;
import de.denizaltun.analyticsservice.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
@Tag(name = "Analytics", description = "Fleet and vehicle analytics endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final DailyAggregationScheduler scheduler;

    /**
     * Get fleet-wide analytics summary.
     */
    @Operation(summary = "Get fleet analytics", description = "Returns fleet-wide metrics including total vehicles, average speed, and fuel consumption")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved fleet metrics")
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
    @Operation(summary = "Get vehicle analytics", description = "Returns analytics for a specific vehicle")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle metrics")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<VehicleAnalyticsResponse> getVehicleAnalytics(
            @Parameter(description = "Unique vehicle identifier", example = "FIRE-001")
            @PathVariable String vehicleId) {
        VehicleMetrics metrics = analyticsService.getVehicleMetrics(vehicleId);

        if (metrics == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(metrics));
    }

    /**
     * Get analytics for all vehicles.
     */
    @Operation(summary = "Get all vehicle analytics")
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
    @Operation(summary = "Get analytics by vehicle type")
    @GetMapping("/vehicles/type/{type}")
    public ResponseEntity<List<VehicleAnalyticsResponse>> getAnalyticsByType(
            @Parameter(description = "Vehicle type", example = "FIRE_TRUCK")
            @PathVariable VehicleType type) {
        List<VehicleAnalyticsResponse> responses = analyticsService.getMetricsByType(type).stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Simple health/stats endpoint.
     */
    @Operation(summary = "Get quick stats")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
                "trackedVehicles", analyticsService.getTrackedVehicleCount(),
                "totalTelemetry", analyticsService.getFleetMetrics().getTotalTelemetryReceived().get(),
                "fleetAverageSpeed", String.format("%.2f km/h", analyticsService.getFleetAverageSpeed())
        ));
    }

    @Operation(summary = "Trigger manual aggregation", description = "Admin endpoint to manually trigger daily metrics aggregation")
    @ApiResponse(responseCode = "200", description = "Aggregation triggered successfully")
    @PostMapping("/admin/aggregate/{date}")
    public ResponseEntity<String> triggerAggregation(
            @Parameter(description = "Date to aggregate", example = "2025-12-28")
            @PathVariable String date) {
        LocalDate targetDate = LocalDate.parse(date);
        scheduler.triggerManualAggregation(targetDate);
        return ResponseEntity.ok("Aggregation triggered for " + date);
    }

    @Operation(summary = "Get historical metrics", description = "Retrieve aggregated metrics from MongoDB for a date range")
    @GetMapping("/history")
    public ResponseEntity<HistoricalMetricsResponse> getHistoricalMetrics(
            @Parameter(description = "Start date", example = "2025-12-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date", example = "2025-12-28")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(analyticsService.getHistoricalMetrics(from, to));
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
