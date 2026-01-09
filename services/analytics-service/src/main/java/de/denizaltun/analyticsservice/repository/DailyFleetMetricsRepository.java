package de.denizaltun.analyticsservice.repository;

import de.denizaltun.analyticsservice.entity.DailyFleetMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyFleetMetricsRepository extends JpaRepository<DailyFleetMetrics, Long > {

    Optional<DailyFleetMetrics> findByDate(LocalDate date);

    List<DailyFleetMetrics> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
}
