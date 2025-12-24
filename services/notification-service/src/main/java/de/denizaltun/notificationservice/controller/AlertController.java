package de.denizaltun.notificationservice.controller;

import de.denizaltun.notificationservice.model.Alert;
import de.denizaltun.notificationservice.model.VehicleType;
import de.denizaltun.notificationservice.service.AlertService;
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
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Alert>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Alert>> getAlertsByVehicle(@PathVariable String vehicleId) {
        return ResponseEntity.ok(alertService.getAlertsByVehicle(vehicleId));
    }

    @GetMapping("/vehicle/type/{vehicleType}")
    public ResponseEntity<List<Alert>> getAlertsByVehicleType(@PathVariable VehicleType vehicleType) {
        return ResponseEntity.ok(alertService.getAlertsByVehicleType(vehicleType));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAlertStats() {
        return ResponseEntity.ok(Map.of(
                "activeAlerts", alertService.getActiveAlertCount(),
                "totalAlerts", alertService.getAllAlerts().size()
        ));
    }

    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.acknowledgeAlert(id));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolveAlert(id));
    }
}
