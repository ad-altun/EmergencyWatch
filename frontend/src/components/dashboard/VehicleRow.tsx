import { Activity, Fuel, Thermometer } from "lucide-react";
import type { VehicleTelemetry, VehicleStatus } from "@/types";

interface VehicleRowProps {
    vehicle: VehicleTelemetry;
}

const vehicleIcons: Record<string, string> = {
    AMBULANCE: "🚑",
    FIRE_TRUCK: "🚒",
    POLICE: "🚓",
};

const statusColors: Record<VehicleStatus, string> = {
    IDLE: "bg-emerald-500",
    EN_ROUTE: "bg-red-500",
    ON_SCENE: "bg-amber-500",
    RETURNING: "bg-blue-500",
};

export function VehicleRow({ vehicle }: VehicleRowProps) {
    return (
        <div className="flex items-center justify-between py-2.5 px-3 hover:bg-slate-800/50 rounded-lg transition-colors">
            <div className="flex items-center gap-3">
                <span className="text-lg">{vehicleIcons[vehicle.vehicleType] || "🚗"}</span>
                <div>
                    <p className="font-medium text-white text-sm">{vehicle.vehicleId}</p>
                    <p className="text-xs text-slate-500">{vehicle.vehicleType.replace("_", " ")}</p>
                </div>
            </div>

            <div className="flex items-center gap-4">
                <div className="hidden md:flex items-center gap-3 text-xs text-slate-400">
          <span className="flex items-center gap-1">
            <Activity size={12} />
              {vehicle.speed}
          </span>
                    <span className={`flex items-center gap-1 ${vehicle.fuelLevel < 40 ? "text-amber-400" : ""}`}>
            <Fuel size={12} />
                        {vehicle.fuelLevel}%
          </span>
                    <span className={`flex items-center gap-1 ${vehicle.engineTemp > 93 ? "text-red-400" : ""}`}>
            <Thermometer size={12} />
                        {vehicle.engineTemp}°
          </span>
                </div>

                <div className="flex items-center gap-2">
                    <div className={`w-2 h-2 rounded-full ${statusColors[vehicle.vehicleStatus]}`} />
                    <span className="text-xs text-slate-400 w-16 truncate">{vehicle.vehicleStatus}</span>
                </div>
            </div>
        </div>
    );
}