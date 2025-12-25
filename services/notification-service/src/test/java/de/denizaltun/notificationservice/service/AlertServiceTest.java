package de.denizaltun.notificationservice.service;

import de.denizaltun.notificationservice.dto.AlertEvent;
import de.denizaltun.notificationservice.model.Alert;
import de.denizaltun.notificationservice.model.AlertStatus;
import de.denizaltun.notificationservice.model.AlertType;
import de.denizaltun.notificationservice.model.VehicleType;
import de.denizaltun.notificationservice.repository.AlertRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AlertService
 *
 * Key Testing Concepts:
 * - @ExtendWith(MockitoExtension.class) - Enables Mockito in JUnit 5
 * - @Mock - Creates fake dependencies (AlertRepository)
 * - @InjectMocks - Creates real AlertService with mocked dependencies injected
 * - when().thenReturn() - Defines mock behavior
 * - verify() - Checks if methods were called
 * - ArgumentCaptor - Captures arguments passed to mocked methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService Unit Tests")
class AlertServiceTest {

    // MOCK: create a fake AlertRepository (no real DB calls)
    @Mock
    private AlertRepository alertRepository;

    // Inject Mocks: create real AlertService with mocked repository
    @InjectMocks
    private AlertService alertService;

    // Test data - reusable across tests
    private AlertEvent sampleAlertEvent;
    private Alert sampleAlert;

    /**
    * Setup method runs before EACH test
    * Creates fresh test data to avoid test pollution
    */
    @BeforeEach
    void setUp() {
        // create sample AlertEvent
        sampleAlertEvent = AlertEvent.builder()
                .vehicleId("FIRE_TRUCK_001")
                .vehicleType(VehicleType.FIRE_TRUCK)
                .alertType(AlertType.LOW_FUEL)
                .message("Fuel level below 20%")
                .thresholdValue(20.0)
                .actualValue(15.5)
                .build();

        // create sample Alert entity
        sampleAlert = Alert.builder()
                .id(1L)
                .vehicleId("FIRE_TRUCK_001")
                .vehicleType(VehicleType.FIRE_TRUCK)
                .alertType(AlertType.LOW_FUEL)
                .status(AlertStatus.ACTIVE)
                .message("Fuel Level below 20%")
                .thresholdValue(20.0)
                .actualValue(15.5)
                .build();
    }

    /**
     * NESTED TEST CLASS: Groups related tests together
     * Tests for the processAlertEvent() method
     */
    @Nested
    @DisplayName("processAlertEvent() Tests")
    class ProcessAlertEventTests {

        @Test
        @DisplayName("Should create new alert when no active alert exists")
        void shouldCreateNewAlertWhenNoneExists() {
            // ARRANGE: setup mock behavior
            // When repository checks for existing alert, return empty (no duplicate)
            when(alertRepository.findByVehicleIdAndAlertTypeAndStatus(
                    eq("FIRE_TRUCK_001"),
                    eq(AlertType.LOW_FUEL),
                    eq(AlertStatus.ACTIVE)
            )).thenReturn(Optional.empty());

            // When repo saves an alert, return it with an ID
            when(alertRepository.save(any(Alert.class))).thenReturn(sampleAlert);

            // ACT: call the tested method
            Alert result = alertService.processAlertEvent(sampleAlertEvent);

            // Assert: verify the results
            assertThat(result).isNotNull();
            assertThat(result.getVehicleId()).isEqualTo("FIRE_TRUCK_001");
            assertThat(result.getAlertType()).isEqualTo(AlertType.LOW_FUEL);
            assertThat(result.getStatus()).isEqualTo(AlertStatus.ACTIVE);

            // VERIFY: Check that repository methods were called correctly
            verify(alertRepository, times(1)).findByVehicleIdAndAlertTypeAndStatus(
                    eq("FIRE_TRUCK_001"),
                    eq(AlertType.LOW_FUEL),
                    eq(AlertStatus.ACTIVE)
            );
            verify(alertRepository, times(1)).save(any(Alert.class));
        }

        @Test
        @DisplayName("Should return existing alert when active duplicate exists (deduplication)")
        void shouldReturnExistingAlertWhenDuplicateExists() {

            // ARRANGE: Mock returns an existing active alert (duplicate detected!)
            when(alertRepository.findByVehicleIdAndAlertTypeAndStatus(
                    eq("FIRE_TRUCK_001"),
                    eq(AlertType.LOW_FUEL),
                    eq(AlertStatus.ACTIVE)
            )).thenReturn(Optional.of(sampleAlert));

            // ACT: call the method
            Alert result = alertService.processAlertEvent(sampleAlertEvent);

            // ASSERT: should return the existing alert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result).isSameAs(sampleAlert); // Same object reference

            // VERIFY: Should check for duplicates but NOT save a new alert
            verify(alertRepository, times(1)).findByVehicleIdAndAlertTypeAndStatus(
                    any(), any(), any()
            );
            verify(alertRepository, never()).save(any(Alert.class)); // IMPORTANT: No save!
        }

