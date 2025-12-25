package de.denizaltun.dataprocessor.service;

import de.denizaltun.dataprocessor.dto.AlertEvent;
import de.denizaltun.dataprocessor.dto.AlertType;
import de.denizaltun.dataprocessor.dto.VehicleTelemetryMessage;
import de.denizaltun.dataprocessor.model.VehicleStatus;
import de.denizaltun.dataprocessor.model.VehicleTelemetry;
import de.denizaltun.dataprocessor.model.VehicleType;
import de.denizaltun.dataprocessor.repository.VehicleTelemetryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TelemetryProcessingService Unit Tests")
class TelemetryProcessingServiceTest {

    @Mock
    private VehicleTelemetryRepository repository;

    @Mock
    private AlertPublisher alertPublisher;

    @InjectMocks
    private TelemetryProcessingService service;

    private VehicleTelemetryMessage validMessage;
    private VehicleTelemetry savedTelemetry;

    @BeforeEach
    void setUp() {
        validMessage = VehicleTelemetryMessage.builder()
                .vehicleId("FIRE_TRUCK_001")
                .vehicleType(VehicleType.FIRE_TRUCK)
                .vehicleStatus(VehicleStatus.IDLE)
                .timeStamp(LocalDateTime.now())
                .latitude(48.1351)
                .longitude(11.5820)
                .speed(0.0)
                .fuelLevel(50.0)
                .batteryVoltage(24.5)
                .engineTemp(75.0)
                .emergencyLightsActive(false)
                .build();

        savedTelemetry = VehicleTelemetry.builder()
                .id(1L)
                .vehicleId("FIRE_TRUCK_001")
                .vehicleStatus(VehicleStatus.IDLE)
                .timeStamp(LocalDateTime.now())
                .latitude(48.1351)
                .longitude(11.5820)
                .speed(0.0)
                .fuelLevel(50.0)
                .batteryVoltage(24.5)
                .engineTemp(75.0)
                .emergencyLightsActive(false)
                .build();
    }

    @Nested
    @DisplayName("processTelemetry() - Main Flow Tests")
    class ProcessTelemetryTests {

        @Test
        @DisplayName("Should save valid telemetry to database")
        void shouldSaveValidTelemetry() {
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            verify(repository, times(1)).save(any(VehicleTelemetry.class));
        }

        @Test
        @DisplayName("Should map all fields correctly when converting to entity")
        void shouldMapAllFieldsCorrectly() {
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            ArgumentCaptor<VehicleTelemetry> captor = ArgumentCaptor.forClass(VehicleTelemetry.class);
            verify(repository).save(captor.capture());
            VehicleTelemetry captured = captor.getValue();

            assertThat(captured.getVehicleId()).isEqualTo("FIRE_TRUCK_001");
            assertThat(captured.getVehicleStatus()).isEqualTo(VehicleStatus.IDLE);
            assertThat(captured.getFuelLevel()).isEqualTo(50.0);
            assertThat(captured.getBatteryVoltage()).isEqualTo(24.5);
            assertThat(captured.getEngineTemp()).isEqualTo(75.0);
            assertThat(captured.getLatitude()).isEqualTo(48.1351);
            assertThat(captured.getLongitude()).isEqualTo(11.5820);
        }

        @Test
        @DisplayName("Should not save telemetry with null vehicleId")
        void shouldNotSaveWithNullVehicleId() {
            validMessage.setVehicleId(null);

            service.processTelemetry(validMessage);

            verify(repository, never()).save(any(VehicleTelemetry.class));
            verify(alertPublisher, never()).publishAlert(any(AlertEvent.class));
        }

        @Test
        @DisplayName("Should not save telemetry with empty vehicleId")
        void shouldNotSaveWithEmptyVehicleId() {
            validMessage.setVehicleId("");

            service.processTelemetry(validMessage);

            verify(repository, never()).save(any(VehicleTelemetry.class));
        }

        @Test
        @DisplayName("Should not save telemetry with null timestamp")
        void shouldNotSaveWithNullTimestamp() {
            validMessage.setTimeStamp(null);

            service.processTelemetry(validMessage);

            verify(repository, never()).save(any(VehicleTelemetry.class));
        }

        @Test
        @DisplayName("Should not save telemetry with null coordinates")
        void shouldNotSaveWithNullCoordinates() {
            validMessage.setLatitude(null);

            service.processTelemetry(validMessage);

            verify(repository, never()).save(any(VehicleTelemetry.class));
        }
    }

    @Nested
    @DisplayName("Alert Detection - Low Fuel Tests")
    class LowFuelAlertTests {

