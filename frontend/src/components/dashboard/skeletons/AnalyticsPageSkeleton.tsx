import { Skeleton } from "@/components/ui/Skeleton";
import { AnalyticsSummarySkeleton } from "./AnalyticsSummarySkeleton";
import { AnalyticsChartSkeleton } from "./AnalyticsChartSkeleton";

export function AnalyticsPageSkeleton() {
    return (
        <div className="flex flex-col h-full gap-6">
            {/* Header Skeleton */}
            <div className="flex-shrink-0">
                <Skeleton className="h-7 w-48 mb-2" />
                <Skeleton className="h-4 w-96" />
            </div>

            {/* Summary Stats Skeleton */}
            <AnalyticsSummarySkeleton />

            {/* Charts Skeleton */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1 min-h-0">
                <AnalyticsChartSkeleton />
                <AnalyticsChartSkeleton />
                <div className="lg:col-span-2">
                    <AnalyticsChartSkeleton />
                </div>
            </div>
        </div>
    );
}