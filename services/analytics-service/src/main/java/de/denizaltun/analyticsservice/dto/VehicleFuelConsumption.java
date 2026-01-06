package de.denizaltun.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleFuelConsumption {
    private String vehicleId;
    private String vehicleType;
    private Double totalConsumed;
}