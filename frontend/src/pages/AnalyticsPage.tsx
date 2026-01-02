import { useHistoricalMetrics } from "@/hooks";
import { SpeedTrendChart, SpeedByTypeChart, FuelByVehicleChart } from "@/components/charts";

export function AnalyticsPage() {
    // Default to last 7 days
    const today = new Date();
    const weekAgo = new Date(today);
    weekAgo.setDate(weekAgo.getDate() - 7);

    const from = weekAgo.toISOString().split("T")[0];
    const to = today.toISOString().split("T")[0];

    const { data, isLoading, error } = useHistoricalMetrics(from, to);

    if (isLoading) {
        return (
            <div className="flex items-center justify-center h-full">
                <p className="text-slate-400">Loading analytics...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center justify-center h-full">
                <p className="text-red-400">Failed to load analytics data.</p>
            </div>
        );
    }

    if (!data) return null;

    // Get latest day's data for type breakdown
    const latestMetrics = data.dailyFleetMetrics.find(m => m.fleetAverageSpeed !== null);

    return (
        <div className="flex flex-col h-full gap-6">
            {/* Header */}
            <div className="flex-shrink-0">
                <h1 className="text-xl font-bold text-white">Fleet Analytics</h1>
                <p className="text-slate-400 text-sm">
                    Historical trends from {data.fromDate} to {data.toDate} ({data.totalDays} days)
                </p>
            </div>

            {/* Summary Stats */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-slate-800 rounded-lg p-4">
                    <p className="text-slate-400 text-sm">Average Fleet Speed</p>
                    <p className="text-2xl font-bold text-white">
                        {data.averageFleetSpeed.toFixed(1)} km/h
                    </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4">
                    <p className="text-slate-400 text-sm">Total Fuel Consumed</p>
                    <p className="text-2xl font-bold text-white">
                        {data.totalFuelConsumed.toFixed(0)} L
                    </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4">
                    <p className="text-slate-400 text-sm">Data Points</p>
                    <p className="text-2xl font-bold text-white">{data.totalDataPoints}</p>
                </div>
            </div>

            {/* Charts */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1 min-h-0">
                {/* Speed Trend */}
                <div className="bg-slate-800 rounded-lg p-4">
                    <h2 className="text-lg font-semibold text-white mb-4">Speed Trend</h2>
                    <SpeedTrendChart data={data.dailyFleetMetrics} />
                </div>

                {/* Speed by Vehicle Type */}
                <div className="bg-slate-800 rounded-lg p-4">
                    <h2 className="text-lg font-semibold text-white mb-4">Speed by Vehicle Type</h2>
                    {latestMetrics?.averageSpeedByType ? (
                        <SpeedByTypeChart data={latestMetrics.averageSpeedByType} />
                    ) : (
                        <p className="text-slate-500">No data available</p>
                    )}
                </div>

                {/* Fuel by Vehicle */}
                <div className="bg-slate-800 rounded-lg p-4 lg:col-span-2">
                    <h2 className="text-lg font-semibold text-white mb-4">Average Fuel Level by Vehicle</h2>
                    {data.dailyVehicleMetrics.length > 0 ? (
                        <FuelByVehicleChart data={data.dailyVehicleMetrics} />
                    ) : (
                        <p className="text-slate-500">No vehicle data available</p>
                    )}
                </div>
            </div>
        </div>
    );
}