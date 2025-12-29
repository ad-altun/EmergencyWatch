/**
 * EmergencyWatch Swagger UI Initializer
 * Supports switching between Analytics and Notification API specs
 */

const API_CONFIGS = {
    'openapi-analytics.yaml': {
        name: 'Analytics',
        badgeClass: 'analytics'
    },
    'openapi-notification.yaml': {
        name: 'Notification',
        badgeClass: 'notification'
    }
};

let ui = null;

function initSwaggerUI(specUrl) {
    ui = SwaggerUIBundle({
        url: specUrl,
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        plugins: [
            SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: 'StandaloneLayout',
        docExpansion: 'list',
        defaultModelsExpandDepth: 1,
        defaultModelExpandDepth: 1,
        displayRequestDuration: true,
        filter: true,
        showExtensions: true,
        showCommonExtensions: true,
        tryItOutEnabled: false  // Disabled since services run locally
    });

    return ui;
}

function loadApi(specFile) {
    const config = API_CONFIGS[specFile];

    const badge = document.getElementById('api-badge');
    if (badge && config) {
        badge.textContent = config.name;
        badge.className = `api-badge ${config.badgeClass}`;
    }

    // Reload Swagger UI with new spec
    if (ui) {
        ui.specActions.updateUrl(specFile);
        ui.specActions.download();
    }
}

// Initialize on page load
window.onload = function() {
    const defaultSpec = 'openapi-analytics.yaml';
    initSwaggerUI(defaultSpec);

    // Set initial badge
    const config = API_CONFIGS[defaultSpec];
    const badge = document.getElementById('api-badge');
    if (badge && config) {
        badge.textContent = config.name;
        badge.className = `api-badge ${config.badgeClass}`;
    }
};