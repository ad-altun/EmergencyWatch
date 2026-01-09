package de.denizaltun.analyticsservice.service;

import de.denizaltun.analyticsservice.entity.DailyFleetMetrics;
import de.denizaltun.analyticsservice.entity.DailyVehicleMetrics;
import de.denizaltun.analyticsservice.repository.DailyFleetMetricsRepository;
import de.denizaltun.analyticsservice.repository.DailyVehicleMetricsRepository;
import de.denizaltun.analyticsservice.repository.VehicleTelemetryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MetricsAggregationService
 *
 * Tests the daily aggregation job that:
 * - Reads data from PostgreSQL (VehicleTelemetry)
 * - Aggregates metrics
 * - Saves to (DailyFleetMetrics, DailyVehicleMetrics)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MetricsAggregationService Unit Tests")
class MetricsAggregationServiceTest {

    @Mock
    private VehicleTelemetryRepository telemetryRepository;

    @Mock
    private DailyFleetMetricsRepository fleetMetricsRepository;

    @Mock
    private DailyVehicleMetricsRepository vehicleMetricsRepository;

    @InjectMocks
    private MetricsAggregationService aggregationService;

    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2025, 12, 25);
    }

    @Nested
    @DisplayName("aggregateMetricsForDate() - Main Flow")
    class AggregateMetricsForDateTests {

        @Test
        @DisplayName("Should aggregate both fleet and vehicle metrics")
        void shouldAggregateBothMetrics() {
            // ARRANGE - Fleet metrics
            when(fleetMetricsRepository.findByDate(testDate)).thenReturn(Optional.empty());
            when(telemetryRepository.countDistinctVehiclesByDate(testDate)).thenReturn(5);
            when(telemetryRepository.calculateAverageSpeedByDate(testDate)).thenReturn(60.0);
            when(telemetryRepository.calculateFuelConsumptionByVehicle(eq(testDate), eq(testDate), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByStatusAndDate(testDate))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByTypeAndDate(testDate))
                    .thenReturn(Collections.emptyList());

            // ARRANGE - Vehicle metrics
            when(telemetryRepository.calculateVehicleMetricsByDate(testDate))
                    .thenReturn(Collections.emptyList());

            // ACT
            aggregationService.aggregateMetricsForDate(testDate);

            // ASSERT
            verify(fleetMetricsRepository, times(1)).save(any(DailyFleetMetrics.class));
            verify(telemetryRepository, times(1)).calculateVehicleMetricsByDate(testDate);
        }

        @Test
        @DisplayName("Should call all PostgreSQL aggregation queries")
        void shouldCallAllAggregationQueries() {
            // ARRANGE
            when(fleetMetricsRepository.findByDate(testDate)).thenReturn(Optional.empty());
            when(telemetryRepository.countDistinctVehiclesByDate(testDate)).thenReturn(3);
            when(telemetryRepository.calculateAverageSpeedByDate(testDate)).thenReturn(50.0);
            when(telemetryRepository.calculateFuelConsumptionByVehicle(eq(testDate), eq(testDate), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByStatusAndDate(testDate))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByTypeAndDate(testDate))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateVehicleMetricsByDate(testDate))
                    .thenReturn(Collections.emptyList());

            // ACT
            aggregationService.aggregateMetricsForDate(testDate);

            // ASSERT - Verify all PostgreSQL queries were called
            verify(telemetryRepository).countDistinctVehiclesByDate(testDate);
            verify(telemetryRepository).calculateAverageSpeedByDate(testDate);
            verify(telemetryRepository).calculateFuelConsumptionByVehicle(eq(testDate), eq(testDate), any(LocalDate.class));
            verify(telemetryRepository).calculateAverageSpeedByStatusAndDate(testDate);
            verify(telemetryRepository).calculateAverageSpeedByTypeAndDate(testDate);
            verify(telemetryRepository).calculateVehicleMetricsByDate(testDate);
        }
    }

    @Nested
    @DisplayName("Fleet Metrics Aggregation")
    class FleetMetricsAggregationTests {

        @Test
        @DisplayName("Should save fleet metrics with correct values")
        void shouldSaveFleetMetricsWithCorrectValues() {
            // ARRANGE
            List<Object[]> fuelData = Arrays.asList(
                    new Object[]{"FIRE_TRUCK_001", "FIRE_TRUCK", 250.5},
                    new Object[]{"AMBULANCE_001", "AMBULANCE", 150.75},
                    new Object[]{"POLICE_001", "POLICE", 99.0}
            );

            when(fleetMetricsRepository.findByDate(testDate)).thenReturn(Optional.empty());
            when(telemetryRepository.countDistinctVehiclesByDate(testDate)).thenReturn(10);
            when(telemetryRepository.calculateAverageSpeedByDate(testDate)).thenReturn(75.5);
            when(telemetryRepository.calculateFuelConsumptionByVehicle(eq(testDate), eq(testDate), any(LocalDate.class)))
                    .thenReturn(fuelData);
            when(telemetryRepository.calculateAverageSpeedByStatusAndDate(testDate))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByTypeAndDate(testDate))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateVehicleMetricsByDate(testDate))
                    .thenReturn(Collections.emptyList());

            // ACT
            aggregationService.aggregateMetricsForDate(testDate);

            // ASSERT - Capture what was saved
            ArgumentCaptor<DailyFleetMetrics> captor = ArgumentCaptor.forClass(DailyFleetMetrics.class);
            verify(fleetMetricsRepository).save(captor.capture());

            DailyFleetMetrics saved = captor.getValue();
            assertThat(saved.getDate()).isEqualTo(testDate);
            assertThat(saved.getTotalVehicles()).isEqualTo(10);
            assertThat(saved.getFleetAverageSpeed()).isEqualTo(75.5);
            assertThat(saved.getTotalFuelConsumed()).isEqualTo(500.25);
        }

        @Test
        @DisplayName("Should include speed by vehicle type in fleet metrics")
        void shouldIncludeSpeedByVehicleType() {
            // ARRANGE
            List<Object[]> speedByType = Arrays.asList(
                    new Object[]{"FIRE_TRUCK", 60.0},
                    new Object[]{"AMBULANCE", 80.0},
                    new Object[]{"POLICE", 90.0}
            );

            when(fleetMetricsRepository.findByDate(testDate)).thenReturn(Optional.empty());
            when(telemetryRepository.countDistinctVehiclesByDate(testDate)).thenReturn(5);
            when(telemetryRepository.calculateAverageSpeedByDate(testDate)).thenReturn(70.0);
            when(telemetryRepository.calculateFuelConsumptionByVehicle(eq(testDate), eq(testDate), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByStatusAndDate(testDate))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByTypeAndDate(testDate))
                    .thenReturn(speedByType);
            when(telemetryRepository.calculateVehicleMetricsByDate(testDate))
                    .thenReturn(Collections.emptyList());

            // ACT
            aggregationService.aggregateMetricsForDate(testDate);

            // ASSERT
            ArgumentCaptor<DailyFleetMetrics> captor = ArgumentCaptor.forClass(DailyFleetMetrics.class);
            verify(fleetMetricsRepository).save(captor.capture());

            DailyFleetMetrics saved = captor.getValue();
            Map<String, Double> avgSpeedByType = saved.getAverageSpeedByType();

            assertThat(avgSpeedByType).hasSize(3);
            assertThat(avgSpeedByType.get("FIRE_TRUCK")).isEqualTo(60.0);
            assertThat(avgSpeedByType.get("AMBULANCE")).isEqualTo(80.0);
            assertThat(avgSpeedByType.get("POLICE")).isEqualTo(90.0);
        }

        @Test
        @DisplayName("Should skip fleet metrics if already exists (idempotence)")
        void shouldSkipIfFleetMetricsAlreadyExist() {
            // ARRANGE - Existing metrics found
            DailyFleetMetrics existing = new DailyFleetMetrics(
                    testDate, 5, 60.0, 100.0, new HashMap<>(), new HashMap<>()
            );
            when(fleetMetricsRepository.findByDate(testDate)).thenReturn(Optional.of(existing));

            // ACT
            aggregationService.aggregateMetricsForDate(testDate);

            // ASSERT - Should NOT query PostgreSQL or save again
            verify(telemetryRepository, never()).countDistinctVehiclesByDate(any());
            verify(telemetryRepository, never()).calculateAverageSpeedByDate(any());
            verify(fleetMetricsRepository, never()).save(any(DailyFleetMetrics.class));
        }

        @Test
        @DisplayName("Should propagate exception when DB save fails")
        void shouldPropagateExceptionOnSaveFailure() {
            // ARRANGE
            when(fleetMetricsRepository.findByDate(testDate)).thenReturn(Optional.empty());
            when(telemetryRepository.countDistinctVehiclesByDate(testDate)).thenReturn(5);
            when(telemetryRepository.calculateAverageSpeedByDate(testDate)).thenReturn(60.0);
            when(telemetryRepository.calculateFuelConsumptionByVehicle(eq(testDate), eq(testDate), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByStatusAndDate(testDate))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByTypeAndDate(testDate))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateVehicleMetricsByDate(testDate))
                    .thenReturn(Collections.emptyList());

            // DB save fails
            when(fleetMetricsRepository.save(any(DailyFleetMetrics.class)))
                    .thenThrow(new RuntimeException("DB connection failed"));

            // ACT & ASSERT
            assertThatThrownBy(() -> aggregationService.aggregateMetricsForDate(testDate))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB connection failed");
        }
    }

    @Nested
    @DisplayName("Vehicle Metrics Aggregation")
    class VehicleMetricsAggregationTests {

        @Test
        @DisplayName("Should save metrics for all vehicles")
        void shouldSaveMetricsForAllVehicles() {
            // ARRANGE
            when(fleetMetricsRepository.findByDate(testDate)).thenReturn(Optional.empty());
            setupFleetMetricsMocks();

            List<Object[]> vehicleData = Arrays.asList(
                    createVehicleDataRow("FIRE_TRUCK_001", "EN_ROUTE", "FIRE_TRUCK", 60.0, 80.0, 40.0, 75.0, 50.0, 100L),
                    createVehicleDataRow("AMBULANCE_001", "EN_ROUTE", "AMBULANCE", 70.0, 90.0, 50.0, 80.0, 60.0, 120L)
            );
            when(telemetryRepository.calculateVehicleMetricsByDate(testDate)).thenReturn(vehicleData);

            // ACT
            aggregationService.aggregateMetricsForDate(testDate);

            // ASSERT
            ArgumentCaptor<List<DailyVehicleMetrics>> captor = ArgumentCaptor.forClass(List.class);
            verify(vehicleMetricsRepository, times(1)).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(2);
        }

        @Test
        @DisplayName("Should correctly map vehicle data to DailyVehicleMetrics")
        void shouldCorrectlyMapVehicleData() {
            // ARRANGE
            when(fleetMetricsRepository.findByDate(testDate)).thenReturn(Optional.empty());

            // Mock fuel consumption
            when(telemetryRepository.calculateFuelConsumptionByVehicle(eq(testDate), eq(testDate), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // Mock vehicle data
            List<Object[]> vehicleData = new ArrayList<>();
            vehicleData.add(
                    createVehicleDataRow("FIRE_TRUCK_001", "EN_ROUTE", "FIRE_TRUCK", 65.5, 85.0, 45.0, 72.3, 55.0, 150L)
            );
            when(telemetryRepository.calculateVehicleMetricsByDate(testDate)).thenReturn(vehicleData);

            // Mock fleet metrics queries (needed because aggregateMetricsForDate does both)
            when(telemetryRepository.countDistinctVehiclesByDate(testDate)).thenReturn(1);
            when(telemetryRepository.calculateAverageSpeedByDate(testDate)).thenReturn(60.0);
            when(telemetryRepository.calculateAverageSpeedByStatusAndDate(testDate))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByTypeAndDate(testDate))
                    .thenReturn(Collections.emptyList());

            // ACT
            aggregationService.aggregateMetricsForDate(testDate);

            // ASSERT
            ArgumentCaptor<List<DailyVehicleMetrics>> captor = ArgumentCaptor.forClass(List.class);
            verify(vehicleMetricsRepository).saveAll(captor.capture());

            DailyVehicleMetrics saved = captor.getValue().get(0);
            assertThat(saved.getVehicleId()).isEqualTo("FIRE_TRUCK_001");
            assertThat(saved.getDate()).isEqualTo(testDate);
            assertThat(saved.getVehicleType()).isEqualTo("FIRE_TRUCK");
            assertThat(saved.getAverageSpeed()).isEqualTo(65.5);
            assertThat(saved.getMaxSpeed()).isEqualTo(85.0);
            assertThat(saved.getMinSpeed()).isEqualTo(45.0);
            assertThat(saved.getAverageFuelLevel()).isEqualTo(72.3);
            assertThat(saved.getMinFuelLevel()).isEqualTo(55.0);
            assertThat(saved.getTotalTelemetryPoints()).isEqualTo(150);
        }

        @Test
        @DisplayName("Should handle empty vehicle data gracefully")
        void shouldHandleEmptyVehicleData() {
            // ARRANGE
            when(fleetMetricsRepository.findByDate(testDate)).thenReturn(Optional.empty());
            setupFleetMetricsMocks();
            when(telemetryRepository.calculateVehicleMetricsByDate(testDate))
                    .thenReturn(Collections.emptyList());

            // ACT
            aggregationService.aggregateMetricsForDate(testDate);

            // ASSERT - saveAll called with empty list
            verify(vehicleMetricsRepository, times(1)).saveAll(Collections.emptyList());
        }

        @Test
        @DisplayName("Should include fuel consumption in vehicle metrics")
        void shouldIncludeFuelConsumptionInVehicleMetrics() {
            // ARRANGE
            when(fleetMetricsRepository.findByDate(testDate)).thenReturn(Optional.empty());

            // Mock fuel consumption data
            List<Object[]> fuelData = List.<Object[]>of(
                    new Object[]{"FIRE_TRUCK_001", "FIRE_TRUCK", 50.5}
            );
            when(telemetryRepository.calculateFuelConsumptionByVehicle(eq(testDate), eq(testDate), any(LocalDate.class)))
                    .thenReturn(fuelData);

            when(telemetryRepository.countDistinctVehiclesByDate(testDate)).thenReturn(1);
            when(telemetryRepository.calculateAverageSpeedByDate(testDate)).thenReturn(60.0);
            when(telemetryRepository.calculateAverageSpeedByStatusAndDate(testDate))
                    .thenReturn(Collections.emptyList());
            when(telemetryRepository.calculateAverageSpeedByTypeAndDate(testDate))
                    .thenReturn(Collections.emptyList());

            List<Object[]> vehicleData = new ArrayList<>();
            vehicleData.add(
                    createVehicleDataRow("FIRE_TRUCK_001", "EN_ROUTE", "FIRE_TRUCK", 65.5, 85.0, 45.0, 72.3, 55.0, 150L)
            );
            when(telemetryRepository.calculateVehicleMetricsByDate(testDate)).thenReturn(vehicleData);

            // ACT
            aggregationService.aggregateMetricsForDate(testDate);

            // ASSERT
            ArgumentCaptor<List<DailyVehicleMetrics>> captor = ArgumentCaptor.forClass(List.class);
            verify(vehicleMetricsRepository).saveAll(captor.capture());

            DailyVehicleMetrics saved = captor.getValue().get(0);
            assertThat(saved.getFuelConsumed()).isEqualTo(50.5);
        }
    }

    // Helper methods
    private void setupFleetMetricsMocks() {
        when(telemetryRepository.countDistinctVehiclesByDate(testDate)).thenReturn(5);
        when(telemetryRepository.calculateAverageSpeedByDate(testDate)).thenReturn(60.0);
        when(telemetryRepository.calculateFuelConsumptionByVehicle(eq(testDate), eq(testDate), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(telemetryRepository.calculateAverageSpeedByStatusAndDate(testDate))
                .thenReturn(Collections.emptyList());
        when(telemetryRepository.calculateAverageSpeedByTypeAndDate(testDate))
                .thenReturn(Collections.emptyList());
    }

    private Object[] createVehicleDataRow(
            String vehicleId, String vehicleStatus, String vehicleType,
            Double avgSpeed, Double maxSpeed, Double minSpeed,
            Double avgFuel, Double minFuel, Long totalPoints) {
        return new Object[]{
                vehicleId,      // 0
                vehicleStatus,  // 1
                vehicleType,    // 2
                avgSpeed,       // 3
                maxSpeed,       // 4
                minSpeed,       // 5
                avgFuel,        // 6
                minFuel,        // 7
                totalPoints     // 8
        };
    }
}