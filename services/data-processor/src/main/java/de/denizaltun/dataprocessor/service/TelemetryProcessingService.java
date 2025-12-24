package de.denizaltun.dataprocessor.service;

import de.denizaltun.dataprocessor.dto.AlertEvent;
import de.denizaltun.dataprocessor.dto.AlertType;
import de.denizaltun.dataprocessor.dto.VehicleTelemetryMessage;
import de.denizaltun.dataprocessor.model.VehicleTelemetry;
import de.denizaltun.dataprocessor.model.VehicleType;
import de.denizaltun.dataprocessor.repository.VehicleTelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for processing and storing vehicle telemetry data.
 * Handles business logic, validation, and database operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryProcessingService {

    private final VehicleTelemetryRepository repository;
    private final AlertPublisher alertPublisher;

    // Alert thresholds
    private static final double LOW_FUEL_THRESHOLD = 20.0;
    private static final double HIGH_ENGINE_TEMP_THRESHOLD = 95.0;
    private static final double LOW_BATTERY_12V_THRESHOLD = 11.5;
    private static final double LOW_BATTERY_24V_THRESHOLD = 23.0;

    /**
     * Process incoming telemetry message from Kafka.
     * Validates, transforms, and stores in PostgreSQL.
     */
    @Transactional
    public void processTelemetry(VehicleTelemetryMessage message) {
        log.info("Processing telemetry for vehicle: {}", message.getVehicleId());

        // Validate message
        if (!isValid(message)) {
            log.warn("Invalid telemetry message received: {}", message);
            return;
        }

        // Convert DTO to Entity
        VehicleTelemetry telemetry = convertToEntity(message);

        // Save to database
        VehicleTelemetry saved = repository.save(telemetry);
        log.debug("Saved telemetry with ID: {}", saved.getId());

        // Check for alert conditions
        checkAlertConditions(message);
    }

    /**
     * Validate telemetry message.
     */
    private boolean isValid(VehicleTelemetryMessage message) {
        if (message.getVehicleId() == null || message.getVehicleId().isEmpty()) {
            return false;
        }
        if (message.getTimeStamp() == null) {
            return false;
        }
        if (message.getLatitude() == null || message.getLongitude() == null) {
            return false;
        }
        return true;
    }

    /**
     * Convert DTO to JPA Entity.
     */
    private VehicleTelemetry convertToEntity(VehicleTelemetryMessage message) {
        return VehicleTelemetry.builder()
                .vehicleId(message.getVehicleId())
                .vehicleStatus(message.getVehicleStatus())
                .timeStamp(message.getTimeStamp())
                .latitude(message.getLatitude())
                .longitude(message.getLongitude())
                .speed(message.getSpeed())
                .fuelLevel(message.getFuelLevel())
                .batteryVoltage(message.getBatteryVoltage())
                .engineTemp(message.getEngineTemp())
                .emergencyLightsActive(message.getEmergencyLightsActive())
                .build();
    }

    /**
     * Check for alert conditions (low fuel, high temp, etc.).
     * For now, just log alerts. Later, we can publish to an alert topic.
     */
    private void checkAlertConditions(VehicleTelemetryMessage  message) {
        // Low fuel alert
        if (message.getFuelLevel() < LOW_FUEL_THRESHOLD) {
            publishAlert(message, AlertType.LOW_FUEL,
                    String.format("Low fuel: %.1f%%", message.getFuelLevel()),
                    LOW_FUEL_THRESHOLD,
                    message.getFuelLevel());
        }

        // High engine temperature alert
        if (message.getEngineTemp() > HIGH_ENGINE_TEMP_THRESHOLD) {
            publishAlert(message, AlertType.HIGH_ENGINE_TEMP,
                    String.format("High engine temp: %.1f°C", message.getEngineTemp()),
                    HIGH_ENGINE_TEMP_THRESHOLD,
                    message.getEngineTemp());
        }

        // Low battery voltage alert (threshold depends on vehicle type)
        checkBatteryAlert(message);

        // Emergency lights active during idle (potential issue)
        // Emergency status change
        if (message.getEmergencyLightsActive() != null && message.getEmergencyLightsActive()) {
            publishAlert(message, AlertType.EMERGENCY_STATUS_CHANGE,
                    "Emergency lights activated",
                    null,
                    null);
        }
    }

    private void checkBatteryAlert(VehicleTelemetryMessage message) {
        if (message.getBatteryVoltage() == null || message.getVehicleType() == null) {
            return;
        }

        double threshold = (message.getVehicleType() == VehicleType.FIRE_TRUCK)
                ? LOW_BATTERY_24V_THRESHOLD
                : LOW_BATTERY_12V_THRESHOLD;

        if (message.getBatteryVoltage() < threshold) {
            publishAlert(message, AlertType.LOW_BATTERY,
                    String.format("Low battery: %.1fV (threshold: %.1fV)",
                            message.getBatteryVoltage(), threshold),
                    threshold,
                    message.getBatteryVoltage());
        }
    }

    private void publishAlert(VehicleTelemetryMessage message, AlertType alertType,
                              String alertMessage, Double threshold, Double actualValue) {
        AlertEvent alertEvent = AlertEvent.builder()
                .vehicleId(message.getVehicleId())
                .vehicleType(message.getVehicleType())
                .alertType(alertType)
                .message(alertMessage)
                .thresholdValue(threshold)
                .actualValue(actualValue)
                .timestamp(LocalDateTime.now())
                .build();

        alertPublisher.publishAlert(alertEvent);
        log.warn("ALERT - {} for vehicle {}: {}", alertType, message.getVehicleId(), alertMessage);
    }

    /**
     * Get statistics for monitoring.
     */
    public long getTotalTelemetryCount() {
        return repository.count();
    }

    /**
     * Get count for a specific vehicle.
     */
    public long getVehicleTelemetryCount(String vehicleId) {
        return repository.countByVehicleId(vehicleId);
    }
}
