package de.denizaltun.dataprocessor.service;

import de.denizaltun.dataprocessor.dto.VehicleTelemetryMessage;
import de.denizaltun.dataprocessor.model.VehicleTelemetry;
import de.denizaltun.dataprocessor.repository.VehicleTelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for processing and storing vehicle telemetry data.
 * Handles business logic, validation, and database operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryProcessingService {

    private final VehicleTelemetryRepository repository;

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
        checkAlertConditions(saved);
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
                .engineTemp(message.getEngineTemp())
                .emergencyLightsActive(message.getEmergencyLightsActive())
                .build();
    }

    /**
     * Check for alert conditions (low fuel, high temp, etc.).
     * For now, just log alerts. Later, we can publish to an alert topic.
     */
    private void checkAlertConditions(VehicleTelemetry telemetry) {
        // Low fuel alert
        if (telemetry.getFuelLevel() < 20.0) {
            log.warn("⚠️ ALERT - Low fuel for vehicle {}: {}%",
                    telemetry.getVehicleId(), telemetry.getFuelLevel());
        }

        // High engine temperature alert
        if (telemetry.getEngineTemp() > 100.0) {
            log.warn("⚠️ ALERT - High engine temperature for vehicle {}: {}°C",
                    telemetry.getVehicleId(), telemetry.getEngineTemp());
        }

        // Emergency lights active during idle (potential issue)
        if ("IDLE".equals(telemetry.getVehicleStatus().toString()) && telemetry.getEmergencyLightsActive()) {
            log.warn("⚠️ ALERT - Emergency lights active while idle for vehicle {}",
                    telemetry.getVehicleId());
        }
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
