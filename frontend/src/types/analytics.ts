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

export interface DailyFleetMetrics {
    id: string;
    date: string;
    totalVehicles: number;
    fleetAverageSpeed: number | null;
    totalFuelConsumed: number | null;
    averageSpeedByStatus: Record<string, number>;
    averageSpeedByType: Record<VehicleType, number>;
}

export interface DailyVehicleMetrics {
    id: string;
    vehicleId: string;
    date: string;
    vehicleStatus: string;
    vehicleType: VehicleType;
    averageSpeed: number;
    maxSpeed: number;
    minSpeed: number;
    averageFuelLevel: number;
    minFuelLevel: number;
    totalTelemetryPoints: number;
}

export interface HistoricalMetricsResponse {
    fromDate: string;
    toDate: string;
    totalDays: number;
    averageFleetSpeed: number;
    totalFuelConsumed: number;
    totalDataPoints: number;
    dailyFleetMetrics: DailyFleetMetrics[];
    dailyVehicleMetrics: DailyVehicleMetrics[];
}