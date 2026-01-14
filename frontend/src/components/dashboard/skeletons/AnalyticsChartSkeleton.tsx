import { Skeleton } from "@/components/ui/Skeleton";

export function AnalyticsChartSkeleton() {
    return (
        <div className="bg-white border border-slate-200 rounded-lg p-4 shadow-sm">
            <Skeleton className="h-6 w-40 mb-4" />
            <div className="space-y-3">
                <Skeleton className="h-48 w-full" />
                <div className="flex gap-2 justify-center">
                    <Skeleton className="h-3 w-16" />
                    <Skeleton className="h-3 w-16" />
                    <Skeleton className="h-3 w-16" />
                </div>
            </div>
        </div>
    );
}