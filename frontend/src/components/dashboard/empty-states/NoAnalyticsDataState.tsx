import { BarChart3 } from "lucide-react";
import { EmptyState } from "@/components/ui/EmptyState";

export function NoAnalyticsDataState() {
    return (
        <EmptyState
            icon={BarChart3}
            title="No Analytics Data Available"
            description="Historical metrics will appear here once vehicles have been active for a day. The aggregation service runs daily to collect analytics data."
        />
    );
}