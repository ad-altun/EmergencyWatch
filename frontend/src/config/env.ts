interface EnvConfig {
    apiBaseUrl: string;
    environment: string;
}

const getEnvVariable = (key: string, defaultValue?: string): string => {
    const value = import.meta.env[key];

    if (!value && !defaultValue) {
        console.warn(`Environment variable ${key} is not defined`); // extra awareness
        // throw error to stop execution if critical config is missing
        throw new Error(`CRITICAL: Environment variable ${key} is not defined!`);
    }

    return value || defaultValue || '';
};

export const env: EnvConfig = {
    apiBaseUrl: getEnvVariable('VITE_API_BASE_URL', 'http://localhost:8080'),
    environment: getEnvVariable('VITE_ENVIRONMENT', 'development'),
};

export const isProduction = (): boolean => env.environment === 'production';
export const isDevelopment = (): boolean => env.environment === 'development';
