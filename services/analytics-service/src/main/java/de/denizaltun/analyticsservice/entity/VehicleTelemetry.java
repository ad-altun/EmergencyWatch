package de.denizaltun.analyticsservice.entity;

import de.denizaltun.analyticsservice.dto.VehicleStatus;
import de.denizaltun.analyticsservice.dto.VehicleType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read-only JPA Entity for querying vehicle telemetry data from PostgreSQL.
 * Synchronized with data-processor's VehicleTelemetry entity.
 * This service only READS data - it does not modify the schema.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "vehicle_telemetry")
public class VehicleTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_status", nullable = false)
    private VehicleStatus vehicleStatus;

    @Column(nullable = false)
    private LocalDateTime timeStamp;  // Maps to "time_stamp" column

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double speed;

    @Column(name = "fuel_level", nullable = false)
    private Double fuelLevel;

    @Column(name = "engine_temp", nullable = false)
    private Double engineTemp;

    @Column(name = "emergency_lights_active", nullable = false)
    private Boolean emergencyLightsActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
