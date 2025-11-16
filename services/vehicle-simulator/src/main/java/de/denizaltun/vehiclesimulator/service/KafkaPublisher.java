package de.denizaltun.vehiclesimulator.service;

import de.denizaltun.vehiclesimulator.model.VehicleTelemetry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Handles publishing telemetry data to Kafka.
 * Single Responsibility: Only concerned with Kafka publishing logic.
 */

@Slf4j                    // Lombok: gives log.debug(), log.error()
@Service                  // Spring: a service bean
@RequiredArgsConstructor  // Lombok: creates constructor with 'final' fields
public class KafkaPublisher {

    private static final String TOPIC_NAME = "vehicle-telemetry";

    private final KafkaTemplate<String, VehicleTelemetry> kafkaTemplate;

    /**
     * Publishes telemetry to Kafka topic.
     * Uses vehicleId as the message key for partitioning.
     */
    public void publish(VehicleTelemetry telemetry) {
        try {
            // Send to Kafka broker:
            //   - Topic: "vehicle-telemetry" (like CAN MESSAGE_ID)
            //   - Key: vehicleId (for partitioning & ordering)
            //   - Value: the actual telemetry data
            kafkaTemplate.send(TOPIC_NAME, telemetry.vehicleId(), telemetry);

            // Log for debugging (not visible in production)
            log.debug("Published telemetry for vehicle: {}, status: {}, speed: {} km/h",
                    telemetry.vehicleId(),
                    telemetry.status(),
                    String.format("%.1f", telemetry.speed()));
        } catch (Exception e) {
            // If Kafka is down or network issue
            log.error("Failed to publish telemetry for vehicle: {}", telemetry.vehicleId(), e);
        }
    }

}
