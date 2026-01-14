import { Gauge } from "lucide-react";

interface AveragesCardProps {
    avgSpeed: number;
    // avgResponseTime: number;
    avgFuel: number;
}

export function AveragesCard({ avgSpeed, avgFuel }: AveragesCardProps) {
    return (
        <div className="bg-white border border-slate-200 rounded-lg p-3 hover:border-slate-300 transition-colors shadow-sm">
            <div className="flex items-center justify-between mb-2">
                <span className="text-slate-600 text-xs font-medium">Fleet Averages</span>
                <Gauge size={14} className="text-amber-600" />
            </div>
            <div className="grid grid-cols-2 gap-2">
                <div className="flex items-baseline gap-2">
                    <p className="text-lg font-bold text-slate-900">{avgSpeed || 0}</p>
                    <p className="text-slate-600 text-xs">km/h</p>
                </div>
                {/*<div className="hidden sm:block">*/}
                {/*    <p className="text-lg font-bold text-slate-900">{avgResponseTime || 0}</p>*/}
                {/*    <p className="text-slate-600 text-xs">min resp</p>*/}
                {/*</div>*/}
                <div className="flex items-baseline gap-2">
                    <p className="text-lg font-bold text-slate-900">{avgFuel || 0}%</p>
                    <p className="text-slate-600 text-xs">fuel</p>
                </div>
            </div>
        </div>
    );
}