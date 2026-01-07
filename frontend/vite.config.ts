import path from "path"
import react from "@vitejs/plugin-react"
import { defineConfig } from "vite"

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src"),
        },
    },
    server: {
        port: 5173,
        proxy: {
            // Analytics Service - Clean APIM path structure
            "/analytics": {
                target: "http://localhost:8082",
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/analytics/, "/api/analytics"),
            },
            // Notification Service - Clean APIM path structure
            "/notifications": {
                target: "http://localhost:8083",
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/notifications/, "/api/alerts"),
            },
        },
    },
});
