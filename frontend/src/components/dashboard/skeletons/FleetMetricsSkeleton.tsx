import { Skeleton } from "@/components/ui/Skeleton";

export function FleetMetricsSkeleton() {
    return (
        <div className="bg-white border border-slate-200 rounded-xl p-4 shadow-sm">
            <Skeleton className="h-5 w-32 mb-4" />

            <div className="grid grid-cols-2 gap-4">
                {Array.from({ length: 4 }).map((_, i) => (
                    <MetricCardSkeleton key={i} />
                ))}
            </div>
        </div>
    );
}

function MetricCardSkeleton() {
    return (
        <div className="bg-slate-50 rounded-lg p-3 border border-slate-200">
            <div className="flex items-center gap-2 mb-2">
                <Skeleton className="h-4 w-4 rounded" />
                <Skeleton className="h-3 w-20" />
            </div>
            <Skeleton className="h-7 w-16" />
        </div>
    );
}