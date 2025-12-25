package de.denizaltun.vehiclesimulator.model;

public enum VehicleStatus {
    IDLE,           // At station, waiting
    EN_ROUTE,      // En route to emergency
    ON_SCENE,       // Arrived at emergency location
    RETURNING       // Returning to station
}
