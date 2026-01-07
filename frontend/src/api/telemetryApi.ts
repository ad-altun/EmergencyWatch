import { axiosClient } from "./axiosClient";
import { apiPaths } from "./apiPaths";
import type { VehicleTelemetry } from "@/types";

export const telemetryApi = {
    getLatestTelemetry: async (): Promise<VehicleTelemetry[]> => {
        const response = await axiosClient.get(apiPaths.analytics.telemetryLatest);
        return response.data;
    },

    getVehicleTelemetry: async (vehicleId: string): Promise<VehicleTelemetry[]> => {
        const response = await axiosClient.get(apiPaths.analytics.vehicleTelemetry(vehicleId));
        return response.data;
    },
};