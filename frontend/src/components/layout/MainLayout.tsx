import { useState } from "react";
import { Header } from "./Header";
import { Sidebar } from "./Sidebar";
import { Outlet } from "react-router-dom";

export function MainLayout() {
    const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    return (
        <div className="h-screen w-screen bg-slate-950 text-white flex flex-col overflow-hidden">
            <Header onMenuClick={() => setMobileMenuOpen(true)} />

            <div className="flex flex-1 min-h-0">
                <Sidebar
                    collapsed={sidebarCollapsed}
                    onToggle={() => setSidebarCollapsed(!sidebarCollapsed)}
                    mobileOpen={mobileMenuOpen}
                    onMobileClose={() => setMobileMenuOpen(false)}
                />

                <main className="flex-1 flex flex-col min-h-0 min-w-0 p-4 overflow-auto">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}