package de.denizaltun.analyticsservice.service;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "daily_vehicle_metrics")
public class DailyVehicleMetrics {

    @Id
    private String id;

    private String vehicleId;
    private LocalDate date;
    private String vehicleType;
    private Double averageSpeed;
    private Double maxSpeed;
    private Double minSpeed;
    private Double averageFuel;
    private Double minFuel;
    private Integer totalTelemetryPoints;

    // Constructors
    public DailyVehicleMetrics() {}

    public DailyVehicleMetrics(String vehicleId, LocalDate date, String vehicleType,
                               Double averageSpeed, Double maxSpeed, Double minSpeed,
                               Double averageFuel, Double minFuel, Integer totalTelemetryPoints) {
        this.vehicleId = vehicleId;
        this.date = date;
        this.vehicleType = vehicleType;
        this.averageSpeed = averageSpeed;
        this.maxSpeed = maxSpeed;
        this.minSpeed = minSpeed;
        this.averageFuel = averageFuel;
        this.minFuel = minFuel;
        this.totalTelemetryPoints = totalTelemetryPoints;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(Double averageSpeed) { this.averageSpeed = averageSpeed; }

    public Double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; }

    public Double getMinSpeed() { return minSpeed; }
    public void setMinSpeed(Double minSpeed) { this.minSpeed = minSpeed; }

    public Double getAverageFuel() { return averageFuel; }
    public void setAverageFuel(Double averageFuel) { this.averageFuel = averageFuel; }

    public Double getMinFuel() { return minFuel; }
    public void setMinFuel(Double minFuel) { this.minFuel = minFuel; }

    public Integer getTotalTelemetryPoints() { return totalTelemetryPoints; }
    public void setTotalTelemetryPoints(Integer totalTelemetryPoints) {
        this.totalTelemetryPoints = totalTelemetryPoints;
    }
}
