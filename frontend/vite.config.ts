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
            "/api/analytics": {
                target: "http://localhost:8082",
                changeOrigin: true,
            },
            "/api/alerts": {
                target: "http://localhost:8083",
                changeOrigin: true,
            },
        },
    },
});
