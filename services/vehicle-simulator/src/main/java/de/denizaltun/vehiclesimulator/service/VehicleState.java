package de.denizaltun.vehiclesimulator.service;

import de.denizaltun.vehiclesimulator.model.VehicleStatus;
import de.denizaltun.vehiclesimulator.model.VehicleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Random;

// Maintains state for a single simulated vehicle.
// Tracks operational status and handles state transitions
@Data
public class VehicleState {

    private final String vehicleId;
    private final VehicleType vehicleType;
    private VehicleStatus status;
    private double fuelLevel;
    private LocalDateTime nextPublishTime;
    private int stateCounter;       // used for state transitions

    private static final Random random = new Random();

    public VehicleState(String vehicleId, VehicleType vehicleType, LocalDateTime nextPublishTime) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.status = VehicleStatus.IDLE;
        this.fuelLevel = 80.0 + random.nextDouble() * 20.0;     // 80 - 100%
        this.nextPublishTime = nextPublishTime;
        this.stateCounter = 0;
    }

    // checks if this vehicle should publish telemetry now
    public boolean shouldPublishNow() {
        return LocalDateTime.now().isAfter(nextPublishTime)  ||
                LocalDateTime.now().isEqual(nextPublishTime);
    }

    // update next publish time (with interval)
    public void updateNextPublishTime(int intervalSeconds) {
        this.nextPublishTime = this.nextPublishTime.plusSeconds(intervalSeconds);
    }

    // Handles state transitions: IDLE → RESPONDING → ON_SCENE → RETURNING → IDLE
    // Transitions happen probabilistically to create realistic variety.
    public void transitionState() {
        stateCounter++;

        switch (status) {
            case IDLE:
                // 20% chance to start responding each update
                if (random.nextDouble() < 0.2) {
                    status = VehicleStatus.RESPONDING;
                    stateCounter = 0;
                }
                break;

            case RESPONDING:
                // after ~5 updates (15 seconds), arrive on scene
                if (stateCounter >= 5) {
                    status = VehicleStatus.ON_SCENE;
                    stateCounter = 0;
                }
                break;

            case ON_SCENE:
                // stay on scene for ~10 updates (30 seconds)
                if (stateCounter >=10) {
                    status = VehicleStatus.RETURNING;
                    stateCounter = 0;
                }
                break;

            case RETURNING:
                // after ~5 updates, return to idle
                if (stateCounter >= 5) {
                    status = VehicleStatus.IDLE;
                    stateCounter = 0;
                }
                break;
        }
    }
}
