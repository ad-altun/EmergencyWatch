package de.denizaltun.vehiclesimulator.service;

import de.denizaltun.vehiclesimulator.config.SimulatorConfig;
import de.denizaltun.vehiclesimulator.model.VehicleState;
import de.denizaltun.vehiclesimulator.model.VehicleTelemetry;
import de.denizaltun.vehiclesimulator.model.VehicleType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates vehicle telemetry simulation.
 * Manages multiple vehicles and coordinates generation + publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleSimulatorService {

    private final SimulatorConfig config;
    private final TelemetryGenerator generator;

    // optional dependency - works without Kafka for testing
    @Autowired(required = false)
    private final KafkaPublisher publisher;

    private List<VehicleState> vehicles;

    /**
     * Initialize vehicles with staggered publish times.
     * Creates realistic "operations center" feel with continuous updates.
     */
    @PostConstruct
    public void initialize() {
        vehicles = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        int vehicleCount = config.getVehicles().getCount();
        VehicleType[] types = VehicleType.values();

        for (int i = 0; i < vehicleCount; i++) {
            VehicleType type = types[i % types.length]; // Cycle through types
            String vehicleId = String.format("%s-%03d", type.name(), i + 1);

            // Explicit stagger: each vehicle starts 1 second apart
            LocalDateTime initialPublishTime = now.plusSeconds(i);

            vehicles.add(new VehicleState(vehicleId, type, initialPublishTime));
        }

        log.info("Initialized {} vehicles with {} second publish interval",
                vehicleCount, config.getTelemetry().getIntervalSeconds());

        if (publisher == null) {
            log.warn("KafkaPublisher not available - telemetry will only be logged");
        }
    }

    /**
     * Scheduled task that checks all vehicles and publishes telemetry when needed.
     * Runs every 1 second for ±1 second timing accuracy.
     */
    @Scheduled(fixedDelayString = "${app.telemetry.interval-seconds}000")
    public void checkAndPublish() {
        for (VehicleState vehicle : vehicles) {
            if (vehicle.shouldPublishNow()) {
                VehicleTelemetry telemetry = generator.generate(vehicle);

                if (publisher != null) {
                    publisher.publish(telemetry);
                } else {
                    // Fallback: just log the telemetry
                    log.info(String.format("Generated telemetry - Vehicle: %s, Status: %s, Speed: %.1f km/h, Fuel: %.1f%%, Lights: %s",
                            telemetry.vehicleId(),
                            telemetry.vehicleStatus(),
                            telemetry.speed(),
                            telemetry.fuelLevel(),
                            telemetry.emergencyLightsActive() ? "ON" : "OFF"));
                }
                vehicle.updateNextPublishTime(config.getTelemetry().getIntervalSeconds());
            }
        }
    }
}
