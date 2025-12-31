export type VehicleType = "POLICE" | "AMBULANCE" | "FIRE_TRUCK";

export type VehicleStatus = "IDLE" | "EN_ROUTE" | "ON_SCENE" | "RETURNING";

export interface Vehicle {
    vehicleId: string;
    vehicleType: VehicleType;
    vehicleStatus: VehicleStatus;
}