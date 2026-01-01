import { Filter } from "lucide-react";
import { useState } from "react";
import type { VehicleTelemetry, VehicleStatus } from "@/types";
import { VehicleRow } from "./VehicleRow";

interface VehicleListPanelProps {
    vehicles: VehicleTelemetry[];
}

export function VehicleListPanel({ vehicles }: VehicleListPanelProps) {
    const [statusFilter, setStatusFilter] = useState<VehicleStatus | "ALL">("ALL");

    const filteredVehicles = statusFilter === "ALL"
        ? vehicles
        : vehicles.filter((v) => v.vehicleStatus === statusFilter);

    return (
        <div className="flex-1 bg-slate-900/50 border border-slate-800 rounded-xl flex flex-col min-h-0 min-w-0">
            <div className="flex items-center justify-between p-3 border-b border-slate-800 flex-shrink-0">
                <div className="flex items-center gap-2">
                    <h2 className="font-semibold text-white text-sm">Vehicles</h2>
                    <span className="text-xs text-slate-500">
            {filteredVehicles.length}/{vehicles.length}
          </span>
                </div>

                <div className="flex items-center gap-2">
                    <Filter size={12} className="text-slate-500" />
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value as VehicleStatus | "ALL")}
                        className="bg-slate-800 border border-slate-700 rounded px-2 py-1 text-xs text-slate-300 focus:outline-none"
                    >
                        <option value="ALL">All</option>
                        <option value="EN_ROUTE">En Route</option>
                        <option value="ON_SCENE">On Scene</option>
                        <option value="RETURNING">Returning</option>
                        <option value="IDLE">Idle</option>
                    </select>
                </div>
            </div>

            <div className="flex-1 overflow-y-auto p-2 space-y-1">
                {filteredVehicles.map((vehicle) => (
                    <VehicleRow key={vehicle.vehicleId} vehicle={vehicle} />
                ))}
            </div>
        </div>
    );
}