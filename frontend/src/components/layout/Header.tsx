import { Menu, AlertTriangle } from "lucide-react";

interface HeaderProps {
    onMenuClick: () => void;
}

export function Header( { onMenuClick }: HeaderProps ) {
    return (
        <header className="h-14 bg-white border-b border-slate-200 flex items-center
        justify-between px-4 flex-shrink-0 shadow-sm">
            {/* Left side */ }
            <div className="flex items-center gap-3">
                <button
                    onClick={ onMenuClick }
                    className="lg:hidden p-2 hover:bg-slate-100 rounded-lg transition-colors"
                >
                    <Menu size={ 20 } className="text-slate-600"/>
                </button>

                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-red-600 rounded-lg flex items-center justify-center">
                        <AlertTriangle size={ 18 } className="text-white"/>
                    </div>
                    <span className="font-semibold text-slate-900 text-lg tracking-tight">
            EmergencyWatch
          </span>
                </div>
            </div>

            {/* Right side */ }
            <div className="flex items-center gap-4">
                <div className="flex items-center gap-2 text-sm">
                    <div className="w-2 h-2 bg-emerald-500 rounded-full animate-pulse"/>
                    <span className="text-slate-700 hidden sm:inline">Connected</span>
                </div>

          {/*      <button className="relative p-2 hover:bg-slate-100 rounded-lg transition-colors">*/}
          {/*          <Bell size={ 20 } className="text-slate-600"/>*/}
          {/*          <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 rounded-full text-xs*/}
          {/*          flex items-center justify-center text-white font-medium">*/}
          {/*  3*/}
          {/*</span>*/}
          {/*      </button>*/}
            </div>
        </header>
    );
}