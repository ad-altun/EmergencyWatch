import { Filter } from "lucide-react";
import { useState } from "react";
import type { VehicleTelemetry, VehicleStatus } from "@/types";
import { VehicleRow } from "./VehicleRow";
import { NoVehiclesState, NoFilteredVehiclesState } from "./empty-states";

interface VehicleListPanelProps {
    vehicles: VehicleTelemetry[];
}

export function VehicleListPanel( { vehicles }: VehicleListPanelProps ) {
    const [ statusFilter, setStatusFilter ] = useState<VehicleStatus | "ALL">("ALL");

    // Defensive: Ensure vehicles is always an array
    const safeVehicles = Array.isArray(vehicles) ? vehicles : [];

    const filteredVehicles = statusFilter === "ALL"
        ? safeVehicles
        : safeVehicles.filter(( v ) => v.vehicleStatus === statusFilter);

    // Helper function to format status for display
    const getStatusLabel = ( status: VehicleStatus | "ALL" ) => {
        if ( status === "ALL" ) return "All";
        return status.replace(/_/g, " ").replace(/\b\w/g, ( c ) => c.toUpperCase());
    };

    return (
        <div className="flex-1 min-h-[300px] lg:min-h-0 bg-white border border-slate-200 rounded-xl flex flex-col min-w-0 shadow-sm">
            <div className="flex items-center justify-between p-3 border-b border-slate-200 flex-shrink-0">
                <div className="flex items-center gap-2">
                    <h2 className="font-semibold text-slate-900 text-sm">Vehicles</h2>
                    <span className="text-xs text-slate-600">
                        { filteredVehicles.length }/{ safeVehicles.length }
                    </span>
                </div>

                <div className="flex items-center gap-2">
                    <Filter size={ 12 } className="text-slate-600"/>
                    <select
                        value={ statusFilter }
                        onChange={ ( e ) => setStatusFilter(e.target.value as VehicleStatus | "ALL") }
                        className="bg-slate-50 border border-slate-300 rounded px-2 py-1 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                        <option value="ALL">All</option>
                        <option value="EN_ROUTE">En Route</option>
                        <option value="ON_SCENE">On Scene</option>
                        <option value="RETURNING">Returning</option>
                        <option value="IDLE">Idle</option>
                    </select>
                </div>
            </div>

            <div className="flex-1 overflow-y-auto min-h-0">
                {/* No vehicles at all */ }
                { safeVehicles.length === 0 ? (
                    <NoVehiclesState/>
                ) : filteredVehicles.length === 0 ? (
                    /* Filter returned no results */
                    <NoFilteredVehiclesState
                        filterStatus={ getStatusLabel(statusFilter) }
                        onClearFilter={ () => setStatusFilter("ALL") }
                    />
                ) : (
                    /* Normal vehicle list */
                    <div className="p-2 space-y-1">
                        { filteredVehicles.map(( vehicle ) => (
                            <VehicleRow key={ vehicle.vehicleId } vehicle={ vehicle }/>
                        )) }
                    </div>
                ) }
            </div>
        </div>
    );
}