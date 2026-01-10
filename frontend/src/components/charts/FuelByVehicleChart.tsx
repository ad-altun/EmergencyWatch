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
    const safeData = data ?? [];

    // Group by vehicle and calculate total consumption
    // const vehicleConsumption = safeData.reduce((acc, metric) => {
    //     const vehicleId = metric.vehicleId;
    //     if (!acc[vehicleId]) {
    //         acc[vehicleId] = {
    //             vehicleId,
    //             vehicleType: metric.vehicleType,
    //             totalConsumption: 0,
    //             dataPoints: 0
    //         };
    //     }
    //
    //     // Calculate fuel consumed based on vehicle type tank capacity
    //     const tankCapacity = metric.vehicleType === 'FIRE_TRUCK' ? 200
    //         : metric.vehicleType === 'AMBULANCE' ? 80
    //             : 60; // POLICE
    //
    //     // Estimate consumption from average fuel level drop
    //     const consumedPercentage = 100 - (metric.averageFuelLevel || 0);
    //     const consumedLiters = (consumedPercentage / 100) * tankCapacity;
    //
    //     acc[vehicleId].totalConsumption += consumedLiters;
    //     acc[vehicleId].dataPoints++;
    //
    //     return acc;
    // }, {} as Record<string, { vehicleId: string; vehicleType: string; totalConsumption: number; dataPoints: number }>);

    // const vehicles = Object.values(vehicleConsumption);
    const chartData = {
        // labels: safeData.map(d => d.vehicleId),
        labels: [...new Set(safeData.map(d => d.vehicleId))],
        datasets: [
            {
                label: "Fuel Consumed (L)",
                data: safeData.map(v => v.totalConsumed),
                backgroundColor: [
                    "rgba(59, 130, 246, 0.8)",   // Blue
                    "rgba(34, 197, 94, 0.8)",    // Green
                    "rgba(239, 68, 68, 0.8)",    // Red
                    "rgba(168, 85, 247, 0.8)",   // Purple
                    "rgba(249, 115, 22, 0.8)",   // Orange
                ],
                borderColor: "#1e293b",
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
                labels: { color: "#94a3b8" },
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