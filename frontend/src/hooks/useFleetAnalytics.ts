import { useQuery } from "@tanstack/react-query";
import { analyticsApi } from "@/api";

export function useFleetAnalytics() {
    return useQuery({
        queryKey: ["fleetAnalytics"],
        queryFn: analyticsApi.getFleetAnalytics,
        refetchInterval: 5000, // Poll every 5 seconds
    });
}