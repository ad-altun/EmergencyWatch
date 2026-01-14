import { Doughnut } from "react-chartjs-2";
import {
    Chart as ChartJS,
    ArcElement,
    Tooltip,
    Legend,
} from "chart.js";
import type { VehicleFuelConsumption } from "@/types";

ChartJS.register(ArcElement, Tooltip, Legend);

interface FuelByVehicleChartProps {
    data: VehicleFuelConsumption[];
}

export function FuelByVehicleChart({ data }: FuelByVehicleChartProps) {
    // Defensive: Ensure data is always an array
    const safeData = Array.isArray(data) ? data : [];

    // Color palette
    const colors = [
        "rgba(59, 130, 246, 0.8)",   // Blue
        "rgba(34, 197, 94, 0.8)",    // Green
        "rgba(239, 68, 68, 0.8)",    // Red
        "rgba(168, 85, 247, 0.8)",   // Purple
        "rgba(249, 115, 22, 0.8)",   // Orange
    ];

    // Get all unique vehicle IDs and sort alphabetically for consistent color mapping
    const allVehicleIds = [...new Set(safeData.map(d => d.vehicleId))].sort();

    // Create color map based on alphabetical order
    const vehicleColorMap = allVehicleIds.reduce((acc, vehicleId, index) => {
        acc[vehicleId] = colors[index % colors.length];
        return acc;
    }, {} as Record<string, string>);

    const chartData = {
        labels: allVehicleIds,
        datasets: [
            {
                label: "Fuel Consumed (L)",
                data: allVehicleIds.map(vehicleId => {
                    const vehicle = safeData.find(v => v.vehicleId === vehicleId);
                    return vehicle ? vehicle.totalConsumed : 0;
                }),
                backgroundColor: allVehicleIds.map(vehicleId => vehicleColorMap[vehicleId]),
                borderColor: "#f1f5f9",
                borderWidth: 2,
            },
        ],
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: "right" as const,
                labels: { color: "#64748b" },
            },
            tooltip: {
                callbacks: {
                    label: function(context: any) {
                        return `${context.label}: ${context.parsed.toFixed(1)} L`;
                    }
                }
            }
        },
    };

    return (
        <div className="h-48">
            <Doughnut data={chartData} options={options} />
        </div>
    );
}