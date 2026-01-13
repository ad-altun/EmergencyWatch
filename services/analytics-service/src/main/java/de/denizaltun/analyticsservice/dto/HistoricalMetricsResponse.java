package de.denizaltun.analyticsservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.denizaltun.analyticsservice.entity.DailyFleetMetrics;
import de.denizaltun.analyticsservice.entity.DailyVehicleMetrics;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class HistoricalMetricsResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    private Integer totalDays;
    private Integer daysWithData;

    // Fleet-level summary
    private Double averageFleetSpeed;
    private Double totalFuelConsumed;
    private Integer totalDataPoints;

    // Daily breakdown
    private List<DailyFleetMetrics> dailyFleetMetrics;
    private List<DailyVehicleMetrics> dailyVehicleMetrics;

    private List<VehicleFuelConsumption> vehicleFuelConsumption;
}