        @Test
        @DisplayName("Should publish LOW_FUEL alert when fuel < 20%")
        void shouldPublishLowFuelAlert() {
            validMessage.setFuelLevel(15.5);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            ArgumentCaptor<AlertEvent> captor = ArgumentCaptor.forClass(AlertEvent.class);
            verify(alertPublisher, times(1)).publishAlert(captor.capture());

            AlertEvent alert = captor.getValue();
            assertThat(alert.getAlertType()).isEqualTo(AlertType.LOW_FUEL);
            assertThat(alert.getVehicleId()).isEqualTo("FIRE_TRUCK_001");
            assertThat(alert.getThresholdValue()).isEqualTo(20.0);
            assertThat(alert.getActualValue()).isEqualTo(15.5);
        }

        @Test
        @DisplayName("Should NOT publish alert when fuel >= 20%")
        void shouldNotPublishAlertWhenFuelNormal() {
            validMessage.setFuelLevel(20.0);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            verify(alertPublisher, never()).publishAlert(any(AlertEvent.class));
        }

        @Test
        @DisplayName("Should publish alert when fuel exactly at 19.99%")
        void shouldPublishAlertAtEdgeCase() {
            validMessage.setFuelLevel(19.99);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            verify(alertPublisher, times(1)).publishAlert(any(AlertEvent.class));
        }
    }

    @Nested
    @DisplayName("Alert Detection - High Engine Temperature Tests")
    class HighEngineTempAlertTests {

        @Test
        @DisplayName("Should publish HIGH_ENGINE_TEMP alert when temp > 95°C")
        void shouldPublishHighTempAlert() {
            validMessage.setEngineTemp(98.5);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            ArgumentCaptor<AlertEvent> captor = ArgumentCaptor.forClass(AlertEvent.class);
            verify(alertPublisher, times(1)).publishAlert(captor.capture());

            AlertEvent alert = captor.getValue();
            assertThat(alert.getAlertType()).isEqualTo(AlertType.HIGH_ENGINE_TEMP);
            assertThat(alert.getThresholdValue()).isEqualTo(95.0);
            assertThat(alert.getActualValue()).isEqualTo(98.5);
        }

        @Test
        @DisplayName("Should NOT publish alert when temp <= 95°C")
        void shouldNotPublishAlertWhenTempNormal() {
            validMessage.setEngineTemp(95.0);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            verify(alertPublisher, never()).publishAlert(any(AlertEvent.class));
        }
    }

    @Nested
    @DisplayName("Alert Detection - Battery Voltage Tests")
    class BatteryVoltageAlertTests {

        @Test
        @DisplayName("Should publish LOW_BATTERY alert for FIRE_TRUCK when voltage < 23.0V")
        void shouldPublishLowBatteryAlertForFireTruck() {
            validMessage.setVehicleType(VehicleType.FIRE_TRUCK);
            validMessage.setBatteryVoltage(22.5);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            ArgumentCaptor<AlertEvent> captor = ArgumentCaptor.forClass(AlertEvent.class);
            verify(alertPublisher, times(1)).publishAlert(captor.capture());

            AlertEvent alert = captor.getValue();
            assertThat(alert.getAlertType()).isEqualTo(AlertType.LOW_BATTERY);
            assertThat(alert.getThresholdValue()).isEqualTo(23.0);
            assertThat(alert.getActualValue()).isEqualTo(22.5);
        }

        @Test
        @DisplayName("Should NOT publish alert for FIRE_TRUCK when voltage >= 23.0V")
        void shouldNotPublishAlertForFireTruckWhenVoltageNormal() {
            validMessage.setVehicleType(VehicleType.FIRE_TRUCK);
            validMessage.setBatteryVoltage(24.0);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            verify(alertPublisher, never()).publishAlert(any(AlertEvent.class));
        }

        @Test
        @DisplayName("Should publish LOW_BATTERY alert for POLICE when voltage < 11.5V")
        void shouldPublishLowBatteryAlertForPolice() {
            validMessage.setVehicleType(VehicleType.POLICE);
            validMessage.setBatteryVoltage(11.0);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            ArgumentCaptor<AlertEvent> captor = ArgumentCaptor.forClass(AlertEvent.class);
            verify(alertPublisher, times(1)).publishAlert(captor.capture());

            AlertEvent alert = captor.getValue();
            assertThat(alert.getAlertType()).isEqualTo(AlertType.LOW_BATTERY);
            assertThat(alert.getThresholdValue()).isEqualTo(11.5);
            assertThat(alert.getActualValue()).isEqualTo(11.0);
        }

