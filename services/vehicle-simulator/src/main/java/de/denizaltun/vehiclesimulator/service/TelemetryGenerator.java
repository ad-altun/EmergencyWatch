package de.denizaltun.vehiclesimulator.service;

import de.denizaltun.vehiclesimulator.model.VehicleStatus;
import de.denizaltun.vehiclesimulator.model.VehicleTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Generates realistic telemetry data for emergency vehicles.
 * Focuses on Munich area with realistic vehicle metrics based on operational status.
 */

@Slf4j
@Service
public class TelemetryGenerator {

    private static final double MUNICH_LAT = 48.1351;
    private static final double MUNICH_LON = 11.5820;
    private static final double MOVEMENT_RANGE = 0.05;  // ~5km radius

    private final Random random = new Random();

    // Generates telemetry for a vehicle based on its current state.
    public VehicleTelemetry generate(VehicleState vehicleState) {

        // Generate GPS coordinates (Munich area with small random movement)
        double latitude = MUNICH_LAT + (random.nextDouble() * MOVEMENT_RANGE * 2 - MOVEMENT_RANGE);
        double longitude = MUNICH_LON + (random.nextDouble() * MOVEMENT_RANGE * 2 - MOVEMENT_RANGE);

        // Generate metrics based on vehicle status
        double speed = generateSpeed(vehicleState.getStatus());
        double engineTemp = generateEngineTemp(vehicleState.getStatus());
        double fuelLevel = vehicleState.getFuelLevel() - random.nextDouble() * 0.5; // Gradual fuel consumption
        boolean lightsActive = vehicleState.getStatus() == VehicleStatus.RESPONDING;

        // Update vehicle state for next iteration
        vehicleState.setFuelLevel(Math.max(fuelLevel, 10.0)); // Don't go below 10%
        vehicleState.transitionState(); // May change status

        return VehicleTelemetry.builder()
                .vehicleId(vehicleState.getVehicleId())
                .vehicleType(vehicleState.getVehicleType())
                .timeStamp(LocalDateTime.now())
                .latitude(latitude)
                .longitude(longitude)
                .speed(speed)
                .engineTemp(engineTemp)
                .fuelLevel(fuelLevel)
                .status(vehicleState.getStatus())
                .emergencyLightsActive(lightsActive)
                .build();
    }

    /**
     * Generate realistic speed based on vehicle status.
     */
    private double generateSpeed(VehicleStatus status) {
        return switch (status) {
            case IDLE -> 0.0;
            case RESPONDING -> 80.0 + random.nextDouble() * 40.0; // 80-120 km/h
            case ON_SCENE -> 0.0;
            case RETURNING -> 50.0 + random.nextDouble() * 30.0; // 50-80 km/h
        };
    }

    /**
     * Generate realistic engine temperature based on status.
     */
    private double generateEngineTemp(VehicleStatus status) {
        double baseTemp = switch (status) {
            case IDLE -> 60.0;
            case RESPONDING -> 95.0; // Higher when driving fast
            case ON_SCENE -> 75.0;
            case RETURNING -> 85.0;
        };
        return baseTemp + random.nextDouble() * 10.0 - 5.0; // ±5°C variance
    }

}
