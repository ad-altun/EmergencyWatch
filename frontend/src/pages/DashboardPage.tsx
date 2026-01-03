import { StatsCardGrid } from "@/components/dashboard/StatsCardGrid";
import { VehicleListPanel } from "@/components/dashboard/VehicleListPanel";
import { AlertsPanel } from "@/components/dashboard/AlertsPanel";
import { VehicleListSkeleton, FleetMetricsSkeleton } from "@/components/dashboard/skeletons";
import { Skeleton } from "@/components/ui/Skeleton";
import { useVehicles, useAlerts } from "@/hooks";
import type { VehicleStatus } from "@/types";

export function DashboardPage() {
    const { data: vehicles = [], isLoading: vehiclesLoading, error: vehiclesError } = useVehicles();
    const { data: alerts = [], isLoading: alertsLoading, error: alertsError } = useAlerts();

    const error = vehiclesError || alertsError;

    if (error) {
        return (
            <div className="flex items-center justify-center h-full">
                <div className="text-center">
                    <p className="text-red-400 font-semibold mb-2">Failed to load data</p>
                    <p className="text-slate-400 text-sm">Please check if the backend services are running</p>
                </div>
            </div>
        );
    }

    // Show skeletons while loading
    if (vehiclesLoading || alertsLoading) {
        return (
            <div className="flex flex-col h-full gap-4">
                {/* Header Skeleton */}
                <div className="flex-shrink-0">
                    <Skeleton className="h-7 w-48 mb-2" />
                    <Skeleton className="h-4 w-64" />
                </div>

                {/* Stats Cards Skeleton */}
                <div className="flex-shrink-0">
                    <FleetMetricsSkeleton />
                </div>

                {/* Panels Skeleton */}
                <div className="flex-1 flex gap-4 min-h-0">
                    <VehicleListSkeleton />
                    <VehicleListSkeleton /> {/* Reuse for alerts panel */}
                </div>
            </div>
        );
    }

    const statusCounts: Record<VehicleStatus, number> = {
        IDLE: vehicles.filter((v) => v.vehicleStatus === "IDLE").length,
        EN_ROUTE: vehicles.filter((v) => v.vehicleStatus === "EN_ROUTE").length,
        ON_SCENE: vehicles.filter((v) => v.vehicleStatus === "ON_SCENE").length,
        RETURNING: vehicles.filter((v) => v.vehicleStatus === "RETURNING").length,
    };

    const criticalAlerts = alerts.filter(
        (a) => a.alertType === "HIGH_ENGINE_TEMP" || a.alertType === "LOW_FUEL"
    ).length;
    const warningAlerts = alerts.length - criticalAlerts;

    const avgSpeed = vehicles.length > 0
        ? Math.round(vehicles.reduce((sum, v) => sum + v.speed, 0) / vehicles.length)
        : 0;
    const avgFuel = vehicles.length > 0
        ? Math.round(vehicles.reduce((sum, v) => sum + v.fuelLevel, 0) / vehicles.length)
        : 0;

    return (
        <div className="flex flex-col h-full gap-4">
            {/* Header */}
            <div className="flex-shrink-0">
                <h1 className="text-xl font-bold text-white">Fleet Dashboard</h1>
                <p className="text-slate-400 text-sm">Real-time fleet monitoring</p>
            </div>

            {/* Stats Cards */}
            <div className="flex-shrink-0">
                <StatsCardGrid
                    availableVehicles={statusCounts.IDLE}
                    totalVehicles={vehicles.length}
                    totalAlerts={alerts.length}
                    criticalAlerts={criticalAlerts}
                    warningAlerts={warningAlerts}
                    avgSpeed={avgSpeed}
                    avgResponseTime={4.2}
                    avgFuel={avgFuel}
                    statusCounts={statusCounts}
                />
            </div>

            {/* Panels */}
            <div className="flex-1 flex gap-4 min-h-0">
                <VehicleListPanel vehicles={vehicles} />
                <AlertsPanel alerts={alerts} criticalCount={criticalAlerts} />
            </div>
        </div>
    );
}