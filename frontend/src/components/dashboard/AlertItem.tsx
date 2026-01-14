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
        ? "text-red-700 border-red-300"
        : "text-amber-500 border-amber-300";

    const isAcknowledged = alert.status === "ACKNOWLEDGED";

    return (
        <div className={`flex items-center gap-3 p-3 rounded-lg border`}>
            <AlertTriangle size={16} className={`flex-shrink-0 ${severityStyles}`} />
            <div className="flex-1 min-w-0">
                <p className="font-medium text-xs ">
                    {alert.vehicleId}: {alert.alertType.replace(/_/g, " ")}
                </p>
                <p className="text-xs opacity-70">{alert.message}</p>
            </div>
            <div className="flex gap-1 flex-shrink-0">
                {!isAcknowledged && onAcknowledge && (
                    <button
                        onClick={() => onAcknowledge(alert.id)}
                        disabled={isLoading}
                        className="p-1.5 rounded hover:bg-black/10 transition-colors disabled:opacity-50"
                        title="Acknowledge"
                    >
                        <Check size={14} />
                    </button>
                )}
                {onResolve && (
                    <button
                        onClick={() => onResolve(alert.id)}
                        disabled={isLoading}
                        className="p-1.5 rounded hover:bg-black/10 transition-colors disabled:opacity-50"
                        title="Resolve"
                    >
                        <CheckCheck size={14} />
                    </button>
                )}
            </div>
        </div>
    );
}