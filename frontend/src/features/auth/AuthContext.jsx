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
        } catch (err) {
          console.warn('Session expired or invalid, clearing local session');
          tokenService.clearSession();
          setUser(null);
        }
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

  const logout = useCallback(() => {
    authService.logout();
    setUser(null);
    setError(null);
  }, []);

  const hasRole = useCallback((requiredRole) => {
    if (!user || !user.roles) return false;
    const normalized = requiredRole.startsWith('ROLE_') ? requiredRole : `ROLE_${requiredRole}`;
    return user.roles.includes(normalized);
  }, [user]);

  const value = useMemo(() => ({
    user,
    loading,
    error,
    isAuthenticated: Boolean(user),
    login,
    register,
    logout,
    hasRole,
  }), [user, loading, error, login, register, logout, hasRole]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
