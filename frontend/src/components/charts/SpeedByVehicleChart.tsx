import { Bar } from "react-chartjs-2";
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from "chart.js";
import type { DailyVehicleMetrics } from "@/types";

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

interface SpeedByVehicleChartProps {
    data: DailyVehicleMetrics[];
}

export function SpeedByVehicleChart({ data }: SpeedByVehicleChartProps) {
    // Defensive: Ensure data is always an array
    const safeData = Array.isArray(data) ? data : [];

    // Color palette matching FuelByVehicleChart
    const colors = [
        "rgba(59, 130, 246, 0.8)",   // Blue
        "rgba(34, 197, 94, 0.8)",    // Green
        "rgba(239, 68, 68, 0.8)",    // Red
        "rgba(168, 85, 247, 0.8)",   // Purple
        "rgba(249, 115, 22, 0.8)",   // Orange
    ];

    // Group by vehicle and calculate average speed
    const vehicleSpeedMap = safeData.reduce((acc, metric) => {
        const vehicleId = metric.vehicleId;
        if (!acc[vehicleId]) {
            acc[vehicleId] = {
                totalSpeed: 0,
                count: 0,
            };
        }
        acc[vehicleId].totalSpeed += metric.averageSpeed;
        acc[vehicleId].count++;
        return acc;
    }, {} as Record<string, { totalSpeed: number; count: number }>);

    // Get all unique vehicle IDs and sort alphabetically for consistent color mapping
    const allVehicleIds = Object.keys(vehicleSpeedMap).sort();

    // Create color map based on alphabetical order
    const vehicleColorMap = allVehicleIds.reduce((acc, vehicleId, index) => {
        acc[vehicleId] = colors[index % colors.length];
        return acc;
    }, {} as Record<string, string>);

    // Calculate averages and sort by speed
    const vehicles = Object.entries(vehicleSpeedMap)
        .map(([vehicleId, { totalSpeed, count }]) => ({
            vehicleId,
            averageSpeed: totalSpeed / count,
        }))
        .sort((a, b) => b.averageSpeed - a.averageSpeed);

    const chartData = {
        labels: vehicles.map(v => v.vehicleId),
        datasets: [
            {
                label: "Average Speed (km/h)",
                data: vehicles.map(v => v.averageSpeed),
                backgroundColor: vehicles.map(v => vehicleColorMap[v.vehicleId]),
                borderColor: vehicles.map(v => vehicleColorMap[v.vehicleId].replace('0.8', '1')),
                borderWidth: 1,
            },
        ],
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        indexAxis: "y" as const,
        plugins: {
            legend: {
                display: false,
            },
            tooltip: {
                callbacks: {
                    label: function(context: any) {
                        return `${context.parsed.x.toFixed(1)} km/h`;
                    }
                }
            }
        },
        scales: {
            x: {
                grid: { color: "#334155" },
                ticks: { color: "#94a3b8" },
                title: {
                    display: true,
                    text: "Average Speed (km/h)",
                    color: "#94a3b8",
                },
            },
            y: {
                grid: { display: false },
                ticks: { color: "#94a3b8" },
            },
        },
    };

    return (
        <div className="h-64">
            <Bar data={chartData} options={options} />
        </div>
    );
}
