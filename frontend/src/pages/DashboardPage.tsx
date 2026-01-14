import { StatsCardGrid } from "@/components/dashboard/StatsCardGrid";
import { VehicleListPanel } from "@/components/dashboard/VehicleListPanel";
import { AlertsPanel } from "@/components/dashboard/AlertsPanel";
import { VehicleListSkeleton, FleetMetricsSkeleton } from "@/components/dashboard/skeletons";
import { Skeleton } from "@/components/ui/Skeleton";
import { useVehicles, useAlerts } from "@/hooks";
import type { VehicleStatus } from "@/types";

export function DashboardPage() {
    const { data: vehicles, isLoading: vehiclesLoading, error: vehiclesError } = useVehicles();
    const { data: alerts, isLoading: alertsLoading, error: alertsError } = useAlerts();

    // Defensive: Ensure arrays even if API returns unexpected data (e.g. error objects)
    const safeVehicles = Array.isArray(vehicles) ? vehicles : [];
    const safeAlerts = Array.isArray(alerts) ? alerts : [];

    const error = vehiclesError || alertsError;

    if (error) {
        return (
            <div className="flex items-center justify-center h-full">
                <div className="text-center">
                    <p className="text-red-600 font-semibold mb-2">Failed to load data</p>
                    <p className="text-slate-600 text-sm">Please check if the server is active</p>
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
                <div className="flex-1 flex flex-col lg:flex-row gap-4 min-h-0">
                    <VehicleListSkeleton />
                    <VehicleListSkeleton /> {/* Reuse for alerts panel */}
                </div>
            </div>
        );
    }

    const statusCounts: Record<VehicleStatus, number> = {
        IDLE: safeVehicles.filter((v) => v.vehicleStatus === "IDLE").length,
        EN_ROUTE: safeVehicles.filter((v) => v.vehicleStatus === "EN_ROUTE").length,
        ON_SCENE: safeVehicles.filter((v) => v.vehicleStatus === "ON_SCENE").length,
        RETURNING: safeVehicles.filter((v) => v.vehicleStatus === "RETURNING").length,
    };

    const criticalAlerts = safeAlerts.filter(
        (a) => a.alertType === "HIGH_ENGINE_TEMP" || a.alertType === "LOW_FUEL"
    ).length;
    const warningAlerts = safeAlerts.length - criticalAlerts;

    const avgSpeed = safeVehicles.length > 0
        ? Math.round(safeVehicles.reduce((sum, v) => sum + v.speed, 0) / safeVehicles.length)
        : 0;
    const avgFuel = safeVehicles.length > 0
        ? Math.round(safeVehicles.reduce((sum, v) => sum + v.fuelLevel, 0) / safeVehicles.length)
        : 0;

    return (
        <div className="flex flex-col h-full gap-4 pb-0">
            {/* Header */}
            <div className="flex-shrink-0">
                <h1 className="text-xl font-bold text-slate-900">Fleet Dashboard</h1>
                <p className="text-slate-800 text-md">Real-time fleet monitoring</p>
            </div>

            {/* Stats Cards */}
            <div className="flex-shrink-0">
                <StatsCardGrid
                    availableVehicles={statusCounts.IDLE}
                    totalVehicles={safeVehicles.length}
                    totalAlerts={safeAlerts.length}
                    criticalAlerts={criticalAlerts}
                    warningAlerts={warningAlerts}
                    avgSpeed={avgSpeed}
                    // avgResponseTime={4.2}
                    avgFuel={avgFuel}
                    statusCounts={statusCounts}
                />
            </div>

            {/* Panels */}
            <div className="flex-1 flex flex-col lg:flex-row gap-4 min-h-0">
                <VehicleListPanel vehicles={safeVehicles} />
                <AlertsPanel alerts={safeAlerts} criticalCount={criticalAlerts} />
            </div>
        </div>
    );
}