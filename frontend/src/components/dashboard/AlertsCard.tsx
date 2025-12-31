import { AlertTriangle } from "lucide-react";

interface AlertsCardProps {
    total: number;
    critical: number;
    warning: number;
}

export function AlertsCard({ total, critical, warning }: AlertsCardProps) {
    return (
        <div className="bg-slate-800/50 border border-slate-700 rounded-lg p-3 hover:border-slate-600 transition-colors">
            <div className="flex items-center justify-between mb-2">
                <span className="text-slate-400 text-xs font-medium">Active Alerts</span>
                <AlertTriangle size={14} className="text-red-400" />
            </div>
            <div className="flex items-baseline gap-1">
                <span className="text-2xl font-bold text-white">{total}</span>
            </div>
            <div className="flex items-center gap-2 mt-1">
                <span className="text-red-400 text-xs">{critical} critical</span>
                <span className="text-slate-600">·</span>
                <span className="text-amber-400 text-xs">{warning} warning</span>
            </div>
        </div>
    );
}