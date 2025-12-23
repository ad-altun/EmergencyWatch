package de.denizaltun.analyticsservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Read-only JPA Entity for querying vehicle telemetry data from PostgreSQL.
 * Synchronized with data-processor's VehicleTelemetry entity.
 * This service only READS data - it does not modify the schema.
 */
@Entity
@Table(name = "vehicle_telemetry")
public class VehicleTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

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

    // Constructors
    public VehicleTelemetry() {}

    // Getters only (read-only entity)
    public Long getId() { return id; }
    public String getVehicleId() { return vehicleId; }
    public VehicleStatus getVehicleStatus() { return vehicleStatus; }
    public LocalDateTime getTimeStamp() { return timeStamp; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getSpeed() { return speed; }
    public Double getFuelLevel() { return fuelLevel; }
    public Double getEngineTemp() { return engineTemp; }
    public Boolean getEmergencyLightsActive() { return emergencyLightsActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }}
