package de.denizaltun.notificationservice.repository;

import de.denizaltun.notificationservice.model.Alert;
import de.denizaltun.notificationservice.model.AlertStatus;
import de.denizaltun.notificationservice.model.AlertType;
import de.denizaltun.notificationservice.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatus(AlertStatus status);

    List<Alert> findByVehicleId(String vehicleId);

    List<Alert> findByVehicleType(VehicleType vehicleType);

    List<Alert> findByVehicleIdAndStatus(String vehicleId, AlertStatus status);

    // For deduplication: check if active alert already exists
    Optional<Alert> findByVehicleIdAndAlertTypeAndStatus(
            String vehicleId,
            AlertType alertType,
            AlertStatus status
    );

    List<Alert> findByCreatedAtAfter(LocalDateTime since);

    long countByStatus(AlertStatus status);
}
