package de.denizaltun.notificationservice.service;

import de.denizaltun.notificationservice.dto.AlertEvent;
import de.denizaltun.notificationservice.model.Alert;
import de.denizaltun.notificationservice.model.AlertStatus;
import de.denizaltun.notificationservice.model.VehicleType;
import de.denizaltun.notificationservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional
    public Alert processAlertEvent(AlertEvent event) {
        // Deduplication: Check if active alert already exists for this vehicle + type
        Optional<Alert> existingAlert = alertRepository.findByVehicleIdAndAlertTypeAndStatus(
                event.getVehicleId(),
                event.getAlertType(),
                AlertStatus.ACTIVE
        );

        if (existingAlert.isPresent()) {
            log.debug("Active alert already exists for vehicle {} with type {}",
                    event.getVehicleId(), event.getAlertType());
            return existingAlert.get();
        }

        // Create new alert
        Alert alert = Alert.builder()
                .vehicleId(event.getVehicleId())
                .vehicleType(event.getVehicleType())
                .alertType(event.getAlertType())
                .status(AlertStatus.ACTIVE)
                .message(event.getMessage())
                .thresholdValue(event.getThresholdValue())
                .actualValue(event.getActualValue())
                .build();

        Alert saved = alertRepository.save(alert);
        log.info("New alert created: {} for vehicle {}", event.getAlertType(), event.getVehicleId());

        return saved;
    }

    @Transactional
    public Alert acknowledgeAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());

        log.info("Alert {} acknowledged", alertId);
        return alertRepository.save(alert);
    }

    @Transactional
    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());

        log.info("Alert {} resolved", alertId);
        return alertRepository.save(alert);
    }

    public List<Alert> getActiveAlerts() {
        return alertRepository.findByStatus(AlertStatus.ACTIVE);
    }

    public List<Alert> getAlertsByVehicle(String vehicleId) {
        return alertRepository.findByVehicleId(vehicleId);
    }

    public List<Alert> getAlertsByVehicleType(VehicleType vehicleType) {
        return alertRepository.findByVehicleType(vehicleType);
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public long getActiveAlertCount() {
        return alertRepository.countByStatus(AlertStatus.ACTIVE);
    }
}
