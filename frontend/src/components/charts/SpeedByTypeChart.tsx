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
    // Defensive: Ensure data is always an object
    const safeData = data ?? {} as Record<VehicleType, number>;
    const labels = Object.keys(safeData) as VehicleType[];

    const chartData = {
        labels: labels.map(l => l.replace("_", " ")),
        datasets: [
            {
                label: "Average Speed (km/h)",
                data: labels.map(l => safeData[l]),
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
                ticks: { color: "#64748b" },
                grid: { color: "#e2e8f0" },
            },
            y: {
                ticks: { color: "#64748b" },
                grid: { color: "#e2e8f0" },
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