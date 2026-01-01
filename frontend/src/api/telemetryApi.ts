import { axiosClient } from "./axiosClient";
import type { VehicleTelemetry } from "@/types";

export const telemetryApi = {
    getLatestTelemetry: async (): Promise<VehicleTelemetry[]> => {
        const response = await axiosClient.get("/api/analytics/vehicles");
        return response.data;
    },

    getVehicleTelemetry: async (vehicleId: string): Promise<VehicleTelemetry[]> => {
        const response = await axiosClient.get(`/api/analytics/vehicles/${vehicleId}`);
        return response.data;
    },
};