import React, { createContext, useState, useEffect, useCallback, useMemo } from 'react';
import { authService } from '../../services/authService';
import { tokenService } from '../../services/tokenService';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => tokenService.getUser());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Synchronize auth state on mount
  useEffect(() => {
    const initAuth = async () => {
      if (tokenService.isAuthenticated()) {
        try {
          const profile = await authService.getCurrentUser();
          setUser(profile);
        } catch {
          // If token expired, try refresh before giving up
          const refreshToken = tokenService.getRefreshToken();
          if (refreshToken) {
            try {
              const refreshed = await authService.refreshToken();
              setUser(refreshed.user || tokenService.getUser());
            } catch {
              tokenService.clearSession();
              setUser(null);
            }
          } else {
            tokenService.clearSession();
            setUser(null);
          }
        }
      } else {
        tokenService.clearSession();
        setUser(null);
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = useCallback(async (usernameOrEmail, password) => {
    setError(null);
    try {
      const data = await authService.login(usernameOrEmail, password);
      setUser(data.user);
      return data;
    } catch (err) {
      setError(err.message || 'Login failed');
      throw err;
    }
  }, []);

  const register = useCallback(async (payload) => {
    setError(null);
    try {
      const data = await authService.register(payload);
      setUser(data.user);
      return data;
    } catch (err) {
      setError(err.message || 'Registration failed');
      throw err;
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      setUser(null);
      setError(null);
    }
  }, []);

  const refreshToken = useCallback(async () => {
    try {
      const data = await authService.refreshToken();
      if (data.user) setUser(data.user);
      return data;
    } catch (err) {
      setUser(null);
      throw err;
    }
  }, []);

  const hasRole = useCallback((requiredRole) => {
    if (!user || !user.roles || !Array.isArray(user.roles)) return false;
    const normalized = requiredRole.startsWith('ROLE_') ? requiredRole : `ROLE_${requiredRole}`;
    return user.roles.includes(normalized);
  }, [user]);

  const hasAnyRole = useCallback((roles = []) => {
    if (!roles || roles.length === 0) return true;
    return roles.some((r) => hasRole(r));
  }, [hasRole]);

  const updateUser = useCallback((updatedUser) => {
    setUser(updatedUser);
    tokenService.setUser(updatedUser);
  }, []);

  const value = useMemo(() => ({
    user,
    loading,
    error,
    isAuthenticated: Boolean(user),
    login,
    register,
    logout,
    refreshToken,
    hasRole,
    hasAnyRole,
    updateUser,
  }), [user, loading, error, login, register, logout, refreshToken, hasRole, hasAnyRole, updateUser]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
