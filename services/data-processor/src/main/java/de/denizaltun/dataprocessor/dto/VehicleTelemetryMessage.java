package de.denizaltun.dataprocessor.dto;

import de.denizaltun.dataprocessor.model.VehicleStatus;
import de.denizaltun.dataprocessor.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for deserializing vehicle telemetry from Kafka messages.
 * Matches the structure sent by vehicle-simulator service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleTelemetryMessage {
    private String vehicleId;
    private VehicleType vehicleType;
    private LocalDateTime timeStamp;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double fuelLevel;
    private Double engineTemp;
    private VehicleStatus vehicleStatus;
    private Boolean emergencyLightsActive;
}
