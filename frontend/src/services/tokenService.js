/**
 * Token and Session Storage Management for CivicFix Platform.
 */

const ACCESS_TOKEN_KEY = 'civicfix_access_token';
const REFRESH_TOKEN_KEY = 'civicfix_refresh_token';
const USER_KEY = 'civicfix_user_profile';

export const tokenService = {
  getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY),

  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY),

  setTokens: ({ accessToken, refreshToken }) => {
    if (accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  },

  getUser: () => {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  },

  setUser: (user) => {
    if (user) {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } else {
      localStorage.removeItem(USER_KEY);
    }
  },

  clearSession: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },

  isAuthenticated: () => {
    return Boolean(localStorage.getItem(ACCESS_TOKEN_KEY));
  },

  hasRole: (requiredRole) => {
    const user = tokenService.getUser();
    if (!user || !user.roles) return false;
    const normalizedRole = requiredRole.startsWith('ROLE_') ? requiredRole : `ROLE_${requiredRole}`;
    return user.roles.includes(normalizedRole);
  },
};
