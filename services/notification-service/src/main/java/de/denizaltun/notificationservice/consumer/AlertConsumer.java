package de.denizaltun.notificationservice.consumer;

import de.denizaltun.notificationservice.dto.AlertEvent;
import de.denizaltun.notificationservice.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertConsumer {

    private final AlertService alertService;

    @KafkaListener(topics = "vehicle-alerts", groupId = "notification-service-group")
    public void consumeAlert(AlertEvent alertEvent) {
        log.info("Received alert event: {} for vehicle {}",
                alertEvent.getAlertType(), alertEvent.getVehicleId());

        try {
            alertService.processAlertEvent(alertEvent);
        } catch (Exception e) {
            log.error("Error processing alert event: {}", e.getMessage(), e);
        }
    }
}
