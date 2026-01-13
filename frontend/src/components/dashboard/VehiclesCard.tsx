import { Truck } from "lucide-react";

interface VehiclesCardProps {
    available: number;
    total: number;
}

export function VehiclesCard({ available, total }: VehiclesCardProps) {
    return (
        <div className="bg-slate-800/50 border border-slate-700 rounded-lg p-3 hover:border-slate-600 transition-colors">
            <div className="flex items-center justify-between mb-2">
                <span className="text-slate-400 text-xs font-medium">Vehicles</span>
                <Truck size={14} className="text-blue-400" />
            </div>
            <div className="flex items-baseline gap-3">
                <div className="flex items-baseline gap-1">
                    <span className="text-xl font-bold text-white">{available}</span>
                    <span className="text-slate-500 text-sm">/ {total}</span>
                </div>
                <p className="text-slate-500 text-xs mt-1">available</p>
            </div>
        </div>
    );
}