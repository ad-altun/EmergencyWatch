import { VehiclesCard } from "./VehiclesCard";
import { AlertsCard } from "./AlertsCard";
import { AveragesCard } from "./AveragesCard";
import { StatusOverviewCard } from "./StatusOverviewCard";
import type { VehicleStatus } from "@/types";

interface StatsCardGridProps {
    availableVehicles: number;
    totalVehicles: number;
    totalAlerts: number;
    criticalAlerts: number;
    warningAlerts: number;
    avgSpeed: number;
    avgResponseTime: number;
    avgFuel: number;
    statusCounts: Record<VehicleStatus, number>;
}

export function StatsCardGrid({
    availableVehicles,
    totalVehicles,
    totalAlerts,
    criticalAlerts,
    warningAlerts,
    avgSpeed,
    avgResponseTime,
    avgFuel,
    statusCounts,
}: StatsCardGridProps) {
    return (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
            <VehiclesCard available={availableVehicles} total={totalVehicles} />
            <AlertsCard total={totalAlerts} critical={criticalAlerts} warning={warningAlerts} />
            <AveragesCard avgSpeed={avgSpeed} avgResponseTime={avgResponseTime} avgFuel={avgFuel} />
            <StatusOverviewCard statusCounts={statusCounts} />
        </div>
    );
}