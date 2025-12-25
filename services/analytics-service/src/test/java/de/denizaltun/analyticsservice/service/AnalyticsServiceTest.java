package de.denizaltun.analyticsservice.service;

import de.denizaltun.analyticsservice.dto.VehicleStatus;
import de.denizaltun.analyticsservice.dto.VehicleTelemetryMessage;
import de.denizaltun.analyticsservice.dto.VehicleType;
import de.denizaltun.analyticsservice.model.FleetMetrics;
import de.denizaltun.analyticsservice.model.VehicleMetrics;
import de.denizaltun.analyticsservice.repository.DailyFleetMetricsRepository;
import de.denizaltun.analyticsservice.repository.DailyVehicleMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Unit Tests")
class AnalyticsServiceTest {

    @Mock
    private DailyFleetMetricsRepository fleetMetricsRepository;

    @Mock
    private DailyVehicleMetricsRepository vehicleMetricsRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(fleetMetricsRepository, vehicleMetricsRepository);
    }

    @Nested
    @DisplayName("processTelemetry() Tests")
    class ProcessTelemetryTests {

        @Test
        @DisplayName("Should register new vehicle on first telemetry")
        void shouldRegisterNewVehicle() {
            VehicleTelemetryMessage message = createMessage("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK);

            analyticsService.processTelemetry(message);

            VehicleMetrics metrics = analyticsService.getVehicleMetrics("FIRE_TRUCK_001");
            assertThat(metrics).isNotNull();
            assertThat(metrics.getVehicleId()).isEqualTo("FIRE_TRUCK_001");
            assertThat(metrics.getVehicleType()).isEqualTo(VehicleType.FIRE_TRUCK);
        }

        @Test
        @DisplayName("Should update vehicle metrics on subsequent telemetry")
        void shouldUpdateExistingVehicleMetrics() {
            VehicleTelemetryMessage message1 = createMessage("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK);
            VehicleTelemetryMessage message2 = createMessage("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK);

            analyticsService.processTelemetry(message1);
            analyticsService.processTelemetry(message2);

            VehicleMetrics metrics = analyticsService.getVehicleMetrics("FIRE_TRUCK_001");
            assertThat(metrics.getTelemetryCount().get()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should increment fleet telemetry counter")
        void shouldIncrementFleetTelemetryCounter() {
            VehicleTelemetryMessage message = createMessage("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK);

            analyticsService.processTelemetry(message);

            FleetMetrics fleetMetrics = analyticsService.getFleetMetrics();
            assertThat(fleetMetrics.getTotalTelemetryReceived().get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should track vehicle status changes")
        void shouldTrackStatusChanges() {
            VehicleTelemetryMessage message1 = createMessageWithStatus("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, VehicleStatus.IDLE);
            VehicleTelemetryMessage message2 = createMessageWithStatus("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, VehicleStatus.EN_ROUTE);

            analyticsService.processTelemetry(message1);
            analyticsService.processTelemetry(message2);

            FleetMetrics fleetMetrics = analyticsService.getFleetMetrics();
            // Status changed from IDLE to EN_ROUTE
            assertThat(fleetMetrics.getCurrentStatusCounts().get(VehicleStatus.EN_ROUTE).get()).isEqualTo(1);
            assertThat(fleetMetrics.getCurrentStatusCounts().get(VehicleStatus.IDLE).get()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should register vehicle by type in fleet metrics")
        void shouldRegisterVehicleByType() {
            VehicleTelemetryMessage fireTruck = createMessage("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK);
            VehicleTelemetryMessage ambulance = createMessage("AMBULANCE_001", VehicleType.AMBULANCE);

            analyticsService.processTelemetry(fireTruck);
            analyticsService.processTelemetry(ambulance);

            FleetMetrics fleetMetrics = analyticsService.getFleetMetrics();
            assertThat(fleetMetrics.getVehiclesByType().get(VehicleType.FIRE_TRUCK).get()).isEqualTo(1);
            assertThat(fleetMetrics.getVehiclesByType().get(VehicleType.AMBULANCE).get()).isEqualTo(1);
            assertThat(fleetMetrics.getTotalVehicles().get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Vehicle Metrics Calculations")
    class VehicleMetricsCalculationsTests {

        @Test
        @DisplayName("Should calculate average speed correctly")
        void shouldCalculateAverageSpeed() {
            VehicleTelemetryMessage msg1 = createMessageWithSpeed("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 50.0);
            VehicleTelemetryMessage msg2 = createMessageWithSpeed("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 60.0);
            VehicleTelemetryMessage msg3 = createMessageWithSpeed("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 70.0);

            analyticsService.processTelemetry(msg1);
            analyticsService.processTelemetry(msg2);
            analyticsService.processTelemetry(msg3);

            VehicleMetrics metrics = analyticsService.getVehicleMetrics("FIRE_TRUCK_001");
            assertThat(metrics.getAverageSpeed()).isEqualTo(60.0);
        }

        @Test
        @DisplayName("Should track fuel consumption")
        void shouldTrackFuelConsumption() {
            VehicleTelemetryMessage msg1 = createMessageWithFuel("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 100.0);
            VehicleTelemetryMessage msg2 = createMessageWithFuel("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 90.0);
            VehicleTelemetryMessage msg3 = createMessageWithFuel("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 85.0);

            analyticsService.processTelemetry(msg1);
            analyticsService.processTelemetry(msg2);
            analyticsService.processTelemetry(msg3);

            VehicleMetrics metrics = analyticsService.getVehicleMetrics("FIRE_TRUCK_001");
            // Consumed: (100-90) + (90-85) = 10 + 5 = 15
            assertThat(metrics.getTotalFuelConsumed()).isEqualTo(15.0);
        }

        @Test
        @DisplayName("Should ignore fuel increases (refueling)")
        void shouldIgnoreFuelIncreases() {
            VehicleTelemetryMessage msg1 = createMessageWithFuel("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 50.0);
            VehicleTelemetryMessage msg2 = createMessageWithFuel("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 100.0); // Refueled
            VehicleTelemetryMessage msg3 = createMessageWithFuel("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 95.0);

            analyticsService.processTelemetry(msg1);
            analyticsService.processTelemetry(msg2);
            analyticsService.processTelemetry(msg3);

            VehicleMetrics metrics = analyticsService.getVehicleMetrics("FIRE_TRUCK_001");
            // Should only count: 100-95 = 5 (ignore the refuel)
            assertThat(metrics.getTotalFuelConsumed()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Should track status distribution")
        void shouldTrackStatusDistribution() {
            VehicleTelemetryMessage idle1 = createMessageWithStatus("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, VehicleStatus.IDLE);
            VehicleTelemetryMessage idle2 = createMessageWithStatus("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, VehicleStatus.IDLE);
            VehicleTelemetryMessage responding1 = createMessageWithStatus("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, VehicleStatus.EN_ROUTE);
            VehicleTelemetryMessage responding2 = createMessageWithStatus("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, VehicleStatus.EN_ROUTE);

            analyticsService.processTelemetry(idle1);
            analyticsService.processTelemetry(idle2);
            analyticsService.processTelemetry(responding1);
            analyticsService.processTelemetry(responding2);

            VehicleMetrics metrics = analyticsService.getVehicleMetrics("FIRE_TRUCK_001");
            Map<VehicleStatus, Double> distribution = metrics.getStatusDistribution();

            // 50% IDLE, 50% EN_ROUTE
            assertThat(distribution.get(VehicleStatus.IDLE)).isEqualTo(50.0);
            assertThat(distribution.get(VehicleStatus.EN_ROUTE)).isEqualTo(50.0);
        }
    }

    @Nested
    @DisplayName("Fleet-Wide Calculations")
    class FleetCalculationsTests {

        @Test
        @DisplayName("Should calculate fleet average speed")
        void shouldCalculateFleetAverageSpeed() {
            // Vehicle 1: avg speed = 60
            analyticsService.processTelemetry(createMessageWithSpeed("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 50.0));
            analyticsService.processTelemetry(createMessageWithSpeed("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 70.0));

            // Vehicle 2: avg speed = 40
            analyticsService.processTelemetry(createMessageWithSpeed("AMBULANCE_001", VehicleType.AMBULANCE, 30.0));
            analyticsService.processTelemetry(createMessageWithSpeed("AMBULANCE_001", VehicleType.AMBULANCE, 50.0));

            // Fleet average: (60 + 40) / 2 = 50
            double fleetAvg = analyticsService.getFleetAverageSpeed();
            assertThat(fleetAvg).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should calculate total fuel consumed across fleet")
        void shouldCalculateTotalFuelConsumed() {
            // Vehicle 1: consumes 10
            analyticsService.processTelemetry(createMessageWithFuel("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 100.0));
            analyticsService.processTelemetry(createMessageWithFuel("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 90.0));

            // Vehicle 2: consumes 15
            analyticsService.processTelemetry(createMessageWithFuel("AMBULANCE_001", VehicleType.AMBULANCE, 80.0));
            analyticsService.processTelemetry(createMessageWithFuel("AMBULANCE_001", VehicleType.AMBULANCE, 65.0));

            double totalFuel = analyticsService.getTotalFuelConsumed();
            assertThat(totalFuel).isEqualTo(25.0);
        }

        @Test
        @DisplayName("Should track total vehicle count")
        void shouldTrackVehicleCount() {
            analyticsService.processTelemetry(createMessage("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK));
            analyticsService.processTelemetry(createMessage("AMBULANCE_001", VehicleType.AMBULANCE));
            analyticsService.processTelemetry(createMessage("POLICE_001", VehicleType.POLICE));

            int count = analyticsService.getTrackedVehicleCount();
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Should calculate average speed by vehicle type")
        void shouldCalculateAverageSpeedByType() {
            // Fire trucks: avg = 60
            analyticsService.processTelemetry(createMessageWithSpeed("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK, 60.0));
            analyticsService.processTelemetry(createMessageWithSpeed("FIRE_TRUCK_002", VehicleType.FIRE_TRUCK, 60.0));

            // Ambulances: avg = 80
            analyticsService.processTelemetry(createMessageWithSpeed("AMBULANCE_001", VehicleType.AMBULANCE, 80.0));
            analyticsService.processTelemetry(createMessageWithSpeed("AMBULANCE_002", VehicleType.AMBULANCE, 80.0));

            Map<VehicleType, Double> avgByType = analyticsService.getAverageSpeedByType();

            assertThat(avgByType.get(VehicleType.FIRE_TRUCK)).isEqualTo(60.0);
            assertThat(avgByType.get(VehicleType.AMBULANCE)).isEqualTo(80.0);
            assertThat(avgByType.get(VehicleType.POLICE)).isEqualTo(0.0); // No data
        }
    }

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodsTests {

        @Test
        @DisplayName("Should get all vehicle metrics")
        void shouldGetAllVehicleMetrics() {
            analyticsService.processTelemetry(createMessage("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK));
            analyticsService.processTelemetry(createMessage("AMBULANCE_001", VehicleType.AMBULANCE));
            analyticsService.processTelemetry(createMessage("POLICE_001", VehicleType.POLICE));

            Collection<VehicleMetrics> allMetrics = analyticsService.getAllVehicleMetrics();
            assertThat(allMetrics).hasSize(3);
        }

        @Test
        @DisplayName("Should filter metrics by vehicle type")
        void shouldFilterByVehicleType() {
            analyticsService.processTelemetry(createMessage("FIRE_TRUCK_001", VehicleType.FIRE_TRUCK));
            analyticsService.processTelemetry(createMessage("FIRE_TRUCK_002", VehicleType.FIRE_TRUCK));
            analyticsService.processTelemetry(createMessage("AMBULANCE_001", VehicleType.AMBULANCE));

            Collection<VehicleMetrics> fireTrucks = analyticsService.getMetricsByType(VehicleType.FIRE_TRUCK);
            assertThat(fireTrucks).hasSize(2);

            Collection<VehicleMetrics> ambulances = analyticsService.getMetricsByType(VehicleType.AMBULANCE);
            assertThat(ambulances).hasSize(1);
        }

        @Test
        @DisplayName("Should return null for unknown vehicle")
        void shouldReturnNullForUnknownVehicle() {
            VehicleMetrics metrics = analyticsService.getVehicleMetrics("UNKNOWN_999");
            assertThat(metrics).isNull();
        }
    }

    // Helper methods to create test messages
    private VehicleTelemetryMessage createMessage(String vehicleId, VehicleType type) {
        return VehicleTelemetryMessage.builder()
                .vehicleId(vehicleId)
                .vehicleType(type)
                .vehicleStatus(VehicleStatus.IDLE)
                .timeStamp(LocalDateTime.now())
                .latitude(48.1351)
                .longitude(11.5820)
                .speed(0.0)
                .fuelLevel(100.0)
                .engineTemp(75.0)
                .batteryVoltage(24.0)
                .emergencyLightsActive(false)
                .build();
    }

    private VehicleTelemetryMessage createMessageWithSpeed(String vehicleId, VehicleType type, Double speed) {
        return VehicleTelemetryMessage.builder()
                .vehicleId(vehicleId)
                .vehicleType(type)
                .vehicleStatus(VehicleStatus.IDLE)
                .timeStamp(LocalDateTime.now())
                .latitude(48.1351)
                .longitude(11.5820)
                .speed(speed)
                .fuelLevel(100.0)
                .engineTemp(75.0)
                .batteryVoltage(24.0)
                .emergencyLightsActive(false)
                .build();
    }

    private VehicleTelemetryMessage createMessageWithFuel(String vehicleId, VehicleType type, Double fuelLevel) {
        return VehicleTelemetryMessage.builder()
                .vehicleId(vehicleId)
                .vehicleType(type)
                .vehicleStatus(VehicleStatus.IDLE)
                .timeStamp(LocalDateTime.now())
                .latitude(48.1351)
                .longitude(11.5820)
                .speed(0.0)
                .fuelLevel(fuelLevel)
                .engineTemp(75.0)
                .batteryVoltage(24.0)
                .emergencyLightsActive(false)
                .build();
    }

    private VehicleTelemetryMessage createMessageWithStatus(String vehicleId, VehicleType type, VehicleStatus status) {
        return VehicleTelemetryMessage.builder()
                .vehicleId(vehicleId)
                .vehicleType(type)
                .vehicleStatus(status)
                .timeStamp(LocalDateTime.now())
                .latitude(48.1351)
                .longitude(11.5820)
                .speed(0.0)
                .fuelLevel(100.0)
                .engineTemp(75.0)
                .batteryVoltage(24.0)
                .emergencyLightsActive(false)
                .build();
    }
}