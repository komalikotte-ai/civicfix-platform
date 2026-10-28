import axios from 'axios';
import { tokenService } from './tokenService';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

// Request Interceptor: Attach JWT Bearer Token
apiClient.interceptors.request.use(
  (config) => {
    const token = tokenService.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Standardize Responses and Handle Global Errors
apiClient.interceptors.response.use(
  (response) => {
    // Backend returns { success: true, statusCode: 200, message: "...", data: ... }
    return response.data?.data !== undefined ? response.data.data : response.data;
  },
  (error) => {
    const errorPayload = error.response?.data;

    if (error.response?.status === 401) {
      tokenService.clearSession();
      if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
        window.location.href = '/login?session_expired=true';
      }
    }

    const formattedError = {
      message: errorPayload?.message || error.message || 'An unexpected network error occurred',
      errorCode: errorPayload?.errorCode || 'NETWORK_ERROR',
      statusCode: error.response?.status || 500,
      validationErrors: errorPayload?.validationErrors || [],
      path: errorPayload?.path || null,
      raw: error,
    };

    return Promise.reject(formattedError);
  }
);

export default apiClient;
