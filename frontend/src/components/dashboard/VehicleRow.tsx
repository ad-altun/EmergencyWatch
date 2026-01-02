import { Gauge, Fuel, Thermometer } from "lucide-react";
import type { VehicleTelemetry, VehicleStatus, VehicleType } from "@/types";

interface VehicleRowProps {
    vehicle: VehicleTelemetry;
}

const vehicleTextColors: Record<VehicleType, string> = {
    POLICE: "text-blue-500",
    AMBULANCE: "text-white-500",
    FIRE_TRUCK: "text-red-400",
};

const statusColors: Record<VehicleStatus, string> = {
    IDLE: "bg-emerald-500",
    EN_ROUTE: "bg-red-500",
    ON_SCENE: "bg-amber-500",
    RETURNING: "bg-violet-500",
};

export function VehicleRow( { vehicle }: VehicleRowProps ) {
    return (
        <div
            className="flex items-center justify-between py-2.5 px-3 hover:bg-slate-800/50 rounded-lg transition-colors">
            <div className="flex items-center gap-3">
                <div>
                    <p className={ `font-medium text-sm ${ vehicleTextColors[ vehicle.vehicleType ] }` }>{ vehicle.vehicleId }</p>
                    <p className="text-xs text-slate-500">{ vehicle.vehicleType.replace("_", " ") }</p>
                </div>
            </div>

            <div className="flex items-center gap-8">
                <div className="hidden md:flex items-center gap-3 text-xs text-slate-400">
          <span className="flex items-center justify-between min-w-[4rem] gap-x-1">
            <Gauge size={ 12 }/>
              { vehicle.speed.toFixed(0) } km/h
          </span> |
                    <span className={ `flex items-center gap-1 ${ vehicle.fuelLevel < 40 ? "text-amber-400" : "" }` }>
            <Fuel size={ 12 }/>
                        { vehicle.fuelLevel.toFixed(0) }%
          </span> |
                    <span className={ `flex items-center gap-1 ${ vehicle.engineTemp > 93 ? "text-red-400" : "" }` }>
            <Thermometer size={ 12 }/>
                        { vehicle.engineTemp.toFixed(1) }°
          </span>
                </div>

                <div className="flex items-center gap-2 w-24">
                    <div className={ `w-2 h-2 rounded-full ${ statusColors[ vehicle.vehicleStatus ] }` }/>
                    <span className="text-xs text-slate-400 w-20">{ vehicle.vehicleStatus }</span>
                </div>
            </div>
        </div>
    );
}