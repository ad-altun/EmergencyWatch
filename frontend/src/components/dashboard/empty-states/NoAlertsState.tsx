import { CheckCheck } from 'lucide-react';
import { EmptyState } from '@/components/ui/EmptyState';

export function NoAlertsState() {
    return (
        <EmptyState
            icon={ CheckCheck }
            title="No Active Alerts"
            description="All vehicles are operating without any alert..."
        />
    );
}