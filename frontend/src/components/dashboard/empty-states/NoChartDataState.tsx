import { TrendingUp } from "lucide-react";

export function NoChartDataState({ message }: { message: string }) {
    return (
        <div className="flex flex-col items-center justify-center h-48 text-center">
            <div className="bg-slate-100 rounded-full p-3 mb-3">
                <TrendingUp size={24} className="text-slate-400" />
            </div>
            <p className="text-slate-600 text-sm">{message}</p>
        </div>
    );
}