import { Skeleton } from "@/components/ui/Skeleton";

export function VehicleListSkeleton() {
    return (
        <div className="flex-1 bg-slate-900/50 border border-slate-800 rounded-xl flex flex-col min-h-0 min-w-0">
            {/* Header */}
            <div className="flex items-center justify-between p-3 border-b border-slate-800 flex-shrink-0">
                <div className="flex items-center gap-2">
                    <Skeleton className="h-4 w-16" />
                    <Skeleton className="h-3 w-8" />
                </div>
                <Skeleton className="h-7 w-24" />
            </div>

            {/* Vehicle rows */}
            <div className="flex-1 overflow-y-auto p-2 space-y-1">
                {Array.from({ length: 8 }).map((_, i) => (
                    <VehicleRowSkeleton key={i} />
                ))}
            </div>
        </div>
    );
}

function VehicleRowSkeleton() {
    return (
        <div className="bg-slate-800/30 hover:bg-slate-800/50 transition-colors p-2 rounded-lg border border-slate-700/50">
            <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                    <Skeleton className="h-4 w-4 rounded" />
                    <Skeleton className="h-4 w-24" />
                </div>
                <Skeleton className="h-5 w-16 rounded-full" />
            </div>

            <div className="grid grid-cols-3 gap-2 text-xs">
                <div className="flex items-center gap-1">
                    <Skeleton className="h-3 w-3" />
                    <Skeleton className="h-3 w-12" />
                </div>
                <div className="flex items-center gap-1">
                    <Skeleton className="h-3 w-3" />
                    <Skeleton className="h-3 w-10" />
                </div>
                <div className="flex items-center gap-1">
                    <Skeleton className="h-3 w-3" />
                    <Skeleton className="h-3 w-8" />
                </div>
            </div>
        </div>
    );
}