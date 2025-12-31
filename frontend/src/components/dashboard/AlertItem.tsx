import { AlertTriangle } from "lucide-react";
import type { Alert } from "@/types";

interface AlertItemProps {
    alert: Alert;
}

export function AlertItem({ alert }: AlertItemProps) {
    const severityStyles = alert.alertType === "HIGH_ENGINE_TEMP" || alert.alertType === "LOW_FUEL"
        ? "bg-red-500/20 text-red-400 border-red-500/30"
        : "bg-amber-500/20 text-amber-400 border-amber-500/30";

    return (
        <div className={`flex items-center gap-3 p-3 rounded-lg border ${severityStyles}`}>
            <AlertTriangle size={16} className="flex-shrink-0" />
            <div className="flex-1 min-w-0">
                <p className="font-medium text-sm truncate">
                    {alert.vehicleId}: {alert.alertType.replace(/_/g, " ")}
                </p>
                <p className="text-xs opacity-70">{alert.message}</p>
            </div>
        </div>
    );
}