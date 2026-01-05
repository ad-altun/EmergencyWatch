import { useQuery } from "@tanstack/react-query";
import { telemetryApi } from "@/api";

export function useVehicles() {
    return useQuery({
        queryKey: ["vehicles"],
        queryFn: telemetryApi.getLatestTelemetry,
        refetchInterval: 15000,
    });
}