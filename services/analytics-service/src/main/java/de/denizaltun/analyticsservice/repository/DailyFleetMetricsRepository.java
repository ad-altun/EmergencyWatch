package de.denizaltun.analyticsservice.repository;

import de.denizaltun.analyticsservice.service.DailyFleetMetrics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyFleetMetricsRepository extends MongoRepository<DailyFleetMetrics, String > {

    Optional<DailyFleetMetrics> findByDate(LocalDate date);

    List<DailyFleetMetrics> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
}
