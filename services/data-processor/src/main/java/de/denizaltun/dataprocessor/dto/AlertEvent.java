package de.denizaltun.dataprocessor.dto;

import de.denizaltun.dataprocessor.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {
    private String vehicleId;
    private VehicleType vehicleType;
    private AlertType alertType;
    private String message;
    private Double thresholdValue;
    private Double actualValue;
    private LocalDateTime timestamp;
}
