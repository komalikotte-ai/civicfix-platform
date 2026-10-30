/**
 * Token and Session Storage Management for CivicFix Platform.
 * Supports JWT decoding, expiry checking, role-based authorization, and token lifecycle.
 */

const ACCESS_TOKEN_KEY = 'civicfix_access_token';
const REFRESH_TOKEN_KEY = 'civicfix_refresh_token';
const USER_KEY = 'civicfix_user_profile';

/**
 * Safely decodes the payload of a JWT without external libraries.
 */
function parseJwt(token) {
  if (!token || typeof token !== 'string') return null;
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    let jsonPayload;
    if (typeof atob === 'function') {
      jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
    } else {
      // Node.js test environment fallback
      jsonPayload = Buffer.from(base64, 'base64').toString('utf8');
    }
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export const tokenService = {
  getAccessToken: () => {
    if (typeof localStorage === 'undefined') return null;
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  },

  getRefreshToken: () => {
    if (typeof localStorage === 'undefined') return null;
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  },

  setTokens: ({ accessToken, refreshToken }) => {
    if (typeof localStorage === 'undefined') return;
    if (accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  },

  setAccessToken: (accessToken) => {
    if (typeof localStorage === 'undefined') return;
    if (accessToken) {
      localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    } else {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
    }
  },

  getUser: () => {
    if (typeof localStorage === 'undefined') return null;
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  },

  setUser: (user) => {
    if (typeof localStorage === 'undefined') return;
    if (user) {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } else {
      localStorage.removeItem(USER_KEY);
    }
  },

  clearSession: () => {
    if (typeof localStorage === 'undefined') return;
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },

  isAuthenticated: () => {
    const token = tokenService.getAccessToken();
    if (!token) return false;
    return !tokenService.isTokenExpired(token);
  },

  isTokenExpired: (token) => {
    const decoded = parseJwt(token);
    if (!decoded || !decoded.exp) return false;
    // exp is in seconds, Date.now() in ms
    return decoded.exp * 1000 < Date.now();
  },

  isAccessTokenExpired: () => {
    const token = tokenService.getAccessToken();
    return tokenService.isTokenExpired(token);
  },

  decodeToken: (token) => {
    return parseJwt(token);
  },

  hasRole: (requiredRole) => {
    const user = tokenService.getUser();
    if (!user || !user.roles || !Array.isArray(user.roles)) return false;
    const normalized = requiredRole.startsWith('ROLE_') ? requiredRole : `ROLE_${requiredRole}`;
    return user.roles.includes(normalized);
  },

  hasAnyRole: (roles = []) => {
    if (!roles || roles.length === 0) return true;
    return roles.some((role) => tokenService.hasRole(role));
  },
};

export default tokenService;
