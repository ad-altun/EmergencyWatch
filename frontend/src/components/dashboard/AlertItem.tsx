import { AlertTriangle, Check, CheckCheck } from "lucide-react";
import type { Alert } from "@/types";

interface AlertItemProps {
    alert: Alert;
    onAcknowledge?: (id: number) => void;
    onResolve?: (id: number) => void;
    isLoading?: boolean;
}

export function AlertItem({ alert, onAcknowledge, onResolve, isLoading }: AlertItemProps) {
    const severityStyles = alert.alertType === "HIGH_ENGINE_TEMP" || alert.alertType === "LOW_FUEL"
        ? "bg-red-500/20 text-red-400 border-red-500/30"
        : "bg-amber-500/20 text-amber-400 border-amber-500/30";

    const isAcknowledged = alert.status === "ACKNOWLEDGED";

    return (
        <div className={`flex items-center gap-3 p-3 rounded-lg border ${severityStyles}`}>
            <AlertTriangle size={16} className="flex-shrink-0" />
            <div className="flex-1 min-w-0">
                <p className="font-medium text-sm truncate">
                    {alert.vehicleId}: {alert.alertType.replace(/_/g, " ")}
                </p>
                <p className="text-xs opacity-70">{alert.message}</p>
            </div>
            <div className="flex gap-1 flex-shrink-0">
                {!isAcknowledged && onAcknowledge && (
                    <button
                        onClick={() => onAcknowledge(alert.id)}
                        disabled={isLoading}
                        className="p-1.5 rounded hover:bg-white/10 transition-colors disabled:opacity-50"
                        title="Acknowledge"
                    >
                        <Check size={14} />
                    </button>
                )}
                {onResolve && (
                    <button
                        onClick={() => onResolve(alert.id)}
                        disabled={isLoading}
                        className="p-1.5 rounded hover:bg-white/10 transition-colors disabled:opacity-50"
                        title="Resolve"
                    >
                        <CheckCheck size={14} />
                    </button>
                )}
            </div>
        </div>
    );
}