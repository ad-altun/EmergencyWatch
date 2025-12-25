package de.denizaltun.analyticsservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "daily_fleet_metrics")
public class DailyFleetMetrics {

    @Id
    private String id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Integer totalVehicles;
    private Double fleetAverageSpeed;
    private Double totalFuelConsumed;

    // Average speed grouped by vehicle status (IDLE, EN_ROUTE, ON_SCENE, RETURNING)
    private Map<String, Double> averageSpeedByStatus;
    // Average speed grouped by vehicle type ()
    private Map<String, Double> averageSpeedByType;

    /**
     * Constructor without id (MongoDB generates id automatically)
     */
    public DailyFleetMetrics(LocalDate date, Integer totalVehicles, Double fleetAverageSpeed,
                             Double totalFuelConsumed, Map<String, Double> averageSpeedByStatus,
                             Map<String, Double> averageSpeedByType) {
        this.date = date;
        this.totalVehicles = totalVehicles;
        this.fleetAverageSpeed = fleetAverageSpeed;
        this.totalFuelConsumed = totalFuelConsumed;
        this.averageSpeedByStatus = averageSpeedByStatus;
        this.averageSpeedByType = averageSpeedByType;
    }
}
