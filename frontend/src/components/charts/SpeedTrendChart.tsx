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
    const validData = data.filter(d => d.fleetAverageSpeed !== null);

    const chartData = {
        labels: validData.map(d => d.date),
        datasets: [
            {
                label: "Fleet Average Speed (km/h)",
                data: validData.map(d => d.fleetAverageSpeed),
                borderColor: "rgb(59, 130, 246)",
                backgroundColor: "rgba(59, 130, 246, 0.5)",
                tension: 0.3,
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
        },
        scales: {
            x: {
                ticks: { color: "#94a3b8" },
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