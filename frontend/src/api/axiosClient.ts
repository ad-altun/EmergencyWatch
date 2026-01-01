import axios from 'axios';

export const axiosClient = axios.create({
    // baseURL: ${VITE_API_BASE_URL} || "http://localhost:8080",
    baseURL: "",
    headers: {
        "Content-Type": "application/json",
    },
});