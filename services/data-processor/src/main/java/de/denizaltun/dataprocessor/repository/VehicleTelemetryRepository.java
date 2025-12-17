package de.denizaltun.dataprocessor.repository;

import de.denizaltun.dataprocessor.model.VehicleTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for vehicle telemetry data.
 * Spring Data JPA automatically implements basic CRUD operations.
 */
@Repository
public interface VehicleTelemetryRepository extends JpaRepository<VehicleTelemetry, Long> {
    /**
     * Find all telemetry for a specific vehicle, ordered by timeStamp descending.
     */
    List<VehicleTelemetry> findByVehicleIdOrderByTimeStampDesc(String vehicleId);

    /**
     * Find telemetry within a time range.
     */
    List<VehicleTelemetry> findByTimeStampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find the latest telemetry for a specific vehicle.
     */
    VehicleTelemetry findFirstByVehicleIdOrderByTimeStampDesc(String vehicleId);

    /**
     * Count telemetry records for a specific vehicle.
     */
    long countByVehicleId(String vehicleId);

    /**
     * Find telemetry with low fuel levels (potential alerts).
     */
    @Query("SELECT t FROM VehicleTelemetry t WHERE t.fuelLevel < :threshold ORDER BY t.timeStamp DESC")
    List<VehicleTelemetry> findLowFuelVehicles(double threshold);
}
