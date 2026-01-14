import { AlertTriangle } from "lucide-react";

interface AlertsCardProps {
    total: number;
    critical: number;
    warning: number;
}

export function AlertsCard({ total, critical, warning }: AlertsCardProps) {
    return (
        <div className="bg-white border border-slate-200 rounded-lg p-3 hover:border-slate-300 transition-colors shadow-sm">
            <div className="flex items-center justify-between mb-2">
                <span className="text-slate-600 text-xs font-medium">Active Alerts</span>
                <AlertTriangle size={14} className="text-red-600" />
            </div>
            <div className="flex items-baseline gap-4">
                <div className="flex items-baseline gap-1">
                    <span className="text-xl font-bold text-slate-900">{total}</span>
                </div>
                <div className="flex items-center gap-2 mt-1">
                    <span className="text-red-600 text-xs">{critical} critical</span>
                    <span className="text-slate-400">·</span>
                    <span className="text-amber-600 text-xs">{warning} warning</span>
                </div>
            </div>
        </div>
    );
}