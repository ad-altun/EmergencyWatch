import { Filter } from "lucide-react";
import { EmptyState } from "@/components/ui/EmptyState.tsx";

interface NoFilteredVehiclesStateProps {
    filterStatus: string;
    onClearFilter: () => void;
}

export function NoFilteredVehiclesState({ filterStatus, onClearFilter }: NoFilteredVehiclesStateProps) {
    return (
        <EmptyState
            icon={Filter}
            title={`No ${filterStatus} Vehicles`}
            description={`There are currently no vehicles with status "${filterStatus}". 
            Try selecting a different filter.`}
            action={{
                label: "Clear Filter",
                onClick: onClearFilter,
            }}
        />
    );
}