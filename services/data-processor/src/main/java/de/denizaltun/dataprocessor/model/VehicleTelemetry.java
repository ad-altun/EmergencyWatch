package de.denizaltun.dataprocessor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for storing vehicle telemetry data in PostgreSQL.
 * This matches the structure from vehicle-simulator's Kafka messages.
 */
@Entity
@Table(name = "vehicle_telemetry", indexes = {
        @Index(name = "idx_vehicle_id", columnList = "vehicle_id"),
        @Index(name = "idx_timeStamp", columnList = "timeStamp"),
        @Index(name = "idx_vehicle_status", columnList = "vehicle_status"),
        @Index(name = "idx_vehicle_type", columnList = "vehicle_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType; // POLICE, AMBULANCE, FIRE_TRUCK

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus vehicleStatus; // IDLE, EN_ROUTE, ON_SCENE, RETURNING

    @Column(nullable = false)
    private LocalDateTime timeStamp;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double speed; // km/h

    @Column(name = "fuel_level", nullable = false)
    private Double fuelLevel; // percentage

    @Column(name = "engine_temp", nullable = false)
    private Double engineTemp; // Celsius

    @Column(name = "battery_voltage", nullable = false)
    private Double batteryVoltage; // Volts

    @Column(name = "emergency_lights_active", nullable = false)
    private Boolean emergencyLightsActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
