import { Gauge } from "lucide-react";

interface AveragesCardProps {
    avgSpeed: number;
    avgResponseTime: number;
    avgFuel: number;
}

export function AveragesCard({ avgSpeed, avgResponseTime, avgFuel }: AveragesCardProps) {
    return (
        <div className="bg-slate-800/50 border border-slate-700 rounded-lg p-3 hover:border-slate-600 transition-colors">
            <div className="flex items-center justify-between mb-2">
                <span className="text-slate-400 text-xs font-medium">Fleet Averages</span>
                <Gauge size={14} className="text-amber-400" />
            </div>
            <div className="grid grid-cols-3 gap-2">
                <div>
                    <p className="text-lg font-bold text-white">{avgSpeed}</p>
                    <p className="text-slate-500 text-xs">km/h</p>
                </div>
                <div className="hidden sm:block">
                    <p className="text-lg font-bold text-white">{avgResponseTime}</p>
                    <p className="text-slate-500 text-xs">min resp</p>
                </div>
                <div>
                    <p className="text-lg font-bold text-white">{avgFuel}%</p>
                    <p className="text-slate-500 text-xs">fuel</p>
                </div>
            </div>
        </div>
    );
}