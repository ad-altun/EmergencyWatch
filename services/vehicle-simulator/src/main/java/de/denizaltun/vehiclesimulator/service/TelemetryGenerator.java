package de.denizaltun.vehiclesimulator.service;

import de.denizaltun.vehiclesimulator.model.VehicleState;
import de.denizaltun.vehiclesimulator.model.VehicleStatus;
import de.denizaltun.vehiclesimulator.model.VehicleTelemetry;
import de.denizaltun.vehiclesimulator.model.VehicleType;
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

        vehicleState.transitionState();

        // Generate GPS coordinates (Munich area with small random movement)
        double latitude = MUNICH_LAT + (random.nextDouble() * MOVEMENT_RANGE * 2 - MOVEMENT_RANGE);
        double longitude = MUNICH_LON + (random.nextDouble() * MOVEMENT_RANGE * 2 - MOVEMENT_RANGE);

        // Generate metrics based on vehicle status
        double speed = generateSpeed(vehicleState.getVehicleStatus(), vehicleState.getVehicleType());
        double engineTemp = generateEngineTemp(vehicleState.getVehicleStatus());
        double fuelConsumption = calculateFuelConsumption(vehicleState.getVehicleStatus(), vehicleState.getVehicleType());
        double fuelLevel = vehicleState.getFuelLevel() - fuelConsumption;
        VehicleStatus vehicleStatus = vehicleState.getVehicleStatus();

        // Refuel if below 35% AND vehicle is IDLE (simulate refueling at base)
        if (fuelLevel < 35.0 && vehicleStatus == VehicleStatus.IDLE) {
            fuelLevel = 85.0 + random.nextDouble() * 15.0; // Refuel to 85-100%
            log.debug("Vehicle {} refueled to {:.1f}%", vehicleState.getVehicleId(), fuelLevel);
        }

        double batteryVoltage = generateBatteryVoltage(vehicleState.getVehicleType());
        boolean lightsActive = vehicleState.getVehicleStatus() == VehicleStatus.EN_ROUTE;

        // Update vehicle state for next iteration
        vehicleState.setFuelLevel(Math.max(fuelLevel, 30.0)); // Don't go below 30%

        return VehicleTelemetry.builder()
                .vehicleId(vehicleState.getVehicleId())
                .vehicleType(vehicleState.getVehicleType())
                .timeStamp(LocalDateTime.now())
                .latitude(latitude)
                .longitude(longitude)
                .speed(speed)
                .engineTemp(engineTemp)
                .fuelLevel(fuelLevel)
                .batteryVoltage(batteryVoltage)
                .vehicleStatus(vehicleState.getVehicleStatus())
                .emergencyLightsActive(lightsActive)
                .build();
    }

    /**
     * Generate realistic speed based on vehicle status and vehicle type.
     */
    private double generateSpeed(VehicleStatus status, VehicleType vehicleType) {
        return switch (status) {
            case IDLE -> 0.0;       // Parked

            case EN_ROUTE -> switch (vehicleType) {
                    case AMBULANCE -> 50.0 + random.nextDouble() * 70.0;    // 50-120 km/h
                    case POLICE -> 70.0 + random.nextDouble() * 70.0;       // 70-140 km/h
                    case FIRE_TRUCK -> 50.0 + random.nextDouble() * 60.0;   // 80-140 km/h
                };

            case ON_SCENE -> 0.0;

            case RETURNING -> switch (vehicleType) {
                case AMBULANCE -> 35.0 + random.nextDouble() * 35.0;    // 35-70 km/h
                case POLICE -> 55.0 + random.nextDouble() * 45.0;       // 55-100 km/h
                case FIRE_TRUCK -> 30.0 +random.nextDouble() *45.0;     // 30-75 km/h
            };
        };
    }

    /**
     * Generate realistic engine temperature based on status.
     */
    private double generateEngineTemp(VehicleStatus status) {
        double baseTemp = switch (status) {
            case IDLE -> 50.0;
            case EN_ROUTE -> 95.0; // Higher when driving fast
            case ON_SCENE -> 75.0;
            case RETURNING -> 85.0;
        };
        return baseTemp + random.nextDouble() * 10.0 - 5.0; // ±5°C variance
    }

    /**
     * Calculate fuel consumption based on vehicle status and type.
     * - IDLE: No consumption
     * - Fire trucks: 2x police cars
     * - Ambulances: 1.33x police cars (or fire trucks / 1.5)
     */
    private double calculateFuelConsumption(VehicleStatus status, VehicleType vehicleType) {
        // No consumption when IDLE
        if (status == VehicleStatus.IDLE) {
            return 0.0;
        }

        // Base consumption rate for police cars (up to 0.5% per cycle)
        double baseConsumption = random.nextDouble() * 0.5;

        // Apply multipliers based on vehicle type
        return switch (vehicleType) {
            case POLICE -> baseConsumption;                // 1x (base rate)
            case AMBULANCE -> baseConsumption * 1.33;      // 1.33x police
            case FIRE_TRUCK -> baseConsumption * 2.0;      // 2x police
        };
    }

    private double generateBatteryVoltage(VehicleType vehicleType) {
        // Fire trucks use 24V system, others use 12V
        double nominalVoltage = (vehicleType == VehicleType.FIRE_TRUCK) ? 24.0 : 12.0;

        // Normal range: 95-102% of nominal voltage
        // Occasionally generates low voltage to trigger alerts
        if (random.nextDouble() < 0.05) {
            // 5% chance of low battery (85-95% of nominal)
            return nominalVoltage * (0.85 + random.nextDouble() * 0.10);
        }
        return nominalVoltage * (0.95 + random.nextDouble() * 0.07);
    }
}
