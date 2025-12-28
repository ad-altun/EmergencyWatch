package de.denizaltun.notificationservice.controller;

import de.denizaltun.notificationservice.model.Alert;
import de.denizaltun.notificationservice.model.VehicleType;
import de.denizaltun.notificationservice.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Alert management and notification endpoints")
public class AlertController {

    private final AlertService alertService;

    @Operation(summary = "Get  all alerts")
    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @Operation(summary = "Get active alerts", description = "Returns only alerts with ACTIVE status")
    @GetMapping("/active")
    public ResponseEntity<List<Alert>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @Operation(summary = "Get alerts by vehicle")
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Alert>> getAlertsByVehicle(
            @Parameter(description = "Vehicle type", example = "FIRE_TRUCK")
            @PathVariable String vehicleId) {
        return ResponseEntity.ok(alertService.getAlertsByVehicle(vehicleId));
    }

    @Operation(summary = "Get alerts by vehicle type")
    @GetMapping("/vehicle/type/{vehicleType}")
    public ResponseEntity<List<Alert>> getAlertsByVehicleType(
            @Parameter(description = "Vehicle type", example = "FIRE_TRUCK")
            @PathVariable VehicleType vehicleType) {
        return ResponseEntity.ok(alertService.getAlertsByVehicleType(vehicleType));
    }

    @Operation(summary = "Get alert statistics")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAlertStats() {
        return ResponseEntity.ok(Map.of(
                "activeAlerts", alertService.getActiveAlertCount(),
                "totalAlerts", alertService.getAllAlerts().size()
        ));
    }

    @Operation(summary = "Acknowledge an alert", description = "Changes alert status from ACTIVE to ACKNOWLEDGED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert acknowledged successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(
            @Parameter(description = "Alert ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(alertService.acknowledgeAlert(id));
    }

    @Operation(summary = "Resolve an alert", description = "Changes alert status to RESOLVED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert resolved successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(
            @Parameter(description = "Alert ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolveAlert(id));
    }
}
