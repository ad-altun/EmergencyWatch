import { Gauge, Fuel, Thermometer, Plug2, Siren } from "lucide-react";
import type { VehicleTelemetry, VehicleStatus } from "@/types";

interface VehicleRowProps {
    vehicle: VehicleTelemetry;
}

const statusColors: Record<VehicleStatus, string> = {
    IDLE: "bg-emerald-500",
    EN_ROUTE: "bg-red-500",
    ON_SCENE: "bg-amber-500",
    RETURNING: "bg-violet-500",
};

export function VehicleRow( { vehicle }: VehicleRowProps ) {
    return (
        <div
            className="flex items-center justify-between py-2.5 px-3 hover:bg-slate-100 rounded-lg transition-colors">
            <div className="flex items-center gap-3">
                <div>
                    <p className="font-medium text-sm" >{ vehicle.vehicleId }</p>
                    <p className="text-xs text-slate-600">{ vehicle.vehicleType.replace("_", " ") }</p>
                </div>
            </div>

            <div className="flex items-center gap-8">
                <div className="hidden md:flex items-center gap-3 text-xs text-slate-600">
                    { vehicle.emergencyLightsActive ?
                        ( <span className="flex items-center justify-between min-w-[1rem] gap-x-1">
            <Siren size={ 16 } color="blue" />
          </span> ) : "" } |
                    <span className="flex items-center justify-between min-w-[4rem] gap-x-1">
            <Gauge size={ 12 }/>
                        { vehicle.speed.toFixed(0) } km/h
          </span> |
                    <span className={ `flex items-center gap-1 ${ vehicle.fuelLevel < 40 ? "text-amber-600" : "" }` }>
            <Fuel size={ 12 }/>
                        { vehicle.fuelLevel.toFixed(0) }%
          </span> |
                    <span className={ `flex items-center gap-1 ${ vehicle.engineTemp > 93 ? "text-red-600" : "" }` }>
            <Thermometer size={ 12 }/>
                        { vehicle.engineTemp.toFixed(1) }°
          </span> |
                    <span className={ `flex items-center gap-1 ${ "" }` }>
            <Plug2 size={ 12 }/>
                        { vehicle.batteryVoltage.toFixed(2) } V
          </span>
                </div>

                <div className="flex items-center gap-2 w-24">
                    <div className={ `w-2 h-2 rounded-full ${ statusColors[ vehicle.vehicleStatus ] }` }/>
                    <span className="text-xs text-slate-600 w-20">{ vehicle.vehicleStatus }</span>
                </div>
            </div>
        </div>
    );
}