package de.denizaltun.analyticsservice.repository;

import de.denizaltun.analyticsservice.entity.DailyVehicleMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyVehicleMetricsRepository extends JpaRepository<DailyVehicleMetrics, Long> {

    Optional<DailyVehicleMetrics> findByVehicleIdAndDate(String vehicleId, LocalDate date);

    List<DailyVehicleMetrics> findByDateBetweenOrderByVehicleIdAsc(LocalDate startDate, LocalDate endDate);

    List<DailyVehicleMetrics> findByVehicleIdAndDateBetweenOrderByDateAsc(
            String vehicleId, LocalDate startDate, LocalDate endDate
    );
}
