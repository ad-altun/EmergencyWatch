import { Line } from "react-chartjs-2";
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
} from "chart.js";
import type { DailyFleetMetrics } from "@/types";

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
);

interface SpeedTrendChartProps {
    data: DailyFleetMetrics[];
}

export function SpeedTrendChart({ data }: SpeedTrendChartProps) {
    // Defensive: Ensure data is always an array
    const safeData = Array.isArray(data) ? data : [];

    const chartData = {
        labels: safeData.map(d => d.date),
        datasets: [
            {
                label: "Fleet Average Speed (km/h)",
                data: safeData.map(d => d.fleetAverageSpeed),
                borderColor: "rgb(59, 130, 246)",
                backgroundColor: "rgba(59, 130, 246, 0.5)",
                tension: 0.3,
                spanGaps: false, // Show gaps where data is missing
            },
        ],
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                labels: { color: "#94a3b8" },
            },
            tooltip: {
                callbacks: {
                    title: function(context: any) {
                        return context[0].label;
                    }
                }
            }
        },
        scales: {
            x: {
                ticks: {
                    color: "#94a3b8",
                    maxRotation: 45,
                    minRotation: 45,
                },
                grid: { color: "#334155" },
            },
            y: {
                ticks: { color: "#94a3b8" },
                grid: { color: "#334155" },
                beginAtZero: true,
            },
        },
    };

    return (
        <div className="h-64">
            <Line data={chartData} options={options} />
        </div>
    );
}