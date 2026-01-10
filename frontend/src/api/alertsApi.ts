import { axiosClient } from "./axiosClient";
import { apiPaths } from "./apiPaths";
import type { Alert } from "@/types";

export const alertsApi = {
    getActiveAlerts: async (): Promise<Alert[]> => {
        const response = await axiosClient.get(apiPaths.alerts.active);
        return response.data;
    },

    acknowledgeAlert: async (alertId: number): Promise<Alert> => {
        const response = await axiosClient.patch(apiPaths.alerts.acknowledge(alertId));
        return response.data;
    },

    resolveAlert: async (alertId: number): Promise<Alert> => {
        const response = await axiosClient.patch(apiPaths.alerts.resolve(alertId));
        return response.data;
    },
};