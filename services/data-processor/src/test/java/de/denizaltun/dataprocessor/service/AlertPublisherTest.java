package de.denizaltun.dataprocessor.service;

import de.denizaltun.dataprocessor.dto.AlertEvent;
import de.denizaltun.dataprocessor.dto.AlertType;
import de.denizaltun.dataprocessor.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AlertPublisher
 * Tests Kafka message publishing logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlertPublisher Unit Tests")
class AlertPublisherTest {

    @Mock
    private KafkaTemplate<String, AlertEvent> alertKafkaTemplate;

    @InjectMocks
    private AlertPublisher alertPublisher;

    private AlertEvent sampleAlertEvent;
    private CompletableFuture<SendResult<String, AlertEvent>> successFuture;

    @BeforeEach
    void setUp() {
        sampleAlertEvent = AlertEvent.builder()
                .vehicleId("FIRE_TRUCK_001")
                .vehicleType(VehicleType.FIRE_TRUCK)
                .alertType(AlertType.LOW_FUEL)
                .message("Fuel level below 20%")
                .thresholdValue(20.0)
                .actualValue(15.5)
                .timestamp(LocalDateTime.now())
                .build();

        // Mock successful Kafka send
        successFuture = CompletableFuture.completedFuture(mock(SendResult.class));
    }

    @Test
    @DisplayName("Should publish alert to 'vehicle-alerts' topic")
    void shouldPublishAlertToCorrectTopic() {
        when(alertKafkaTemplate.send(anyString(), anyString(), any(AlertEvent.class)))
                .thenReturn(successFuture);

        alertPublisher.publishAlert(sampleAlertEvent);

        verify(alertKafkaTemplate).send(
                eq("vehicle-alerts"),
                anyString(),
                any(AlertEvent.class)
        );
    }

    @Test
    @DisplayName("Should use vehicleId as message key")
    void shouldUseVehicleIdAsKey() {
        when(alertKafkaTemplate.send(anyString(), anyString(), any(AlertEvent.class)))
                .thenReturn(successFuture);

        alertPublisher.publishAlert(sampleAlertEvent);

        verify(alertKafkaTemplate).send(
                eq("vehicle-alerts"),
                eq("FIRE_TRUCK_001"),  // vehicleId as key
                any(AlertEvent.class)
        );
    }

    @Test
    @DisplayName("Should publish the complete AlertEvent")
    void shouldPublishCompleteAlertEvent() {
        when(alertKafkaTemplate.send(anyString(), anyString(), any(AlertEvent.class)))
                .thenReturn(successFuture);

        alertPublisher.publishAlert(sampleAlertEvent);

        ArgumentCaptor<AlertEvent> captor = ArgumentCaptor.forClass(AlertEvent.class);
        verify(alertKafkaTemplate).send(
                eq("vehicle-alerts"),
                eq("FIRE_TRUCK_001"),
                captor.capture()
        );

        AlertEvent captured = captor.getValue();
        assertThat(captured.getVehicleId()).isEqualTo("FIRE_TRUCK_001");
        assertThat(captured.getVehicleType()).isEqualTo(VehicleType.FIRE_TRUCK);
        assertThat(captured.getAlertType()).isEqualTo(AlertType.LOW_FUEL);
        assertThat(captured.getMessage()).isEqualTo("Fuel level below 20%");
        assertThat(captured.getThresholdValue()).isEqualTo(20.0);
        assertThat(captured.getActualValue()).isEqualTo(15.5);
    }

    @Test
    @DisplayName("Should handle different alert types")
    void shouldPublishDifferentAlertTypes() {
        AlertEvent highTempAlert = AlertEvent.builder()
                .vehicleId("AMBULANCE_002")
                .vehicleType(VehicleType.AMBULANCE)
                .alertType(AlertType.HIGH_ENGINE_TEMP)
                .message("Engine temperature critical")
                .thresholdValue(95.0)
                .actualValue(98.5)
                .timestamp(LocalDateTime.now())
                .build();

        when(alertKafkaTemplate.send(anyString(), anyString(), any(AlertEvent.class)))
                .thenReturn(successFuture);

        alertPublisher.publishAlert(highTempAlert);

        ArgumentCaptor<AlertEvent> captor = ArgumentCaptor.forClass(AlertEvent.class);
        verify(alertKafkaTemplate).send(
                eq("vehicle-alerts"),
                eq("AMBULANCE_002"),
                captor.capture()
        );

        assertThat(captor.getValue().getAlertType()).isEqualTo(AlertType.HIGH_ENGINE_TEMP);
    }

    @Test
    @DisplayName("Should handle multiple consecutive publishes")
    void shouldHandleMultiplePublishes() {
        when(alertKafkaTemplate.send(anyString(), anyString(), any(AlertEvent.class)))
                .thenReturn(successFuture);

        AlertEvent alert1 = sampleAlertEvent;
        AlertEvent alert2 = AlertEvent.builder()
                .vehicleId("POLICE_003")
                .alertType(AlertType.LOW_BATTERY)
                .build();
        AlertEvent alert3 = AlertEvent.builder()
                .vehicleId("FIRE_TRUCK_001")
                .alertType(AlertType.HIGH_ENGINE_TEMP)
                .build();

        alertPublisher.publishAlert(alert1);
        alertPublisher.publishAlert(alert2);
        alertPublisher.publishAlert(alert3);

        verify(alertKafkaTemplate, times(3)).send(
                anyString(),
                anyString(),
                any(AlertEvent.class)
        );
    }

    @Test
    @DisplayName("Should not throw exception when Kafka send fails")
    void shouldHandleKafkaFailureGracefully() {
        CompletableFuture<SendResult<String, AlertEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));

        when(alertKafkaTemplate.send(anyString(), anyString(), any(AlertEvent.class)))
                .thenReturn(failedFuture);

        // Should not throw exception
        alertPublisher.publishAlert(sampleAlertEvent);

        verify(alertKafkaTemplate).send(
                eq("vehicle-alerts"),
                anyString(),
                any(AlertEvent.class)
        );
    }
}