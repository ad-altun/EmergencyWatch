import { axiosClient } from "@/api/axiosClient.ts";
import type { FleetAnalytics } from "@/types";

export const analyticsApi = {
    getFleetAnalytics: async (): Promise<FleetAnalytics> => {
        const response = await axiosClient.get("/api/analytics/fleet");
        return response.data;
    },
};