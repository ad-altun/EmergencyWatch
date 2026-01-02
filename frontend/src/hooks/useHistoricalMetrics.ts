import { useQuery } from "@tanstack/react-query";
import { analyticsApi } from "@/api/analyticsApi";

export function useHistoricalMetrics(from: string, to: string) {
    return useQuery({
        queryKey: ["historicalMetrics", from, to],
        queryFn: () => analyticsApi.getHistoricalMetrics(from, to),
        staleTime: 60 * 1000, // 1 minute
    });
}