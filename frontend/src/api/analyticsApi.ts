import { axiosClient } from "@/api/axiosClient.ts";
import { apiPaths } from "./apiPaths";
import type { FleetAnalytics, HistoricalMetricsResponse  } from "@/types";

export const analyticsApi = {
    getFleetAnalytics: async (): Promise<FleetAnalytics> => {
        const response = await axiosClient.get(apiPaths.analytics.fleet);
        return response.data;
    },

    getHistoricalMetrics: async (from: string, to: string): Promise<HistoricalMetricsResponse> => {
        const response = await axiosClient.get(apiPaths.analytics.history, {
            params: { from, to }
        });
        return response.data;
    },
};