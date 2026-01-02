import { createBrowserRouter } from "react-router-dom";
import { MainLayout } from "@/components/layout/MainLayout.tsx";
import { DashboardPage } from "@/pages/DashboardPage.tsx";
import { AnalyticsPage } from "@/pages/AnalyticsPage.tsx";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <MainLayout />,
        children: [
            {
                index: true,
                element: <DashboardPage />,
            },
            {
                path: "analytics",
                element: <AnalyticsPage />,
            }
        ]
    }
])