import { Filter } from 'lucide-react';
import { EmptyState } from '@/components/ui/EmptyState.tsx';

export function NoVehiclesState() {
    return (
        <EmptyState
            icon={ Filter }
            title="No Vehicles Found"
            description="No emergency vehicles are currently being tracked..."
        />
    );
}