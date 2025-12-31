import { StatsCardGrid } from "@/components/dashboard/StatsCardGrid";
import { VehicleListPanel } from "@/components/dashboard/VehicleListPanel";
import { AlertsPanel } from "@/components/dashboard/AlertsPanel";
import type { VehicleTelemetry, Alert, VehicleStatus } from "@/types";

// TODO: Replace with real API data
const mockVehicles: VehicleTelemetry[] = Array.from({ length: 30 }, (_, i) => {
    const types = ["AMBULANCE", "FIRE_TRUCK", "POLICE"] as const;
    const statuses: VehicleStatus[] = ["IDLE", "EN_ROUTE", "ON_SCENE", "RETURNING"];
    const type = types[i % 3];
    const prefix = type === "AMBULANCE" ? "AMB" : type === "FIRE_TRUCK" ? "FIRE" : "POL";
    const status = statuses[Math.floor(Math.random() * 4)];

    return {
        id: i + 1,
        vehicleId: `${prefix}-${String(i + 1).padStart(3, "0")}`,
        vehicleType: type,
        vehicleStatus: status,
        timestamp: new Date().toISOString(),
        latitude: 48.7758 + Math.random() * 0.1,
        longitude: 9.1829 + Math.random() * 0.1,
        speed: status === "IDLE" || status === "ON_SCENE" ? 0 : Math.floor(Math.random() * 80) + 30,
        fuelLevel: Math.floor(Math.random() * 60) + 20,
        engineTemp: Math.floor(Math.random() * 25) + 75,
        batteryVoltage: 12 + Math.random() * 2,
        emergencyLightsActive: status === "EN_ROUTE" || status === "ON_SCENE",
    };
});

const mockAlerts: Alert[] = [
    { id: 1, vehicleId: "AMB-004", vehicleType: "AMBULANCE", alertType: "LOW_FUEL", status: "ACTIVE", message: "Fuel below 20%", thresholdValue: 20, actualValue: 15, createdAt: new Date().toISOString() },
    { id: 2, vehicleId: "FIRE-008", vehicleType: "FIRE_TRUCK", alertType: "HIGH_ENGINE_TEMP", status: "ACTIVE", message: "Engine temp above 95°C", thresholdValue: 95, actualValue: 102, createdAt: new Date().toISOString() },
    { id: 3, vehicleId: "POL-012", vehicleType: "POLICE", alertType: "LOW_BATTERY", status: "ACTIVE", message: "Battery below 11V", thresholdValue: 11, actualValue: 10.5, createdAt: new Date().toISOString() },
    { id: 4, vehicleId: "AMB-019", vehicleType: "AMBULANCE", alertType: "LOW_FUEL", status: "ACTIVE", message: "Fuel below 20%", thresholdValue: 20, actualValue: 18, createdAt: new Date().toISOString() },
    { id: 5, vehicleId: "FIRE-023", vehicleType: "FIRE_TRUCK", alertType: "HIGH_ENGINE_TEMP", status: "ACTIVE", message: "Engine temp above 95°C", thresholdValue: 95, actualValue: 97, createdAt: new Date().toISOString() },
    { id: 6, vehicleId: "POL-006", vehicleType: "POLICE", alertType: "LOW_BATTERY", status: "ACTIVE", message: "Battery below 11V", thresholdValue: 11, actualValue: 10.8, createdAt: new Date().toISOString() },
    { id: 7, vehicleId: "AMB-028", vehicleType: "AMBULANCE", alertType: "LOW_BATTERY", status: "ACTIVE", message: "Battery below 11V", thresholdValue: 11, actualValue: 10.2, createdAt: new Date().toISOString() },
];

export function DashboardPage() {
    const statusCounts: Record<VehicleStatus, number> = {
        IDLE: mockVehicles.filter((v) => v.vehicleStatus === "IDLE").length,
        EN_ROUTE: mockVehicles.filter((v) => v.vehicleStatus === "EN_ROUTE").length,
        ON_SCENE: mockVehicles.filter((v) => v.vehicleStatus === "ON_SCENE").length,
        RETURNING: mockVehicles.filter((v) => v.vehicleStatus === "RETURNING").length,
    };

    const criticalAlerts = mockAlerts.filter((a) => a.alertType === "HIGH_ENGINE_TEMP" || a.alertType === "LOW_FUEL").length;
    const warningAlerts = mockAlerts.length - criticalAlerts;

    const avgSpeed = Math.round(
        mockVehicles.reduce((sum, v) => sum + v.speed, 0) / mockVehicles.length
    );
    const avgFuel = Math.round(
        mockVehicles.reduce((sum, v) => sum + v.fuelLevel, 0) / mockVehicles.length
    );

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
                    totalVehicles={mockVehicles.length}
                    totalAlerts={mockAlerts.length}
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
                <VehicleListPanel vehicles={mockVehicles} />
                <AlertsPanel alerts={mockAlerts} criticalCount={criticalAlerts} />
            </div>
        </div>
    );
}