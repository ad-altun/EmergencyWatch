package de.denizaltun.analyticsservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Document(collection = "daily_vehicle_metrics")
public class DailyVehicleMetrics {

    @Id
    private String id;

    private String vehicleId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String vehicleStatus;
    private String vehicleType;
    private Double averageSpeed;
    private Double maxSpeed;
    private Double minSpeed;
    private Double averageFuelLevel;
    private Double minFuelLevel;
    private Integer totalTelemetryPoints;

    /**
     * Constructor without id (MongoDB generates id automatically)
     */
    public DailyVehicleMetrics(String vehicleId, LocalDate date,
                               String vehicleStatus, String vehicleType,
                               Double averageSpeed, Double maxSpeed, Double minSpeed,
                               Double averageFuelLevel, Double minFuelLevel,
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
        this.totalTelemetryPoints = totalTelemetryPoints;
    }
}
