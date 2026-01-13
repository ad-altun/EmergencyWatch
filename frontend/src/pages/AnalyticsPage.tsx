import { useHistoricalMetrics } from "@/hooks";
import { SpeedTrendChart, SpeedByTypeChart, FuelByVehicleChart, SpeedByVehicleChart } from "@/components/charts";
import { AnalyticsPageSkeleton } from "@/components/dashboard/skeletons";
import { NoAnalyticsDataState, NoChartDataState } from "@/components/dashboard/empty-states";

export function AnalyticsPage() {
    // Default to last 7 days
    const today = new Date();
    const weekAgo = new Date(today);
    weekAgo.setDate(weekAgo.getDate() - 7);

    const from = weekAgo.toISOString().split("T")[ 0 ];
    const to = today.toISOString().split("T")[ 0 ];

    const { data, isLoading, error } = useHistoricalMetrics(from, to);

    // Error state
    if ( error ) {
        return (
            <div className="flex items-center justify-center h-full">
                <div className="text-center">
                    <p className="text-red-400 font-semibold mb-2">Failed to load analytics data</p>
                    <p className="text-slate-400 text-sm">Please check if the server is active</p>
                </div>
            </div>
        );
    }

    // Loading state with skeletons
    if ( isLoading ) {
        return <AnalyticsPageSkeleton/>;
    }

    // Defensive: Ensure arrays even if API returns unexpected data
    const safeDailyMetrics = Array.isArray(data?.dailyFleetMetrics) ? data.dailyFleetMetrics : [];
    const safeFuelConsumption = Array.isArray(data?.vehicleFuelConsumption) ? data.vehicleFuelConsumption : [];
    const safeVehicleMetrics = Array.isArray(data?.dailyVehicleMetrics) ? data.dailyVehicleMetrics : [];

    // No data state
    if ( !data || data.totalDays === 0 || safeDailyMetrics.length === 0 ) {
        return (
            <div className="flex flex-col h-full gap-6">
                {/* Header */ }
                <div className="flex-shrink-0">
                    <h1 className="text-xl font-bold text-white">Fleet Analytics</h1>
                    <p className="text-slate-400 text-sm">Historical trends and metrics</p>
                </div>

                {/* Empty state */ }
                <div className="flex-1 flex items-center justify-center">
                    <NoAnalyticsDataState/>
                </div>
            </div>
        );
    }

    // Get latest day's data for type breakdown
    const latestMetrics = safeDailyMetrics.find(m => m.fleetAverageSpeed !== null);

    return (
        <div className="flex flex-col h-full gap-6 max-w-7xl">
            {/* Header */ }
            <div className="flex-shrink-0">
                <h1 className="text-xl font-bold text-white">EmergencyWatch Analytics</h1>
                <p className="text-slate-400 text-sm">
                    Historical trends from { data.fromDate } to { data.toDate } ({ data.totalDays } days, { data.daysWithData } days with data)
                </p>
            </div>

            {/* Summary Stats */ }
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-0">
                <div className="bg-slate-800 rounded-lg p-4 flex items-baseline gap-3">
                    <p className="text-slate-400 text-sm">Average Fleet Speed</p>
                    <p className="text-xl font-bold text-white">
                        { data.averageFleetSpeed.toFixed(0) } km/h
                    </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4 flex items-baseline gap-3 md:ml-12">
                    <p className="text-slate-400 text-sm">Total Fuel Consumed</p>
                    <p className="text-xl font-bold text-white">
                        { data.totalFuelConsumed.toFixed(0) } L
                    </p>
                </div>
            </div>

            {/* Charts */ }
            <div className="-mb-12">
                <h2 className="text-lg font-semibold text-white mt-6 mb-3">Fleet-Level Metrics</h2>
                <div className="grid grid-cols-1 lg:grid-cols-2 flex-1 min-h-0 gap-6 lg:gap-0">
                    {/* Speed Trend */ }
                    <div className="bg-slate-800 rounded-lg p-4">
                        <h2 className="text-lg font-semibold text-white mb-4">Fleet Average Speed Trend</h2>
                        { safeDailyMetrics.length > 0 ? (
                            <SpeedTrendChart data={ safeDailyMetrics }/>
                        ) : (
                            <NoChartDataState message="No speed trend data available for this period"/>
                        ) }
                    </div>

                    {/* Average Speed by Vehicle Type */ }
                    <div className="bg-slate-800 rounded-lg p-4 lg:ml-12">
                        <h2 className="text-lg font-semibold text-white mb-4">Average Speed by Vehicle Type</h2>
                        { latestMetrics?.averageSpeedByType && Object.keys(
                            latestMetrics.averageSpeedByType).length > 0 ? (
                            <SpeedByTypeChart data={ latestMetrics.averageSpeedByType }/>
                        ) : (
                            <NoChartDataState message="No vehicle type data available"/>
                        ) }
                    </div>
                </div>

                {/* Fuel by Vehicle */ }
                <div>
                    <h2 className="text-lg font-semibold text-white mt-6 my-3">Vehicle-Level Metrics</h2>
                    <div className="grid grid-cols-1 lg:grid-cols-2 flex-1 min-h-0 gap-6 lg:gap-0">
                        <div className="bg-slate-800 rounded-lg p-4">
                            <h2 className="text-lg font-semibold text-white mb-4">
                                Fuel Consumption by Vehicle
                            </h2>
                            { safeFuelConsumption.length > 0 ? (
                                <FuelByVehicleChart data={ safeFuelConsumption }/>
                            ) : (
                                <NoChartDataState message="No fuel consumption data available"/>
                            ) }
                        </div>
                        <div className="bg-slate-800 rounded-lg p-4 lg:ml-12">
                            <h2 className="text-lg font-semibold text-white mb-4">
                                Average Speed by Vehicle
                            </h2>
                            { safeVehicleMetrics.length > 0 ? (
                                <SpeedByVehicleChart data={ safeVehicleMetrics }/>
                            ) : (
                                <NoChartDataState message="No vehicle speed data available"/>
                            ) }
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}