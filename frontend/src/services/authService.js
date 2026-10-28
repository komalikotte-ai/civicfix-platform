import apiClient from './apiClient';
import { tokenService } from './tokenService';

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

  getCurrentUser: async () => {
    const profile = await apiClient.get('/auth/me');
    tokenService.setUser(profile);
    return profile;
  },

  logout: () => {
    tokenService.clearSession();
  },
};
