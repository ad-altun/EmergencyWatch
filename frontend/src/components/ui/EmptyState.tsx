import type { LucideIcon } from "lucide-react";

interface EmptyStateProps {
    icon: LucideIcon;
    title: string;
    description: string;
    action?: {
        label: string;
        onClick: () => void;
    };
}

export function EmptyState({ icon: Icon, title, description, action }: EmptyStateProps) {
    return (
        <div className="flex flex-col items-center justify-center h-full p-8 text-center">
            <div className="bg-slate-800/30 rounded-full p-4 mb-4">
                <Icon size={32} className="text-slate-500" />
            </div>
            <h3 className="text-slate-300 font-semibold mb-2">{title}</h3>
            <p className="text-slate-500 text-sm max-w-sm mb-4">{description}</p>
            {action && (
                <button
                    onClick={action.onClick}
                    className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded-lg transition-colors"
                >
                    {action.label}
                </button>
            )}
        </div>
    );
}