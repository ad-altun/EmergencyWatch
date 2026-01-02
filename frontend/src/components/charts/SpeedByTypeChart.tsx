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
import type { VehicleType } from "@/types";

ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
);

interface SpeedByTypeChartProps {
    data: Record<VehicleType, number>;
}

const typeColors: Record<VehicleType, string> = {
    POLICE: "rgba(59, 130, 246, 0.8)",
    AMBULANCE: "rgba(34, 197, 94, 0.8)",
    FIRE_TRUCK: "rgba(239, 68, 68, 0.8)",
};

export function SpeedByTypeChart({ data }: SpeedByTypeChartProps) {
    const labels = Object.keys(data) as VehicleType[];

    const chartData = {
        labels: labels.map(l => l.replace("_", " ")),
        datasets: [
            {
                label: "Average Speed (km/h)",
                data: labels.map(l => data[l]),
                backgroundColor: labels.map(l => typeColors[l]),
            },
        ],
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: false,
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
            <Bar data={chartData} options={options} />
        </div>
    );
}