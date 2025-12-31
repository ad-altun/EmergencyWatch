import type { VehicleStatus, VehicleType } from "./vehicle";

export interface FleetAnalytics {
    totalVehicles: number;
    totalTelemetryReceived: number;
    fleetAverageSpeed: number;
    totalFuelConsumed: number;
    vehiclesByType: Record<VehicleType, number>;
    averageSpeedByType: Record<VehicleType, number>;
    currentStatusOverview: Record<VehicleStatus, number>;
}