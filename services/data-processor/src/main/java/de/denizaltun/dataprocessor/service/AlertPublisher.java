package de.denizaltun.dataprocessor.service;

import de.denizaltun.dataprocessor.dto.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertPublisher {

    private static final String ALERT_TOPIC = "vehicle-alerts";

    private final KafkaTemplate<String, AlertEvent> alertKafkaTemplate;

    public void publishAlert(AlertEvent alertEvent) {
        log.info("Publishing alert: {} for vehicle {}",
                alertEvent.getAlertType(), alertEvent.getVehicleId());

        alertKafkaTemplate.send(ALERT_TOPIC, alertEvent.getVehicleId(), alertEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish alert: {}", ex.getMessage());
                    } else {
                        log.debug("Alert published successfully to partition {}",
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
