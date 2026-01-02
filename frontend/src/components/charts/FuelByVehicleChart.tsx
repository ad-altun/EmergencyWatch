import { Doughnut } from "react-chartjs-2";
import {
    Chart as ChartJS,
    ArcElement,
    Tooltip,
    Legend,
} from "chart.js";
import type { DailyVehicleMetrics } from "@/types";

ChartJS.register(ArcElement, Tooltip, Legend);

interface FuelByVehicleChartProps {
    data: DailyVehicleMetrics[];
}

export function FuelByVehicleChart({ data }: FuelByVehicleChartProps) {
    const chartData = {
        labels: data.map(d => d.vehicleId),
        datasets: [
            {
                data: data.map(d => d.averageFuelLevel),
                backgroundColor: [
                    "rgba(59, 130, 246, 0.8)",
                    "rgba(34, 197, 94, 0.8)",
                    "rgba(239, 68, 68, 0.8)",
                    "rgba(168, 85, 247, 0.8)",
                    "rgba(249, 115, 22, 0.8)",
                    "rgba(236, 72, 153, 0.8)",
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
        },
    };

    return (
        <div className="h-64">
            <Doughnut data={chartData} options={options} />
        </div>
    );
}