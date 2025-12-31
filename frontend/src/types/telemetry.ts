import type { VehicleStatus, VehicleType } from "./vehicle.ts";

export interface VehicleTelemetry {
    id: number;
    vehicleId: string;
    vehicleType: VehicleType;
    vehicleStatus: VehicleStatus;
    timestamp: string; // ISO date string
    latitude: number;
    longitude: number;
    speed: number;
    fuelLevel: number;
    engineTemp: number;
    batteryVoltage: number;
    emergencyLightsActive: boolean;
}