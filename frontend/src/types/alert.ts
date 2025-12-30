import type { VehicleType } from "./vehicle";

export type AlertType =
    | "LOW_FUEL"
    | "HIGH_ENGINE_TEMP"
    | "LOW_BATTERY"
    | "VEHICLE_IDLE_TOO_LONG"
    | "EMERGENCY_STATUS_CHANGE";

export type AlertStatus = "ACTIVE" | "ACKNOWLEDGED" | "RESOLVED";

export interface Alert {
    id: number;
    vehicleId: string;
    vehicleType: VehicleType;
    alertType: AlertType;
    status: AlertStatus;
    message: string;
    thresholdValue: number;
    actualValue: number;
    createdAt: string;
    acknowledgedAt?: string;
    resolvedAt?: string;
}