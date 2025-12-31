import { CircleDot } from "lucide-react";
import type { VehicleStatus } from "@/types";

type StatusCounts = Record<VehicleStatus, number>;

interface StatusOverviewCardProps {
    statusCounts: StatusCounts;
}

const statusConfig: { key: VehicleStatus; label: string; color: string }[] = [
    { key: "IDLE", label: "Idle", color: "bg-emerald-500" },
    { key: "EN_ROUTE", label: "EnRoute", color: "bg-red-500" },
    { key: "ON_SCENE", label: "OnScene", color: "bg-amber-500" },
    { key: "RETURNING", label: "Return", color: "bg-blue-500" },
];

export function StatusOverviewCard({ statusCounts }: StatusOverviewCardProps) {
    return (
        <div className="bg-slate-800/50 border border-slate-700 rounded-lg p-3 hover:border-slate-600 transition-colors">
            <div className="flex items-center justify-between mb-2">
                <span className="text-slate-400 text-xs font-medium">Status Overview</span>
                <CircleDot size={14} className="text-emerald-400" />
            </div>
            <div className="grid grid-cols-4 gap-1">
                {statusConfig.map(({ key, label, color }) => (
                    <div key={key} className="text-center">
                        <div className="flex items-center justify-center gap-1">
                            <div className={`w-2 h-2 rounded-full ${color}`} />
                            <span className="text-lg font-bold text-white">{statusCounts[key]}</span>
                        </div>
                        <p className="text-slate-500 text-xs">{label}</p>
                    </div>
                ))}
            </div>
        </div>
    );
}