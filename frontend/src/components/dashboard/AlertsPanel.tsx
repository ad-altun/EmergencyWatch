import type { Alert } from "@/types";
import { AlertItem } from "./AlertItem";
import { useAcknowledgeAlert, useResolveAlert } from "@/hooks";

interface AlertsPanelProps {
    alerts: Alert[];
    criticalCount: number;
}

export function AlertsPanel({ alerts, criticalCount }: AlertsPanelProps) {
    const acknowledgeMutation = useAcknowledgeAlert();
    const resolveMutation = useResolveAlert();

    const isLoading = acknowledgeMutation.isPending || resolveMutation.isPending;

    const handleAcknowledge = (id: number) => {
        acknowledgeMutation.mutate(id);
    };

    const handleResolve = (id: number) => {
        resolveMutation.mutate(id);
    };

    return (
        <div className="w-72 lg:w-80 bg-slate-900/50 border border-slate-800 rounded-xl flex flex-col min-h-0 flex-shrink-0">
            <div className="flex items-center justify-between p-3 border-b border-slate-800 flex-shrink-0">
                <h2 className="font-semibold text-white text-sm">Alerts</h2>
                <span className="px-2 py-0.5 bg-red-500/20 text-red-400 rounded-full text-xs font-medium">
                    {criticalCount} critical
                </span>
            </div>

            <div className="flex-1 overflow-y-auto p-3 space-y-2">
                {alerts.length === 0 ? (
                    <p className="text-slate-500 text-sm text-center py-4">No active alerts</p>
                ) : (
                    alerts.map((alert) => (
                        <AlertItem
                            key={alert.id}
                            alert={alert}
                            onAcknowledge={handleAcknowledge}
                            onResolve={handleResolve}
                            isLoading={isLoading}
                        />
                    ))
                )}
            </div>

            {/*<div className="p-3 border-t border-slate-800 flex-shrink-0">*/}
            {/*    <button className="w-full py-2 text-sm text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors">*/}
            {/*        View All →*/}
            {/*    </button>*/}
            {/*</div>*/}
        </div>
    );
}