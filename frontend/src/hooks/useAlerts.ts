import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { alertsApi } from "@/api";

export function useAlerts() {
    return useQuery({
        queryKey: ["alerts"],
        queryFn: alertsApi.getActiveAlerts,
        refetchInterval: 5000,
    });
}

export function useAcknowledgeAlert() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: alertsApi.acknowledgeAlert,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["alerts"] });
        },
    });
}

export function useResolveAlert() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: alertsApi.resolveAlert,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["alerts"] });
        },
    });
}