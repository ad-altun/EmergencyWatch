package de.denizaltun.analyticsservice.service;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Map;

@Document(collection = "daily_fleet_metrics")
public class DailyFleetMetrics {

    @Id
    private String id;

    private LocalDate date;
    private Integer totalVehicles;
    private Double fleetAverageSpeed;
    private Double totalFuelConsumed;
    private Map<String, Double> averageSpeedByType; // FIRE_TRUCK, AMBULANCE

    // Constructors
    public DailyFleetMetrics() {}

    public DailyFleetMetrics(LocalDate date, Integer totalVehicles,
                             Double fleetAverageSpeed, Double totalFuelConsumed,
                             Map<String, Double> averageSpeedByType) {
        this.date = date;
        this.totalVehicles = totalVehicles;
        this.fleetAverageSpeed = fleetAverageSpeed;
        this.totalFuelConsumed = totalFuelConsumed;
        this.averageSpeedByType = averageSpeedByType;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getTotalVehicles() { return totalVehicles; }
    public void setTotalVehicles(Integer totalVehicles) { this.totalVehicles = totalVehicles; }

    public Double getFleetAverageSpeed() { return fleetAverageSpeed; }
    public void setFleetAverageSpeed(Double fleetAverageSpeed) { this.fleetAverageSpeed = fleetAverageSpeed; }

    public Double getTotalFuelConsumed() { return totalFuelConsumed; }
    public void setTotalFuelConsumed(Double totalFuelConsumed) { this.totalFuelConsumed = totalFuelConsumed; }

    public Map<String, Double> getAverageSpeedByType() { return averageSpeedByType; }
    public void setAverageSpeedByType(Map<String, Double> averageSpeedByType) {
        this.averageSpeedByType = averageSpeedByType;
    }
}
