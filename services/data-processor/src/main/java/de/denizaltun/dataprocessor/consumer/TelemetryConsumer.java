package de.denizaltun.dataprocessor.consumer;

import de.denizaltun.dataprocessor.dto.VehicleTelemetryMessage;
import de.denizaltun.dataprocessor.service.TelemetryProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens for vehicle telemetry messages.
 * Consumes from 'vehicle.telemetry' topic and processes each message.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryConsumer {

    private final TelemetryProcessingService processingService;

    /**
     * Listen to vehicle.telemetry topic and process each message.
     *
     * @param message The deserialized telemetry message
     * @param partition The Kafka partition this message came from
     * @param offset The offset of this message in the partition
     */
    @KafkaListener(
            topics = "vehicle-telemetry",
            groupId = "data-processor-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTelemetry(
            @Payload VehicleTelemetryMessage message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received telemetry from partition: {}, offset: {}, vehicle: {}",
                partition, offset, message.getVehicleId());

        try {
            processingService.processTelemetry(message);
        } catch (Exception e) {
            log.error("Error processing telemetry for vehicle {}: {}",
                    message.getVehicleId(), e.getMessage(), e);

        }
    }
}
