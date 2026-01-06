import { useHistoricalMetrics } from "@/hooks";
import { SpeedTrendChart, SpeedByTypeChart, FuelByVehicleChart } from "@/components/charts";
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
                    <p className="text-slate-400 text-sm">Please check if the analytics service is running</p>
                </div>
            </div>
        );
    }

    // Loading state with skeletons
    if ( isLoading ) {
        return <AnalyticsPageSkeleton/>;
    }

    // No data state
    if ( !data || data.totalDays === 0 || data.dailyFleetMetrics.length === 0 ) {
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
    const latestMetrics = data.dailyFleetMetrics.find(m => m.fleetAverageSpeed !== null);

    return (
        <div className="flex flex-col h-full gap-6">
            {/* Header */ }
            <div className="flex-shrink-0">
                <h1 className="text-xl font-bold text-white">EmergencyWatch Analytics</h1>
                <p className="text-slate-400 text-sm">
                    Historical trends from { data.fromDate } to { data.toDate } ({ data.totalDays } days)
                </p>
            </div>

            {/* Summary Stats */ }
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-slate-800 rounded-lg p-4">
                    <p className="text-slate-400 text-sm">Average Fleet Speed</p>
                    <p className="text-2xl font-bold text-white">
                        { data.averageFleetSpeed.toFixed(0) } km/h
                    </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4">
                    <p className="text-slate-400 text-sm">Total Fuel Consumed</p>
                    <p className="text-2xl font-bold text-white">
                        { data.totalFuelConsumed.toFixed(0) } L
                    </p>
                </div>
                <div className="bg-slate-800 rounded-lg p-4">
                    <p className="text-slate-400 text-sm">Data Points</p>
                    <p className="text-2xl font-bold text-white">{ data.totalDataPoints }</p>
                </div>
            </div>

            {/* Charts */ }
            <div className="-mb-12">
                <h2 className="text-lg font-semibold text-white mt-6 mb-3">Fleet-Level Metrics</h2>
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1 min-h-0">
                    {/* Speed Trend */ }
                    <div className="bg-slate-800 rounded-lg p-4">
                        <h2 className="text-lg font-semibold text-white mb-4">Fleet Average Speed Trend</h2>
                        { data.dailyFleetMetrics.length > 0 ? (
                            <SpeedTrendChart data={ data.dailyFleetMetrics }/>
                        ) : (
                            <NoChartDataState message="No speed trend data available for this period"/>
                        ) }
                    </div>

                    {/* Average Speed by Vehicle Type */ }
                    <div className="bg-slate-800 rounded-lg p-4">
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
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1 min-h-0">
                        <div className="bg-slate-800 rounded-lg p-4">
                            <h2 className="text-lg font-semibold text-white mb-4">
                                Fuel Consumption by Vehicle ({ data.fromDate } to { data.toDate })
                            </h2>
                            { data.vehicleFuelConsumption && data.vehicleFuelConsumption.length > 0 ? (
                                <FuelByVehicleChart data={ data.vehicleFuelConsumption }/>
                            ) : (
                                <NoChartDataState message="No fuel consumption data available"/>
                            ) }
                        </div>
                        <div className="bg-slate-800 rounded-lg p-4">
                            <h2 className="text-lg font-semibold text-white mb-4">
                                Fuel Consumption by Vehicle ({ data.fromDate } to { data.toDate })
                            </h2>
                            { data.vehicleFuelConsumption && data.vehicleFuelConsumption.length > 0 ? (
                                <FuelByVehicleChart data={ data.vehicleFuelConsumption }/>
                            ) : (
                                <NoChartDataState message="No fuel consumption data available"/>
                            ) }
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}