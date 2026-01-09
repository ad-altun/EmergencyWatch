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
            "WHERE DATE(v.timeStamp) = :date " +
            "AND v.vehicleStatus IN ('EN_ROUTE', 'RETURNING')")
    Double calculateAverageSpeedByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(v.fuelLevel) FROM VehicleTelemetry v " +
            "WHERE DATE(v.timeStamp) = :date")
    Double calculateTotalFuelLevelByDate(@Param("date") LocalDate date);

    // Average speed grouped by vehicle status (IDLE, EN_ROUTE, ON_SCENE, RETURNING)
    @Query("SELECT CAST(v.vehicleStatus AS string) as status, AVG(v.speed) as avgSpeed " +
            "FROM VehicleTelemetry v " +
            "WHERE DATE(v.timeStamp) = :date " +
            "GROUP BY v.vehicleStatus")
    List<Object[]> calculateAverageSpeedByStatusAndDate(@Param("date") LocalDate date);

    // Average speed by vehicle type (only moving vehicles)
    @Query("SELECT CAST(v.vehicleType AS string) as type, AVG(v.speed) as avgSpeed " +
            "FROM VehicleTelemetry v " +
            "WHERE DATE(v.timeStamp) = :date " +
            "AND v.vehicleStatus IN ('EN_ROUTE', 'RETURNING') " +
            "GROUP BY v.vehicleType")
    List<Object[]> calculateAverageSpeedByTypeAndDate(@Param("date") LocalDate date);

    @Query("SELECT v.vehicleId as vehicleId, " +
            "CAST(v.vehicleStatus AS string) as vehicleStatus, " +
            "CAST(v.vehicleType AS string) as vehicleType, " +
            "AVG(v.speed) as avgSpeed, MAX(v.speed) as maxSpeed, MIN(v.speed) as minSpeed, " +
            "AVG(v.fuelLevel) as avgFuel, MIN(v.fuelLevel) as minFuel, COUNT(v) as totalPoints " +
            "FROM VehicleTelemetry v " +
            "WHERE DATE(v.timeStamp) = :date " +
            "AND v.vehicleStatus IN ('EN_ROUTE', 'RETURNING') " +
            "GROUP BY v.vehicleId, v.vehicleStatus, v.vehicleType")
    List<Object[]> calculateVehicleMetricsByDate(@Param("date") LocalDate date);

    @Query("SELECT v FROM VehicleTelemetry v WHERE v.timeStamp = " +
            "(SELECT MAX(v2.timeStamp) FROM VehicleTelemetry v2 WHERE v2.vehicleId = v.vehicleId) " +
            "ORDER BY v.vehicleId")
    List<VehicleTelemetry> findLatestTelemetryPerVehicle();

//    @Query(value =
//            "SELECT COALESCE(SUM( " +
//                    "  CASE vehicle_type " +
//                    "    WHEN 'FIRE_TRUCK' THEN consumed_amount * 2.0 " +
//                    "    WHEN 'AMBULANCE'  THEN consumed_amount * 0.8 " +
//                    "    WHEN 'POLICE'     THEN consumed_amount * 0.6 " +
//                    "    ELSE 0.0 " +
//                    "  END), 0.0) " +
//                    "FROM ( " +
//                    "  SELECT " +
//                    "    vehicle_type, " +
//                    "    (prev_fuel - fuel_level) as consumed_amount " +
//                    "  FROM ( " +
//                    "    SELECT " +
//                    "      vehicle_type, " +
//                    "      fuel_level, " +
//                    "      LAG(fuel_level) OVER (PARTITION BY vehicle_id ORDER BY time_stamp) as prev_fuel " +
//                    "    FROM vehicle_telemetry " +
//                    "    WHERE DATE(time_stamp) = :date " +
//                    "  ) raw_comparisons " +
//                    "  WHERE (prev_fuel - fuel_level) > 0 " + // This filters out Refueling (negative values)
//                    ") calculated_drops",
//            nativeQuery = true)
//    Double calculateActualFuelConsumedByDate(@Param("date") LocalDate date);

    @Query(value =
            "SELECT vehicle_id, vehicle_type, " +
                    "  COALESCE(SUM( " +
                    "    CASE vehicle_type " +
                    "      WHEN 'FIRE_TRUCK' THEN consumed_amount * 2.0 " +
                    "      WHEN 'AMBULANCE'  THEN consumed_amount * 0.8 " +
                    "      WHEN 'POLICE'     THEN consumed_amount * 0.6 " +
                    "      ELSE 0.0 " +
                    "    END), 0.0) as total_consumed " +
                    "FROM (SELECT vehicle_id, vehicle_type, " +
                    "    time_stamp, " +
                    "    (prev_fuel - fuel_level) as consumed_amount " +
                    "  FROM (SELECT vehicle_id, vehicle_type, time_stamp, fuel_level, " +
                    "      LAG(fuel_level) OVER (PARTITION BY vehicle_id ORDER BY time_stamp) as prev_fuel " +
                    "    FROM vehicle_telemetry " +
                    "    WHERE time_stamp >= :bufferDate AND time_stamp < (CAST(:toDate AS date) + 1)) raw_comparisons " +
                    "  WHERE (prev_fuel - fuel_level) > 0) calculated_drops " +
                    "WHERE DATE(time_stamp) BETWEEN :fromDate AND :toDate " +
                    "GROUP BY vehicle_id, vehicle_type ORDER BY vehicle_id",
            nativeQuery = true)
    List<Object[]> calculateFuelConsumptionByVehicle(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("bufferDate") LocalDate bufferDate);

}
