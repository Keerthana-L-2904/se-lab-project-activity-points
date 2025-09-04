import axios from 'axios';

const API_BASE_URL = "/api";  // Vite will proxy this to Spring Boot

export const fetchActivities = async () => {
    try {
        const response = await axios.get(`${API_BASE_URL}/activities`);
        return response.data;
    } catch (error) {
        console.error("Error fetching activities", error);
        throw error;
    }
};
