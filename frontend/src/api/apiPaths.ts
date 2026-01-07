/**
 * API Paths Configuration
 *
 * Local Development:
 *   - Uses Vite proxy to route /analytics/* and /notifications/* to backend services
 *
 * Production (Azure APIM):
 *   - /analytics/* → APIM rewrites to analytics-service /api/analytics/*
 *   - /notifications/* → APIM rewrites to notification-service /api/alerts/*
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

    // Notification Service
    notifications: {
        active: '/notifications/active',
        acknowledge: (alertId: number) => `/notifications/${alertId}/acknowledge`,
        resolve: (alertId: number) => `/notifications/${alertId}/resolve`,
    },
};
