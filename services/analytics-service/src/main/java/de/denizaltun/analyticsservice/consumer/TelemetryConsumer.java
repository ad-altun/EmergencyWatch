package de.denizaltun.analyticsservice.consumer;

import de.denizaltun.analyticsservice.dto.VehicleTelemetryMessage;
import de.denizaltun.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that receives vehicle telemetry messages
 * and forwards them to the analytics service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "vehicle-telemetry", groupId = "analytics-service-group")
    public void consumeTelemetry(ConsumerRecord<String, VehicleTelemetryMessage> record) {
        VehicleTelemetryMessage message = record.value();

        log.debug("Received telemetry for analytics: vehicle={}, speed={}, status={}, fuel_level={}",
                message.getVehicleId(),
                message.getSpeed(),
                message.getVehicleStatus(),
                message.getFuelLevel());

        try {
            analyticsService.processTelemetry(message);
        } catch (Exception e) {
            log.error("Error processing telemetry for vehicle {}: {}",
                    message.getVehicleId(), e.getMessage());
        }
    }
}
