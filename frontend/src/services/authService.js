import apiClient from './apiClient.js';
import { tokenService } from './tokenService.js';

export const authService = {
  login: async (usernameOrEmail, password) => {
    const data = await apiClient.post('/auth/login', { usernameOrEmail, password });
    tokenService.setTokens({
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
    });
    tokenService.setUser(data.user);
    return data;
  },

  register: async (payload) => {
    const data = await apiClient.post('/auth/register', payload);
    tokenService.setTokens({
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
    });
    tokenService.setUser(data.user);
    return data;
  },

  refreshToken: async () => {
    const refreshToken = tokenService.getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }
    const data = await apiClient.post('/auth/refresh-token', { refreshToken });
    tokenService.setTokens({
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
    });
    if (data.user) {
      tokenService.setUser(data.user);
    }
    return data;
  },

  getCurrentUser: async () => {
    const profile = await apiClient.get('/auth/me');
    tokenService.setUser(profile);
    return profile;
  },

  logout: async () => {
    try {
      const refreshToken = tokenService.getRefreshToken();
      if (refreshToken) {
        await apiClient.post('/auth/logout', { refreshToken });
      }
    } catch {
      // Ignore network errors on logout to guarantee client session cleanup
    } finally {
      tokenService.clearSession();
    }
  },

  requestPasswordReset: async (emailOrUsername) => {
    return apiClient.post('/auth/forgot-password', { emailOrUsername });
  },

  resetPassword: async (token, newPassword) => {
    return apiClient.post('/auth/reset-password', { token, newPassword });
  },

  verifyEmail: async (token) => {
    return apiClient.post('/auth/verify-email', { token });
  },
};

export default authService;
