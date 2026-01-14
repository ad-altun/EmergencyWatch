import { NavLink } from "react-router-dom";
import {
    LayoutDashboard,
    // Truck,
    // Bell,
    BarChart3,
    ChevronLeft,
    ChevronRight,
} from "lucide-react";

interface SidebarProps {
    collapsed: boolean;
    onToggle: () => void;
    mobileOpen: boolean;
    onMobileClose: () => void;
}

const navItems = [
    { id: "dashboard", icon: LayoutDashboard, label: "Dashboard", path: "/" },
    // { id: "vehicles", icon: Truck, label: "Vehicles", path: "/vehicles" },
    // { id: "alerts", icon: Bell, label: "Alerts", path: "/alerts" },
    { id: "analytics", icon: BarChart3, label: "Analytics", path: "/analytics" },
];

export function Sidebar( { collapsed, onToggle, mobileOpen, onMobileClose }: SidebarProps ) {
    const sidebarContent = (
        <>
            <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
                { navItems.map(( item ) => (
                    <NavLink
                        key={ item.id }
                        to={ item.path }
                        onClick={ onMobileClose }
                        className={ ( { isActive } ) =>
                            `w-full flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all duration-200 ${
                                isActive
                                    ? "bg-blue-50 text-blue-600 font-medium"
                                    : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
                            }`
                        }
                    >
                        <item.icon size={ 20 }/>
                        { !collapsed && <span className="font-medium">{ item.label }</span> }
                    </NavLink>
                )) }
            </nav>

            <div className="p-3 border-t border-slate-200 flex-shrink-0">
                <button
                    onClick={ onToggle }
                    className="w-full flex items-center justify-center gap-2 px-3 py-2 text-slate-600
                        hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors"
                >
                    { collapsed ? <ChevronRight size={ 18 }/> : <ChevronLeft size={ 18 }/> }
                    { !collapsed && <span className="text-sm">Collapse</span> }
                </button>
            </div>
        </>
    );

    return (
        <>
            {/* Mobile overlay */ }
            { mobileOpen && (
                <div
                    className="fixed inset-0 bg-black/50 z-40 lg:hidden"
                    onClick={ onMobileClose }
                />
            ) }

            {/* Mobile sidebar */ }
            <aside
                className={ `fixed top-14 left-0 bottom-0 w-64 bg-white border-r border-slate-200 z-50 flex flex-col transform transition-transform duration-300 lg:hidden ${
                    mobileOpen ? "translate-x-0" : "-translate-x-full"
                }` }
            >
                { sidebarContent }
            </aside>

            {/* Desktop sidebar */ }
            <aside
                className={ `hidden lg:flex flex-col bg-white border-r border-slate-200 transition-all duration-300 flex-shrink-0 ${
                    collapsed ? "w-16" : "w-56"
                }` }
            >
                { sidebarContent }
            </aside>
        </>
    );
}