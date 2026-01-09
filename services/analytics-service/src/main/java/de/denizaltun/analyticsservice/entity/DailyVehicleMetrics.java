package de.denizaltun.analyticsservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "daily_vehicle_metrics")
public class DailyVehicleMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "vehicle_status")
    private String vehicleStatus;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "average_speed")
    private Double averageSpeed;

    @Column(name = "max_speed")
    private Double maxSpeed;

    @Column(name = "min_speed")
    private Double minSpeed;

    @Column(name = "average_fuel_level")
    private Double averageFuelLevel;

    @Column(name = "min_fuel_level")
    private Double minFuelLevel;

    @Column(name = "fuel_consumed")
    private Double fuelConsumed;

    @Column(name = "total_telemetry_points")
    private Integer totalTelemetryPoints;

    /**
     * Constructor without id (PostgreSQL generates id automatically)
     */
    public DailyVehicleMetrics(String vehicleId, LocalDate date,
                               String vehicleStatus, String vehicleType,
                               Double averageSpeed, Double maxSpeed, Double minSpeed,
                               Double averageFuelLevel, Double minFuelLevel, Double fuelConsumed,
                               Integer totalTelemetryPoints) {
        this.vehicleId = vehicleId;
        this.date = date;
        this.vehicleStatus = vehicleStatus;
        this.vehicleType = vehicleType;
        this.averageSpeed = averageSpeed;
        this.maxSpeed = maxSpeed;
        this.minSpeed = minSpeed;
        this.averageFuelLevel = averageFuelLevel;
        this.minFuelLevel = minFuelLevel;
        this.fuelConsumed = fuelConsumed;
        this.totalTelemetryPoints = totalTelemetryPoints;
    }
}
