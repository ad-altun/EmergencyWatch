import { axiosClient } from "./axiosClient";
import type { Alert } from "@/types";

export const alertsApi = {
    getActiveAlerts: async (): Promise<Alert[]> => {
        const response = await axiosClient.get("/api/alerts/active");
        return response.data;
    },

    acknowledgeAlert: async (alertId: number): Promise<Alert> => {
        const response = await axiosClient.patch(`/api/alerts/${alertId}/acknowledge`);
        return response.data;
    },

    resolveAlert: async (alertId: number): Promise<Alert> => {
        const response = await axiosClient.patch(`/api/alerts/${alertId}/resolve`);
        return response.data;
    },
};