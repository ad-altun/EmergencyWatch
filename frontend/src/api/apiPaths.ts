/**
 * API Paths Configuration
 *
 * Local Development:
 *   - Uses Vite proxy to route /analytics/* and /alerts/* to backend services
 *
 * Production (Azure APIM):
 *   - /analytics/* → APIM rewrites to analytics-service /api/analytics/*
 *   - /alerts/* → APIM rewrites to notification-service /api/alerts/*
 *
 * This approach provides:
 *   - Clean, semantic URLs (industry standard)
 *   - Consistent paths across environments
 *   - Easy versioning (/v1/analytics, /v2/analytics)
 *   - Backend flexibility (change internal paths without breaking clients)
 */

export const apiPaths = {
    // Analytics Service
    analytics: {
        fleet: '/analytics/fleet',
        history: '/analytics/history',
        telemetryLatest: '/analytics/telemetry/latest',
        vehicleTelemetry: (vehicleId: string) => `/analytics/vehicles/${vehicleId}`,
    },

    // Alert Service
    alerts: {
        active: '/alerts/active',
        acknowledge: (alertId: number) => `/alerts/${alertId}/acknowledge`,
        resolve: (alertId: number) => `/alerts/${alertId}/resolve`,
    },
};
