import axios from 'axios';
import { tokenService } from './tokenService.js';

const API_BASE_URL = import.meta.env?.VITE_API_BASE_URL || '/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 20000,
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

// State for handling concurrent 401 refresh requests
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Response Interceptor: Standardize Responses and Handle Global Errors & Token Refresh
apiClient.interceptors.response.use(
  (response) => {
    // Backend returns { success: true, statusCode: 200, message: "...", data: ... }
    return response.data?.data !== undefined ? response.data.data : response.data;
  },
  async (error) => {
    const originalRequest = error.config;
    const errorPayload = error.response?.data;
    const status = error.response?.status;

    // Check if error is 401 and not an auth endpoint or already retried
    const isAuthRoute =
      originalRequest?.url?.includes('/auth/login') ||
      originalRequest?.url?.includes('/auth/refresh-token') ||
      originalRequest?.url?.includes('/auth/register');

    if (status === 401 && !originalRequest?._retry && !isAuthRoute) {
      const refreshToken = tokenService.getRefreshToken();

      if (refreshToken) {
        if (isRefreshing) {
          // Queue request while refresh is in flight
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          })
            .then((newToken) => {
              originalRequest.headers.Authorization = `Bearer ${newToken}`;
              return apiClient(originalRequest);
            })
            .catch((err) => Promise.reject(err));
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
          // Direct axios call to avoid interceptor loop
          const refreshResponse = await axios.post(`${API_BASE_URL}/auth/refresh-token`, {
            refreshToken,
          });

          const authData = refreshResponse.data?.data || refreshResponse.data;
          const newAccessToken = authData.accessToken;
          const newRefreshToken = authData.refreshToken;

          tokenService.setTokens({
            accessToken: newAccessToken,
            refreshToken: newRefreshToken,
          });

          if (authData.user) {
            tokenService.setUser(authData.user);
          }

          processQueue(null, newAccessToken);

          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return apiClient(originalRequest);
        } catch (refreshErr) {
          processQueue(refreshErr, null);
          tokenService.clearSession();
          if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
            window.location.href = '/login?session_expired=true';
          }
          return Promise.reject(formatError(refreshErr));
        } finally {
          isRefreshing = false;
        }
      } else {
        // No refresh token available; clear session and redirect
        tokenService.clearSession();
        if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
          window.location.href = '/login?session_expired=true';
        }
      }
    }

    return Promise.reject(formatError(error, errorPayload));
  }
);

function formatError(error, errorPayload) {
  const payload = errorPayload || error.response?.data;
  return {
    message: payload?.message || error.message || 'An unexpected error occurred',
    errorCode: payload?.errorCode || 'NETWORK_ERROR',
    statusCode: error.response?.status || 500,
    validationErrors: payload?.validationErrors || [],
    path: payload?.path || null,
    requestId: payload?.requestId || null,
    raw: error,
  };
}

export default apiClient;