        @Test
        @DisplayName("Should correctly map all fields from AlertEvent to Alert")
        void shouldMapAllFieldsCorrectly() {
            // ARRANGE
            when(alertRepository.findByVehicleIdAndAlertTypeAndStatus(
                    any(), any(), any()
            )).thenReturn(Optional.empty());

            when(alertRepository.save(any(Alert.class))).thenReturn(sampleAlert);

            // ACT
            alertService.processAlertEvent(sampleAlertEvent);

            // CAPTURE: What was passed to save()?
            ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
            verify(alertRepository).save(alertCaptor.capture());
            Alert capturedAlert = alertCaptor.getValue();

            // ASSERT: Check all fields were mapped correctly
            assertThat(capturedAlert.getVehicleId()).isEqualTo(sampleAlertEvent.getVehicleId());
            assertThat(capturedAlert.getVehicleType()).isEqualTo(sampleAlertEvent.getVehicleType());
            assertThat(capturedAlert.getAlertType()).isEqualTo(sampleAlertEvent.getAlertType());
            assertThat(capturedAlert.getMessage()).isEqualTo(sampleAlertEvent.getMessage());
            assertThat(capturedAlert.getThresholdValue()).isEqualTo(sampleAlertEvent.getThresholdValue());
            assertThat(capturedAlert.getActualValue()).isEqualTo(sampleAlertEvent.getActualValue());
            assertThat(capturedAlert.getStatus()).isEqualTo(AlertStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should handle different vehicle types correctly")
        void shouldHandleDifferentVehicleTypes() {
            // ARRANGE: Create event for AMBULANCE
            AlertEvent ambulanceEvent = AlertEvent.builder()
                    .vehicleId("AMBULANCE_005")
                    .vehicleType(VehicleType.AMBULANCE)
                    .alertType(AlertType.HIGH_ENGINE_TEMP)
                    .message("Engine temperature critical")
                    .thresholdValue(95.0)
                    .actualValue(98.5)
                    .build();

            Alert ambulanceAlert = Alert.builder()
                    .id(2L)
                    .vehicleId("AMBULANCE_005")
                    .vehicleType(VehicleType.AMBULANCE)
                    .alertType(AlertType.HIGH_ENGINE_TEMP)
                    .status(AlertStatus.ACTIVE)
                    .message("Engine temperature critical")
                    .thresholdValue(95.0)
                    .actualValue(98.5)
                    .build();

            when(alertRepository.findByVehicleIdAndAlertTypeAndStatus(
                    any(), any(), any()
            )).thenReturn(Optional.empty());

            when(alertRepository.save(any(Alert.class))).thenReturn(ambulanceAlert);

            // ACT
            Alert result = alertService.processAlertEvent(ambulanceEvent);

            // ASSERT
            assertThat(result.getVehicleType()).isEqualTo(VehicleType.AMBULANCE);
            assertThat(result.getVehicleId()).isEqualTo("AMBULANCE_005");
        }
    }

    /**
     * NESTED TEST CLASS: Tests for acknowledgeAlert()
     */
    @Nested
    @DisplayName("acknowledgeAlert() Tests")
    class AcknowledgeAlertTests {

        @Test
        @DisplayName("Should change alert status to ACKNOWLEDGED")
        void shouldChangeStatusToAcknowledged() {
            // ARRANGE
            when(alertRepository.findById(1L)).thenReturn(Optional.of(sampleAlert));
            when(alertRepository.save(any(Alert.class))).thenReturn(sampleAlert);

            // ACT
            Alert result = alertService.acknowledgeAlert(1L);

            // ASSERT
            assertThat(result.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
            verify(alertRepository).save(sampleAlert);
        }

        @Test
        @DisplayName("Should set acknowledgedAt timestamp")
        void shouldSetAcknowledgedAtTimestamp() {
            // ARRANGE
            when(alertRepository.findById(1L)).thenReturn(Optional.of(sampleAlert));
            when(alertRepository.save(any(Alert.class))).thenReturn(sampleAlert);

            LocalDateTime beforeAck = LocalDateTime.now().minusSeconds(1);

            // ACT
            alertService.acknowledgeAlert(1L);

            LocalDateTime afterAck = LocalDateTime.now().plusSeconds(1);

            // ASSERT: acknowledgedAt should be set to "now"
            assertThat(sampleAlert.getAcknowledgedAt()).isNotNull();
            assertThat(sampleAlert.getAcknowledgedAt()).isBetween(beforeAck, afterAck);
        }

        @Test
        @DisplayName("Should throw exception when alert not found")
        void shouldThrowExceptionWhenAlertNotFound() {
            // ARRANGE: Repository returns empty (alert doesn't exist)
            when(alertRepository.findById(999L)).thenReturn(Optional.empty());

            // ACT & ASSERT: Should throw RuntimeException
            assertThatThrownBy(() -> alertService.acknowledgeAlert(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Alert not found: 999");

            // VERIFY: Should NOT try to save
            verify(alertRepository, never()).save(any(Alert.class));
        }
    }

    /**
     * NESTED TEST CLASS: Tests for resolveAlert()
     */
    @Nested
    @DisplayName("resolveAlert() Tests")
    class ResolveAlertTests {

        @Test
        @DisplayName("Should change alert status to RESOLVED")
        void shouldChangeStatusToResolved() {
            // ARRANGE
            when(alertRepository.findById(1L)).thenReturn(Optional.of(sampleAlert));
            when(alertRepository.save(any(Alert.class))).thenReturn(sampleAlert);

            // ACT
            Alert result = alertService.resolveAlert(1L);

            // ASSERT
            assertThat(result.getStatus()).isEqualTo(AlertStatus.RESOLVED);
            verify(alertRepository).save(sampleAlert);
        }

        @Test
        @DisplayName("Should set resolvedAt timestamp")
        void shouldSetResolvedAtTimestamp() {
            // ARRANGE
            when(alertRepository.findById(1L)).thenReturn(Optional.of(sampleAlert));
            when(alertRepository.save(any(Alert.class))).thenReturn(sampleAlert);

            LocalDateTime beforeResolve = LocalDateTime.now().minusSeconds(1);

            // ACT
            alertService.resolveAlert(1L);

            LocalDateTime afterResolve = LocalDateTime.now().plusSeconds(1);

            // ASSERT: resolvedAt should be set to "now"
            assertThat(sampleAlert.getResolvedAt()).isNotNull();
            assertThat(sampleAlert.getResolvedAt()).isBetween(beforeResolve, afterResolve);
        }

        @Test
        @DisplayName("Should throw exception when alert not found")
        void shouldThrowExceptionWhenAlertNotFound() {
            // ARRANGE
            when(alertRepository.findById(999L)).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> alertService.resolveAlert(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Alert not found: 999");

            verify(alertRepository, never()).save(any(Alert.class));
        }
    }

    /**
     * NESTED TEST CLASS: Tests for query methods
     */
    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {

        @Test
        @DisplayName("getActiveAlerts() should return active alerts from repository")
        void shouldGetActiveAlerts() {
            // ARRANGE
            Alert alert2 = Alert.builder()
                    .id(2L)
                    .vehicleId("POLICE_002")
                    .status(AlertStatus.ACTIVE)
                    .build();

            List<Alert> activeAlerts = Arrays.asList(sampleAlert, alert2);
            when(alertRepository.findByStatus(AlertStatus.ACTIVE)).thenReturn(activeAlerts);

            // ACT
            List<Alert> result = alertService.getActiveAlerts();

            // ASSERT
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(sampleAlert, alert2);
            verify(alertRepository).findByStatus(AlertStatus.ACTIVE);
        }

        @Test
        @DisplayName("getAlertsByVehicle() should return alerts for specific vehicle")
        void shouldGetAlertsByVehicle() {
            // ARRANGE
            List<Alert> vehicleAlerts = Arrays.asList(sampleAlert);
            when(alertRepository.findByVehicleId("FIRE_TRUCK_001")).thenReturn(vehicleAlerts);

            // ACT
            List<Alert> result = alertService.getAlertsByVehicle("FIRE_TRUCK_001");

            // ASSERT
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVehicleId()).isEqualTo("FIRE_TRUCK_001");
            verify(alertRepository).findByVehicleId("FIRE_TRUCK_001");
        }

        @Test
        @DisplayName("getAlertsByVehicleType() should return alerts for vehicle type")
        void shouldGetAlertsByVehicleType() {
            // ARRANGE
            List<Alert> fireTruckAlerts = Arrays.asList(sampleAlert);
            when(alertRepository.findByVehicleType(VehicleType.FIRE_TRUCK))
                    .thenReturn(fireTruckAlerts);

            // ACT
            List<Alert> result = alertService.getAlertsByVehicleType(VehicleType.FIRE_TRUCK);

            // ASSERT
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVehicleType()).isEqualTo(VehicleType.FIRE_TRUCK);
            verify(alertRepository).findByVehicleType(VehicleType.FIRE_TRUCK);
        }

        @Test
        @DisplayName("getAllAlerts() should return all alerts")
        void shouldGetAllAlerts() {
            // ARRANGE
            Alert alert2 = Alert.builder().id(2L).vehicleId("POLICE_002").build();
            Alert alert3 = Alert.builder().id(3L).vehicleId("AMBULANCE_003").build();

            List<Alert> allAlerts = Arrays.asList(sampleAlert, alert2, alert3);
            when(alertRepository.findAll()).thenReturn(allAlerts);

            // ACT
            List<Alert> result = alertService.getAllAlerts();

            // ASSERT
            assertThat(result).hasSize(3);
            verify(alertRepository).findAll();
        }

        @Test
        @DisplayName("getActiveAlertCount() should return count of active alerts")
        void shouldGetActiveAlertCount() {
            // ARRANGE
            when(alertRepository.countByStatus(AlertStatus.ACTIVE)).thenReturn(5L);

            // ACT
            long count = alertService.getActiveAlertCount();

            // ASSERT
            assertThat(count).isEqualTo(5L);
            verify(alertRepository).countByStatus(AlertStatus.ACTIVE);
        }
    }
}