import { Skeleton } from "@/components/ui/Skeleton";

export function AnalyticsSummarySkeleton() {
    return (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="bg-white border border-slate-200 rounded-lg p-4 shadow-sm">
                    <Skeleton className="h-4 w-32 mb-2" />
                    <Skeleton className="h-8 w-24" />
                </div>
            ))}
        </div>
    );
}