        @Test
        @DisplayName("Should publish LOW_BATTERY alert for AMBULANCE when voltage < 11.5V")
        void shouldPublishLowBatteryAlertForAmbulance() {
            validMessage.setVehicleType(VehicleType.AMBULANCE);
            validMessage.setBatteryVoltage(11.2);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            ArgumentCaptor<AlertEvent> captor = ArgumentCaptor.forClass(AlertEvent.class);
            verify(alertPublisher, times(1)).publishAlert(captor.capture());

            AlertEvent alert = captor.getValue();
            assertThat(alert.getAlertType()).isEqualTo(AlertType.LOW_BATTERY);
            assertThat(alert.getThresholdValue()).isEqualTo(11.5);
        }

        @Test
        @DisplayName("Should NOT publish battery alert when voltage is null")
        void shouldNotPublishAlertWhenVoltageNull() {
            validMessage.setBatteryVoltage(null);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            verify(alertPublisher, never()).publishAlert(any(AlertEvent.class));
        }

        @Test
        @DisplayName("Should NOT publish battery alert when vehicleType is null")
        void shouldNotPublishAlertWhenVehicleTypeNull() {
            validMessage.setVehicleType(null);
            validMessage.setBatteryVoltage(10.0); // Very low, but no type to determine threshold
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            verify(alertPublisher, never()).publishAlert(any(AlertEvent.class));
        }
    }

    @Nested
    @DisplayName("Alert Detection - Emergency Lights Tests")
    class EmergencyLightsAlertTests {

        @Test
        @DisplayName("Should publish EMERGENCY_STATUS_CHANGE alert when lights active")
        void shouldPublishEmergencyStatusChangeAlert() {
            validMessage.setEmergencyLightsActive(true);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            ArgumentCaptor<AlertEvent> captor = ArgumentCaptor.forClass(AlertEvent.class);
            verify(alertPublisher, times(1)).publishAlert(captor.capture());

            AlertEvent alert = captor.getValue();
            assertThat(alert.getAlertType()).isEqualTo(AlertType.EMERGENCY_STATUS_CHANGE);
            assertThat(alert.getMessage()).contains("Emergency lights activated");
        }

        @Test
        @DisplayName("Should NOT publish alert when emergency lights inactive")
        void shouldNotPublishAlertWhenLightsInactive() {
            validMessage.setEmergencyLightsActive(false);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            verify(alertPublisher, never()).publishAlert(any(AlertEvent.class));
        }

        @Test
        @DisplayName("Should NOT publish alert when emergency lights null")
        void shouldNotPublishAlertWhenLightsNull() {
            validMessage.setEmergencyLightsActive(null);
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            verify(alertPublisher, never()).publishAlert(any(AlertEvent.class));
        }
    }

    @Nested
    @DisplayName("Alert Detection - Multiple Alerts Tests")
    class MultipleAlertsTests {

        @Test
        @DisplayName("Should publish multiple alerts when multiple conditions triggered")
        void shouldPublishMultipleAlerts() {
            validMessage.setFuelLevel(15.0);           // LOW_FUEL
            validMessage.setEngineTemp(98.0);          // HIGH_ENGINE_TEMP
            validMessage.setEmergencyLightsActive(true); // EMERGENCY_STATUS_CHANGE
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            // Should publish 3 alerts
            verify(alertPublisher, times(3)).publishAlert(any(AlertEvent.class));
        }

        @Test
        @DisplayName("Should publish 4 alerts when all conditions triggered")
        void shouldPublishFourAlerts() {
            validMessage.setVehicleType(VehicleType.FIRE_TRUCK);
            validMessage.setFuelLevel(10.0);           // LOW_FUEL
            validMessage.setEngineTemp(100.0);         // HIGH_ENGINE_TEMP
            validMessage.setBatteryVoltage(22.0);      // LOW_BATTERY (24V system)
            validMessage.setEmergencyLightsActive(true); // EMERGENCY_STATUS_CHANGE
            when(repository.save(any(VehicleTelemetry.class))).thenReturn(savedTelemetry);

            service.processTelemetry(validMessage);

            verify(alertPublisher, times(4)).publishAlert(any(AlertEvent.class));
        }
    }

    @Nested
    @DisplayName("Statistics Methods Tests")
    class StatisticsTests {

        @Test
        @DisplayName("getTotalTelemetryCount() should return repository count")
        void shouldGetTotalCount() {
            when(repository.count()).thenReturn(100L);

            long count = service.getTotalTelemetryCount();

            assertThat(count).isEqualTo(100L);
            verify(repository).count();
        }

        @Test
        @DisplayName("getVehicleTelemetryCount() should return vehicle-specific count")
        void shouldGetVehicleCount() {
            when(repository.countByVehicleId("FIRE_TRUCK_001")).thenReturn(25L);

            long count = service.getVehicleTelemetryCount("FIRE_TRUCK_001");

            assertThat(count).isEqualTo(25L);
            verify(repository).countByVehicleId("FIRE_TRUCK_001");
        }
    }

}