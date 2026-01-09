package de.denizaltun.analyticsservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@Entity
@Table(name = "daily_fleet_metrics")
public class DailyFleetMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Column(name = "total_vehicles")
    private Integer totalVehicles;

    @Column(name = "fleet_average_speed")
    private Double fleetAverageSpeed;

    @Column(name = "total_fuel_consumed")
    private Double totalFuelConsumed;

    // Average speed grouped by vehicle status (IDLE, EN_ROUTE, ON_SCENE, RETURNING)
    @Type(JsonBinaryType.class)
    @Column(name = "avg_speed_by_status", columnDefinition = "jsonb")
    private Map<String, Double> averageSpeedByStatus;

    // Average speed grouped by vehicle type ()
    @Type(JsonBinaryType.class)
    @Column(name = "avg_speed_by_type", columnDefinition = "jsonb")
    private Map<String, Double> averageSpeedByType;

    /**
     * Constructor without id (DB generates id automatically)
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
