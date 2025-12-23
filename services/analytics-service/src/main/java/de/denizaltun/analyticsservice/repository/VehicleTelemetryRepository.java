package de.denizaltun.analyticsservice.repository;

import de.denizaltun.analyticsservice.entity.VehicleTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehicleTelemetryRepository extends JpaRepository<VehicleTelemetry, Long> {

    // Fleet-wide metrics for a specific date
    @Query("SELECT COUNT(DISTINCT v.vehicleId) FROM VehicleTelemetry v " +
            "WHERE DATE(v.timeStamp) = :date")
    Integer countDistinctVehiclesByDate(@Param("date") LocalDate date);

    @Query("SELECT AVG(v.speed) FROM VehicleTelemetry v " +
            "WHERE DATE(v.timeStamp) = :date")
    Double calculateAverageSpeedByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(v.fuelLevel) FROM VehicleTelemetry v " +
            "WHERE DATE(v.timeStamp) = :date")
    Double calculateTotalFuelByDate(@Param("date") LocalDate date);

    // Per vehicle status metrics (was vehicleType, now vehicleStatus)
    @Query("SELECT CAST(v.vehicleStatus AS string) as status, AVG(v.speed) as avgSpeed " +
            "FROM VehicleTelemetry v " +
            "WHERE DATE(v.timeStamp) = :date " +
            "GROUP BY v.vehicleStatus")
    List<Object[]> calculateAverageSpeedByTypeAndDate(@Param("date") LocalDate date);

    // Per vehicle metrics
    @Query("SELECT v.vehicleId as vehicleId, CAST(v.vehicleStatus AS string) as vehicleStatus, " +
            "AVG(v.speed) as avgSpeed, MAX(v.speed) as maxSpeed, MIN(v.speed) as minSpeed, " +
            "AVG(v.fuelLevel) as avgFuel, MIN(v.fuelLevel) as minFuel, COUNT(v) as totalPoints " +
            "FROM VehicleTelemetry v " +
            "WHERE DATE(v.timeStamp) = :date " +
            "GROUP BY v.vehicleId, v.vehicleStatus")
    List<Object[]> calculateVehicleMetricsByDate(@Param("date") LocalDate date);
}
