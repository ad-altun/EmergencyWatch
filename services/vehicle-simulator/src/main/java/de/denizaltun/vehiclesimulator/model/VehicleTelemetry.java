package de.denizaltun.vehiclesimulator.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VehicleTelemetry(
        String vehicleId,
        LocalDateTime timeStamp,
        VehicleStatus vehicleStatus,
        VehicleType vehicleType,

        // GPS data
        double latitude,
        double longitude,

        // vehicle metrics
        double speed,               // km/h
        double engineTemp,          // celcius
        double fuelLevel,           // percentage (0 - 100)
        double batteryVoltage,      // Volts (12V for Police/Ambulance, 24V for Fire Truck)

        // vehicle state
        boolean emergencyLightsActive           // e.g., Blue Lights
) {
}
